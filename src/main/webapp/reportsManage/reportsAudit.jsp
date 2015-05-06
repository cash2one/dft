<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.utils.entity.*"%>
<%@ page import="com.ifugle.dft.check.entity.*"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
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
<title>DNFT-报表审核</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
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
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
		{
	        header: "报表ID",
	        dataIndex: 'bb_id',
	        width: 100,
	        align: 'left'    
	    },{
	        header: "报表名称",
	        dataIndex: 'bb_desc',
	        width: 150,
	        align: 'left'    
	    },{
	        header: "年月",
	        dataIndex: 'ny',
	        width: 100,
	        align: 'left'    
	    },{
	        header: "发布状态",
	        dataIndex: 'checks',
	        width: 70,
	        align: 'left',
	        renderer: function(v,p,r){
				if(v==0){
					return "<font color=red>未发布</font>";
				}else{
					return "<font color=green>已发布</font>";
				}
			}        
	    }       
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'bb_id', type: 'string'},
	{name: 'bb_desc', type: 'string'},
	{name: 'ny', type: 'int'},
	{name: 'checks', type: 'int'},
]);
var ds = new Ext.data.GroupingStore({
	proxy: new Ext.data.DirectProxy({
		directFn: MaintainHandler.getReportsToAudit,
		paramsAsHash: false,
		paramOrder: ['start','limit']
	}), 
	reader: new Ext.data.JsonReader({
		//idProperty:'ny',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord),
	sortInfo:{field: 'ny', direction: "DESC"},
    groupField:'bb_id'
});
var view = new Ext.grid.GroupingView({
    forceFit:true,
    groupTextTpl: '{[values.rs[0].data.bb_id]}'
});
var grid = new Ext.grid.GridPanel({
		title:'报表发布审核',
		store: ds,
		height:250,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		selModel: ssm,
		stripeRows: true,
		tbar: [
    	{
			text: '发布',
			iconCls: 'publish',
            handler : function(){
	    		var records = grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要发布的报表记录!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '是否确定要发布选中月份的报表?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var rpts = new Array();
						for(var i=0;i<records.length;i++){
							var rd = records[i];
							var rpt =new Object();
							rpt.bb_id=rd.get("bb_id");
							rpt.ny=rd.get("ny");
							rpts.push(rpt);
						}
						MaintainHandler.publishReports(Ext.encode(rpts),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
					           	ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中报表已发布！");
							}else{
								Ext.Msg.alert("提示","报表发布时发生错误!");
							}
						});	
					}
				});
			}
		},{
			text: '收回',
			iconCls: 'unPublish',
            handler : function(){
            	var records = grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要收回发布的报表记录!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '是否确定要撤回选中月份的报表?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var rpts = new Array();
						for(var i=0;i<records.length;i++){
							var rd = records[i];
							var rpt =new Object();
							rpt.bb_id=rd.get("bb_id");
							rpt.ny=rd.get("ny");
							rpts.push(rpt);
						}
						MaintainHandler.undoPublishReports(Ext.encode(rpts),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
					           	ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中报表已撤回发布！");
							}else{
								Ext.Msg.alert("提示","撤回发布时发生错误!");
							}
						});	
					}
				});
			}
		}],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: ds,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[grid]
	});
	ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>