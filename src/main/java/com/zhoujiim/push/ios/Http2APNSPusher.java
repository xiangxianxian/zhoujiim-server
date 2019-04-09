package com.zhoujiim.push.ios;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.eva.epc.common.util.CommonUtils;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;

/**
 * 使用Pushy实现iOS的APNs高性能推送。
 * <p>
 * 关于Pushy的介绍，请见：http://www.52im.net/thread-1820-1-1.html
 * 
 * ==================== 关于RainbowChat服务端的iOS设备离线消息推送的方案说明 ==================
 * 【当前方案】：使用Pushy实现苹果提供的HTTP/2长连接高性能推送方案。
 * 【参考文章】：http://www.52im.net/thread-1820-1-1.html
 * 【Github】:https://github.com/relayrides/pushy
 * 【运行要求】：因需要支持HTTP/2以及SSL，运行Pushy需要额外的“netty-tcnative-boringssl
 * 				-static-2.0.12.Final.jar”。
 * 【系统要求】：因“netty-tcnative-boringssl-static-2.0.12.Final.jar”包中含native
 *              代码，且只能支持64位系统，所以想要Pushy正常运行则系统必须64位。
 * 【当前性能】：因使用最新的苹果HTTP/2长连接方式，且网络层用的netty，所以Pushy方案可以实现大数据量、高性能推送。
 * 【优化建议】：因向ios设备的推送可以提炼成独立的服务单独运行，在高负载场景下，可以
 * 				使用MQ作为中间件，将此推送相关代码独立运行，而不是放在RainbowChat核心业务中一起“运行”。
 * 【依赖的Jar】：1）pushy-0.13.6.jar
 *               2）netty-all-4.1.28.Final.jar
 *               3）gson 2.6
 *               4）slf4j 1.7 
 *               5）netty-tcnative-boringssl-static-2.0.12.Final.jar，（此包的作用是实现与苹果APNS的SSL长连接，uber jar下载：http://netty.io/wiki/forked-tomcat-native.html）
 *               6）fast-uuid-0.1.jar，（此包作为pushy依赖的高性能生成uuid的工具类，github：https://github.com/jchambers/fast-uuid）
 * @author Jason
 * @version 4.3
 */
public class Http2APNSPusher
{
	private static final String TAG = "iOS-APNs推送";
	
    
	private static Http2APNSPusher instance = null;
    
    private ApnsClient apnsClient = null;
    
    /** 推送开关：return true表示开启推送、false表示关闭推送（即不进行APNs的推送）*/
    private boolean isOpen = true;
    /** true表示当前推送用于生产模式（即APP上架APP Store后），否则用于开发环境，差别就是苹果要求的证书不同 */
    private boolean isDistribution = false;
    /** 当前推送要用到的证书的路径 */
    private String cerPath = null;
    /** 当前推送证书的密码 */
    private String cerPassword = null;
    /** 你的iOS客户端的Boundle Identifier（见您的XCode工程配置）*/
    private String myAPPBundleId = null;
    
    public static Http2APNSPusher getInstance()
    {
    	if(instance == null)
    		instance = new Http2APNSPusher();
    	return instance;
    }
    
    private Http2APNSPusher()
    {
    	if(!this.isOpen)
    	{
    		System.out.println("【"+TAG+"】iOS设备的离线推送能力处于”关闭“状态，ios离线推送服务不可用。");
    		return;
    	}
    	
    	try 
    	{
    		loadConfig();
    		
    		System.out.println("【"+TAG+"】》》推送开关："+(APNSConf.getInstance().isPushOpen()?"开":"关")
    				+"，当前模式：["+(isDistribution?"生产模式":"开发模式")
    				+"], 证书路径："+cerPath+", 证书密码："+cerPassword
    				+", ios-APP的bundleID="+myAPPBundleId);

    		EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    		apnsClient = new ApnsClientBuilder()
	    		.setApnsServer(isDistribution?ApnsClientBuilder.PRODUCTION_APNS_HOST:ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
	    		.setClientCredentials(new File(cerPath), cerPassword)
	    		.setConcurrentConnections(4)
	    		.setEventLoopGroup(eventLoopGroup)
	    		.build();
    	} 
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		System.out.println("【"+TAG+"】初始化Pushy失败，本次iOS推送将无法继续!");
    		return;
    	}
    }
    
    private void loadConfig()
    {
    	this.isOpen = APNSConf.getInstance().isPushOpen();
    	this.isDistribution = APNSConf.getInstance().isDistrubuttionMode();
    	this.cerPath = APNSConf.getInstance().getPushCerPath();
    	this.cerPassword = APNSConf.getInstance().getPUSH_CER_PASSWORD();
    	this.myAPPBundleId = APNSConf.getInstance().getPUSH_MY_IOS_APP_BUNDLE_ID();
    }
    
    /**
     * 向指定的ios设备推送消息。
     * 
     * @param deviceToken ios设备token（登陆时上报的）
     * @param alertTitle 推送的消息标题（不需要中填null）
     * @param alertBody 推送的消息内容（不需要中填null）
     * @param badgeNumber 推送到手机端要在手机图标上显示的未读数，-1将不影响
     * 手机端原有的未读数、0表示清除未读数、大于0将设置为新的未读数
     * @throws Exception
     */
    public void push(String deviceToken, String alertTitle, String alertBody
    		, int badgeNumber) throws Exception 
    {
    	if(CommonUtils.isStringEmpty(deviceToken, true))
    	{
    		throw new IllegalArgumentException("【"+TAG+"】无效的参数deviceToken="+deviceToken);
    	}
    	
    	ArrayList<String> al = new ArrayList<String>();
    	al.add(deviceToken);
    	this.push(al, alertTitle, alertBody, badgeNumber);
    }
    
