<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.utils.entity.*"%>
<%@ page import="com.ifugle.dft.check.entity.*"%>
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
	Map dictionary = cg.getDJFieldsToShow();
	List financeFlds = (List)dictionary.get("DJ_CZ");
	String sEnType = request.getParameter("enType")==null?"0":request.getParameter("enType");
	Report taxRpt = TemplatesLoader.getTemplatesLoader().getReportTemplate("_sys_enTax");
	Report jhRpt = TemplatesLoader.getTemplatesLoader().getReportTemplate("_sys_enJh");
	Report aidRpt = TemplatesLoader.getTemplatesLoader().getReportTemplate("_sys_enAid");
	String strTabs = cg.getString("EnInfoShowTabs","");
	String strTabsName = cg.getString("EnInfoShowTabsTitle","");
	String[] tabs = strTabs.split(",");
	String[] tabsName = strTabsName.split(",");
	Map rptMaps = new HashMap();
	rptMaps.put("jhGrid","_sys_enJh");
	rptMaps.put("taxGrid","_sys_enTax");
	rptMaps.put("aidGrid","_sys_enAid"); 
	
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
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/LockingGridView.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/query.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/CodeTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ParamTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/DynamicGrid.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>
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
Ext.query.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.query.REMOTING_API);
App.ux.defaultPageSize=<%=cg.getString("pageSize","40")%>;
var cEnid = 0;
var cCheckType = 0;
var conditions ="";
var cMetaDataLoaded = false;
var cLoadDefaultMeata = true;
var cLoadUserMeata = false;
var cOperator="";
var cFltRecord = {};
var condition ={};
var cRindex =0;
var titleInHead = false;
var cRptID ="";
var fForm = new Ext.FormPanel({
    id: 'formatForm',
    frame: true,
    labelAlign:'right',
    width: 340,
    height: 250,
    layout: 'absolute', 
    items: [
	    {
	        x: 5,
	        y: 5,
	        xtype:'label',   
            text:'导出格式'   
	    },{
	        x: 5,
	        y: 25,
	        xtype:'radio',   
            boxLabel:'Excel 97-2003(xls)',   
            name:'ftype',   
            id:'formatXls',
            checked: true,
            hideLabel:true
	    },{
	        x: 125,
	        y: 25,
	        xtype:'radio',   
            boxLabel:'Excel 2007(xlsx)',   
            name:'ftype',   
            id:'formatXlsx', 
            hideLabel:true
	    },{
	        x: 5,
	        y: 70,
	        xtype:'label',   
            text:'导出范围'   
	    },{
	        x: 5,
	        y: 90,
	        xtype:'radio',   
            boxLabel:'全部',   
            name:'exportRange',   
            id:'allRds',
            checked: true,
            hideLabel:true
	    },{
	        x: 125,
	        y: 90,
	        xtype:'radio',   
            boxLabel:'部分',   
            name:'exportRange',   
            id:'partRds',
            hideLabel:true,
            listeners:{
    		    check: function(obj,checked){
    				if(checked){
    					fForm.getForm().findField("start").enable();
    					fForm.getForm().findField("end").enable();
    				}else{
    					fForm.getForm().findField("start").disable();
    					fForm.getForm().findField("end").disable();
    				}
    		    }
    	    }
	    },{
	        x: 125,
	        y: 120,
	        xtype:'label',   
            text:'第'
	    },{
	        x: 140,
	        y: 115,
	        xtype:'numberfield',
	        width: 40, 
	        disabled : true,  
            name:'start'
	    },{
	        x: 185,
	        y: 120,
	        xtype:'label', 
            text:'条到'
	    },{
	        x: 215,
	        y: 115,
	        xtype:'numberfield',
	        width: 40, 
	        disabled : true,   
            name:'end'
	    },{
	        x: 260,
	        y: 120,
	        xtype:'label',   
            text:'条'
	    }
	]
}); 
var winFormat = new Ext.Window({
    title: '格式',
    width: 340,
    height: 250,
    layout: 'fit',
    buttonAlign:'center',
    items: fForm,
    buttons: [{
        text: '确定',
        handler:function(){
	    	var rangeMode = 0,expStart=0, expEnd=0,eformat = 0;
		    if(Ext.getCmp('partRds').checked){
		    	expStart = fForm.getForm().findField("start").getValue();
				expEnd = fForm.getForm().findField("end").getValue();
				rangeMode = 1;
		    }else{
		    	rangeMode = 0;
		    }
	        if(Ext.getCmp('formatXls').checked){
	        	eformat = 0;
		    }else{
		    	eformat = 1;
		    }
	        var expUrl='getRptData.query?doType=toExcel&eformat='+eformat+'&rangeMode='+rangeMode+'&expStart='+expStart+'&expEnd='+expEnd;
	        expUrl = expUrl+'&rptID='+cRptID;
	        window.open(expUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
	        winFormat.hide(); 
		}
    },{
        text: '取消',
        handler:function(){
	        winFormat.hide(); 
	    }
    }]
});

var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var cm = new Ext.grid.ColumnModel({
	columns: [
		//ssm,
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
	enDs.baseParams.conditions=conditions;
});
enDs.on("load",function(){
	//清空 已加载单户信息
	taxGrid.getStore().removeAll();
	jhGrid.getStore().removeAll();
	aidGrid.getStore().removeAll();
});
var view = new Ext.grid.GridView();
var pagingBar = new Ext.PagingToolbar({
    pageSize: <%=cg.getString("pageSize","40")%>,
    store: enDs,
    displayInfo: true,
    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
    emptyMsg: "没有数据",
    items: ['-']
});
var enGrid = new Ext.grid.GridPanel({
	title:'单户查询',
	store: enDs,
	height:300,
	cm: cm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : view,
	//selModel: ssm,
	stripeRows: true,
	tbar: [
	{
		text: '筛选',
		iconCls: 'filter',
        handler : function(){
			fltWin.show();
		}
	},new Ext.Toolbar.Separator(),{
		xtype:'radio',   
		boxLabel:'税号',    
	    name:'ftype',   
	    id:'sh',   
	    hideLabel:true,
	    listeners:{check:function(){
    		Ext.getCmp('paras').setValue("");
	    }}
	},{ 
		xtype:'radio',   
	    boxLabel:'名称&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',   
	    name:'ftype',   
	    id:'mc',  
	    checked:true, 
	    hideLabel:true 
	},{
		xtype:'textfield',
		id:'paras',
		width:180,
		enableKeyEvent:true,
		name:'paras',
		hideLabel:true
	},{
        text: '搜索',
        iconCls: 'filter',
        handler : function(){
			queryForEns(1);
        }
	},new Ext.Toolbar.Separator(),{
		text: '导入',
		iconCls: 'impExcel',
        handler : function(){
			excelWin.show();
		}
	}],
	bbar: pagingBar
});
enGrid.on("rowdblclick",function(grid,rIndex,e){
	enWin.show();
	loadTabContent(rIndex);
});
function loadTabContent(rIndex){
	cRindex = rIndex;
	cEnid = enGrid.getStore().getAt(rIndex).get("xh");
	if(cRindex==0){
		Ext.getCmp("gotoPre").disable();
		Ext.getCmp("gotoNext").enable();
	}else if(cRindex==enGrid.getStore().getCount()-1){
		Ext.getCmp("gotoPre").enable();
		Ext.getCmp("gotoNext").disable();
	}else{
		Ext.getCmp("gotoPre").enable();
		Ext.getCmp("gotoNext").enable();
	}
	enWin.setTitle(enGrid.getStore().getAt(rIndex).get("mc"));
	taxGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
	jhGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
	aidGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
	CheckHandler.getEnDjInfo(cEnid,9,function(data){
		var obj = Ext.decode(data);
		if(obj){
			var fInfo = obj.finance;
			if(fInfo){
				var fForm = djForm.getForm();
				<%for(int i=0;i<financeFlds.size();i++){
					En_field fld = (En_field)financeFlds.get(i);%>
					<%if(fld.getVal_src()==2){%>
					fForm.findField('<%=fld.getField()%>_fn').setValue(fInfo.<%=fld.getField()+"_MC"%>==null?"未知":fInfo.<%=fld.getField()+"_MC"%>);
					fForm.findField('<%=fld.getField()%>_fn_bm').setValue(fInfo.<%=fld.getField()%>==null?"":fInfo.<%=fld.getField()%>);
					<%}else{%>
					fForm.findField('<%=fld.getField()%>_fn').setValue(fInfo.<%=fld.getField()%>);
				<%}}%>
			}
		}
	});
}
/*****************************单户企业的分tab详情*******************************/
function buildEnParams(cGrid){
	var dParams = new Object();
	dParams.metaDataLoaded = cGrid.metaDataLoaded;
	var mps = new Object();
	mps["xh"]=cEnid.toString();
	dParams.macroParams = mps;
	return dParams;
}
var taxGrid = new App.ux.DynamicGridPanelAuto({
	columns : [],
	enableColumnMove :true,
	stripeRows: true,
	store : new Ext.data.DirectStore({
		directFn : QueryHandler.queryGeneralDataDynamic,
		paramsAsHash : false,
		remoteSort: <%=taxRpt.getRemoteSort()==1?"true":"false"%>,
		paramOrder: ['rptID','start','limit','condition'],
		fields : []
	})
});
taxGrid.getStore().grid = taxGrid;
taxGrid.getStore().on("beforeload",function(ds,op){
	//组织参数
	ds.baseParams.rptID = "<%=taxRpt.getId()%>";
	var dParams = buildEnParams(taxGrid);
	op.params.condition = Ext.encode(dParams);
});
var jhGrid = new App.ux.DynamicGridPanelAuto({
	columns : [],
	enableColumnMove :true,
	stripeRows: true,
	store : new Ext.data.DirectStore({
		directFn : QueryHandler.queryGeneralDataDynamic,
		paramsAsHash : false,
		remoteSort: <%=jhRpt.getRemoteSort()==1?"true":"false"%>,
		paramOrder: ['rptID','start','limit','condition'],
		fields : []
	})
});
jhGrid.getStore().grid = jhGrid;
jhGrid.getStore().on("beforeload",function(ds,op){
	//组织参数
	ds.baseParams.rptID = "<%=jhRpt.getId()%>";
	var dParams = buildEnParams(jhGrid);
	op.params.condition = Ext.encode(dParams);
});
var aidGrid = new App.ux.DynamicGridPanelAuto({
	columns : [],
	enableColumnMove :true,
	stripeRows: true,
	store : new Ext.data.DirectStore({
		directFn : QueryHandler.queryGeneralDataDynamic,
		paramsAsHash : false,
		remoteSort: <%=aidRpt.getRemoteSort()==1?"true":"false"%>,
		paramOrder: ['rptID','start','limit','condition'],
		fields : []
	})
});
aidGrid.getStore().grid = aidGrid;
aidGrid.getStore().on("beforeload",function(ds,op){
	//组织参数
	ds.baseParams.rptID = "<%=aidRpt.getId()%>";
	var dParams = buildEnParams(aidGrid);
	op.params.condition = Ext.encode(dParams);
});
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
		    //style:'background:none;border:0px;',
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
var djForm  = new Ext.FormPanel({
	layout : 'column',
	id : 'djForm',
	frame: true,
	border : false,
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
var dtailTabs=new Ext.TabPanel({  
	//id:'dtailTabs',
	activeTab:0,  
	frame: true,
	enableTabScroll:true,
	layoutOnTabChange:true,
	items:[
	<%for(int i=0;i<tabs.length;i++){%>
	{
		layout:'fit',
        title: '<%=tabsName[i]%>',
        closable: false,
        listeners: {
            activate: function(panel){
            	var store =  panel.items.get(0).store;
            	if(store ){
                	if(!panel.hasRender){
            			panel.hasRender = true;
            			store.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
                	}
            		cRptID= '<%=(String)rptMaps.get(tabs[i])%>';
            	}
            }
        },
        items: [<%=tabs[i]%>] 
    }<%if(i<tabs.length-1){%>,
    <%}
    }%>]
});  

var enWin = new Ext.Window({
    title : '单户信息',
    width : 600,
    height : 450,
    layout : 'fit',
    items : [dtailTabs],
    closeAction:'hide',
    buttons : [{
    	text : "关闭",
	    handler:function(){
	    	enWin.hide();
    	}
    },{
    	text : "上一个",
    	id: 'gotoPre',
	    handler:function(){
	    	cRindex = cRindex-1;
	    	if(cRindex>=0){
	    		loadTabContent(cRindex);
	    		enGrid.getSelectionModel().selectRow(cRindex);
		    	//cEnid = enGrid.getStore().getAt(cRindex).get("xh");
		    	//enWin.setTitle(enGrid.getStore().getAt(cRindex).get("mc"));
		    	//taxGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	//jhGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	//aidGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	if(cRindex==0){
		    		Ext.getCmp("gotoPre").disable();
		    	}
	    	}
	    	Ext.getCmp("gotoNext").enable();
    	}
    },{
    	text : "下一个",
    	id : 'gotoNext',
	    handler:function(){
	    	cRindex = cRindex+1;
	    	if(cRindex<enGrid.getStore().getCount()){
	    		loadTabContent(cRindex);
	    		
	    		enGrid.getSelectionModel().selectRow(cRindex);
		    	//cEnid = enGrid.getStore().getAt(cRindex).get("xh");
		    	//enWin.setTitle(enGrid.getStore().getAt(cRindex).get("mc"));
		    	//taxGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	//jhGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	//aidGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
		    	if(cRindex==enGrid.getStore().getCount()-1){
		    		Ext.getCmp("gotoNext").disable();
		    	}
	    	}
	    	Ext.getCmp("gotoPre").enable();
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
    labelAlign: 'right',
    fileUpload : true,
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
	}]
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
							enGrid.getStore().load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
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
		paramOrder: ['usage'],
		paramsAsHash: false
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
	height : 190,
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
var sfssm = new Ext.grid.CheckboxSelectionModel({singleSelect: false});
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
	proxy : new Ext.data.DirectProxy({
		directFn : CheckHandler.getFields2Show,
		paramsAsHash : false
	}),
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
	height : 190,
	autoScroll: true,
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	selModel: sfssm,
	stripeRows : true
});
showFldsGrid.on("rowdblclick",function(grid,rowIndex,e){
	var record = grid.getStore().getAt(rowIndex);
	if(record.data.field=="SWDJZH"||record.data.field=="MC"||record.data.field=="DZ"
		||record.data.field=="FDDBR"||record.data.field=="CZFPBM"){
		return;
	}
	addFieldsToLeft(record);
});
var fltPanel = new Ext.Panel({
	region: 'north',
	height : 150,
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
				for(var i=0;i<records.length;i++){
					var record = records[i];
					addFieldsToRight(record);
				}
			}
		}, {
			iconCls : 'icon-left',
			scope : this,
			handler : function() {
				var records = showFldsGrid.getSelectionModel().getSelections();
				if (!records||records.length==0) {
					return;
				}
				for(var i=0;i<records.length;i++){
					var record = records[i];
					if(record.data.field=="SWDJZH"||record.data.field=="MC"||record.data.field=="DZ"
						||record.data.field=="FDDBR"||record.data.field=="CZFPBM"){
						continue;
					}
					addFieldsToLeft(record);
				}
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
		showFldsGrid.getStore().add(record);
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
    	paramOrder: ['usage'],
    	paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'field'
    }, fltCdtRecord)
});
fltCdtDs.on("beforeload",function(){
	fltCdtDs.baseParams.usage="where";
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
	},{
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
var fltTreeSingleWin;
var fltTreeMultiWin;
var fltTreeWin;
commonTriggerClick=function(){
	if(cOperator=='in'){
		if(!fltTreeMultiWin){
			fltTreeMultiWin = new App.widget.CodeTreeWindow({
				directFn:CheckHandler.getBmCodesTree,
				checkModel : 'multiple',
				treeId: 'm_'+cMapBm,
				codeTable: cMapBm,
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
				treeId: 's_'+ cMapBm,
				codeTable: cMapBm,
				defaultValue: ''
			});
		}
		fltTreeWin = fltTreeSingleWin;
	}
	var p = {table: cMapBm,selectedVals: ''};
	fltTreeWin.onSelect = function(value){
		if(!value)return;
		cFltRecord.set("fldValue",value.text); 
		cFltRecord.set("hValue",value.id);
	};
	fltTreeWin.setTreeParams(p);
	fltTreeWin.refreshTree();
	fltTreeWin.show();
};
cdtGrid.on('beforeedit',function(e){ 
	cFltRecord = e.record;
	var editField = e.field;
	var czd = e.record.get("fld");
	var idx = fltCdtDs.indexOfId(czd);
	var valRenderMod = fltCdtDs.getAt(idx).get("val_src");
	cMapBm = fltCdtDs.getAt(idx).get("mapbm");
	cOperator =  e.record.get("ops");
	if(editField == "fldValue"){
		if(valRenderMod >1){
			var commonTrigger = new Ext.form.TriggerField({
				fieldLabel:'',
				editable: false
			});
			edtCb =  commonTrigger ;
			commonTrigger.onTriggerClick = commonTriggerClick;
		}else{
			var commonTextFld = new Ext.form.TextField({selectOnFocus : true,maxLength : 200});
			edtCb = new Ext.grid.GridEditor(commonTextFld);
		}
		cdtCm.setEditor(3,edtCb); 
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
	closable: false,
	closeAction: 'hide',
	title : "筛选条件",
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
	closable: false,
	closeAction: 'hide',
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
	var tbVal = Ext.getCmp('paras').getValue();
	if(tbVal){
		var field ='mc';
		if(Ext.getCmp('sh').checked){
			field='swdjzh';
		}else{
			field='mc';
		}
		fldNames.push(field);
		fldValues.push(tbVal);
		relations.push("like");
		if(rds&&rds.length>0){
			connections.push("_and");
		}else{
			connections.push("empty");
		}
	}
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
		CheckHandler.tryGetEns(9,opType,Ext.encode(cdts),function(data){
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
							enGrid.getStore().removeAll();
							if(result.columnModel){
								var tmpRecord = Ext.data.Record.create(result.store);
								cstore = new Ext.data.Store({
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
								cstore.on("beforeload",function(){
									cstore.baseParams.enType=9;
									cstore.baseParams.conditions=conditions;
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
							}
							enGrid.reconfigure(cstore,ccm);
							pagingBar.bind(cstore);
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
	if(fldfltDs.getCount()<1){
		fldfltDs.load();
		showFldDs.load();
	}
});
fltCdtDs.load();
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[enGrid]
	});
	enGrid.getStore().load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>