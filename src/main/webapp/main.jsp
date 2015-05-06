<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%
	User user = (User) session.getAttribute("user");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>DNFT</title>
<link rel="stylesheet" type="text/css"
	href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/homecss.css"
	rel="stylesheet" type="text/css" />
<script type="text/javascript"
	src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript"
	src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript"
	src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript"
	src="<%=request.getContextPath()%>/js/sys.js"></script>
<style type="text/css">
html,body {
	font: normal 12px verdana;
	margin: 0;
	padding: 0;
	border: 0 none;
	overflow: hidden;
	height: 100%;
}
.about{
	margin: 5px 10px 10px 50px; /* 上 右 下 左 */
}
.info {
	font-size: 12px;
	color: #333333;
	font-family: airial;
	font-weight: normal;
}

a:link {
	color: #000000;
	text-decoration: none;
}

a:visited {
	color: #000000;
	text-decoration: none;
}

a:hover {
	color: #000000;
	text-decoration: none;
}

a:active {
	color: #000000;
	text-decoration: none;
}

.right {
	background-image: url(images/right.jpg);
	background-repeat: no-repeat;
	background-position: right;
}

.x-panel-body p {
	margin: 5px;
}

.x-column-layout-ct .x-panel {
	margin-bottom: 5px;
}

.x-column-layout-ct .x-panel-dd-spacer {
	margin-bottom: 5px;
}

.settings {
	background-image: url(images/folder_wrench.png) !important;
}

.nav {
	background-image: url(images/folder_go.png) !important;
}

a.menu_a {
	padding: 5px 10px;
	background: #F5F9FC;
	display: block;
	border-bottom: 1px solid #ddd;
	color: black;
	text-decoration: none;
}

a.menu_a.current {
	background: #D6E0EA;
}

a.menu_a:hover {
	background: #D6E0EA;
	color: #466a8f;
	text-decoration: none;
}
</style>
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
var userid = '<%=user == null ? "" : user.getUserid()%>';
Ext.BLANK_IMAGE_URL = 'libs/ext-3.4.0/resources/images/default/s.gif';
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
var loaded = false;
var moduleTree = new Ext.tree.TreePanel({
	id: 'moduleTree',
	width:250,
	region: 'west',
	title: '模块',
    split: true,
    collapsible: true,
    autoScroll: true,
    rootVisible: false,
    lines: false,
    singleExpand: true,
    useArrows: true,
    loader: new Ext.tree.TreeLoader({
        dataUrl:'login.mt?doType=getModules'
    }),
    root: new Ext.tree.AsyncTreeNode()
});
moduleTree.getLoader().on("beforeload", function(treeLoader, node) {
	var cId = loaded?node.id:"";
	treeLoader.baseParams.pid=cId;
}, this); 
moduleTree.getLoader().on("load", function(treeLoader, node) {
	loaded = true;
}, this); 
  
var pswForm = new Ext.FormPanel({
	id: 'pswForm',
	frame: true,
	labelAlign: 'left',
	bodyStyle:'padding:5px',
	width: 200,
    api: {
        submit: MaintainHandler.changePswd
    },
    paramOrder: ['userid'],
	layout: 'absolute', 
	items: [{
		x: 10,
		y: 15,
		xtype:'label',
		text: '现在的密码：'
	},{
		x: 80,
		y: 10,
		xtype: 'textfield',
		name: 'oPswd',
		inputType:'password',
		anchor:'96%  90%'
	},{
		x: 10,
		y: 50,
		xtype:'label',
		text: '输入新密码：'
	},{
		x: 80,
		y: 45,
		xtype: 'textfield',
		name: 'newPswd',
		maxLength:100,
		inputType:'password',
		anchor:'96%  90%'
	},{
		x: 10,
		y: 85,
		xtype:'label',
		text: '确认新密码：'
	},{
		x: 80,
		y: 80,
		xtype: 'textfield',
		name: 'confirmPswd',
		inputType:'password',
		maxLength:100,
		anchor:'96%  90%'
	}
	]
});
	    
