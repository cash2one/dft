<%@ page contentType="text/html; charset=UTF-8" %>
<%
    //设置页面不缓存	
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
    response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	
	//读取cookies里的用户名
	String userName = "";
	Cookie[] cookies = request.getCookies();
	if(cookies!=null)
	{
	  for(int j=0; j < cookies.length; j++)
	  {
	     Cookie cookie = cookies[j];
	     if ("ifugle_dft_user".equals(cookie.getName()))
	     {
	       userName = cookie.getValue();
	       break;
	     }
	  }
	}
%>
<html>  
<head>  
<title>DNFT</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/logincss.css" />
</head>  
<style type="text/css">
.login_bg{
	background-image: url(images/login_bg.jpg);
	background-repeat: no-repeat;
	background-position: center center;
	height: 570px;
}
.STYLE1 {font-size: 12px}
</style>
<script language="JavaScript" >
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API); 
if (top != window)   
      top.location.href = window.location.href;  
      
function gotoNext()
{
  if(event.keyCode==13 && event.srcElement.type!='button' && event.srcElement.type!='submit' && event.srcElement.type!='reset' && event.srcElement.type!='textarea' && event.srcElement.type!='')
     event.keyCode=9;
}
function gotoSub(){
	if(event.keyCode==13){
		loginForm.submit();
	}
}
function onloads(){
  	document.loginForm.userAlias.select();
	document.loginForm.userAlias.focus();
	if(top.window != window)top.window = window;
}
function doSubmit(){
	document.loginForm.submit();
}
function doReset(){
	loginForm.reset();
}

function getAlias(){
	var loginName = document.getElementById("userAlias").value;
	var userid =document.getElementById("userID").value; 
	MaintainHandler.getAlias(loginName,function(data){
		if(data!=""){
		   	var result = Ext.util.JSON.decode(data);
		   	if(result&&result.alias!=""){
		   		document.getElementById("userID").value = document.getElementById("userAlias").value;
		   		document.getElementById("userAlias").value = result.alias;
		    }else{
		        document.getElementById("userAlias").value = "没有此用户！";
		        document.getElementById("userID").value = '';
		    }
		}
	});
}
</script>
<body onload="onloads();" scroll="no" >
<form action="login.mt?doType=login" method="post" id="loginForm" name="loginForm">
	<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0"> 
	<tr> 
	<td align="center"><table width="798" height="300" border="0" cellpadding="0" cellspacing="0" > 
	<tr> 
	<td align="center" class="login_bg">
	<div class="login_bg">
	  <table width="86%" height="300" border="0" cellpadding="0" cellspacing="0">
	    <tr>
	      <td width="51%" height="200">&nbsp;</td>
	      <td width="49%">&nbsp;</td>
	    </tr>
	    <tr>
	      <td height="200">&nbsp;</td>
	      <td ><table width="96%" height="100" border="0" cellpadding="0" cellspacing="0">
	        <tr>
	          <td width="18%" height="37" align="right"><span class="STYLE1">用户名：</span></td>
	          <td width="82%"><input tabindex="1" type="text" id="userAlias" name="uAlias" style="width:150px" onkeydown="gotoNext();" onblur="getAlias();" 
	            maxlength="30" value="<%=userName==null?"":userName%>" class="login_input" />
	            <input type="hidden" id="userID" name="userID" value="<%=userName==null?"":userName%>"/>
	          </td>
	        </tr>
	        <tr>
	          <td align="right"><span class="STYLE1">密　码：</span></td>
	          <td><input tabindex="2" type="password" name="pswd" style="width:150px" onkeydown="gotoSub();" class="login_input" maxlength="30"/></td>
	        </tr>
	        <tr>
	          <td height="50" colspan="2" align="center" valign="middle"><table width="100%" height="39" border="0" cellpadding="0" cellspacing="0">
	            <tr>
	              <td width="45%" align="right"><input name="bt_submit" type="button" class="login_submit" value="登录" onclick="javascript:doSubmit();"/></td>
	              <td width="5%">&nbsp;</td>
	              <td width="50%"><input name="bt_reset" type="reset" class="login_submit" value="重置"/></td>
	            </tr>
	          </table></td>
	          </tr>
	      </table></td>
	    </tr>
	  </table>
	</div>
	
	</td> 
	</tr> 
	</table></td> 
	</tr> 
	</table> 
</form>
</body>
</html>

