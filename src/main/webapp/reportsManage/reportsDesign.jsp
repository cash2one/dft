<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.fruit.query.data.*"%>
<%@ page import="com.fruit.query.report.*"%>
<%@ page import="com.fruit.query.service.*"%>
<%@ page import="com.fruit.query.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.fruit.query.parser.TemplatesLoader"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration) ContextUtil.getBean("config");
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
<link href="<%=request.getContextPath()%>/css/LockingGridView.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/SqlWin.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/ProcedureWin.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/ImplClassWin.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/rptForm.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/paramsGrid.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/colPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/dtForm.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/hfForm.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/chartForm.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/rptDesign/expForm.js"></script>
<script type="text/javascript">
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
var cRid="";
var UPDATEINFO_FUNS = {
	tHeadFoot : {partName:'headFoot',buildSaveInfoFun: buildHeadFoot}
	,tParams: {partName:'parameters',buildSaveInfoFun: buildParameters}
	,tCols: {partName:'columnDefine',buildSaveInfoFun: buildColumns}
	,tData: {partName:'dataSets',buildSaveInfoFun: buildDataSets}
	,tChart: {partName:'chart',buildSaveInfoFun: buildChart}
	,tExport: {partName:'dirExport',buildSaveInfoFun: buildDirExport}
}
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "报表ID",
	        dataIndex: 'id',
	        width: 150,
	        align: 'left'
	    },{
	        header: "报表名称",
	        dataIndex: 'name',
	        width: 250,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "说明",
	        dataIndex: 'description',
	        width: 300,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'id', type: 'string'},
	{name: 'name', type: 'string'},
	{name: 'saveFileName', type: 'string'},
	{name: 'description', type: 'string'}
]);
var rptProxy=new Ext.data.HttpProxy({url:'getData.design?doType=getRptTemplates'});  
var ds = new Ext.data.Store({
    proxy: rptProxy, 
	reader: new Ext.data.JsonReader({
		idProperty:'id',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
var grid = new Ext.grid.GridPanel({
	title:'报表模板',
	store: ds,
	height:250,
	cm: cm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : new Ext.grid.GridView(),
	selModel: ssm,
	stripeRows: true,
	tbar: [
    {
		text: '增加',
		iconCls: 'add',
        handler : function(){
            addWin.show();
		}
	},{
		text: '编辑',
		iconCls: 'details',
        handler : function(){
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择报表!");
				return;
			}
			cRid = records[0].get("id");
			edtWin.show();
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的报表!");
				return;
			}
		    cRid = records[0].get("id");
		    delWin.show();
		}
	},{
		text: '提交',
		iconCls: 'icon-up',
        handler : function(){
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要提交到生产环境的报表!");
				return;
			}
		    cRid = records[0].get("id");
		    Ext.MessageBox.confirm('确认', '是否要将选中的报表模板提交到生产环境?', function(btn){
				if(btn == 'yes') {
					Ext.Ajax.request({
						url : 'getData.design?doType=commitRpt',
						params : {rptId: cRid},
						success : function(response, options){
							var obj = Ext.decode(response.responseText);
							if(obj.success){
								Ext.Msg.alert("提示","报表已经提交!");
							}else{
								var info = obj.errorInfo;
							    Ext.Msg.alert('失败', "报表提交过程中发生错误。"+info);
							}
						},
						failure : function(response,option) {
							var obj = Ext.decode(response.responseText);
							var info = obj.errorInfo;
						    Ext.Msg.alert('失败', "报表提交过程中发生错误。"+info);
						}
					});		
				}
			});
		}
	},{
		text: '拉取',
		iconCls: 'icon-down',
        handler : function(){
        	productWin.show();
		}
	},{
		text: '导入',
		iconCls: 'icon-down',
        handler : function(){
			excelWin.show();
		}
	},{
		text: '导出',
		iconCls: 'icon-up',
        handler : function(){
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要导出的报表设计模板!");
				return;
			}
		    cRid = records[0].get("id");
		    var expUrl='getRptTemplate.design?doType=export&rptId='+cRid;
	        window.open(expUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
		}
	}],
	bbar: new Ext.PagingToolbar({
        pageSize: 20,
        store: ds,
        displayInfo: true,
        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
        emptyMsg: "没有数据",
        items: ['-']
    })
});
/*****************删除报表***********************/
var delForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 5,
	autoScroll:true,
    layout : 'form',
	items:[{
		hideLabel: true,
		boxLabel: '同时删除生产环境中的模板',
		xtype: 'checkbox',
		name: 'synDelete',
		width: 200,
		checked : false
	}]
});
var delWin = new Ext.Window({
	id : 'delWin',
	title : '删除报表',
	items : [delForm],
	layout : 'fit',
	width : 240,
	height : 130,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var sd=delForm.getForm().findField("synDelete").getValue();
			var synDelete = 0;
			if(sd=="on"||sd==true||sd=="1"){
				synDelete =1;
			}else{
				synDelete =0;
			}
			Ext.Ajax.request({
				url : 'getData.design?doType=deleteReport',
				params : {rptId: cRid,synDelete: synDelete},
				success : function(response, options){
					cRid="";
					var obj = Ext.decode(response.responseText);
					if(obj.success){
						Ext.Msg.alert("提示","报表已经删除!");
						ds.reload();
						delWin.hide();
					}else{
						var info = obj.errorInfo;
					    Ext.Msg.alert('失败', "报表删除过程中发生错误。"+info);
					}
				},
				failure : function(response,option) {
					var obj = Ext.decode(response.responseText);
					var info = obj.errorInfo;
				    Ext.Msg.alert('失败', "报表删除过程中发生错误。"+info);
				}
			});			
		}
	},{
		text : "取消",
		handler : function() {
			delWin.hide();
		}
	}]
});
delWin.on("show",function(){
	delForm.getForm().findField("synDelete").setValue(0);
});
/******************生产环境中的报表模板*********************************/
var proSsm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
proSsm.handleMouseDown = Ext.emptyFn;
var proCm = new Ext.grid.ColumnModel({
	columns: [
		proSsm,
	    {
	        header: "报表ID",
	        dataIndex: 'id',
	        width: 150,
	        align: 'left'
	    },{
	        header: "报表名称",
	        dataIndex: 'name',
	        width: 250,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "说明",
	        dataIndex: 'description',
	        width: 300,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var proRptRecord = Ext.data.Record.create([  
	{name: 'id', type: 'string'},
	{name: 'name', type: 'string'},
	{name: 'saveFileName', type: 'string'},
	{name: 'description', type: 'string'}
]);
var proRptProxy=new Ext.data.HttpProxy({url:'getData.design?doType=getRptTemplatesOfProduct'});  
var proDs = new Ext.data.Store({
    proxy: proRptProxy, 
	reader: new Ext.data.JsonReader({
		idProperty:'id',
		root: 'rows',
		totalProperty: 'totalCount'
	}, proRptRecord)
});
var proRptGrid = new Ext.grid.GridPanel({
	title:'',
	store: proDs,
	height:250,
	cm: proCm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : new Ext.grid.GridView(),
	selModel: proSsm,
	stripeRows: true,
	bbar: new Ext.PagingToolbar({
        pageSize: 20,
        store: proDs,
        displayInfo: true,
        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
        emptyMsg: "没有数据",
        items: ['-']
    })
});
var productWin = new Ext.Window({
	id : 'productWin',
	title : '生产环境中的报表模板',
	items : [proRptGrid],
	layout : 'fit',
	width : 460,
	height : 360,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var records = proRptGrid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要拉取到编辑库的报表模板!");
				return;
			}
		    cRid = records[0].get("id");
		    Ext.MessageBox.confirm('确认', '该操作将会覆盖编辑库中相同ID的模板，是否要拉取?', function(btn){
				if(btn == 'yes') {
					Ext.Ajax.request({
						url : 'getData.design?doType=pullRptsFromProduct',
						params : {rptId: cRid},
						success : function(response,option){
							var obj = Ext.decode(response.responseText);
							if(obj.success){
								Ext.Msg.alert("提示","完成从生产库的拉取!");
								ds.reload();
								delWin.hide();
							}else{
								var info = obj.errorInfo;
							    Ext.Msg.alert('失败', "从生产库拉取报表发生错误。"+info);
							}
						},
						failure : function(response,option) {
							var obj = Ext.decode(response.responseText);
							var info = obj.errorInfo;
						    Ext.Msg.alert('失败', "报表拉取过程中发生错误。"+info);
						}
					});			
				}
			});		
		}
	},{
		text : "取消",
		handler : function() {
			productWin.hide();
		}
	}]
});
productWin.on("show",function(){
	proDs.load({params:{start:0,limit:20}});
	proSsm.clearSelections();
});
//新增报表
var addForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 90,
	autoScroll:true,
    layout : 'form',
	items:[{
		fieldLabel: '报表ID',
		xtype: 'textfield',
		name: 'rptId',
		width: 150
	},{
		fieldLabel: '报表名称',
		xtype: 'textfield',
		name: 'rptName',
		width: 150
	},{
		fieldLabel: '报表说明',
		xtype: 'textarea',
		name: 'description',
		width: 150,
	    height: 80
	}]
});
var addWin = new Ext.Window({
	id : 'addWin',
	title : '新增报表',
	items : [addForm],
	layout : 'fit',
	width : 400,
	height : 300,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		id: 'btnAddNew',
		handler : function(){
			saveNew(0);
		}
	},{
		text : "继续编辑",
		handler : function(){
			saveNew(1);
		}
	},{
		text : "关闭",
		handler : function() {
			addWin.hide();
		}
	}]
});
addWin.on("show",function(){
	addForm.getForm().findField("rptId").setValue("");
	addForm.getForm().findField("rptName").setValue("");
	addForm.getForm().findField("description").setValue("");
});
function saveNew(type){
	//先检查重复ID
	var rid = addForm.getForm().findField("rptId").getValue(); 
	Ext.Ajax.request({
		url : 'getData.design?doType=checkRptId',
		params : {rid: rid},
		success : function(form, action){
			var obj = Ext.decode(form.responseText);
			if(obj.isDuplicated){
				Ext.Msg.alert('系统提示','报表ID重复，请修改！');
				Ext.getCmp('btnAddNew').enable();
	            return ;
			}else{
				Ext.getCmp("btnAddNew").disable();
				if(addForm.getForm().isValid()) {
					if(rid == '' || rid == null){
						Ext.Msg.alert('系统提示','报表ID必须填写！');
						Ext.getCmp('btnAddNew').enable();
			            return ;
			        }
					var name = addForm.getForm().findField("rptName").getValue();  
					if(name == '' || name == null){
						Ext.Msg.alert('系统提示','报表名称必须填写！');
						Ext.getCmp('btnAddNew').enable();
			            return ;
			        }
					addForm.getForm().doAction('submit', {
			            url : 'getData.design?doType=updateRptBase',
			            params : {saveType: 0},
			            method : 'POST',
			            success : function(form, action){
							addWin.hide();
			            	Ext.getCmp('btnAddNew').enable();
			            	ds.load({params:{start:0,limit:20}});
			            	if(type==1){
				            	cRid = rid;
				            	edtWin.show();
			            	}else{
			            		Ext.Msg.alert('成功', "新增报表成功！");
			            	}
						},
			            failure : function(form,action) {
							addWin.hide();
			            	Ext.getCmp('btnAddNew').enable();
			            	var info = action.result.errorInfo;
		            		Ext.Msg.alert('失败', "新增报表过程中发生错误。"+info);
			            }
			        });	
				}
			}
		}
	});		
}

