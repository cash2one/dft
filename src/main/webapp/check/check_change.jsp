<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.utils.entity.*"%>
<%@ page import="com.ifugle.dft.check.entity.*"%>
<%@ page import="java.text.SimpleDateFormat;"%>
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
	String sEnType = request.getParameter("enType")==null?"0":request.getParameter("enType");
	String czfpbm_BJ= cg.getString("czfpbm_BJ","");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
	String sYear = sdf.format(new Date());
	String wScreen = cg.getString("wScreen","yes");
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
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/GroupHeaderPlugin.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<style type="text/css">
.x-grid3-cell-inner{
	border-right:1px solid #eceff6;
} 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/GroupHeaderPlugin.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
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
var cEnid = 0;
var cQymc;
var cCheckType = 0;
var conditions ="";
var btnCount = 0;
var cExecuteType=1,cAffectMode=0;
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
//ssm.handleMouseDown = Ext.emptyFn;
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
	enDs.baseParams.enType=<%=sEnType%>;
	enDs.baseParams.conditions=conditions;
});
enDs.on("load",function(){
	financeForm.getForm().reset();
	dsForm.getForm().reset();
	gsForm.getForm().reset();
	for(var i=0;i<btnCount;i++){
		Ext.getCmp("btnChange"+i).disable();
	}
});
var view = new Ext.grid.GridView({
	<%if("".equals(czfpbm_BJ)){%>
	getRowClass : function(r, rowIndex){
		if(r.get("czfpbm")=='<%=czfpbm_BJ%>'){
			return "changed";
		}
	}
	<%}%>
});
var enGrid = new App.ux.ExportGridPanel({
		title:'变更企业',
		store: enDs,
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
			text: '筛选',
			iconCls: 'filter',
            handler : function(){
				fltWin.show();
			}
		},/*{
			text: '批量核定',
			iconCls: 'details',
            handler : function(){
            	batchWin.show();
			}
		},{
			text: '导入',
			iconCls: 'impExcel',
            handler : function(){
				excelWin.show();
			}
		},*/{
			text: '转为正常',
			iconCls: 'accept',
            handler : function(){
				var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要操作的企业!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '确定要转为正常?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var ens = new Array();
						for(var i=0;i<records.length;i++){
							var sh = records[i].get("xh");
							ens.push(sh);
						}
						CheckHandler.acceptEn(ens.join(),2,function(data){
							var obj = Ext.decode(data);
							if(obj){
								if(obj.result){
									conditions = "";
						           	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
						           	changeDs.removeAll();
								}
							}
						});	
					}
				});
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
//enGrid.getSelectionModel().on("rowselect",function(sm,rIndex,e){
enGrid.on("rowclick",function(grid,rIndex,e){
	cEnid = enDs.getAt(rIndex).get("xh");
	cQymc = enDs.getAt(rIndex).get("mc");
	//载入变更信息
	changeDs.load();
	//载入单户企业的内容
	CheckHandler.getEnDjInfo(cEnid,9,function(data){
		var obj = Ext.decode(data);
		if(obj){
			for(var i=0;i<btnCount;i++){
				Ext.getCmp("btnChange"+i).enable();
			}
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
				<%		}
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
				<%		}
					}
				}%>
			}
		}
	});
});
/*enGrid.getSelectionModel().on("rowdeselect",function(sm,rIndex,e){
	financeForm.getForm().reset();
	dsForm.getForm().reset();
	gsForm.getForm().reset();
	for(var i=0;i<btnCount;i++){
		Ext.getCmp("btnChange"+i).disable();
	}
});*/
/*****************************单户企业的分tab详情*******************************/
	//登记财政form
	<%
	int cpr = financeFlds.size()/2;
	int mod = financeFlds.size()%2;
	int btnCount=0;
	for(int i=0;i<financeFlds.size();i++){
		En_field fld = (En_field)financeFlds.get(i);%>
		var fnFld_<%=fld.getField()%> = new Ext.create({
			xtype : 'container',
            layout : 'column',
			items: [{
				columnWidth : <%="yes".equals(wScreen)?".55":".68"%>,
				border: true,
                xtype : 'container',
                layout : 'form',
                items  : [
                {
					id : 'fn_<%=fld.getField()%>',
					fieldLabel :'<%=fld.getMc()%>',
				    name : '<%=fld.getField()%>_fn',
				    readOnly: true,
				    width :<%="yes".equals(wScreen)?"200":"150"%>,
				    //style:'background:none;border:0px;',
					xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
				}
	            <%if(fld.getVal_src()==2){%>
				,{
					id : 'fn_<%=fld.getField()%>_bm',
				    fieldLabel :'',
				    name : '<%=fld.getField()%>_fn_bm',
					xtype : 'hidden'
				}
				<%}%>
	            ]
			}
			<%if(fld.getShowmod()>1){%>
			,{
				columnWidth : .16,
				items:{
					name : 'change',
	                xtype: 'button',
	                text: '更改',
	                id: 'btnChange<%=btnCount%>',
	                disabled : true,
	                handler: function(){
						cCheckType = 0;
	                	var cVal = financeForm.getForm().findField('<%=fld.getField()%>_fn').getValue();
	                	<%if(fld.getVal_src()<2){%>
	                	showValueWin(cVal,'<%=fld.getField()%>','<%=fld.getMc()%>',<%=fld.getIsrtk()%>);
	                	<%}else{%>
	                	cVal = financeForm.getForm().findField('<%=fld.getField()%>_fn_bm').getValue();
	                	showTreeWin(cVal,'<%=fld.getField()%>','<%=fld.getMc()%>','<%=fld.getMapbm()%>',<%=fld.getIsrtk()%>);
	                	<%}%>
					}
				}
			}<%
			btnCount++;
			}%>
			]
		});
	<%}%>
	btnCount = <%=btnCount%>;
	var financeForm  = new Ext.FormPanel({
		layout : 'column',
		id : 'financeForm',
		frame: true,
		border : false,
		autoScroll: true,
		bodyStyle:'padding:7px',
		items : [
			{
				columnWidth : .50,
				layout : 'form',
				labelWidth : 80,
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
				labelWidth : 80,
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
		    width : 180,
		    //style:'background:none;border:0px;',
			xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
		});
	<%	}
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
			columnWidth : .5,
			layout : 'form',
			labelWidth : 90,
			border : false,
			labelAlign : 'right',
			items : [
			<%if(dsFlds!=null){
				for(int i=0;i<cpr+mod;i++){
					En_field fld = (En_field)dsFlds.get(i);%>
					dsFld_<%=fld.getField()%>
					<%if(i<cpr+mod-1){out.print(",");}%>
			<%	}
			}%>
			] 	
		},{
			columnWidth : .5,
			layout : 'form',
			labelWidth : 90,
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
	    width : 180,
	    //style:'background:none;border:0px;',
		xtype : '<%=fld.getValuetype()==0?"textfield":"numberfield"%>'
	});
	<%}
}%>
var gsForm = new Ext.FormPanel({
	layout : 'column',
	id : 'gsForm',
	frame: true,
	border : false,
	autoScroll: true,
	bodyStyle:'padding:7px',
	items : [
		{
			columnWidth : .5,
			layout : 'form',
			labelWidth : 90,
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
			columnWidth : .5,
			layout : 'form',
			labelWidth : 90,
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
//变更信息
var changeCm = new Ext.grid.ColumnModel({
	columns : [
	{
		header : "变更部门",
		dataIndex : 'wdly',
		width : 90,
		align : 'left',
		renderer : function(v,p,r){
			if(v==1){
				return "国税";
			}else if(v==0){
				return "地税";
			}else {
				return "其它";
			}
		}
	},{
		header : "变更内容",
		dataIndex : 'c_fldname',
		width : 80,
		align : 'left',
		renderer : renderFoo
	},{
		header : "变更前",
		dataIndex : 'finame',
		width : 150,
		align : 'left',
		renderer : renderFoo
	},{
		header : "变更后",
		dataIndex : 'fdname',
		width : 150,
		align : 'left',
		renderer : renderFoo
	},{
		header : "变更日期",
		dataIndex : 'bgrq',
		width : 100,
		align : 'left',
		renderer : renderFoo
	}/*,{
		header : "操作",
		dataIndex : '',
		width : 90,
		align : 'left',
		renderer: function cancel(v,p,r){  
		    var cancelButton ='<a href=javascript:acceptNewVal(';
		    cancelButton+='"'+r.get("c_field")+'","'+r.get("c_fldname")+'","'+r.get("f_dest")+'","'+r.get("fdname")+'")>接受新值</a>';  
		    return cancelButton;  
		}
	}*/],
	defaultSortable : false
});
var changeRecord = Ext.data.Record.create([
	{name : 'c_field',type : 'string'}, 
	{name : 'c_fldname',type : 'string'},
	{name : 'bgrq',type : 'int'},
	{name : 'wdly',type : 'int'},
	{name : 'tdname',type : 'string'},
	{name : 'fdname',type : 'string'},
	{name : 'tiname',type : 'string'},
	{name : 'finame',type : 'string'},
	{name : 'f_now',type : 'string'},
	{name : 'fnname',type : 'string'},
	{name : 'f_init',type : 'string'},
	{name : 'f_dest',type : 'string'},
	{name : 't_init',type : 'string'},
	{name : 't_dest',type : 'string'}
	
]);
var changeDs = new Ext.data.Store({
	proxy : new Ext.data.DirectProxy({
		directFn : CheckHandler.getEnChangeInfo,
		paramOrder: ['xh'],
		paramsAsHash : false
	}),
	reader : new Ext.data.JsonReader({
		idProperty : 'c_field'
	}, changeRecord)
});
changeDs.on("beforeload",function(){
	changeDs.baseParams.xh=cEnid;
});
var changeGrid = new Ext.grid.GridPanel({
	title : '',
	store : changeDs,
	cm : changeCm,
	frame : false,
	height : 240,
	autoScroll: true,
	stripeRows : true,
	loadMask : {msg : '正在加载数据....'},
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	stripeRows : true
});
function acceptNewVal(fld,fldname,fdest,fdname){
	Ext.MessageBox.confirm('确认', '是否确定将'+cQymc+'的"'+fldname+'"属性值设置为"'+fdname+'"?', function(btn){
		if(btn == 'yes') {// 选中了是按钮
			CheckHandler.checkChangeEn(cEnid,fld,fdest,function(data){
				var obj = Ext.decode(data);
				if(obj){
					if(obj.result){
						Ext.Msg.alert("确定","财政登记表相应字段已更新！");
						changeDs.load();
					}
				}
			});	
		}
	});
}
//分tab显示三方信息
var dtailTabs=new Ext.TabPanel({  
	id:'dtailTabs',
	activeTab:0,  
	frame: true,
	enableTabScroll:true,
	layoutOnTabChange:true,
	items:[
		{
			id: 'changeDetail',
			layout:'fit',
            title: '变更信息',
            closable: false,
            items: changeGrid 
		},{
			id: 'fDetail',
			layout:'fit',
            title: '企业登记信息(财政)',
            closable: false,
            //listeners: {activate: fActivate},
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
/**************************核定*******************************/
function showValueWin(cVal,fldName,fldMc,isRTK){
	Ext.Msg.prompt(fldMc+'核定', '输入'+fldMc+"值:", function(btn, text){
	    if (btn == 'ok'){
	    	if(Ext.isEmpty(text)){
				Ext.Msg.alert("提示","核定目标值不能为空！");
				return;
			}
	    	if(text==cVal){
				Ext.Msg.alert("提示","新值与原值相同，不做更新！");
				return;
			};
	    	//根据是否能影响入退库显示入退库窗体。
	    	if(isRTK==1){
	    		//affectRTK(fldName,cVal,text,text);
	    		showCheckInfoWin(1,fldName,cVal,text,text);
	    	}else{
	    		showCheckInfoWin(0,fldName,cVal,text,text);
	    	}
	    }
	},this,false,cVal);
}
var treeWin;
var cText;
function showTreeWin(cVal,fldName,fldMc,mapBM,isRTK){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn:CheckHandler.getBmCodesTree,
			onlyLeafCheckable: true,
			codeTable: mapBM,
			defaultValue: cVal
		});
	}
	var p = {table: mapBM,selectedVals: cVal};
	treeWin.onSelect = function(value){
		if(!value)return;
		if(Ext.isEmpty(value.id)){
			Ext.Msg.alert("提示","核定目标值不能为空！");
			return;
		}
		if(value.id==cVal){
			Ext.Msg.alert("提示","新值与原值相同，不做更新！");
			return;
		}
		cText = value.text;
		//根据是否能影响入退库显示入退库窗体。
    	if(isRTK == 1){
    		//affectRTK(fldName,cVal,value.id,value.text);
    		showCheckInfoWin(1,fldName,cVal,value.id,value.text);
    	}else{
    		showCheckInfoWin(0,fldName,cVal,value.id,value.text);
    	}
	};
	treeWin.setTreeParams( p);
	treeWin.refreshTree();
	treeWin.show();
}
var ckTypeRecord = Ext.data.Record.create([
    {name : 'mc',type : 'string'}, 
    {name : 'bm',type : 'string'}
]);
var ckTypeDs = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
        directFn : CheckHandler.getComboBms,
        paramOrder: ['tbname'],
        paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'bm'
    }, ckTypeRecord)
});
ckTypeDs.on("beforeload",function(){
    ckTypeDs.baseParams.tbname="BM_HDLX";
});
ckTypeDs.load({});
var rtkForm = new Ext.FormPanel({
	frame: true,
	width :300,
	labelAlign:'left',
	layout:'form',
	items:[
	{
		xtype:'fieldset',
		autoHeight: true,
        title: '影响税款',
        layout:'form',
        labelWidth: 30,
		items: [{
			layout : 'column',
            border : false,
            autoScroll :true,
            items :[
            {
                columnWidth : .36,
                layout : 'form',
                border : true,
                overflow:'auto',
                items : [
                    new Ext.form.Radio({
	    				id:'noaffect',
	    				name : 'affect',
	    				labelSeparator : '',
	    				boxLabel : '不影响税款',
	    				hideLabel: true,
	    				inputValue : '0',
	    				checked : true,
	    				listeners:{
	    					check:function(fld,checked){
	    				    	if(checked){
	    				    		rtkForm.getForm().findField("from").disable();
	    				    		rtkForm.getForm().findField("to").disable();
	    				    	}
	    					}
	    				}
	    			}),new Ext.form.TextField({ 
	    			    fieldLabel: '',
	    			    hidden: true,
	    			    hideLabel: true,
	    			    name: 'fldName',
	    			    id: 'fldName'
	    			}),new Ext.form.TextField({ 
	    			    fieldLabel: '',
	    			    hidden: true,
	    			    hideLabel: true,
	    			    name: 'oldVal',
	    			    id: 'oldVal'
	    			}),new Ext.form.TextField({ 
	    			    fieldLabel: '',
	    			    hidden: true,
	    			    hideLabel: true,
	    			    name: 'newVal',
	    			    id: 'newVal'
	    			}),new Ext.form.TextField({ 
	    			    fieldLabel: '',
	    			    hidden: true,
	    			    hideLabel: true,
	    			    name: 'newValDesc',
	    			    id: 'newValDesc'
	    			})
	    		]
            },{
            	columnWidth : .35,
                layout : 'form',
                border : true,
                overflow:'auto',
                items : [
					new Ext.form.Radio({
						id:'allaffect',
						name : 'affect',
						labelSeparator : '',
						hideLabel: true,
						boxLabel : '影响所有税款',
						inputValue : '1',
						listeners:{
							check:function(fld,checked){
						    	if(checked){
						    		rtkForm.getForm().findField("from").disable();
						    		rtkForm.getForm().findField("to").disable();
						    	}
							}
						} 
					})
				]
            },{
            	columnWidth : .29,
                layout : 'form',
                border : true,
                overflow:'auto',
                items : [
					new Ext.form.Radio({
						id:'affectpart',
						name : 'affect',
						labelSeparator : '',
						hideLabel: true,
						boxLabel : '影响部分税款',
						inputValue : '2',
						listeners:{
							check:function(fld,checked){
						    	if(checked){
						    		rtkForm.getForm().findField("from").enable();
						    		rtkForm.getForm().findField("to").enable();
						    	}
							}
						}
					})
				]
            }]
		},{
			layout : 'column',
            border : false,
            autoScroll :true,
            items : [
            {
            	columnWidth : .36,
                layout : 'form',
                border : true,
                overflow:'auto',
                items : [
				{
					id : 'ifrom',
				    name : 'from',
				    fieldLabel :'起始',
				    width : 60,
				    disabled: true,
				    value: '<%=sYear%>01',
					xtype : 'numberfield'
				}]
            },{
            	columnWidth : .35,
                layout : 'form',
                border : true,
                overflow:'auto',
                items : [
				{
					id : 'ito',
					fieldLabel :'终止',
					name : 'to',
					width : 60,
					value: '<%=sYear%>12',
					disabled: true,
					xtype : 'numberfield'
				}]
			},{
	        	columnWidth : .29,
	            layout : 'form',
	            border : true,
	            overflow:'auto',
	            items : [
				new Ext.form.Label({
					id:"tFormat",
					text:"格式:YYYYMM"
				})]
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
		title: '备注',
		layout : 'form',
		items:[
			new Ext.form.TextArea({
				name : 'remark',
				hideLabel : true,
				width : 390,
				height: 60
			})
		] 	
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '其他',
		layout : 'form',
		labelWidth: 60,
		items:[
			new Ext.form.ComboBox({
				name : 'cType',
				width : 150,
				hiddenName: 'checkType',
				fieldLabel: "核定类型",
				displayField : 'mc',
                valueField : 'bm',
                typeAhead : true,
                mode : 'local',
                triggerAction : 'all',
                emptyText : '核定类型',
                selectOnFocus : true,
                editable : false,
                store : ckTypeDs
			}),new Ext.form.Checkbox({
				id:'taxAffect',
				name : 'affectMode',
				hideLabel: true,
				boxLabel : '仅影响税收',
				checked  : false
			})
		]
	}]	   	
});	
var rtkWin = new Ext.Window({
	id : 'rtkWin',
	title : '核定信息',
	items : [rtkForm],
	layout : 'fit',
	width : 440,
	height : 375,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [{
		text : "确定",
		handler : function() {
			var ats = document.getElementsByName('affect');//根据名字得到一组数组
			var aType;
			for(var i =0;i<ats.length;i++){
				if(ats[i].checked){
					aType = ats[i].value;
				}
			}	
			var from = rtkForm.getForm().findField("from").getValue();
			var to = rtkForm.getForm().findField("to").getValue();
			var fldName = rtkForm.getForm().findField("fldName").getValue();
			var oldVal = rtkForm.getForm().findField("oldVal").getValue();
			var newVal = rtkForm.getForm().findField("newVal").getValue();
			var newValDesc = rtkForm.getForm().findField("newValDesc").getValue();
			var checkType = rtkForm.getForm().findField("checkType").getValue();
			if(aType=="1"){
				from=0;
				to=999999;
			}
			if(aType=="2"){
				if(from==null||from==""){
					Ext.Msg.alert("错误","请输入影响税款的起始时间。");
					return;
				}
				if(to==null||to==""){
					Ext.Msg.alert("错误","请输入影响税款的终止时间。");
					return;
				}
				var f = from.toString().match(/^\d{4}(0[1-9]|1[0-2])$/);
				var t = to.toString().match(/^\d{4}(0[1-9]|1[0-2])$/);
				if(f==null){
					Ext.Msg.alert("错误","起始时间格式不正确！");
					return;
				}
				if(t==null){
					Ext.Msg.alert("错误","终止时间格式不正确！");
					return;
				}
				if(Number(from)>=Number(to)){
					Ext.Msg.alert("错误","起始时间不应大于终止时间！");
					return;
				}
			}
			var ams = document.getElementsByName("affectMode")[0];
			if(ams.checked){
				cAffectMode = 1;
			}else{
				cAffectMode = 0;
			}
			var remark = rtkForm.getForm().findField("remark").getValue();
			rtkWin.hide();
			check(fldName,oldVal,newVal,aType,from,to,newValDesc,checkType,remark);
		}
	},{
		text : "取消",
		handler : function() {
			rtkWin.hide();
		}
	}]
});	
function showCheckInfoWin(isrtk,fldName,oldVal,newVal,newValDesc){
	rtkForm.getForm().findField("fldName").setValue(fldName);
	rtkForm.getForm().findField("oldVal").setValue(oldVal);
	rtkForm.getForm().findField("newVal").setValue(newVal);
	rtkForm.getForm().findField("newValDesc").setValue(newValDesc);
	rtkForm.getForm().findField("remark").setValue("");
	rtkForm.getForm().findField("remark").setValue("");
	if(isrtk==1){
		//启用，默认值
		Ext.getCmp("affectpart").enable();
		Ext.getCmp("allaffect").enable();
		Ext.getCmp("noaffect").enable();
		Ext.getCmp("noaffect").setValue(true);
		rtkForm.getForm().findField("from").disable();
		rtkForm.getForm().findField("to").disable();
		
	}else{
		Ext.getCmp("affectpart").disable();
		Ext.getCmp("allaffect").disable();
		Ext.getCmp("noaffect").disable();
		Ext.getCmp("noaffect").setValue(true);
		rtkForm.getForm().findField("from").disable();
		rtkForm.getForm().findField("to").disable();
		
	}
	rtkWin.show();
}
function check(fldName,oldVal,newVal,affect,from,to,newValDesc,checkType,remark){
	var iAffect = Number(affect);
	var iFrom = Number(from);
	var iTo = Number(to);
	var xhs = new Array();
	if(cCheckType == 1){
		var rds = enGrid.getSelectionModel().getSelections();
       	for(var i=0;i<rds.length;i++){
       		xhs.push(rds[i].get("xh"));
       	}	
       	cEnid = xhs.join();
	}else{
		cEnid = cEnid+"";
	}
	Ext.MessageBox.confirm('核定', "确定要提交？", function(btn){
	    if (btn == 'yes'){
	    	Ext.Msg.wait("正在核定...");
	    	//后台核定
	    	CheckHandler.check(cEnid,fldName,newVal,iAffect,iFrom,iTo,Number(cExecuteType),Number(cAffectMode),checkType,remark,function(data){
	    		Ext.Msg.hide();
	    		var obj = Ext.decode(data);
	    		if(obj&&obj.result=="1"){
	    			//重载
	    			Ext.Msg.alert("提示",obj.info==""?"核定完成！":obj.info);
	    			var fForm = financeForm.getForm();
	    			if(fForm.findField(fldName+"_fn_bm")){
	    				fForm.findField(fldName+"_fn_bm").setValue(newVal);
	    				fForm.findField(fldName+"_fn").setValue(cText);
	    			}else{
	    				fForm.findField(fldName+"_fn").setValue(newVal);
	    			}	
	    		}else{
	    			Ext.Msg.alert("错误",obj.info==""?"核定过程中发生错误，核定失败！":obj.info);
	    		}
	    	});
	    }
	});
}
/*********************批量核定************************/
var bfRecord = Ext.data.Record.create([
    {name: 'field', type: 'string' },
    {name: 'mc', type: 'string' },
    {name: 'mapbm', type: 'string' },
    {name: 'val_src', type: 'int' },
    {name: 'isrtk', type: 'int' }
]);
var bfStore = new Ext.data.Store({
    proxy: new Ext.data.DirectProxy({
    	directFn: CheckHandler.getBatchCheckFields,
    	paramsAsHash: false
	}), 
    reader: new Ext.data.JsonReader({idProperty: "field"}, bfRecord)
});
bfStore.load();
var batchForm = new Ext.FormPanel({
	id: 'batchForm',
	frame: true,
	labelAlign:'right',
	items: [{
		id: "batchFlds",
        name: "bFlds",
        xtype:'combo',
        fieldLabel: '核定属性',
        width:100,
        mode : 'local', 
        triggerAction : 'all', 
        hiddenName:'bField',
        forceSelection:true,
        store : bfStore,
        valueField : "field", 
        displayField : "mc", 
        readOnly: false
	}]
});
var batchWin = new Ext.Window({
	title : '批量核定',
	width : 250,
	height : 180,
	autoScroll : true,
	layout : 'fit',
	items : [batchForm],
	closeAction:'hide',
	buttons : [{
		text : "确定",
		handler:function(){
			var records = enGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要核定的企业!");
				return;
			}	
			cCheckType = 1;
			var cfld = batchForm.getForm().findField("bField").getValue();
			var frd = bfStore.getById(cfld);
			if(frd.get("val_src")<2){//如果val_src<2，输入新值，否则跳出树
				showValueWin("",frd.get("field"),frd.get("mc"),frd.get("isrtk"));
			}else{
				showTreeWin("",frd.get("field"),frd.get("mc"),frd.get("mapbm"),frd.get("isrtk"));
			}
			batchWin.hide();
		}
	},{
        text : "关闭",
        handler : function() {
			batchWin.hide();
        }
    }]
});
/*********************excel导入***********************/
function required(para){
	var obj=document.getElementById(para);
	if(obj.getAttribute("value")&&obj.getAttribute("value")!=""){
		return "success";
	}else{
		return "不能为空！";
	}
}
var radiogroup= new Ext.form.RadioGroup({   
    fieldLabel : '匹配模式',
	width:100,  
	id:'d_type',
    items : [{   
        boxLabel : '税号',   
        inputValue : "sh",      
        name : "type"
    }, {   
        boxLabel : '名称',   
        name : "type",   
        checked:true,
        inputValue : "mc"  
    }]   
}); 
var excelForm = new Ext.FormPanel({    
	id: 'excelForm',
	frame:true,
	labelWidth:60,
	api: {
        submit: CheckHandler.importEnExcel
    },
    bodyStyle:'padding-top:2px',
    layout : 'form',
    fileUpload : true,
    labelAlign: 'right',
	items:[
	{
		layout:'column',
		frame: true,
		items:[
		{
			columnWidth:.5,
			layout: 'form',
			items:[
			{ 
			    fieldLabel: 'Excel文件',
			    inputType:'file',
			    width:120,
			    height:20,
			    xtype: 'textfield',
			    name: 'filepath',
			    id: 'filepath'
			},{ 
		    	fieldLabel:'匹配列',
			    id : 'matchCol',
			    name:'matchCol',
			    xtype:'numberfield',
			    width:120,
			    allowDecimals:false,
			    allowBlank:false
			}]
		},{
			columnWidth:.5,
			layout: 'form',
			items:[
				radiogroup,
			{ 
			    fieldLabel:'起始行',
				id : 'beginRow',
				name:'beginRow',
				xtype:'numberfield',
				width:120,
				allowDecimals:false,
				allowBlank:false
			}
			]
		}]
	}],
	buttons:[]
});
var excelWin = new Ext.Window({
    title : 'Excel文件导入',
    width : 600,
    height : 300,
    layout : 'fit',
    items : [excelForm],
    closeAction:'hide',
    buttons : [
    {
    	text : "导入",
	    handler:function(){
	    	if(required('filepath')!='success'){
	  			Ext.Msg.alert("提示","请选择要导入的文件!");
	  			return;
	  		}
	  		var x=document.getElementById('filepath').value;
	  		if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLS'&&x.substr(x.lastIndexOf(".")).toUpperCase()!="XLSX"){
	  			Ext.Msg.alert("提示","请选择Excel文件导入！");
	  			return;
			}  
	  		if (excelForm.getForm().isValid()) {  
				Ext.Msg.wait("正在导入...");
				excelForm.getForm().submit({
		       		timeout: 10*60*1000,
		       		params:{d_type:Ext.getCmp('d_type').getValue()},
		       		success: function(form, action) {
		       			Ext.Msg.hide();
		       			var obj = action.result;
						if(obj&&obj.infos){
							Ext.Msg.show({title:'成功',
								msg: obj.infos.msg,
								buttons: Ext.Msg.OK,
								icon: Ext.MessageBox.INFO});
							conditions='{matchExcel:true}';
				           	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
						}
		       		},
		       		failure: function(form,action){
						var obj = action.result;
						if(obj&&obj.errors){
							if(obj.errors.table_bm){
								Ext.Msg.alert("警告",obj.errors.table_bm);
							}else{
								Ext.Msg.alert("警告",obj.errors.msg);
							}
						}
					},
					exceptionHandler : function(msg){
						Ext.Msg.hide();
						Ext.Msg.alert('提示',msg);
						return ; 
					}
		       	});
			}
	    }
    },{
    	text : "关闭",
	    handler:function(){
    		excelWin.hide();
	    }
    }]
});

/***************************筛选*************************/
// 字段选择
var fltssm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
fltssm.handleMouseDown = Ext.emptyFn;
var fltcm = new Ext.grid.ColumnModel({
	columns : [
	fltssm,
	{
		header : "字段",
		dataIndex : 'field',
		width : 80,
		align : 'left',
		renderer : renderFoo
	}, {
		header : "名称",
		dataIndex : 'mc',
		width : 100,
		align : 'left',
		renderer : renderFoo
	}],
	defaultSortable : false
});
var fldfltRecord = Ext.data.Record.create([
	{name : 'field',type : 'string'}, 
	{name : 'mc',type : 'string'}
]);
var fldfltDs = new Ext.data.Store({
	proxy : new Ext.data.DirectProxy({
		directFn : CheckHandler.getFieldsFilter,
		paramOrder:['usage'],
		paramsAsHash : false
	}),
	reader : new Ext.data.JsonReader({
		idProperty : 'field'
	}, fldfltRecord)
});
fldfltDs.on("beforeload",function(){
	fldfltDs.baseParams.usage="header";
});
var fldsfltGrid = new Ext.grid.GridPanel({
	title : '字段列表',
	store : fldfltDs,
	cm : fltcm,
	frame : false,
	height : 240,
	autoScroll: true,
	stripeRows : true,
	loadMask : {msg : '正在加载数据....'},
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	selModel: fltssm,
	stripeRows : true
});
fldsfltGrid.on("rowdblclick",function(grid,rowIndex,e){
	var record = grid.getStore().getAt(rowIndex);
	addFieldsToRight(record);
});
// 选中显示的字段
var sfssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
sfssm.handleMouseDown = Ext.emptyFn;
var showFldsCm = new Ext.grid.ColumnModel({
	columns : [
	sfssm,
	{
		header : "字段",
		dataIndex : 'field',
		width : 80,
		align : 'left',
		renderer : renderFoo
	}, {
		header : "名称",
		dataIndex : 'mc',
		width : 100,
		align : 'left',
		renderer : renderFoo
	}],
	defaultSortable : false
});
var showFldRecord = Ext.data.Record.create([
	{name : 'field',type : 'string'}, 
	{name : 'mc',type : 'string'}
]);
var showFldDs = new Ext.data.Store({
	reader : new Ext.data.JsonReader({
		idProperty : 'field'
	}, showFldRecord)
});
var showFldsGrid = new Ext.grid.GridPanel({
	title : '显示字段',
	store : showFldDs,
	cm : showFldsCm,
	frame : false,
	stripeRows : true,
	height : 240,
	autoScroll: true,
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	selModel: sfssm,
	stripeRows : true
});
showFldsGrid.on("rowdblclick",function(grid,rowIndex,e){
	var record = grid.getStore().getAt(rowIndex);
	addFieldsToLeft(record);
});
var fltPanel = new Ext.Panel({
	region: 'north',
	height : 200,
	layout : 'hbox',
	layoutConfig : {
		align : 'stretch',
		pack : 'start'
	},
	items:[
	{
		flex : 3,
		items:[fldsfltGrid]
	},{
		width : 50,
		xtype : 'panel',
		layout : 'vbox',
		defaultType : 'button',
		defaults : {
			flex : 2,
			style : "padding-top:15px ;",
			xtype : 'button'
		},
		layoutConfig : {
			align : 'center',
			padding : 10
		},
		items : [{
			iconCls : 'icon-up',
			scope : this,
			handler : function() {
				var records = showFldsGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
				var store = showFldsGrid.getStore();
				if (!records||records.length==0) {
					return;
				}
				var record = records[0];
				var index = store.indexOf(record);
				if (index > 0) {
					store.removeAt(index);
					store.insert(index - 1, record);
					showFldsGrid.getSelectionModel().selectRow(index - 1);
				}
			}
		}, {
			iconCls : 'icon-right',
			scope : this,
			handler : function() {
				var records = fldsfltGrid.getSelectionModel().getSelections();
				if (!records||records.length==0) {
					return;
				}
				var record = records[0];
				addFieldsToRight(record);
			}
		}, {
			iconCls : 'icon-left',
			scope : this,
			handler : function() {
				var records = showFldsGrid.getSelectionModel().getSelections();
				if (!records||records.length==0) {
					return;
				}
				var record = records[0];
				addFieldsToLeft(record);
			}
		}, {
			iconCls : 'icon-down',
			scope : this,
			handler : function() {
				var records = showFldsGrid.getSelectionModel().getSelections();
				var store = showFldsGrid.getStore();
				if (!records||records.length==0) {
					return;
				}
				var record = records[0];
				var index = store.indexOf(record);
				if (index < store.getCount() - 1) {
					store.removeAt(index);
					store.insert(index + 1, record);
					showFldsGrid.getView().refresh();
					showFldsGrid.getSelectionModel().selectRow(index + 1);
				}
			}
		}]
	},{
		flex : 3,
		items:[showFldsGrid]
	}]
});
function addFieldsToRight(record){
	var idx = showFldsGrid.getStore().find('field',record.data.field);
	if (idx < 0) {
		showFldsGrid.getStore().loadData(record.data, true);
		fldsfltGrid.getStore().remove(record);
	}
}
function addFieldsToLeft(record){
	fldsfltGrid.getStore().add(record);
	showFldsGrid.getStore().remove(record);
}
// *******************条件组合**************************
var fltCdtRecord = Ext.data.Record.create([
    {name : 'field',type : 'string'}, 
    {name : 'mc',type : 'string'},
    {name: 'mapbm', type: 'string' },
    {name: 'val_src', type: 'int' },
    {name: 'isrtk', type: 'int' }
]);
var fltCdtDs = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
    	directFn : CheckHandler.getFieldsFilter,
    	paramOrder:['usage'],
    	paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'field'
    }, fltCdtRecord)
});
fltCdtDs.on("beforeload",function(){
	fltCdtDs.baseParams.usage="condition";
});
var cb_flds2filter = new Ext.form.ComboBox({
	displayField : 'mc',
	valueField : 'field',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	emptyText : '字段',
	selectOnFocus : true,
	editable : false,
	store : fltCdtDs
});
// 值运算符
var cbOprStore = new Ext.data.SimpleStore({
	fields : ['mc', 'bm'],
	data : [['等于', "equ"], ['大于', 'gt'], ['小于', 'lt'], ['大于等于', 'gt_e'],
			['小于等于', 'lt_e'], ['不等于', 'not_e'], ['匹配', 'like'],
			['包含', 'in']]
});
var cb_oprator = new Ext.form.ComboBox({
	displayField : 'mc',
	valueField : 'bm',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	emptyText : '运算符',
	selectOnFocus : true,
	editable : false,
	store : cbOprStore
});
var cbConnectionStore = new Ext.data.SimpleStore({
	fields : ['mc', 'bm'],
	data : [['空', 'empty'], ['并且', '_and'], ['或者', '_or'], ['并且（', 'andL'],
			['或者（', 'orL'], ['）并且', 'Ror'], ['）并且（', 'RandL'],
			['）或者（', 'RorL'], ['）', 'RBr']]
});

