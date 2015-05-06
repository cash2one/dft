<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	String today = sdf.format(new Date());
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
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/CodeTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript">
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.ck.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.ck.REMOTING_API);
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "企业地址",
	        dataIndex: 'dz',
	        width: 120,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var enDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: EnHandler.testEncodingList,
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
	}, cRecord)
});
var view = new Ext.grid.GridView({});
var enGrid = new Ext.grid.GridPanel({
		title:'本地中文列表',
		store: enDs,
		region: 'center',
		height:250,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		selModel: ssm,
		stripeRows: true
});
var gssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
gssm.handleMouseDown = Ext.emptyFn;
var gscm = new Ext.grid.ColumnModel({
	columns: [
		gssm,
	    {
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "企业地址",
	        dataIndex: 'dz',
	        width: 120,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var gsRecord = Ext.data.Record.create([  
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var gsDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: EnHandler.testEncodingGsList,
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
	}, gsRecord)
});
var gview = new Ext.grid.GridView({});
var gsGrid = new Ext.grid.GridPanel({
		title:'国税UTF8列表',
		store: gsDs,
		region: 'north',
		height:250,
	    cm: gscm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : gview,
		selModel: gssm,
		stripeRows: true
});
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'border',
        autoScroll:true,
        items:[enGrid,gsGrid]
	});
	enDs.load();
	gsDs.load();
});         
</script>
</head>
<body>
</body>
</html>