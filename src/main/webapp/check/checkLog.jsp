<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.parser.TemplatesLoader"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	Report cLogRpt = TemplatesLoader.getTemplatesLoader().getReportTemplate("_sys_check_log");
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
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/DynamicGrid.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript"><!--

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
var cHid = -1;
var cMetaDataLoaded = false;
var cLoadDefaultMeata = true;
var cLoadUserMeata = false;
var condition ={};
var cRptID ="";
function buildEnParams(cGrid){
	var dParams = new Object();
	dParams.metaDataLoaded = grid.metaDataLoaded;
	var mps = new Object();
	mps["hid"]=cHid.toString();
	dParams.macroParams = mps;
	return dParams;
}
var grid = new App.ux.DynamicGridPanelAuto({
	columns : [],
	enableColumnMove :true,
	stripeRows: true,
	store : new Ext.data.DirectStore({
		directFn : QueryHandler.queryGeneralDataDynamic,
		paramsAsHash : false,
		remoteSort: <%=cLogRpt.getRemoteSort()==1?"true":"false"%>,
		paramOrder: ['rptID','start','limit','condition'],
		fields : []
	})
});
grid.getStore().grid = grid;
grid.getStore().on("beforeload",function(ds,op){
	//组织参数
	ds.baseParams.rptID = "<%=cLogRpt.getId()%>";
	var dParams = buildEnParams(grid);
	op.params.condition = Ext.encode(dParams);
});
var lForm = new Ext.FormPanel({
    id: 'lForm',
    border:false,
    frame:true,
    labelAlign: 'right',
	layout:'form',
    items: [{
    	name:'OLDVAL',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '核定原值',
	    readOnly: true
    },{
    	name:'NEWVAL',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '核定新值',
	    readOnly: true
    },{
    	name:'HDLX',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '核定类型',
	    readOnly: true
    }/*,{
    	name:'filepath',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '附件',
	    readOnly: true
    }*/,{
    	name:'YXRTKNAME',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '影响税款',
	    readOnly: true
    },{
    	name:'FROMDAY',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '起始日期',
	    readOnly: true
    },{
    	name:'TODAY',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '终止日期',
	    readOnly: true
    },{
    	name:'YXTYPENAME',
    	xtype: 'textfield',
		width: 150,
	    fieldLabel : '影响登记',
	    readOnly: true
    },{
    	name:'hasTaxDetail',
    	xtype: 'label',
        html: '',
        width: 150,
        fieldLabel : '税款明细',
        id: 'hasTaxDetail'
    },{
    	name:'USERNAME',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '核定用户',
	    readOnly: true
    },{
    	name:'IP',
		xtype:"textfield",
		width: 150,
	    fieldLabel : '操作地址',
	    readOnly: true
    },{
    	name:'REMARK',
		xtype:"textarea",
		width: 150,
		height: 50,
	    fieldLabel : '备注信息',
	    readOnly: true
    }]
});
grid.getSelectionModel().on("rowselect",function(sm,rIndex,e){
	cHid = grid.getStore().getAt(rIndex).get("hid");
	CheckHandler.getCheckLogInfo(cHid,function(data){
		var obj = Ext.decode(data);
		if(obj&&obj.log){
			lForm.getForm().loadRecord(obj.log);
			if(obj.log.YXRTK==0){
				Ext.getCmp("hasTaxDetail").update("");
			}else{
				var h = '<a href="rtkinfo.query?rptID=_sys_rtkOfCheck&hid='+cHid;
				h = h+'" target="_blank" style=text-decoration:underline;vertical-align:bottom;color:red;>查看</a>';
				Ext.getCmp("hasTaxDetail").update(h);
			}
			
		}
	});
});
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'column',
       	frame:false,
        border:false,
       	items:[
       	{
           	layout:'fit',
			columnWidth: .65,
			frame: true,
			border: false,
			items:[grid]
		},{
			layout:'fit',
			columnWidth: .35,
			frame: true,
			border: false,
			items:[lForm]
		}
	});
	ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
</script>
</head>
<body>
</body>
</html>