    /**
     * 向指定的ios设备列表批量推送消息。
     * 
     * @param deviceToken ios设备token列表（它们登陆时上报的）
     * @param alertTitle 推送的消息标题（不需要中填null）
     * @param alertBody 推送的消息内容（不需要中填null）
     * @param badgeNumber 推送到手机端要在手机图标上显示的未读数，-1将不影响
     * 手机端原有的未读数、0表示清除未读数、大于0将设置为新的未读数
     * @throws Exception
     */
    public void push(final List<String> deviceTokens, String alertTitle, String alertBody
    		, int badgeNumber) throws Exception 
    {
    	if(!this.isOpen)
    	{
    		System.out.println("【"+TAG+"】iOS设备的离线推送能力处于”关闭“状态，本条推送将被忽略！");
    		return;
    	}
    	
    	if(deviceTokens == null || deviceTokens.size()<= 0 )
    	{
    		throw new IllegalArgumentException("【"+TAG+"】无效的参数"
    				+(deviceTokens == null?"deviceTokens=null":"deviceTokens.size="+deviceTokens.size()));
    	}
    	
    	if(this.apnsClient == null)
    	{
    		throw new IllegalArgumentException("【"+TAG+"】apnsClient对象是空的！");
    	}
    	
        System.out.println("【"+TAG+"】[0/2]正在准备向ios设备"+Arrays.toString(deviceTokens.toArray())+"推送内容为:{alertTitle="
        		+alertTitle+", alertBody="+alertBody+", badgeNumber="+badgeNumber+"}的APNs消息。。。。");
        
        //* 构建payload
        ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertBody(alertBody);
        payloadBuilder.setAlertTitle(alertTitle);
        payloadBuilder.setBadgeNumber(badgeNumber);
        // 默认通知是没有声音的，需要设置一下
        payloadBuilder.setSound(ApnsPayloadBuilder.DEFAULT_SOUND_FILENAME);
        String payload = payloadBuilder.buildWithDefaultMaximumLength();
 
        //* 向token逐个推送
        for (String deviceToken : deviceTokens) 
        {
            final String token = TokenUtil.sanitizeTokenString(deviceToken);
            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, myAPPBundleId, payload);
            
            // 向APNs服务器发出一条推送
            final Future<PushNotificationResponse<SimpleApnsPushNotification>> future = apnsClient.sendNotification(pushNotification);
            // 通过netty的异步回调来监听发送结果
            future.addListener(new GenericFutureListener<Future<PushNotificationResponse>>() {
                @Override
                public void operationComplete(Future<PushNotificationResponse> pushNotificationResponseFuture) throws Exception
                {
                	// 请求通过与APNs的长连接成功送出
                    if (future.isSuccess()) 
                    {
                    	System.out.println("【"+TAG+"】[1/2]向APNs服务端的网络请求已成功发出(目标设备token="+token+")...");
                    	
                    	// 读取苹果APNs服务器的返回结果
                        final PushNotificationResponse<SimpleApnsPushNotification> response = future.getNow();
                        // 向APNs服务器送出的通知已被APNs接受（它将交由APNs服务器完成到ios手机的最终推送）
                        if (response.isAccepted()) 
                        {
                        	System.out.println("【"+TAG+"】[2/2]APNs服务器已成功接受向设备"+token+"的推送请求.【OK】");
                        } 
                        // APNs服务器拒绝了本次推送请求
                        else
                        {
                            Date invalidTime = response.getTokenInvalidationTimestamp();
                            System.out.println("【"+TAG+"】[2/2]APNs服务器已拒绝了您向设备"+token
                            		+"的推送请求，原因是： " + response.getRejectionReason()+"【NO】");
                            
                            if (invalidTime != null)
                                System.out.println("【"+TAG+"】[2/2]\t…and the token is invalid as of " 
                                		+ response.getTokenInvalidationTimestamp());
                        }
                    } 
                    else
                    {
                    	
                    	System.out.println("【"+TAG+"】[1/2]向APNs服务端的网络请求失败了(目标设备token="+token+"), 原因是："
                    			+future.cause().getMessage()+".【NO】");
                    }
                }
            });
        }
    }
    
    public void release()
    {
    	if(this.apnsClient != null)
    	{
    		try
			{
				this.apnsClient.close();
			}
			catch (Exception e)
			{
				System.out.println(TAG);
				e.printStackTrace();
			}
    		finally
    		{
    			this.apnsClient = null;
    			
    			System.out.println("【"+TAG+"】》》Pushy已成功关闭。");
    		}
    	}
    }
    
    public static void main(String[] args) throws Exception
	{
		Http2APNSPusher.getInstance().push("7dd30c783d4c63e3e7171a20a7c0b9729ad148c97578534e0d4325612a8345e8"
				, "老司机", "邀请你进入群聊", -1);
	}
	
}

