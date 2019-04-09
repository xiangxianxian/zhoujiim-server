<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="renderer" content="webkit">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <title></title>
  <link rel="stylesheet" href="layui/css/layui.css"> 
</head>
<body>
  <div style="margin: 30px;">
	<fieldset class="layui-elem-field index-button" style="margin-top: 30px;">
	  <legend>注册</legend>
	  <div>
	   <form class="layui-form" action="">
	    		<div class="layui-form-item">
				    <label class="layui-form-label">账号</label>
				    <div class="layui-input-block">
				      <input type="text" name="account"  lay-verify="required" autocomplete="off" placeholder="请输入账号" class="layui-input">
				    </div>
				 </div>
				 <div class="layui-form-item">
				    <label class="layui-form-label">密码</label>
				    <div class="layui-input-block">
				      <input type="text" name="password" lay-verify="required" placeholder="请输入密码" autocomplete="off" class="layui-input">
				    </div>
				 </div>
				 <div class="layui-form-item">
				    <label class="layui-form-label">姓名</label>
				    <div class="layui-input-block">
				      <input type="text" name="userInfo.name" lay-verify="required" placeholder="请输入姓名" autocomplete="off" class="layui-input">
				    </div>
				 </div>
				 <div class="layui-form-item">
				    <label class="layui-form-label">所在部门</label>
				    <div class="layui-input-block">
				      <select name="userInfo.deptid" lay-verify="required">
				        	<option value="1">技术部</option>
				         	<option value="2">人事部</option>
				          	<option value="3">客服部</option>
				          	<option value="4">财务部</option>
				      </select>
				    </div>
				  </div>
				 <div class="layui-form-item">
				    <div class="layui-input-block">
				      <button class="layui-btn" lay-submit="" lay-filter="reg">立即注册</button>
				      <button type="button" class="layui-btn layui-btn-primary showform" >登录</button>
				    </div>
				  </div>
	    </form>
	  </div>
	</fieldset>
  </div>
<script src="layui/layui.js"></script>  
<script>
layui.use(['form'], function(){
  var form = layui.form
  ,$ = layui.jquery
  ,layer = layui.layer;
  
 
  //自定义验证规则
  form.verify({
	  account: function(value){
      if(value.length < 1){
        return '请输入账号';
      }
    }
    ,password: [/(.+){6,12}$/, '密码必须6到12位']
    ,'userInfo.name': function(value){
    	 if(value.length < 1){
    	        return '请输入姓名';
    	  }
    }
  });
  
  $(".showform").on("click",function(){
		 var btntext = $(this).text();
		 var forms = $(".layui-elem-field");
		 if(btntext=='登录'){
			 localStorage.removeItem("account_");
			 localStorage.removeItem("password_");
			 location.href="login.jsp";
		 } 
  }) 
 
  
  //监听提交
  form.on('submit(reg)', function(data){
   /*  layer.alert(JSON.stringify(data.field), {
      title: '最终的提交信息'
    }) */
    $.ajax({
		type : "post",
		url : "useraccount/save",
		data :data.field,
		dataType : "json",
		async : false,
		success : function(data){
			if(data.data!=null&&data.data.id!=null){
				$(".laccount").val(data.data.account);
				$(".lpwd").val(data.data.password);
				$(".loginform").submit();
			}else{
				layer.msg("注册失败，请检查用户名或账号是否重复！");
			}
			
			
		}
	}); 
    
    return false;
  });
  
  
});
</script>
</body>
</html>