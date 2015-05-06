<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>DNFT</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/treasury.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.treasury.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.treasury.REMOTING_API);
var mTypeCombo = new Ext.form.ComboBox({ 
    fieldLabel: '数据文件格式',
    name: 'd_type',
    value: 'eksml',
    width: 120,
    mode : 'local', 
    triggerAction : 'all', 
    forceSelection:true,
    store : new Ext.data.SimpleStore({ 
    	fields : ["fid", "text"], 
    	data : [ 
    	['eksml', 'XML格式'], 
    	['tekst', '文本格式']
     	] 
    })  ,
    valueField : "fid", 
    displayField : "text", 
    readOnly: false,
    id: 'd_type'
});
var impForm = new Ext.FormPanel({    
	id: 'impForm',
	frame:true,
	labelWidth:80,
	bodyStyle:'padding-top:2px',
	layout : 'form',
	labelAlign:'right',
	fileUpload : true,
	items:[
	{
		mTypeCombo,
		{ 
			fieldLabel: 'XML文件',
			inputType:'file',
			width:120,
			height:25,
			xtype: 'textfield',
			name: 'filepath',
			id: 'filepath'
		}
	}],
	buttons:[{
		name: 'import',
		id: 'import',
		text: '导入',
		handler : function() {
			if(!impForm.getForm().findField.('filepath').getValue()){
	  			Ext.Msg.alert("提示","请选择要导入的文件!");
	  			return;
	  		}
	    }
	}]
});

Ext.onReady(function(){
	Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	//布局，上面一个form，下面是tabs
	var viewport = new Ext.Viewport({
	    layout:'border',
	    items:[
	    {	
	        id:'north-panel',
	        region:'north',
	        margins:'2 0 0 2',
	        layout:'fit',
	        frame:true,
	        height:160,
			title:"导入配置",
			items: impForm
	    },{  
			region:'center',  
			layout:'fit',  
			split:true,  
			collapsible: true,  
			margins:'0 0 0 0',  
			items: tabPanel  
		}]
	}); 	   
});
</script>
</head>
<body>
</body>
</html>