var pwConfig = {
	title : '修改密码',
	width : 300,
	height : 200,
	resizable : false,
	autoScroll : true,
	layout : 'fit',
	items : [pswForm],
	closeAction:'hide',
	buttons : [{
		text : "确定",
		handler:function(){
			var f = pswForm.getForm();
			if(!f.isValid()) return;
			if(Ext.get('newPswd').getValue() != Ext.get('confirmPswd').getValue()){
				Ext.Msg.alert('信息','两次输入的密码不一致');
				return;
			}
			if(Ext.get('newPswd').getValue()==""){
				Ext.Msg.alert('信息','密码不能为空！');
				return;
			}
			f.submit({
				waitMsg:'执行中,请稍候...',
				params: {userid: '<%=user.getUserid()%>'},
				success: function(form,action){
					var obj = action.result;
					if(obj&&obj.infos){
						Ext.Msg.show({title:'成功',
							msg: obj.infos.msg,
							buttons: Ext.Msg.OK,
							icon: Ext.MessageBox.INFO});
						pwin.hide();
					}
				},
				failure: function(form,action){
					var obj = action.result;
					if(obj&&obj.errors){
						Ext.Msg.alert("警告",obj.errors.msg);
					}
				}
			});
		}
	},{
		text : "取消",
		handler : function(){
		    pswForm.getForm().reset();
			pwin.hide();	
		}
	}
	]
};
		
pwin = new Ext.Window(pwConfig);
pwin.on("show",function(){
	Ext.getCmp("pswForm").getForm().findField("oPswd").setValue(""); 
	Ext.getCmp("pswForm").getForm().findField("newPswd").setValue(""); 
	Ext.getCmp("pswForm").getForm().findField("confirmPswd").setValue(""); 	
});
var sysInfoWin = new Ext.Window({
	title : '系统信息',
	width : 300,
	height : 200,
	resizable : false,
	autoScroll : true,
	buttonAlign: 'center',
	layout : 'fit',
	items : [{
		id: 'about',    
		layout:'fit',
        title: '',
        html: "<div>&nbsp;</div><div class='about'>使用单位&nbsp;：<%=cg.getString("userEnterprise","")%></div><div class='about'>当前版本&nbsp;：<%=cg.getString("version","")%></div><div class='about'>技术支持&nbsp;：<%=cg.getString("company","")%></div><div class='about'>维护热线&nbsp;：<%=cg.getString("supportTel","")%></div>" 
	}],
	closeAction:'hide',
	buttons : [{
		text : "确定",
		handler:function(){
			sysInfoWin.hide();
		}
	}]
});
function showInfo(){
	sysInfoWin.show();
}
function changePswd(){
	pwin.show();
}	

function loginout(){
	window.top.location.href="<%=request.getContextPath()%>/index.jsp";
}
  
function showNavigate(mid,mc,pmc){
	Ext.getCmp("west-panel").setTitle(pmc+"-->"+mc);
}

Ext.onReady(function(){
    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

    var viewport = new Ext.Viewport({
        layout:'border',
        items:[
        {	
            id:'north-panel',
            region:'north',
            //margins:'5 0 0 5',
            layout:'column',
            height:75,
			title:"",
			items:[
				{contentEl: 'topBanner'}
			]
        },
        moduleTree,
        {
            region:'center',
            //margins:'0 0 5 0',
            layout:'column',
            autoScroll:false,
            html:"<iframe id='mid_right' name='mid_right' src='<%=request.getContextPath()%>/right.jsp' frameborder=0  marginwidth=0 width=100% height=100% ></iframe>"
        }]
    });
    moduleTree.expand();
});
</script>
</head>
<body>
<table id='topBanner' style="width:100%;table-layout:fixed;" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td nowrap width="530" height="73" class="bg"><img src="<%=request.getContextPath()%>/images/topimg.jpg" width="539" height="73" /></td>
    <td nowrap height="73" align="right" width="220" class="bg">&nbsp;</td>
    <td height="73" align="right" class="bg" valign="bottom">
     <table height="35" border="0" cellspacing="0" cellpadding="0">
      <tr>
	<td align="left" valign="middle" width="50">
	  <span><a href="javascript:changePswd();" /><font color="#ffffff">密码</font></a></span>
	</td>
	<td align="left" valign="middle" width="50">
	  <span><a href="javascript:loginout();" /><font color="#ffffff">注销</font></a></span>
	</td>
	<td align="left" valign="middle" width="50">
	  <span><a href="javascript:showInfo();" /><font color="#ffffff">关于</font></a></span>
	</td>
	<td align="left" valign="middle" ><font color="#ffffff">当前用户：<%=user == null ? "" : user.getName()%>&nbsp;&nbsp;&nbsp;</font>
        </td>
      </tr>
     </table>
    </td>
  </tr>
</table>
</body>
</html>