// 条件关系选择的下拉框=================================
var cb_connection = new Ext.form.ComboBox({
	displayField : 'mc',
	valueField : 'bm',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	emptyText : '条件关系',
	selectOnFocus : true,
	editable : false,
	store : cbConnectionStore
});
var fltsm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
fltsm.handleMouseDown = Ext.emptyFn;
var cdtCm = new Ext.grid.ColumnModel({
	columns : [
	fltsm,
	{
		id : 'fld',
		header : "字段",
		dataIndex : 'fld',
		width : 80,
		editor : cb_flds2filter,
		renderer : function(v, p, r) {
			var index = fltCdtDs.find('field', v);
			var cbRec = fltCdtDs.getAt(index);
			var newval= v;
			if (cbRec) {
				newval= cbRec.data.mc;
			} 
			return renderFoo(newval,p,r);
		}
	},{
		id : 'ops',
		header : "运算关系",
		dataIndex : 'ops',
		width : 60,
		renderer : function(v, p, r) {
			var index = cbOprStore.find('bm', v);
			var cbRec = cbOprStore.getAt(index);
			if (cbRec) {
				return cbRec.data.mc;
			}else{
				return v;
			}
		},
		editor : cb_oprator
	},{
		id : 'fldValue',
		header : "值",
		dataIndex : 'fldValue',
		width : 160,
		editor : new Ext.form.TextField({
			selectOnFocus : true,
			maxLength : 200
		}),
		renderer: renderFoo
	}, {
		id : 'treebt',
		header : "",
		dataIndex : 'treebt',
		width : 80,
		editor : new Ext.form.Hidden({}),
		renderer : function(v, p, r) {
			return '<img src="../images/details.gif">'
		}
	}, {
		id : 'connection',
		header : "条件关系",
		dataIndex : 'connection',
		width : 120,
		renderer : function(v, p, r) {
			var index = cbConnectionStore.find('bm', v);
			var cbRec = cbConnectionStore.getAt(index);
			if (cbRec) {
				return cbRec.data.mc;
			} else {
				return v;
			}
		},
		editor : cb_connection
	}, {
		id : 'hValue',
		header : "hValue",
		dataIndex : 'hValue',
		hidden : 'true'
	}],
	defaultSortable : false
});