//总的面板，包括各部分属性设置
var rptTabs = new Ext.TabPanel({  
	//id:'rptTabs',
	activeTab:0,  
	frame: true,
	layoutOnTabChange:true,
	items:[
	{
		layout:'fit',
		id:"tRpt",
	    title: '基础信息',
	    closable: false,
	    items: rptForm
	},{
		layout:'fit',
		id:"tHeadFoot",
	    title: '表头表尾',
	    closable: false,
	    items: hfForm
	},{
		layout:'fit',
		id:"tParams",
	    title: '参数',
	    closable: false,
	    items: paramsGrid
	},{
		layout:'fit',
		id:"tCols", 
	    title: '报表列',
	    closable: false,
	    items: colPanel
	},{
		layout:'fit',
		id:"tData", 
	    title: '数据',
	    closable: false,
	    items: dtForm
	},{
		layout:'fit',
		id:"tChart", 
	    title: '图表',
	    closable: false,
	    items: chartForm
	},{
		layout: 'fit',
		id: 'tExport',
		title: '导出',
		closable: false,
		items: expForm 
	}]
});
var edtWin = new Ext.Window({
	id : 'edtWin',
	title : '报表编辑',
	items : [rptTabs],
	layout : 'fit',
	width : 560,
	height : 480,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "应用",
		handler : function() {
			//保存当前tab的修改，不同的tab应调用不同的保存函数，包括各自的保存前检查，保存请求等。
			var cTab = rptTabs.getActiveTab();
			var ctid = cTab.getId();
			if(ctid=="tRpt"){
				updateRptBase();
			}else{
				var upcf = UPDATEINFO_FUNS[ctid];
				var upFun = upcf.buildSaveInfoFun;
				var upInfo = upFun.apply();
				saveReportPart(upcf.partName,Ext.encode(upInfo));
			}
		}
	},{
		text : "保存",
		handler : function() {
			var allUpInfo = new Object();
			//循环调用所有部分的信息组织，并按partName设置属性，组织object
			var partName = "";
			var uInfo = null;
			for(pro in UPDATEINFO_FUNS){
				partName = UPDATEINFO_FUNS[pro].partName;
				var upFun = UPDATEINFO_FUNS[pro].buildSaveInfoFun;
				upInfo = upFun.apply();
				allUpInfo[partName]=uInfo;
			}
			//保存基础信息
			updateRptBase();
			//保存其他部分
			saveReportPart("all",allUpInfo);
		}
	},{
		text : "关闭",
		handler : function() {
			edtWin.hide();
		}
	}]
});
edtWin.on("show",function(){
	loadRptForm();
	loadHfForm();
	loadParamForm();
	loadColPanel();
	loadDtForm();
	loadChartForm();
	loadExportForm();
});
//报表模板导入
var impForm = new Ext.FormPanel({    
	frame: true,
	labelWidth: 80,
	border: false,
	layout : 'form',
	fileUpload : true,
	buttonAlign: 'center',
	items:[
	{ 
		fieldLabel: 'Excel文件',
		inputType:'file',
		width:190,
		height:25,
		xtype: 'textfield',
		name: 'filepath'
	}]
});
var excelWin = new Ext.Window({
    title : '报表模板文件导入',
    width : 300,
    height : 250,
    layout : 'fit',
    items : [impForm],
    closeAction:'hide',
    buttons : [
    {
    	text : "关闭",
	    handler:function(){
    		excelWin.hide();
	    }
    },{
		name: 'import',
		id: 'import',
		text: '导入',
		handler : function() {
	    	var x=document.getElementById('filepath').value;
			if(!x||x==""){
	  			Ext.Msg.alert("提示","请选择要导入的文件!");
	  			return;
	  		}
	  		if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XML'){
	  			Ext.Msg.alert("提示","请选择XML格式的文件导入！");
	  			return;
			}  
	  		if (impForm.getForm().isValid()) {  
				Ext.Msg.wait("正在导入...");
				impForm.getForm().doAction('submit', {
		       		timeout: 10*60*1000,
		       		url: 'upload.design?doType=import',
		       		success: function(form, action) {
		       			Ext.Msg.hide();
						var jsonData = action.result;	
				        if(jsonData.impResult=="success"){
				           	Ext.Msg.show({title:'成功',
			   					msg: "导入成功",
			   					buttons: Ext.Msg.OK,
			   					icon: Ext.MessageBox.INFO});
				           	ds.load({params:{start:0, limit:<%=Configuration.getConfig().getString("pageSize","40")%>}});
						}else{
				        	Ext.Msg.hide();
				           	Ext.Msg.alert('信息','发生错误，异常信息:'+jsonData.info);
				        }
		       		},
					failure: function(form, action) {
						var jsonData = action.result;	
						Ext.Msg.hide();
						Ext.Msg.alert('错误', '上传文件失败，异常信息:'+jsonData.info);
				    },
					exceptionHandler : function(msg){
						Ext.Msg.hide();
						Ext.Msg.alert('提示',msg);
						return ; 
					}
		       	});
			}
    	}
	}]
});
excelWin.on("show",function(){
	document.getElementById('filepath').value="";
});
function loadReportPart(partName,cbfun){
	Ext.Ajax.request({
		url : 'getData.design?doType=loadReportPart',
		params : {rptId: cRid,partName: partName},
		success : function(response, options){
			var obj = Ext.decode(response.responseText);
			if(obj.success){
				var partInfo = obj.partInfo;
				cbfun(partInfo);
			}else{
				var info = obj.errorInfo;
			    Ext.Msg.alert('失败', "加载报表配置信息过程中发生错误。"+info);
			}
		},
		failure : function(response,option) {
			var obj = Ext.decode(response.responseText);
			var info = obj.errorInfo;
		    Ext.Msg.alert('失败', "加载报表过程中发生错误。"+info);
		}
	});	
}
function saveReportPart(partName,updateInfo){
	Ext.Ajax.request({
		url : 'getData.design?doType=updateReportPart',
		method:'POST',
		params : {rptId: cRid,rptPart: partName, updateInfo: updateInfo},
		timeout: 30000,
		success : function(response, options){
    		var result = Ext.util.JSON.decode(response.responseText);
    		if(result.success){
    			Ext.Msg.alert('信息','信息保存成功!');
	 	    }else{
	 	        Ext.Msg.alert('信息','信息保存时发生错误!'+result.errorInfo);
	 	    }
		},
		failure : function(response,option) {
		    Ext.Msg.alert("失败","信息保存时发生错误!");		
		}
	});
}
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        items:[grid]
	});
	ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
}); 
</script>
</head>
<body>
</body>
</html>