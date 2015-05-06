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
	Map dictionary = cg.getDJFieldsToShow();
	List financeFlds = (List)dictionary.get("DJ_CZ");
	List dsFlds = (List)dictionary.get("DJ_DS");
	List gsFlds = (List)dictionary.get("DJ_GS");
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
var dsConditions={},gsConditions={},mConditions={};
var taxType = 0;
var mapWinOnShow = false;
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "法人",
	        dataIndex: 'fddbr',
	        width: 80,
	        align: 'left',
	        renderer: renderFoo
		},{
			header: "地址",
	        dataIndex: 'dz',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo
		}
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([ 
	{name: 'xh', type: 'int'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'fddbr', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var enDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: CheckHandler.getEns,
		paramOrder: ['enType','start','limit','conditions'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
enDs.on("beforeload",function(){
	enDs.baseParams.enType=9;
	enDs.baseParams.conditions="{ismap:3}";
});
var view = new Ext.grid.GridView();
var enGrid = new Ext.grid.GridPanel({
		title:'',
		store: enDs,
		height: 250,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		selModel: ssm,
		stripeRows: true,
		tbar: [
		/*{
			text: '自动匹配',
			iconCls: 'autoMap',
            handler : function(){
				Ext.MessageBox.confirm('确认', '系统将会按税号对所有企业进行自动匹配，是否继续?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						EnHandler.doAutoMap(function(data){
							var obj = Ext.decode(data);
							if(!obj){
								Ext.Msg.alert("提示","自动映射过程中出错，映射失败!");
							}else{
								Ext.Msg.alert("提示",obj.info);
							}
						});
					}
				});
			}
		},*/{
			text: '解除映射',
			iconCls: 'unMap',
            handler : function(){
				var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要解除映射关系的企业!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '是否要解除当前企业的映射关系?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var ens = new Array();
						for(var i=0;i<records.length;i++){
							var xh = records[i].get("xh");
							ens.push(xh);
						}
						EnHandler.undoMapping(ens.join(),function(data){
							var obj = Ext.decode(data);
							if(!obj)return;
							if(obj.result==1){
								conditons = {};
								enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中企业的映射关系已解除!");
							}else{
								conditons = {};
								enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示",obj.info);
							}
						});
					}
				});
			}
		},{
			text: '刷新',
			iconCls: 'refresh',
            handler : function(){
				conditons = {};
				enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
			}
		}],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: enDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
enGrid.getSelectionModel().on("rowselect",function(sm,rIndex,e){
	cEnid = enGrid.getStore().getAt(rIndex).get("xh");
	//载入单户企业的内容
	CheckHandler.getEnDjInfo(cEnid,9,function(data){
		var obj = Ext.decode(data);
		if(obj){
			var fInfo = obj.finance;
			var dsInfo = obj.ds;
			var gsInfo = obj.gs;
			if(fInfo){
				var fForm = financeForm.getForm();
				<%for(int i=0;i<financeFlds.size();i++){
					En_field fld = (En_field)financeFlds.get(i);%>
					<%if(fld.getVal_src()==2){%>
					fForm.findField('<%=fld.getField()%>_fn').setValue(fInfo.<%=fld.getField()+"_MC"%>==null?"未知":fInfo.<%=fld.getField()+"_MC"%>);
					fForm.findField('<%=fld.getField()%>_fn_bm').setValue(fInfo.<%=fld.getField()%>==null?"":fInfo.<%=fld.getField()%>);
					<%}else{%>
					fForm.findField('<%=fld.getField()%>_fn').setValue(fInfo.<%=fld.getField()%>);
				<%}}%>
			}
			if(dsInfo){
				var dForm = dsForm.getForm();
				<%if(dsFlds!=null){
					for(int i=0;i<dsFlds.size();i++){
						En_field fld = (En_field)dsFlds.get(i);%>
						<%if(fld.getVal_src()==2){%>
						dForm.findField('<%=fld.getField()%>_ds').setValue(dsInfo.<%=fld.getField()+"_MC"%>==null?"未知":dsInfo.<%=fld.getField()+"_MC"%>);
						<%}else{%>
						dForm.findField('<%=fld.getField()%>_ds').setValue(dsInfo.<%=fld.getField()%>==null?"":dsInfo.<%=fld.getField()%>);
					<%  }
					}
				}%>
			}
			if(gsInfo){
				var gForm = gsForm.getForm();
				<%if(gsFlds!=null){
					for(int i=0;i<gsFlds.size();i++){
						En_field fld = (En_field)gsFlds.get(i);%>
						<%if(fld.getVal_src()==2){%>
						gForm.findField('<%=fld.getField()%>_gs').setValue(gsInfo.<%=fld.getField()+"_MC"%>);
						<%}else{%>
						gForm.findField('<%=fld.getField()%>_gs').setValue(gsInfo.<%=fld.getField()%>);
					<%	}
					}
				}%>
			}
		}
	});
});
enGrid.getSelectionModel().on("rowdeselect",function(sm,rIndex,e){
	financeForm.getForm().reset();
	dsForm.getForm().reset();
	gsForm.getForm().reset();
});
/*****************************单户企业的分tab详情*******************************/
//登记财政form
	<%
	int cpr = financeFlds.size()/2;
	int mod = financeFlds.size()%2;
	for(int i=0;i<financeFlds.size();i++){
		En_field fld = (En_field)financeFlds.get(i);%>
		var fnFld_<%=fld.getField()%> = new Ext.create({
			xtype: 'compositefield',
			fieldLabel :'<%=fld.getMc()%>',
			items: [{
				id : 'fn_<%=fld.getField()%>',
			    
			    name : '<%=fld.getField()%>_fn',
			    readOnly: true,
			    width : 150,
				xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
			}<%
			if(fld.getVal_src()==2){%>
			,{
				id : 'fn_<%=fld.getField()%>_bm',
			    fieldLabel :'',
			    name : '<%=fld.getField()%>_fn_bm',
				xtype : 'hidden'
			}
			<%}%>
			]
		});
	<%}%>
	var financeForm  = new Ext.FormPanel({
		layout : 'column',
		id : 'financeForm',
		frame: true,
		autoScroll: true,
		border : false,
		bodyStyle:'padding:7px',
		items : [
			{
				columnWidth : .50,
				layout : 'form',
				labelWidth : 120,
				border : false,
				labelAlign : 'right',
				items : [
				<%for(int i=0;i<cpr+mod;i++){
					En_field fld = (En_field)financeFlds.get(i);%>
					fnFld_<%=fld.getField()%>
					<%if(i<cpr+mod-1){out.print(",");}%>
				<%}%>
				] 	
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth : 120,
				border : false,
				labelAlign : 'right',
				items : [
				<%for(int i=cpr+mod;i<financeFlds.size();i++){
					En_field fld = (En_field)financeFlds.get(i);%>
						fnFld_<%=fld.getField()%>
					<%if(i<financeFlds.size()-1){out.print(",");}%>
				<%}%>
				]	
			}
		]
	});
	//地税
	<%
	cpr = dsFlds==null?0:dsFlds.size()/2;
	mod = dsFlds==null?0:dsFlds.size()%2;
	if(dsFlds!=null){
		for(int i=0;i<dsFlds.size();i++){
			En_field fld = (En_field)dsFlds.get(i);
		%>
		var dsFld_<%=fld.getField()%> = new Ext.form.Field({
			id : 'ds_<%=fld.getField()%>',
		    fieldLabel :'<%=fld.getMc()%>',
		    name : '<%=fld.getField()%>_ds',
		    readOnly: true,
		    width : 200,
			xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
		});
		<%}
	}%>
	
var dsForm = new Ext.FormPanel({
	layout : 'column',
	id : 'dsForm',
	frame: true,
	border : false,
	autoScroll: true,
	bodyStyle:'padding:7px',
	items : [
		{
			columnWidth : .35,
			layout : 'form',
			labelWidth : 120,
			border : false,
			labelAlign : 'right',
			items : [
			<%if(dsFlds!=null){
				for(int i=0;i<cpr+mod;i++){
					En_field fld = (En_field)dsFlds.get(i);%>
					dsFld_<%=fld.getField()%>
					<%if(i<cpr+mod-1){out.print(",");}%>
				<%}
			}%>
			] 	
		},{
			columnWidth : .35,
			layout : 'form',
			labelWidth : 120,
			border : false,
			labelAlign : 'right',
			items : [
			<%if(dsFlds!=null){
				for(int i=cpr+mod;i<dsFlds.size();i++){
					En_field fld = (En_field)dsFlds.get(i);%>
					dsFld_<%=fld.getField()%>
					<%if(i<dsFlds.size()-1){out.print(",");}%>
				<%}
			}%>
			]	
		}
	]
});
//国税
<%
cpr = gsFlds==null?0:gsFlds.size()/2;
mod = gsFlds==null?0:gsFlds.size()%2;
if(gsFlds!=null){
	for(int i=0;i<gsFlds.size();i++){
		En_field fld = (En_field)gsFlds.get(i);
	%>
	var gsFld_<%=fld.getField()%> = new Ext.form.Field({
		id : 'gs_<%=fld.getField()%>',
	    fieldLabel :'<%=fld.getMc()%>',
	    name : '<%=fld.getField()%>_gs',
	    readOnly: true,
	    width : 200,
		xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
	});
	<%}
}%>
var gsForm = new Ext.FormPanel({
	layout : 'column',
	id : 'gsForm',
	frame: true,
	autoScroll: true,
	border : false,
	bodyStyle:'padding:7px',
	items : [
		{
			columnWidth : .35,
			layout : 'form',
			labelWidth : 120,
			border : false,
			labelAlign : 'right',
			items : [
			<%if(gsFlds!=null){
				for(int i=0;i<cpr+mod;i++){
					En_field fld = (En_field)gsFlds.get(i);%>
					gsFld_<%=fld.getField()%>
					<%if(i<cpr+mod-1){out.print(",");}%>
				<%}
			}%>
			] 	
		},{
			columnWidth : .35,
			layout : 'form',
			labelWidth : 120,
			border : false,
			labelAlign : 'right',
			items : [
			<%if(gsFlds!=null){
				for(int i=cpr+mod;i<gsFlds.size();i++){
					En_field fld = (En_field)gsFlds.get(i);%>
					gsFld_<%=fld.getField()%>
					<%if(i<gsFlds.size()-1){out.print(",");}%>
				<%}
			}%>
			]	
		}
	]
});
//分tab显示三方信息
var dtailTabs=new Ext.TabPanel({  
	id:'dtailTabs',
	activeTab:0,  
	frame: true,
	enableTabScroll:true,
	layoutOnTabChange:true,
	items:[
		{
			id: 'fDetail',
			layout:'fit',
            title: '企业登记信息(财政)',
            closable: false,
            items: financeForm 
        }<%
        if(dsFlds!=null&&dsFlds.size()>0){%>
        ,{
        	id: 'dsDetail', 
        	layout:'fit',
            title: '企业登记信息(地税)',
            closable: false, 
            items: dsForm
        }<%}
        if(gsFlds!=null&&gsFlds.size()>0){%>
        ,{
	        id: 'gsDetail', 
	        layout:'fit',
	        title: '企业登记信息(国税)',
	        closable: false, 
	        items: gsForm 
        }
        <%}%>
    ]
});  
/**********************地税未映射*****************************/
var dssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
dssm.handleMouseDown = Ext.emptyFn;
var dscm = new Ext.grid.ColumnModel({
	columns: [
		dssm,
	    {
	        header: "企业内码",
	        dataIndex: 'qynm',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "法人",
	        dataIndex: 'fddbr',
	        width: 80,
	        align: 'left',
	        renderer: renderFoo
		},{
			header: "地址",
	        dataIndex: 'dz',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo
		}
	],
	defaultSortable: false
});
var dsRecord = Ext.data.Record.create([  
	{name: 'xh', type: 'int'},
	{name: 'qynm', type: 'string'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'fddbr', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var dsDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: CheckHandler.getEns,
		paramOrder: ['enType','start','limit','conditions'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, dsRecord)
});
dsDs.on("beforeload",function(){
	dsDs.baseParams.enType=9;
	dsConditions.ismap=0;
	dsDs.baseParams.conditions=Ext.encode(dsConditions);
});

var dsview = new Ext.grid.GridView();
var dsGrid = new Ext.grid.GridPanel({
		title:'',
		store: dsDs,
		height:250,
	    cm: dscm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : dsview,
		selModel: dssm,
		stripeRows: true,
		tbar: [
		{
			text: '筛选',
			iconCls: 'filter',
            handler : function(){
            	taxType = 0;
				fltWin.show();
			}
		},{
			text: '手工映射',
			iconCls: 'autoMap',
            handler : function(){
				var records = dsGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要映射关系的企业!");
					return;
				}
				taxType = 0;
				mapWinOnShow = true;
				mapWin.show();
			}
		},{ 
			text: '刷新',
			iconCls: 'refresh',
            handler : function(){
				dsConditons = {};
				dsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
			}
		}],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: dsDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
var gssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
gssm.handleMouseDown = Ext.emptyFn;
var gscm = new Ext.grid.ColumnModel({
	columns: [
		gssm,
	    {
	        header: "电子档案号",
	        dataIndex: 'dzdah',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 90,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "法人",
	        dataIndex: 'fddbr',
	        width: 80,
	        align: 'left',
	        renderer: renderFoo
		},{
			header: "地址",
	        dataIndex: 'dz',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo
		}
	],
	defaultSortable: false
});
var gsRecord = Ext.data.Record.create([  
	{name: 'xh', type: 'int'},
	{name: 'dzdah', type: 'string'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'fddbr', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var gsDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: CheckHandler.getEns,
		paramOrder: ['enType','start','limit','conditions'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, gsRecord)
});
gsDs.on("beforeload",function(){
	gsDs.baseParams.enType=9;
	gsConditions.ismap=1;
	gsDs.baseParams.conditions=Ext.encode(gsConditions);
});

var gsview = new Ext.grid.GridView();
var gsGrid = new Ext.grid.GridPanel({
	title:'',
	store: gsDs,
	height:250,
	cm: gscm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : gsview,
	selModel: gssm,
	stripeRows: true,
	tbar: [
	{
		text: '筛选',
		iconCls: 'filter',
        handler : function(){
			taxType = 1;
			fltWin.show();
		}
	},{
		text: '手工映射',
		iconCls: 'autoMap',
        handler : function(){
			var records = gsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要映射关系的企业!");
				return;
			}
			taxType = 1;
			mapWinOnShow = true;
			mapWin.show();
		}
	},{
		text: '刷新',
		iconCls: 'refresh',
        handler : function(){
			gsConditons = {};
			gsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	}],
	bbar: new Ext.PagingToolbar({
        pageSize: <%=cg.getString("pageSize","40")%>,
	    store: gsDs,
	    displayInfo: true,
	    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	    emptyMsg: "没有数据",
	    items: ['-']
    })
});
/****************************匹配窗体*******************************/
var mssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
mssm.handleMouseDown = Ext.emptyFn;
var dscm = new Ext.grid.ColumnModel({
	columns: [
		mssm,
	    {
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "企业内码",
	        dataIndex: 'qynm',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "电子档案号",
	        dataIndex: 'dzdah',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 90,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "法人",
	        dataIndex: 'fddbr',
	        width: 70,
	        align: 'left',
	        renderer: renderFoo
		},{
			header: "地址",
	        dataIndex: 'dz',
	        width: 160,
	        align: 'left',
	        renderer: renderFoo
		}
	],
	defaultSortable: false
});
var mRecord = Ext.data.Record.create([ 
	{name: 'xh', type: 'int'},
	{name: 'qynm', type: 'string'},
	{name: 'dzdah', type: 'string'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'fddbr', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var mDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: CheckHandler.getEns,
		paramOrder: ['enType','start','limit','conditions'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, mRecord)
});
mDs.on("beforeload",function(){
	mDs.baseParams.enType=9;
	mConditions.ismap=(taxType==0)?1:0;
	mDs.baseParams.conditions=Ext.encode(mConditions);
});

var mview = new Ext.grid.GridView();
var mapGrid = new Ext.grid.GridPanel({
		title:'',
		store: mDs,
		height:250,
	    cm: dscm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : mview,
		selModel: mssm,
		stripeRows: true,
		tbar: [
		{
		   	text: '筛选',
		   	iconCls: 'filter',
		    handler : function(){
		   		fltWin.show();
		   	}
		}
		],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: mDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
var mapWin = new Ext.Window({
	id : 'mapWin',
	title : '待映射',
	items : [mapGrid],
	layout : 'fit',
	width : 450,
	height : 380,
	modal : true,
	closeAction:'hide',
	closable : false,
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			//映射
			var records = mapGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要映射的企业!");
				return;
			}
			var dsXh = 0, gsXh = 0;
			if(taxType==0){
				var dsSlt = dsGrid.getSelectionModel().getSelections();
				dsXh = dsSlt? dsSlt[0].get("xh"):0;
				gsXh = records[0].get("xh");
			}else{
				var gsSlt = gsGrid.getSelectionModel().getSelections();
				gsXh = gsSlt? gsSlt[0].get("xh"):0;
				dsXh = records[0].get("xh");
			}
	        Ext.MessageBox.confirm('确认', '确定要将当前选中企业建立映射关系?', function(btn){
				if(btn == 'yes') {
					EnHandler.mapEns(dsXh,gsXh,function(data){
						var obj = Ext.decode(data);
						if(!obj)return;
						if(obj.result==1){
							Ext.Msg.alert("提示","企业映射关系已建立!");
							conditons = {};
							enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							dsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							gsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
						}else{
							Ext.Msg.alert("提示",obj.info);
						}
					});
				}
			});
			mapWinOnShow = false;
			mapWin.hide();
		}
	},{
		text : "取消",
		handler : function() {
			mapWinOnShow = false;
			mapWin.hide();
		}
	}]
});	
mapWin.on("show",function(){
	if(taxType==0){
		mapWin.setTitle("待映射国税企业");
	}else{
		mapWin.setTitle("待映射地税企业");
	}
	mConditions = {};
	mDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
});
/*************************筛选窗体:名称、地址、法人*******************/
var fltForm = new Ext.FormPanel({
	frame: true,
	labelWidth:60,
	width :240,
	labelAlign:'right',
	items:[
	{
		name:'qymc',
		id: "qymc",
		xtype:"textfield",
		width:180,
	    fieldLabel : '名称',
	    maxLength: 50
	},{
		name:'address',
		id: "address",
		xtype:"textfield",
		width:180,
	    fieldLabel : '地址',
	    maxLength: 50
	},{
		name:'frdb',
		id: "frdb",
		xtype:"textfield",
		width:180,
	    fieldLabel : '法人',
	    maxLength: 50
	}
	]
});	
var fltWin = new Ext.Window({
	id : 'fltWin',
	title : '筛选',
	items : [fltForm],
	layout : 'fit',
	width : 300,
	height : 200,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			doConditions();
		}
	},{
		text : "取消",
		handler : function() {
			fltWin.hide();
		}
	}]
});	
fltWin.on("show",function(){
	fltForm.getForm().findField("qymc").setValue("");
	fltForm.getForm().findField("address").setValue("");
	fltForm.getForm().findField("frdb").setValue("");
});
function doConditions(){
	var qymc = fltForm.getForm().findField("qymc").getValue();
	var address = fltForm.getForm().findField("address").getValue();
	var frdb = fltForm.getForm().findField("frdb").getValue();
	var cdt = {};
	var fldNames = new Array();
	var fldValues = new Array();
	var relations = new Array();
	var connections = new Array();
	
	if(qymc&&!qymc==""){
		fldNames.push("mc");
		fldValues.push(qymc);
		relations.push("like");
		connections.push("_and");
	}
	if(address&&!address==""){
		fldNames.push("dz");
		fldValues.push(address);
		relations.push("like");
		connections.push("_and");
	}
	if(frdb&&!frdb==""){
		fldNames.push("fddbr");
		fldValues.push(frdb);
		relations.push("like");
		connections.push("_and");
	}
	if(connections.length>0){
		connections[connections.length-1]="empty"; 
	}
	cdt.fldNames=fldNames.join();
	cdt.fldValues=fldValues.join();
	cdt.relations=relations.join();
	cdt.connections=connections.join();
	if(mapWinOnShow){
		mConditions=cdt;
		mDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
	}else{
		if(taxType == 0){
			dsConditions=cdt;
			dsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}else{
			gsConditions=cdt;
			gsDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	}
	fltWin.hide();
}
/**************已映射界面分上下：上面是已映射列表，下面是单户信息***********/
var mappedPanel=new Ext.Panel({
	layout:'border',
    autoScroll:true,
	bodyStyle:'padding:0px;background:none;border:0px;',
	items:[{	
		region:"north",
        height:400,
        bodyStyle:'padding:0px;background:none;border:0px;',
        layout: 'fit',
        items:[enGrid]
	},{	
		region:"center",
        layout: 'fit',
        bodyStyle:'padding:0px;background:none;border:0px;',
        items:[dtailTabs]
	}]
});	 
/***************************tab，三个界面*******************************/
var mapTab = new Ext.TabPanel({  
	id:'mapTab',
	activeTab:0,  
	enableTabScroll:true,
	layoutOnTabChange:true,
	bodyStyle:'padding:0px;background:none;border:0px;',
	items:[
		{
			id: 'mapped',
			layout:'fit',
            title: '已手工映射',width:'100%',
            closable: false,
            items: mappedPanel 
		},{
			id: 'nomap_ds',
			layout:'fit',
            title: '地税未映射',
            closable: false,
            items: dsGrid 
        },{
        	id: 'nomap_gs', 
        	layout:'fit',
            title: '国税未映射',
            closable: false, 
            items: gsGrid
        }
    ]
});   
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
	    layout:'fit',
        autoScroll:true,
        items:[mapTab]
	});
	enDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
	dsDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
	gsDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>