var cdtRecord = Ext.data.Record.create([
    {name : 'fld',type : 'string'}, 
	{name : 'ops',type : 'string'},
	{name : 'fldValue', type :'string'},
	{name : 'connection',type : 'string'},
	{name : 'treebt',type: "string"},
	{name : 'hValue',type : 'string'}
]);
var cdtStore = new Ext.data.Store({
	reader : new Ext.data.JsonReader({
		idProperty : 'field'
	}, cdtRecord)
});

var cdtGrid = new Ext.grid.EditorGridPanel({
	region: 'center',
	id : 'cdtGrid',
	layout:'fit',
	frame : true,
	clicksToEdit : 1,
	store : cdtStore,
	cm : cdtCm,
	selModel: fltsm,
	tbar: [{
	    text: '添加行',
	    id:'add',
	    iconCls: 'add',
	    handler : function(){
	        var cdt = new cdtRecord({
	        	fld: 'SWDJZH',
	        	ops: 'equ',
	        	fldValue: '',
	        	treebt:"",
	        	connection: 'empty',
	        	hValue :''
	        });
	        cdtGrid.stopEditing();
	        cdtStore.insert(cdtStore.getCount(), cdt);
	        //cdtGrid.startEditing(0, 0);
	    }
	},{
	    text: '删除',
	    id:'remove',
	    iconCls: 'remove',
		handler :function(){
	        var records = cdtGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的行!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					cdtStore.getModifiedRecords().remove(records[rc]);
					cdtStore.remove(records[rc]);
				}
	        }
		}
	}]
});
// cellclick事件==========================================================
//cdtGrid.on('cellclick', function(grid, rowIndex, columnIndex, e) {
//	if (columnIndex == 2) {
//		var cfield = grid.store.getAt(rowIndex).get("field");
//		var idx = fltCdtDs.indexOfId(cfield);
//		var valRenderMod = fltCdtDs.getAt(idx).get("var_src");
//		if (valRenderMod <= 1) {
//			return;
//		}else{
//			//弹出窗体
//			var tb = fltCdtDs.getAt(idx).get("mapbm");
//		}
//	}
//});
var fltTreeSingleWin;
var fltTreeMultiWin;
var fltTreeWin;
cdtGrid.on('beforeedit',function(e){ 
	var editField = e.field;
	var czd = e.record.get("fld");
	var idx = fltCdtDs.indexOfId(czd);
	var valRenderMod = fltCdtDs.getAt(idx).get("val_src");
	var mapBm = fltCdtDs.getAt(idx).get("mapbm");
	var cOperator =  e.record.get("ops");
	if(editField == "fldValue"){
		if(valRenderMod>1)	{//不能直接编辑
			e.cancel = true; 
		}
	}
	if(editField == "treebt"){
		if(valRenderMod<=1)	{
			e.cancel = true; 
		}else{
			//弹出值框
			if(cOperator=='in'){
				if(!fltTreeMultiWin){
					fltTreeMultiWin = new App.widget.CodeTreeWindow({
						directFn:CheckHandler.getBmCodesTree,
						checkModel : 'multiple',
						treeId: 'm_'+mapBm,
						codeTable: mapBm,
						defaultValue: ''
					});
				}
				fltTreeWin = fltTreeMultiWin;
			}else{
				if(!fltTreeSingleWin){
					fltTreeSingleWin = new App.widget.CodeTreeWindow({
						directFn:CheckHandler.getBmCodesTree,
						checkModel : 'single',
						onlyLeafCheckable: true,
						treeId: 's_'+mapBm,
						codeTable: mapBm,
						defaultValue: ''
					});
				}
				fltTreeWin = fltTreeSingleWin;
			}
			var p = {table: mapBm,selectedVals: ''};
			fltTreeWin.onSelect = function(value){
				if(!value)return;
				e.record.set("fldValue",value.text); 
				e.record.set("hValue",value.id);
			};
			fltTreeWin.setTreeParams(p);
			fltTreeWin.refreshTree();
			fltTreeWin.show();
		}
	}
});
// afteredit事件=========================================================
cdtGrid.on("afteredit", function(e) {
	var field = e.field;
	if(field == "fld"){//字段变化，重置值。
		if(e.originalValue!=e.value){
			e.record.set("fldValue","");
			e.record.set("hValue","");
		}
	}
});
// Ext.Window=============================================================
var fltWin = new Ext.Window({
	width : 550,
	height : 420,
	title : "筛选条件",
	closable: false,
	closeAction: 'hide',
	layout : 'border',
	items : [fltPanel,cdtGrid],
	buttons : [
	{
		text: "查询语句",
	    handler: function(){
			queryForEns(0);	    
	    }
	},{
		text : "载入查询",
		handler : function() {
			queryForEns(1);
		}
	}, {
		text : "取消",
		handler : function() {
			cdtGrid.getStore().removeAll();
			conditions = "";
			fltWin.hide();
		}
	}],
	buttonAlign : "center"
});
var sqlForm = new Ext.FormPanel({
	id : 'sqlForm',
	frame: true,
	border : false,
	bodyStyle:'padding:1px',
	items : [{
		xtype: 'textarea',
		name :'sql',
		id: 'idSql',
		width : 345,
		height:190,
		hideLabel: true
	}]
});
var winSql = new Ext.Window({
	width : 350,
	height : 200,
	title : "SQL",
	layout : 'fit',
	items : [sqlForm],
	buttons : [
	{
		text: "关闭",
	    handler: function(){
			winSql.hide();  
	    }
	}]
});
function queryForEns(opType){
	var cdts = new Object();
	var fldNames = new Array();
	var fldValues = new Array();
	var relations = new Array();
	var connections = new Array();
	/*if (cdtGrid.getStore().getCount() == 0) {
		fltWin.hide();
	}else{*/
		var rds = cdtGrid.getStore().getRange();
		for (var i = 0; i < rds.length; i++) {
			var rs = rds[i];
			var fields = rs.data;
			var connection = '';
			var value = fields["fldValue"];
			if (i == rds.length - 1) {
				connection = '';
			}
			if (fields["hValue"] != null && fields["hValue"] != '') {
				value = fields["hValue"].replace(/,/g,"|");
			}
			if (fields["connection"] != null&& fields["connection"] != '') {
				connection = fields["connection"];
			}
			fldNames.push(fields["fld"]);
			fldValues.push(value);
			relations.push(fields["ops"]);
			connections.push(connection);
		}
		var heads = new Array();
		for(var i=0;i<showFldsGrid.getStore().getCount();i++){
			var rd = showFldsGrid.getStore().getAt(i);
			heads.push(rd.get("field"));
		}
		cdts.fldNames=fldNames.join();
		cdts.fldValues=fldValues.join();
		cdts.relations=relations.join();
		cdts.connections=connections.join();
		cdts.tHeads = heads.join();
		CheckHandler.tryGetEns(<%=sEnType%>,opType,Ext.encode(cdts),function(data){
			var obj = Ext.decode(data);
			if(!obj)return;
			if(opType==0){
				//弹出sql显示框
				var sqlcont = obj.sql;
				sqlForm.getForm().findField("sql").setValue(sqlcont);
				winSql.show();
			}else{
				if(obj.canOp){
					//重构grid列，store。
					CheckHandler.rebuildEnGrid(heads.join(),function(data){
						var result = Ext.util.JSON.decode(data);
						if(result&&result.columnModel){//模板重新加载
							var ccm;
							var cstore;
							enDs.removeAll();
							if(result.columnModel){
								var tmpRecord = Ext.data.Record.create(result.store);
								enDs = new Ext.data.Store({
									proxy: new Ext.data.DirectProxy({
										directFn: CheckHandler.getEns,
										paramOrder: ['enType','start','limit','conditions'],
										paramsAsHash: false
									}), 
									reader: new Ext.data.JsonReader({
										idProperty:'xh',
										root: 'rows',
										totalProperty: 'totalCount'
									}, tmpRecord)
								});
								enDs.on("beforeload",function(){
									enDs.baseParams.enType=<%=sEnType%>;
									enDs.baseParams.conditions=conditions;
								});
								var cols = result.columnModel;
								for(var i=0;i<cols.length;i++){
									var col = cols[i];
									if(col.renderer){
										if(col.renderer=="renderFoo"){
											col.renderer = renderFoo;
										}
									}
								}
								ccm = new Ext.grid.ColumnModel({
									columns: [ssm].concat(cols)
								});
								cstore= enDs;
							}
							enGrid.reconfigure(cstore,ccm);
						}
						conditions = Ext.encode(cdts);
						cstore.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
					});
					fltWin.hide();
				}else{
					Ext.Msg.alert("错误","拼装后的SQL语句有错误！详情请查看SQL");
				}
			}
		});
	//}
}
fltWin.on("show",function(){
});
fldfltDs.load();
fltCdtDs.load();
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
	    layout:'border',
        autoScroll:true,
        items:[{
            region:"north",
            height:400,
            layout: 'fit',
            items:[enGrid]
        },{
        	region:"center",
            layout: 'fit',
            items:[dtailTabs]
        }]
	});
	enDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>