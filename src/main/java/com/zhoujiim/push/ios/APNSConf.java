package com.zhoujiim.push.ios;

import com.eva.epc.common.util.Platform;
import com.eva.epc.common.util.PropertyMan;

/**
 * iOS的APNs推送相关配置信息读取工具类。
 * 
 * @author Jason
 * @since 4.3
 */
public class APNSConf
{
	private PropertyMan pm = null;
	
	private final static String TAG = APNSConf.class.getSimpleName();
	private static APNSConf instance = null;
	
	public static APNSConf getInstance()
	{
		if(instance == null)
			instance = new APNSConf();
		return instance;
	}
	
	public APNSConf()
	{
//		String path = "/E:/Program Files/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/RobotimeMissU_s_new/WEB-INF/classes/com/robotime/missu_s/ends/base_conf.properties";
		String pathOriginal = "/D:/FeigeDownload/zhoujiim-server/src/main/java/com/zhoujiim/push/ios/apns_conf.properties";
		String path = Platform.isWindows()?pathOriginal.substring(1):pathOriginal;
		
		System.out.println("["+TAG+"] iOS-APNs消息推送配置文件路径："+path);
		
		pm = new PropertyMan(path);
	}
	
	/**
	 * 因Properties文件默认机制是采用ISO8859-1处理的，所以中文字符读取会存在乱码现象。
	 * 本方法是在父类方法的基础上解决乱码问题。
	 * 
	 * @param key
	 * @return
	 */
	public String getPropertyUTF(String key)
	{
		try
		{
			String s = pm.getProperty(key);
			return new String(s.getBytes("ISO-8859-1"), "utf-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("["+TAG+"] iOS-APNs的配置项："+key
					+" 读取失败，请检查您的配置文件路径是否放置正确！");
			return null;
		}
	}
	
	private String getPUSH_CER_PATH_DEV()
	{
		return getPropertyUTF("PUSH_CER_PATH_DEV");
	}
	private String getPUSH_CER_PATH_DISTRIBUTION()
	{
		return getPropertyUTF("PUSH_CER_PATH_DISTRIBUTION");
	}
	public String getPushCerPath()
	{
		System.out.println("["+TAG+"] 正在读取iOS-APNs的证书，当前推送模式："
				+(isDistrubuttionMode()?"生产模式":"开发模式"));
		return this.isDistrubuttionMode()?getPUSH_CER_PATH_DISTRIBUTION():getPUSH_CER_PATH_DEV();
	}
	
	public String getPUSH_CER_PASSWORD()
	{
		return getPropertyUTF("PUSH_CER_PASSWORD");
	}
	
	public String getPUSH_MY_IOS_APP_BUNDLE_ID()
	{
		return getPropertyUTF("PUSH_MY_IOS_APP_BUNDLE_ID");
	}
	
	/**
	 * 推送开关。
	 * 
	 * @return true表示开启推送、false表示关闭推送（即不进行APNs的推送）。
	 */
	public boolean isPushOpen()
	{
		return "1".equals(getPropertyUTF("PUSH_OPEN"));
	}
	
	/**
	 * 是否生产模式。
	 * 
	 * @return
	 */
	public boolean isDistrubuttionMode()
	{
		return "1".equals(getPropertyUTF("PUSH_MODE"));
	}
	
	public static void main(String[] args)
	{
		System.out.println("生产模式？"+APNSConf.getInstance().isDistrubuttionMode());
		System.out.println("getPUSH_CER_PATH_DEV？"+APNSConf.getInstance().getPUSH_CER_PATH_DEV());
		System.out.println("getPUSH_CER_PATH_DISTRIBUTION？"+APNSConf.getInstance().getPUSH_CER_PATH_DISTRIBUTION());
		System.out.println("getPUSH_CER_PASSWORD？"+APNSConf.getInstance().getPUSH_CER_PASSWORD());
		
	}
	
}
