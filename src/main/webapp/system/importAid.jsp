<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	String tid = cg.getString("aidTid");
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
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/CodeTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportEditorGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/datapro.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.datapro.REMOTING_API.enableBuffer = 0;
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.ck.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.ck.REMOTING_API);
Ext.Direct.addProvider(Ext.datapro.REMOTING_API);
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
var matchRecord;
var itemSearch;
var cIid = -1;
var tloader=new Ext.tree.TreeLoader({
	directFn: CodeHandler.getAidItems,
    paramsAsHash: false,
    baseAttrs: { uiProvider: Ext.tree.TreeCheckNodeUI } 
});
//根节点
var root = new Ext.tree.AsyncTreeNode({ 
	id:'tree-root',
	text: '项目',
	uiProvider:Ext.tree.TreeCheckNodeUI
});
//树型面板treePanel
var tree = new Ext.tree.TreePanel({   
    id: 'tree',   
    root:root,   
    split: true,    
    checkModel: 'single',  
	onlyLeafCheckable :true,
    animate: false,   
    rootVisible: true, //是否显示根节点  
    autoScroll:true, //自动滚动条
    title:'',
    header:true,
    loader: tloader,
    tbar: [
  	{
        xtype:'label',
        text: '项目名称：'
    },{
		xtype : 'textfield',
 		id: 'searchFor',
		allowBlank: true,
		width: 120
  	},{
		text: '查找',
		iconCls: 'filter',
		handler : function(){
		    cNode = tree.getSelectionModel().getSelectedNode();
		    var selectedNodeID;
		    if(cNode){
		        selectedNodeID = cNode.id;
		    }
		    var searchFor = Ext.getCmp("searchFor").getValue();
		    if(searchFor=='') return;
		    var startNodeID = null;
		    if(itemSearch != '' && itemSearch == searchFor) {
		    	if(selectedNodeID && selectedNodeID != 'tree-root'){
		    	 	startNodeID = selectedNodeID ;
		    	}
		    }
		   	itemSearch = searchFor;
		    //调用服务端查找
		    CodeHandler.searchForAidItem(searchFor,startNodeID,function(data){
		    	if(data&&data!=""){
					var result = Ext.util.JSON.decode(data);
					if(result.match=='no'){ 
						Ext.Msg.alert( "查找结果","没有匹配节点！");
					}else{//展开路径，注意Path是以节点id加上/来间隔的。
						tree.expandPath('tree/tree-root/' + result.path, 'id', onExpandPathComplete);
					}
		        }
		    });
		}
	}]
});
function onExpandPathComplete(bSuccess, oLastNode) {
	if(!bSuccess) return;
	selectNode(oLastNode);
}
function selectNode(node){
	if(node){
		node.ensureVisible();
 		node.select() ;
 		node.ui.toggleCheck(true);
	}
}
//项目树所在弹出窗
var win_project = new Ext.Window({
	title : '项目选择',
	width : 360,
	height : 420,
	autoScroll : true,
	layout : 'fit',
	closeAction:'hide',
	modal:true,
	items : [tree],
	buttons : [{
        text : "确定",
        handler : function() {	
        	var node = tree.getChecked();
    		if(node.length == 0){
    			Ext.Msg.alert("系统提示","请选择资助项目！");
                return ;
	    	}
    		project_tg.setValue(node[0].text); 
			cIid = node[0].id;
			commonForm.getForm().findField("iid").setValue(node[0].id);
			var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
	    	//根据iid和支付依据字段值，检查该已有的支付数，并可以点击“详情按钮”
	    	checkPaidInfo(node[0].id,pfileno);
	        win_project.hide();
    	}
    },{
        text : "取消",
        handler : function() {
    		win_project.hide();
        }
    }]
});
win_project.on("show",function(){
	root.reload();
});
var project_tg = new Ext.form.TriggerField({
	fieldLabel:'资助项目',
	width:120,
	editable: false,
	name:'project_tg'
});
project_tg.onTriggerClick=function(e){
	win_project.show();
}
var mTypeCombo = new Ext.form.ComboBox({ 
    fieldLabel: '匹配模式',
    name: 'd_type',
    value: 'sh',
    width: 120,
    mode : 'local', 
    triggerAction : 'all', 
    forceSelection:true,
    store : new Ext.data.SimpleStore({ 
    	fields : ["mid", "text"], 
    	data : [ 
    	['sh', '税号'], 
    	['mc', '名称']
     	] 
    })  ,
    valueField : "mid", 
    displayField : "text", 
    readOnly: false,
    id: 'd_type'
});
var bmRecord = Ext.data.Record.create([
    {name : 'mc',type : 'string'}, 
    {name : 'bm',type : 'string'}
]);
//资金类型
var zjlxDs = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
        directFn : CheckHandler.getComboBms,
        paramOrder: ['tbname'],
        paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'bm'
    }, bmRecord)
});
zjlxDs.on("beforeload",function(){
	zjlxDs.baseParams.tbname="BM_ZJTYPE";
});
zjlxDs.load({});
var zjlxCombo = new Ext.form.ComboBox({
	name : 'zjtypeCb',
	width : 120,
	hiddenName: 'zjtype',
	fieldLabel: "资金类型",
	displayField : 'mc',
    valueField : 'bm',
    typeAhead : true,
    mode : 'local',
    triggerAction : 'all',
    emptyText : '',
    selectOnFocus : true,
    editable : false,
    store : zjlxDs
});
//部门
var depDs = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
        directFn : CheckHandler.getComboBms,
        paramOrder: ['tbname'],
        paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'bm'
    }, bmRecord)
});
depDs.on("beforeload",function(){
	depDs.baseParams.tbname="BM_DEP";
});
depDs.load({});
var depCombo = new Ext.form.ComboBox({
	name : 'depCb',
	width : 120,
	hiddenName: 'dep',
	fieldLabel: "主管部门",
	displayField : 'mc',
    valueField : 'bm',
    typeAhead : true,
    mode : 'local',
    triggerAction : 'all',
    selectOnFocus : true,
    editable : false,
    store : depDs
}); 
//列支渠道
var lzqd_tg = new Ext.form.TriggerField({
	fieldLabel:'列支渠道',
	width:120,
	editable: false,
	name:'lzqd_tg'
});
lzqd_tg.onTriggerClick=function(e){
	showTreeWin('lzqd','BM_LZQD',"列支渠道");
}
var kmbm_tg = new Ext.form.TriggerField({
	fieldLabel:'科目',
	width:120,
	editable: false,
	name:'kmbm_tg'
});
kmbm_tg.onTriggerClick=function(e){
	showTreeWin('kmbm','BM_ZCKM',"科目");
}
var treeWin;
function showTreeWin(fldName,mapBM,title){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn:CheckHandler.getBmCodesTree,
			onlyLeafCheckable: true,
			codeTable: mapBM,
			defaultValue: '',
			canSetNull: false
		});
	}
	var p = {table: mapBM,selectedVals: ''};
	treeWin.onSelect = function(value){
		if(!value)return;
		if(Ext.isEmpty(value.id)){
			Ext.Msg.alert("提示","请选择一个值！");
			return;
		}
		var tmpText = value.text;
		commonForm.getForm().findField(fldName).setValue(value.id);
		commonForm.getForm().findField(fldName+"_tg").setValue(tmpText);
	};
	treeWin.setTreeParams( p);
	treeWin.refreshTree();
	treeWin.setTitle(title);
	treeWin.show();
}
//form，包括资助项目，已有资助记录的查询按钮，文件路径
var commonForm = new Ext.FormPanel({    
	frame: true,
	labelWidth: 80,
	border: false,
	layout : 'form',
	api: {
	    submit: DataHandler.impData
	},
	fileUpload : true,
	buttonAlign: 'center',
	items:[
	{
		layout:'column',
		frame: true,
		border: false,
		items:[
		{
			columnWidth:.33,
			layout: 'form',
			border: false,
			items:[
			project_tg,
			zjlxCombo,
			depCombo,
			{ 
				fieldLabel: 'Excel文件',
				inputType:'file',
				width:190,
				height:25,
				xtype: 'textfield',
				name: 'filepath'
			},{
				id: 'iid',
				name: 'iid',
				xtype: 'hidden'
			}]
		},{
			columnWidth:.33,
			layout: 'form',
			border: false,
			items:[
			{
				fieldLabel:'支付依据',
				name :'pfileno',
				xtype:'textfield',
				width: 120,
				listeners: {
					"change": function(fld,newVal,oldVal){
						var iid = commonForm.getForm().findField("iid").getValue();
						checkPaidInfo(iid,newVal);
					}
				}
			},
			{
				fieldLabel: '支付日期',
				name :'pdate',
				xtype:'datefield',
				width: 120,
				format : 'Y-m-d'
			},
			kmbm_tg,
			{
				id: 'kmbm',
				name: 'kmbm',
				xtype: 'hidden'
			},{ 
			    fieldLabel:'起始行',
				name:'beginRow',
				xtype:'numberfield',
				width:120,
				value: 3,
				allowDecimals:false
			}]
		},{
			columnWidth:.33,
			layout: 'form',
			border: false,
			items:[
				lzqd_tg,
				{
					id: 'lzqd',
					name: 'lzqd',
					xtype: 'hidden'
				},
				new Ext.create({
					xtype: 'compositefield',
					fieldLabel :'已导入',
					items: [{
						name:'aidCount',
						xtype:'numberfield',
						readOnly: true,
						style:'background:none;border:0px;',
						width: 120
					},{
						xtype: 'button',
						id: 'aidDetail',
						disabled: true,
						text:'详情',
						handler: function() {
							aidWin.show();
						}
					}]
				}),
				{ 
					fieldLabel:'合计',
					name:'hj',
					xtype:'textfield', 
					width:120,
					value: 0,
					style:'background:none;border:0px;',
					value:"0.00",
					readOnly: true
				},mTypeCombo
			]
		}]
	}],
	buttons:[{
		name: 'import',
		id: 'import',
		text: '导入',
		handler : function() {
		    var x=document.getElementById('filepath').value;
		    if(!x||x==''){
		      	Ext.Msg.alert("提示","请选择要导入的文件!");
		      	return;
		    }
		    if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLS'&&x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLSX'){
		      	Ext.Msg.alert("提示","请选择Excel文件导入！");
		      	return;
			}
		    var iid =  commonForm.getForm().findField("iid").getValue();
			if(!iid || iid == ''){
				Ext.Msg.alert('系统提示','项目不能为空！');
		        return;
			}
			var zjtype = commonForm.getForm().findField("zjtype").getValue();
			if(!zjtype || zjtype == ''){
				Ext.Msg.alert('系统提示','资金类型不能为空！');
		        return;
			}
			var dep = commonForm.getForm().findField("dep").getValue();
			if(!dep || dep == ''){
				Ext.Msg.alert('系统提示','主管部门不能为空！');
		        return;
			}
			var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
			if(!pfileno || pfileno == ''){
				Ext.Msg.alert('系统提示','支付依据不能为空！');
		        return;
			}
			//var pdate = commonForm.getForm().findField("pdate").getValue();
			var pdate =commonForm.getForm().findField("pdate").getRawValue();
			if(!pdate || pdate == ''){
				Ext.Msg.alert('系统提示','支付日期不能为空！');
		        return;
			}
			
			var lzqd = commonForm.getForm().findField("lzqd").getValue();
			if(!lzqd || lzqd == ''){
				Ext.Msg.alert('系统提示','列支渠道不能为空！');
		        return;
			}
		    Ext.Msg.wait("正在导入...");
		    var matchType =Ext.getCmp('d_type').getValue();
		    commonForm.getForm().submit({
			    timeout: 10*60*1000,
				params:{tid: <%=tid%>},
				method:'POST',  
			    success: function(form, action) {
				    Ext.Msg.hide();
				    var obj = action.result;
				    Ext.Msg.hide();
				    if(obj&&obj.infos){
				       	Ext.Msg.alert('提示',obj.infos.msg);
					}else if(obj&&obj.errors){
						Ext.Msg.alert('提示',obj.errors.msg);
					}
				  	//加载数据
				  	aid_grid_ds.baseParams.iid= Number(cIid);
				  	var pfileno = commonForm.getForm().findField("pfileno").getValue();
				  	aid_grid_ds.baseParams.pfileno = pfileno;
   					aid_grid_ds.baseParams.matchType =matchType;
   					aid_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
			    },
				failure: function(form,action){
				    Ext.Msg.hide();
					var obj = action.result;
					if(obj&&obj.errors){
						Ext.Msg.hide();
						Ext.Msg.alert('提示',obj.errors.msg);
					}
				},
				exceptionHandler : function(msg){
					Ext.Msg.hide();
					Ext.Msg.alert('提示',msg);
					return ; 
				}
		    });
		}
	}]
});
function checkPaidInfo(iid,pfileno){
	DataHandler.checkDoneAid(Number(iid),pfileno,function(data){
		var obj = Ext.decode(data);
		if(obj.result){
			commonForm.getForm().findField('aidCount').setValue(obj.aidCount);
			var hj = regMoney(obj.hj);
			commonForm.getForm().findField('hj').setValue(hj);
	        if(obj.aidCount>0){
	        	Ext.getCmp("aidDetail").enable();
	        }else{
	        	Ext.getCmp("aidDetail").disable();
	        }
		}
	});
}
//已资助记录grid及弹出窗体，grid上提供对现有的、关于该项目、支付依据的支付记录的“删除”、“清空”按钮
var doneAidSsm = new Ext.grid.CheckboxSelectionModel(); 
doneAidSsm.handleMouseDown = Ext.emptyFn;
var doneAid_cm = new Ext.grid.ColumnModel({
	columns: [
		doneAidSsm,
	    {
	       header: "导入时间",
	       dataIndex: 'inputtime',
	       width: 130,
	       align:'left'
	    },
	    {
	       header: "操作用户",
	       dataIndex: 'userid',
	       width: 70,
	       align: 'left'
	    },{
		    header: "资助总额",
		    dataIndex: 'hj',
		    width: 80,
		    align: 'right',
		    renderer: regMoney
		},{
		    header: "企业数",
		    dataIndex: 'encount',
		    width: 60,
		    align: 'right'
		},{
		    header: "下达日期",
		    dataIndex: 'pdate',
		    width: 80,
		    align: 'left'
		},{
		    header: "资金类型",
		    dataIndex: 'zjtype',
		    width: 60,
		    align: 'left'
		},{
		    header: "主管部门",
		    dataIndex: 'dep',
		    width: 80,
		    align: 'left'
		},{
		    header: "科目",
		    dataIndex: 'kmmc',
		    width: 100,
		    align: 'left'
		}
	],
	defaultSortable: false
});
var doneAid_Record = Ext.data.Record.create([   
	{name: 'aid', type: 'int'},
    {name: 'zjtype', type: 'string'},
    {name: 'dep', type: 'string'},
    {name: 'kmmc', type: 'string'},
    {name: 'pdate',type:'string'},
    {name: 'hj',type:'float'},
    {name: 'encount',type:'int'},
    {name: 'userid',type:'string'},
    {name: 'inputtime',type:'string'}
]);
var doneAid_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getFormalAidData,
		paramOrder: ['iid','pfileno','start','limit'],
		paramsAsHash: false
	}),  
	reader : new Ext.data.JsonReader({
		idProperty: 'aid',
		root: 'rows',
		totalProperty: 'totalCount'},doneAid_Record)
});
var doneAidGrid = new App.ux.ExportGridPanel({
	cm : doneAid_cm,
	title: '',
	enableColumnMove :true,
	stripeRows: true,
	selModel: doneAidSsm,
	store : doneAid_ds,
	plugins :[new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :60000
	})],
	tbar: [{
		text:"删除",
		iconCls:"remove",
		handler: function(){
			var records = doneAidGrid.getSelectionModel().getSelections();
	    	if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的记录!");
				return;
			}		
			if(records){
				Ext.MessageBox.confirm('确认删除', '确定删除？', function(btn){
	    	    	if(btn == 'yes') {// 选中了是按钮
	    	    		var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
		    	    	var aids = new Array();
	    	    		for(var rc=0;rc<records.length;rc++){						    	    	
	    	    			aids.push(records[rc].get("aid"));
						}
	    	    		DataHandler.deleteFormalAids(0,Number(cIid),pfileno,aids.join(),function(cdata){
	    	    			var cobj = Ext.decode(cdata);
							if(cobj.result){
						    	doneAid_ds.reload();
						    	var iid =commonForm.getForm().findField("iid").getValue();
								var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
						    	checkPaidInfo(iid,pfileno);
						    	Ext.Msg.alert('信息','数据已删除！');
	    					}else{
	    						Ext.Msg.alert('信息','删除数据失败！');
	    	                }
	    				});	
					}
				});
			}
		}
	},{
		text:"清空",
		iconCls:"remove",
		handler: function(){
			Ext.MessageBox.confirm('确认删除', '本操作会将关于当前依据的资助记录全部删除，确定删除？', function(btn){
		    	if(btn == 'yes') {
		    		var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
		    		DataHandler.deleteFormalAids(1,Number(cIid),pfileno,'',function(data){
		    			var obj = Ext.decode(data);
						if(obj.result){
							doneAid_ds.reload();
							var iid =commonForm.getForm().findField("iid").getValue();
					    	checkPaidInfo(iid,pfileno);
					    	Ext.Msg.alert('信息','数据已清空！');
    					}else{
    						Ext.Msg.alert('信息','清空数据失败！');
    	                }
		    		});
		    	}
			});
		}
	}],
	bbar: new Ext.PagingToolbar({
	    pageSize: <%=cg.getString("pageSize", "40")%>,
	    store: doneAid_ds,
	    displayInfo: true,
	    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	    emptyMsg: "没有数据",
	    items: ['-']
	})
});
var aidWin = new Ext.Window({
    title: '已有资助(当前项目已入库的资助信息)',
    width: 500,
    height: 400,
    layout: 'fit',
    buttonAlign:'center',
    closeAction:'hide',
    items: [doneAidGrid],
    buttons: [{
        text: '关闭',
        handler:function(){
    		aidWin.hide(); 
	    }
    }]
});
aidWin.on("show",function(){
	doneAid_ds.baseParams.iid=Number(cIid);
	var pfileno = commonForm.getForm().findField("pfileno").getValue();
	doneAid_ds.baseParams.pfileno = pfileno;
	doneAid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
});
//下部的grid
var ssm = new Ext.grid.CheckboxSelectionModel(); 
ssm.handleMouseDown = Ext.emptyFn;
var aid_cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
		    id:'adjust',
		    header:'手工匹配',
		    width: 80,
		    align:'left',
		    dataIndex:'nomatch',
		    renderer: renderMatch
		},{
	       header: "税号",
	       dataIndex: 'swdjzh',
	       width: 120,
	       align:'left'
	    },
	    {
	       header: "企业名称",
	       dataIndex: 'qymc',
	       width: 200,
	       align: 'left'
	    },{
		    header: "资助额",
		    dataIndex: 'je',
		    width: 100,
		    align: 'right'
		}
	],
	defaultSortable: false
});
var aid_Record = Ext.data.Record.create([   
    {name: 'xh', type: 'int'},
    {name: 'nomatch', type: 'int'},
    {name: 'swdjzh', type: 'string'},
    {name: 'qymc', type: 'string'},
    {name: 'je', type: 'float'}
]);
var aid_grid_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getImportedAidData,
		paramOrder: ['iid','matchType','pfileno','start','limit'],
		paramsAsHash: false
	}),  
	reader : new Ext.data.JsonReader({
		idProperty: 'xh',
		root: 'rows',
		totalProperty: 'totalCount'}, aid_Record)
});

var aid_grid = new Ext.grid.EditorGridPanel({
	cm : aid_cm,
	title: '资助信息(此处为临时信息，请调整后执行“保存”操作以形成正式数据)',
	enableColumnMove :true,
	stripeRows: true,
	selModel: ssm,
	clicksToEdit:1,
	store : aid_grid_ds,
	plugins :[new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :60000
	})],
	tbar: [{
		text:"删除",
		iconCls:"remove",
		id:"delete",
		handler: function(){
			var records = aid_grid.getSelectionModel().getSelections();
	    	if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的记录!");
				return;
			}		
			if(records){
				Ext.MessageBox.confirm('确认删除', '确定删除？', function(btn){
	    	    	if(btn == 'yes') {// 选中了是按钮
		    	    	var aids = new Array();
	    	    		for(var rc=0;rc<records.length;rc++){						    	    	
	    	    			aids.push(records[rc].get("xh"));
						}
	    	    		var pfileno = commonForm.getForm().findField("pfileno").getValue();
	    	    		DataHandler.deleteImportedAids(Number(cIid),pfileno,aids.join(),function(data){
	    	    			var obj = Ext.decode(data);
							if(obj.result){
	    						for(var rc=0;rc<records.length;rc++){						    	    	
						    	    aid_grid_ds.remove(records[rc]);
								}		
	    					}else{
	    						Ext.Msg.alert('信息','删除数据失败！');
	    	                }
	    				});	
					}
				});
			}
		}
	}/*,{
		text : "暂存",
		iconCls: 'save',
 		id:'save',
		handler:function(){	
		    save(0);
	    }
	}*/,{
		text : "保存为资助",
		iconCls: 'save',
 		id:'genBatch',
		handler:function(){	
		    save(1);
	    }
	},{
		text : '导出',
		iconCls : 'expExcel',
		handler : function(btn, e) {
			winFormat.show();
		}
	}],
	bbar: new Ext.PagingToolbar({
	    pageSize: <%=cg.getString("pageSize", "40")%>,
	    store: aid_grid_ds,
	    displayInfo: true,
	    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	    emptyMsg: "没有数据",
	    items: ['-']
	})
});
function renderMatch(v,p,r){
	var xh = r.get("xh");
	if(v==1||v=='1'){//未匹配
	    return "<input type='button' value='手工匹配' onclick='matchFn("+xh+")' style='height:20px;font-size:12px' >" ;    
	}else{
	    return '';
	}
}
function matchFn(index){
	matchRecord=aid_grid_ds.getById(index);
	var iid =  commonForm.getForm().findField("iid").getValue();
	if(!iid || iid == ''){
		Ext.Msg.alert('系统提示','项目不能为空！');
        return;
	}
	enWin.show();
}
function save(acType){
	var iid =  commonForm.getForm().findField("iid").getValue();
	if(!iid || iid == ''){
		Ext.Msg.alert('系统提示','项目不能为空！');
        return;
	}
	var zjtype = commonForm.getForm().findField("zjtype").getValue();
	if(!zjtype || zjtype == ''){
		Ext.Msg.alert('系统提示','资金类型不能为空！');
        return;
	}
	var dep = commonForm.getForm().findField("dep").getValue();
	if(!dep || dep == ''){
		Ext.Msg.alert('系统提示','主管部门不能为空！');
        return;
	}
	var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
	if(!pfileno || pfileno == ''){
		Ext.Msg.alert('系统提示','支付依据不能为空！');
        return;
	}
	var pdate = commonForm.getForm().findField("pdate").getRawValue();
	if(!pdate || pdate == ''){
		Ext.Msg.alert('系统提示','支付日期不能为空！');
        return;
	}
	var kmbm = commonForm.getForm().findField("kmbm").getValue();
	if(!kmbm || kmbm == ''){
		Ext.Msg.alert('系统提示','科目不能为空！');
        return;
	}
	var lzqd = commonForm.getForm().findField("lzqd").getValue();
	if(!lzqd || lzqd == ''){
		Ext.Msg.alert('系统提示','列支渠道不能为空！');
        return;
	}
	Ext.Msg.wait("正在保存...");
	var allRows=new Array();
	for(var i=0;i<aid_grid.getStore().getCount();i++){
		var rs = aid_grid.getStore().getAt(i);
	    var fields=rs.data;
   	    if(!fields["swdjzh"]||fields["swdjzh"]==''){
   	    	Ext.Msg.hide();
	   	    Ext.Msg.alert("系统提示","第"+(i+1)+"行的税号不能为空，删除该记录或手工匹配！");
			return;
	   	}
   	 	if(!fields["qymc"]||fields["qymc"]==''){
   	 		Ext.Msg.hide();
	   	    Ext.Msg.alert("系统提示","第"+(i+1)+"行的企业名称不能为空，删除该记录或手工匹配！");
			return;
	   	}
	    allRows.push(fields["xh"]);
	}
	var aidInfo = new Object();
	aidInfo.iid = iid;
	aidInfo.zjtype = zjtype;
	aidInfo.dep =dep;
	aidInfo.pfileno=pfileno;
	aidInfo.pdate=pdate;
	aidInfo.kmbm=kmbm;
	aidInfo.lzqd=lzqd;
	var saveRows =new Array();
	var rcount = aid_grid.getStore().getCount();
	for(var i=0;i<rcount;i++){
		var row=new Object();
		var rs = aid_grid.getStore().getAt(i);
	    var fields=rs.data;
	    row.xh=fields["xh"];
	    row.swdjzh=fields["swdjzh"];
	    saveRows.push(row);
	}
	DataHandler.saveTempAids(Ext.encode(aidInfo),Ext.encode(saveRows),function(data){   
    	var obj = Ext.util.JSON.decode(data);
    	if(obj.result){
			if(acType==1) {
				generate(Ext.encode(aidInfo),allRows.join());
			}else{
				Ext.Msg.hide();
				Ext.Msg.alert('信息','修改内容保存成功！');
				aid_grid.getStore().reload();
			}
	 	}else{
	 	    Ext.Msg.alert('信息','修改内容保存失败！');
	 	}
	});	      
}
function generate(aidInfo,xhs){
	DataHandler.saveFormalAids(aidInfo,xhs,function(fdata){   
	    var obj = Ext.util.JSON.decode(fdata);
	    if(obj.result){
	    	Ext.Msg.hide();
	    	
	    	Ext.Msg.alert('信息',"保存资助操作成功！"+obj.info);
    	 	/*commonForm.getForm().findField("project_tg").setValue(""); 
    	 	commonForm.getForm().findField("iid").setValue(""); 
    	 	commonForm.getForm().findField("zjtype").setValue("");
    	 	commonForm.getForm().findField("dep").setValue("");
    	 	commonForm.getForm().findField("aidCount").setValue(""); 
    	 	commonForm.getForm().findField("pfileno").setValue(""); 
    	 	commonForm.getForm().findField("pdate").setValue(""); 
    	 	commonForm.getForm().findField("kmbm").setValue(""); 
    	 	commonForm.getForm().findField("kmbm_tg").setValue(""); 
    	 	commonForm.getForm().findField("lzqd_tg").setValue(""); 
    	 	commonForm.getForm().findField("lzqd").setValue("");
    	 	commonForm.getForm().findField("hj").setValue("0.00"); 
    	 	Ext.getCmp("aidDetail").disable();*/
    	 	//var obj = document.getElementById("filepath");        
            //obj.outerHTML = obj.outerHTML;
    	 	aid_grid.getStore().reload();
    	 }else{
    	 	Ext.Msg.hide();
    	 	Ext.Msg.alert('信息',"保存操作失败！"+obj.info);
    	 }
    });	      
}
var fForm = new Ext.FormPanel({
    id: 'formatForm',
    frame: true,
    labelAlign: 'right',
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
        	var opt = {};
	        if(Ext.getCmp('formatXls').checked){
	        	opt = {format : "excel",maxExportRows:65530};
		    }else{
		    	opt = {format : "excel2007",maxExportRows:<%=cg.getString("maxExportRow","100000")%>};
		    }
		    if(Ext.getCmp('partRds').checked){
		    	var expStart = fForm.getForm().findField("start").getValue();
				var expEnd = fForm.getForm().findField("end").getValue();
				opt.rangeMode = 1;
				opt.expStart = expStart;
				opt.expEnd	= expEnd;
		    }else{
		    	opt.rangeMode = 0;
		    }
			opt.title =aid_grid.title;
			aid_grid.exportExcel(opt);
	        winFormat.hide(); 
		}
    },{
        text: '取消',
        handler:function(){
	        winFormat.hide(); 
	    }
    }]
});
//手工匹配使用的弹出窗体，现有企业列表，提供模糊查找，选中后，写入下部grid,并改写相同企业和项目的已有资助数
var ckm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ckm.handleMouseDown = Ext.emptyFn;
var en_cm = new Ext.grid.ColumnModel({
	columns: [
	    ckm,
	{
		header: "企业税号",
		dataIndex: 'swdjzh',
		width: 120,
		align:'left'      
	},{
		header: "企业名称",
		dataIndex: 'mc',
		width: 150,
		align: 'left'    
	},{
		header: "法人代表",
		dataIndex: 'fddbr',
		width: 90,
		align: 'left'    
	}],
	defaultSortable: false
});
var en_Record = Ext.data.Record.create([  
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'fddbr', type: 'string'},
	{name: 'dz', type: 'string'}
]);
var en_grid_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getEns,
		paramOrder: ['start','limit','pField','pValue'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'swdjzh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, en_Record)
});
var en_grid = new Ext.grid.GridPanel({
	title:'',
	store: en_grid_ds,
	cm: en_cm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	selModel: ckm,
	bbar: new Ext.PagingToolbar({
		pageSize: <%=cg.getString("pageSize","40")%>,
		store: en_grid_ds,
		displayInfo: true,
		displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
		emptyMsg: "没有数据",
		items: ['-']
	})
});
var enPanel = new Ext.Panel({
	frame:false,
	layout:'fit',
	autoScroll:true, //自动滚动条
	items:[en_grid],
	tbar: [{
		xtype:'radio',   
		boxLabel:'按税号',   
		name:'ftype',   
		id:'sh',   
		hideLabel:true,
		listeners:{check:function(){
			Ext.getCmp('paras').setValue("");
		}}
	},{ 
		xtype:'radio',   
		boxLabel:'按名称',   
		name:'ftype',   
		id:'mc',  
		checked:true, 
		hideLabel:true 
	},{
		xtype:'textfield',
		id:'paras',
		width:150,
		enableKeyEvent:true,
		name:'paras',
		hideLabel:true
		,listeners:{   
			specialkey:function(field,e){   
				if (e.getKey()==Ext.EventObject.ENTER){  
					var field ='mc';
					if(Ext.getCmp('sh').checked){
						field='swdjzh';
					}else{
						field='mc';
					}
					var value = Ext.getCmp('paras').getValue();
					value=(value==null)?"":value.trim();
					en_grid_ds.baseParams.pField=field;
					en_grid_ds.baseParams.pValue=value;
					en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
				}   
			}   
		}   
	},new Ext.Toolbar.Separator(),
	{
		text: '搜索',
		iconCls: 'filter',
		handler : function(){
			var field ='mc';
			if(Ext.getCmp('sh').checked){
				field='swdjzh';
			}else{
				field='mc';
			}
			var value = Ext.getCmp('paras').getValue();
			value=(value==null)?"":value.trim();
			en_grid_ds.baseParams.pField=field;
			en_grid_ds.baseParams.pValue=value;
			en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	}]
});
var enWin = new Ext.Window({
	title : '企业',
	width : 700,
	height : 450,
	layout : 'fit',
	items : [enPanel],
	closeAction:'hide',
	buttonAlign:"center",
	buttons : [{
		text : "确定",
		handler : function(){
			var rcs = en_grid.getSelectionModel().getSelections();
			if(!rcs||rcs<1){
				Ext.Msg.alert("提示","请先选择企业!");
				return;
			}
			//及时更新后台
			var pfileno = commonForm.getForm().findField("pfileno").getValue(); 
			DataHandler.matchEnOfTmpAid(Number(cIid),pfileno,matchRecord.get("xh"),rcs[0].get("swdjzh"),function(data){
		    	var obj = Ext.util.JSON.decode(data);
		    	if(obj&&obj.result){
		    		enWin.hide();
		    		matchRecord.set("swdjzh",rcs[0].get("swdjzh"));
		    		matchRecord.set("qymc",rcs[0].get("mc"));
			 	}else{
			 	    Ext.Msg.alert('信息','将手工匹配结果更新到数据库时发生错误!');
			 	}
			});
		}
	},{
		text : "关闭",
		handler:function(){
			enWin.hide();
		}
	}]
});
enWin.on("show",function(){
	en_grid_ds.baseParams.pField="";
	en_grid_ds.baseParams.pValue="";
	en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
});

Ext.onReady(function(){
	Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	Ext.QuickTips.init();
	var viewport = new Ext.Viewport({
        layout:'border',
        border: false,
        items:[
		{	
	        region:'north', 
	        layout:'fit',
	        height:170,
	        border: false,
			items: commonForm
	   	},{  
			region:'center',  
			layout:'fit',  
			split:true,  
			autoScroll:true, //自动滚动条
			items: aid_grid  
		}
	    ]
	});
	/*DataHandler.getAppTemplate(node[0].id,function(data){
		var result = Ext.decode(data);
		if(result&&result.columnModel){//模板重新加载
			var astore,doneAidstore;
			var accm,doneAidccm;
			aid_grid_ds.reader.recordType = Ext.data.Record.create(result.store);
			doneAid_ds.reader.recordType = Ext.data.Record.create(result.store);
			var cols = result.columnModel;
			accm = new Ext.grid.ColumnModel({
				columns: [ssm,{
				    id:'adjust',
				    header:'手工匹配',
				    width: 70,
				    align:'left',
				    dataIndex:'NOMATCH',
				    renderer: renderMatch
				},{
				    header:'已资助',
				    width: 70,
				    align:'left',
				    dataIndex:'ENAPPCOUNT',
				    renderer: function(v,r,p){
				    	if(v!=""&&v>0){
				    		p.title = "当前项目、当前企业已有"+v+"条资助记录。";
				    	}
				    	return v;
					}
				}].concat(cols)
			});
			doneAidccm = new Ext.grid.ColumnModel({
				columns: [doneAidSsm].concat(cols)
			});
			astore= aid_grid_ds;
			doneAidstore= doneAid_ds;
			aid_grid.reconfigure(astore,accm);
			doneAidGrid.reconfigure(doneAidstore,doneAidccm);
		}
	});*/	
});
</script>
</head>
<body>
</body>
</html>