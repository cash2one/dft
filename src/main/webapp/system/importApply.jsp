<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	String tid = cg.getString("applyTid");
	Calendar cal = Calendar.getInstance();
	int year = cal.get(Calendar.YEAR);
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
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportEditorGridPanel.js"></script>
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
Ext.Direct.addProvider(Ext.datapro.REMOTING_API);
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
var firstSave = true;
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
	uiProvider:Ext.tree.TreeCheckNodeUI,
	text: '项目'
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
    			Ext.Msg.alert("系统提示","请选择申报项目！");
                return ;
	    	}
    		project_tg.setValue(node[0].text); 
			cIid = node[0].id;
			commonForm.getForm().findField("iid").setValue(node[0].id); 
	    	//填写iid隐藏字段值，检查该项目已有的申报数，并可以点击“详情按钮”
    		DataHandler.checkApplyOfIid(Number(node[0].id),function(data){
    			var obj = Ext.decode(data);
				if(obj.result){
					commonForm.getForm().findField('appCount').setValue(obj.appCount);
			        if(obj.appCount>0){
			        	Ext.getCmp("appliedDetail").enable();
			        }else{
			        	Ext.getCmp("appliedDetail").disable();
			        }
				}
        	});
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
	fieldLabel:'申报项目',
	width:120,
	editable: false,
	//id:'project_tg',
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
    editable: false,
    id: 'd_type'
});
//form，包括申报项目，已有申报记录的查询按钮，文件路径
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
			columnWidth:.5,
			layout: 'form',
			border: false,
			items:[
				project_tg,
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
				},new Ext.create({
					xtype: 'compositefield',
					fieldLabel :'已申请数',
					items: [{
						name:'appCount',
						xtype:'numberfield',
						readOnly: true,
						style:'background:none;border:0px;',
						width: 120
					},{
						xtype: 'button',
						id: 'appliedDetail',
						disabled: true,
						text:'详情',
						handler: function() {
							appWin.show();
						}
					}]
				})
			]
		},{
			columnWidth:.5,
			layout: 'form',
			border: false,
			items:[{ 
			    fieldLabel:'起始行',
				name:'beginRow',
				xtype:'numberfield',
				width:120,
				value: 3,
				allowDecimals:false,
				allowBlank:false
			}, mTypeCombo
			,{
				fieldLabel:'申请年度',
				name:'appYear',
				xtype:'numberfield',
				width:120,
				value: <%=year%>,
				allowDecimals:false,
				allowBlank:false
			}]
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
			var cf = commonForm.getForm();
			if(cf.findField("iid").getValue()==null||cf.findField("iid").getValue()==""){
				Ext.Msg.alert("提示","请输入申报项目！");
		      	return;
			}
			var appYear = cf.findField("appYear").getValue();
			if(appYear==null||appYear==""){
				Ext.Msg.alert("提示","请输入申报年度！");
		      	return;
			}
			
		    Ext.Msg.wait("正在导入...");
		    var matchType =Ext.getCmp('d_type').getValue();
		    commonForm.getForm().submit({
			    timeout: 10*60*1000,
				params:{tid: <%=tid%>},
			    success: function(form, action) {
				    Ext.Msg.hide();
				    firstSave = false;
				    var obj = action.result;
				    Ext.Msg.hide();
				    if(obj&&obj.infos){
				       	Ext.Msg.alert('提示',obj.infos.msg);
					}else if(obj&&obj.errors){
						Ext.Msg.alert('提示',obj.errors.msg);
					}
				  	//加载数据
				  	app_grid_ds.baseParams.iid= Number(cIid);
   					app_grid_ds.baseParams.matchType =matchType;
   					app_grid_ds.baseParams.year =appYear;
   					app_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
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
//已申报记录grid及弹出窗体，动态列模型，grid上提供对现有的、关于该项目的申报记录的“删除”、“清空”按钮
var appliedSsm = new Ext.grid.CheckboxSelectionModel(); 
appliedSsm.handleMouseDown = Ext.emptyFn;
var applied_cm = new Ext.grid.ColumnModel({
	columns: [
		appliedSsm,
	    {
	       id:'swdjzh',
	       header: "税号",
	       dataIndex: 'swdjzh',
	       width: 120,
	       align:'left'
	    },
	    {
	       id:'qymc',
	       header: "企业名称",
	       dataIndex: 'qymc',
	       width: 200,
	       align: 'left'
	    },{
	    	id:'czfp',
		    header: "财政分片",
		    dataIndex: 'czfp',
		    width: 80,
		    align: 'left'
		},{
	        header: "项目申请内容",
	        id:'itemcont',
	        dataIndex: 'itemcont',
	        width: 150,
	        align: 'left'
	    },{
		    header: "申报时间",
		    id:'approvaldate',
		    dataIndex: 'approvaldate',
		    width: 90,
		    align: 'left'
		},{
		    header: "计划资助额",
		    id:'money',
		    dataIndex: 'money',
		    width: 100,
		    align: 'right',
		    renderer: regWanMoney
		},{
		    header: "省市资金",
		    dataIndex: 'sszj',
		    width: 100,
		    align: 'right',
		    renderer: regWanMoney
		},{
		    header: "区配套",
		    dataIndex: 'qptzj',
		    width: 100,
		    align: 'right',
		    renderer: regWanMoney
		},{
		    header: "备注",
		    id:'remark',
		    dataIndex: 'remark',
		    width: 150,
		    align: 'left'
		}
	],
	defaultSortable: false
});
var applied_Record = Ext.data.Record.create([   
	{name: 'id', type: 'int'},
    {name: 'swdjzh', type: 'string'},
    {name: 'qymc', type: 'string'},
    {name: 'czfp', type: 'string'},
    {name: 'itemcont',type:'string'},
    {name: 'approvaldate',type:'string'},
    {name: 'money',type:'float'},
    {name: 'sszj',type:'float'},
    {name: 'qptzj',type:'float'},
    {name: 'remark',type:'string'}
]);
var applied_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getFormalAppData,
		paramOrder: ['iid','start','limit'],
		paramsAsHash: false
	}),  
	reader : new Ext.data.JsonReader({
		idProperty: 'id',
		root: 'rows',
		totalProperty: 'totalCount'},applied_Record)
});
var appliedGrid = new App.ux.ExportGridPanel({
	cm : applied_cm,
	title: '',
	enableColumnMove :true,
	stripeRows: true,
	selModel: appliedSsm,
	store : applied_ds,
	plugins :[new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :60000
	})],
	tbar: [{
		text:"删除",
		iconCls:"remove",
		handler: function(){
			var records = appliedGrid.getSelectionModel().getSelections();
	    	if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的记录!");
				return;
			}		
			if(records){
				Ext.MessageBox.confirm('确认删除', '确定删除？', function(btn){
	    	    	if(btn == 'yes') {// 选中了是按钮
		    	    	var apps = new Array();
	    	    		for(var rc=0;rc<records.length;rc++){						    	    	
	    	    			apps.push(records[rc].get("id"));
						}
	    	    		DataHandler.deleteFormalApps(0,Number(cIid),apps.join(),function(cdata){
	    	    			var cobj = Ext.decode(cdata);
							if(cobj.result){
						    	applied_ds.reload();
						    	DataHandler.checkApplyOfIid(Number(cIid),function(data){
					    			var obj = Ext.decode(data);
									if(obj.result){
										commonForm.getForm().findField('appCount').setValue(obj.appCount);
								        if(obj.appCount>0){
								        	Ext.getCmp("appliedDetail").enable();
								        }else{
								        	Ext.getCmp("appliedDetail").disable();
								        }
									}
					        	});
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
			Ext.MessageBox.confirm('确认删除', '本操作会将关于当前项目的申报记录全部删除，确定删除？', function(btn){
		    	if(btn == 'yes') {// 选中了是按钮
		    		DataHandler.deleteFormalApps(1,Number(cIid),'',function(data){
		    			var obj = Ext.decode(data);
						if(obj.result){
							applied_ds.reload();
							DataHandler.checkApplyOfIid(Number(cIid),function(cdata){
				    			var cobj = Ext.decode(cdata);
								if(cobj.result){
									commonForm.getForm().findField('appCount').setValue(obj.appCount);
							        if(obj.appCount>0){
							        	Ext.getCmp("appliedDetail").enable();
							        }else{
							        	Ext.getCmp("appliedDetail").disable();
							        }
								}
				        	});
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
	    store: applied_ds,
	    displayInfo: true,
	    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	    emptyMsg: "没有数据",
	    items: ['-']
	})
});
var appWin = new Ext.Window({
    title: '已有申报(当前项目已入库的申报信息)',
    width: 460,
    height: 400,
    layout: 'fit',
    buttonAlign:'center',
    closeAction:'hide',
    items: [appliedGrid],
    buttons: [{
        text: '关闭',
        handler:function(){
    		appWin.hide(); 
	    }
    }]
});
appWin.on("show",function(){
	applied_ds.baseParams.iid=Number(cIid);
	applied_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
});
//下部的grid
var ssm = new Ext.grid.CheckboxSelectionModel(); 
ssm.handleMouseDown = Ext.emptyFn;
var app_cm = new Ext.grid.ColumnModel({
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
		    header:'税收和申报',
		    width: 90,
		    align:'right',
		    renderer: function(v,p,r){
				var swdjzh = r.get("swdjzh");
		    	return "<input type='button' value='详情' onclick='showEnHistory(\""+swdjzh+"\")' style='height:20px;font-size:12px' >" ;    
			}
		},{
	       id:'swdjzh',
	       header: "税号",
	       dataIndex: 'swdjzh',
	       width: 120,
	       align:'left'
	    },
	    {
	       id:'qymc',
	       header: "企业名称",
	       dataIndex: 'qymc',
	       width: 200,
	       align: 'left'
	    },{
	    	header: "上年财政贡献",
		    id:'contribute_lst',
		    dataIndex: 'contribute_lst',
		    width: 100,
		    align: 'right'
		},{
			header: "本年财政贡献",
		    id:'contribute',
		    dataIndex: 'contribute',
		    width: 100,
		    align: 'right'
		},{
		    header: "计划资助额",
		    id:'money',
		    dataIndex: 'money',
		    width: 100,
		    align: 'right'
		},{
		    header: "省市资金",
		    dataIndex: 'sszj',
		    width: 100,
		    align: 'right'
		},{
		    header: "区配套",
		    dataIndex: 'qptzj',
		    width: 100,
		    align: 'right'
		},{
	        header: "项目申请内容",
	        id:'itemcont',
	        dataIndex: 'itemcont',
	        width: 220,
	        align: 'left'
	    },{
		    header: "投资总额",
		    dataIndex: 'investment',
		    width: 100,
		    align: 'right'
		},{
	    	id:'czfp',
		    header: "财政分片",
		    dataIndex: 'czfp',
		    width: 90,
		    align: 'left'
		},{
		    header: "申报时间",
		    id:'approvaldate',
		    dataIndex: 'approvaldate',
		    width: 90,
		    align: 'left'
		},{
		    header: "备注",
		    id:'remark',
		    dataIndex: 'remark',
		    width: 220,
		    align: 'left'
		}
	],
	defaultSortable: false
});
var app_Record = Ext.data.Record.create([   
    {name: 'xh', type: 'int'},
    {name: 'swdjzh', type: 'string'},
    {name: 'nomatch', type: 'int'},
    {name: 'qymc', type: 'string'},
    {name: 'czfp', type: 'string'},
    {name: 'itemcont',type:'string'},
    {name: 'approvaldate',type:'string'},
    {name: 'contribute',type:'float'},
    {name: 'contribute_lst',type:'float'},
    {name: 'money',type:'float'},
    {name: 'sszj',type:'float'},
    {name: 'qptzj',type:'float'},
    {name: 'investment',type:'string'},
    {name: 'remark',type:'string'}
]);
var app_grid_ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getImportedAppData,
		paramOrder: ['iid','matchType','year','start','limit'],
		paramsAsHash: false
	}),  
	reader : new Ext.data.JsonReader({
		idProperty: 'xh',
		root: 'rows',
		totalProperty: 'totalCount'}, app_Record)
});

var app_grid = new Ext.grid.EditorGridPanel({
	cm : app_cm,
	title: '申报信息(此处为临时信息，请调整后执行“保存”操作以形成正式数据)',
	enableColumnMove :true,
	stripeRows: true,
	selModel: ssm,
	clicksToEdit:1,
	store : app_grid_ds,
	plugins :[new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :60000
	})],
	tbar: [{
		text:"删除",
		iconCls:"remove",
		id:"delete",
		handler: function(){
			var records = app_grid.getSelectionModel().getSelections();
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
	    	    		DataHandler.deleteImportedApps(Number(cIid),aids.join(),function(data){
	    	    			var obj = Ext.decode(data);
							if(obj.result){
	    						for(var rc=0;rc<records.length;rc++){						    	    	
						    	    app_grid_ds.remove(records[rc]);
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
		text : "保存为申报",
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
	    store: app_grid_ds,
	    displayInfo: true,
	    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	    emptyMsg: "没有数据",
	    items: ['-']
	})
});
function renderMatch(v,p,r){
	var index = app_grid_ds.indexOf(r);
	if(v==1||v=='1'){//未匹配
	    return "<input type='button' value='手工匹配' onclick='matchFn("+index+")' style='height:20px;font-size:12px' >" ;    
	}else{
	    return '';
	}
}
function matchFn(index){
	matchRecord=app_grid_ds.getAt(index);
	var iid =  commonForm.getForm().findField("iid").getValue();
	if(!iid || iid == ''){
		Ext.Msg.alert('系统提示','申报项目不能为空！');
        return;
	}
	enWin.show();
}

//企业历史申报和税收贡献信息
var ehcm = new Ext.grid.ColumnModel({
	columns: [
	    {
	        header: "申请项目",
	        id:'itemname',
	        dataIndex: 'itemname',
	        width: 150,
	        align: 'left'
	    },{
	        header: "申请内容",
	        id:'itemcont',
	        dataIndex: 'itemcont',
	        width: 200,
	        align: 'left'
	    },{
		    header: "申报时间",
		    id:'approvaldate',
		    dataIndex: 'approvaldate',
		    width: 90,
		    align: 'left'
		},{
		    header: "申请金额",
		    id:'money',
		    dataIndex: 'money',
		    width: 100,
		    align: 'right'
		},{
		    header: "税收年度",
		    dataIndex: 'taxyear',
		    width: 70,
		    align: 'center'
		},{
		    header: "税收",
		    dataIndex: 'tax',
		    width: 100,
		    align: 'right'
		},{
		    header: "财政贡献",
		    dataIndex: 'qgx',
		    width: 100,
		    align: 'right'
		}
	],
	defaultSortable: false
});
var ehRecord = Ext.data.Record.create([   
    {name: 'itemname',type:'string'},
    {name: 'itemcont',type:'string'},
    {name: 'approvaldate',type:'string'},
    {name: 'taxyear',type:'string'},
    {name: 'money',type:'float'},
    {name: 'tax',type:'float'},
    {name: 'qgx',type:'float'}
]);
var enhStore = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
        directFn : DataHandler.getEnHistoryData,
        paramOrder: ['swdjzh'],
        paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
    }, ehRecord)
});
var enhGrid = new Ext.grid.GridPanel({
	cm: ehcm,
	title: '',
	enableColumnMove :true,
	stripeRows: true,
	store : enhStore
});
var enHistoryWin = new Ext.Window({
	title: '企业历年贡献及申报信息',
    width: 460,
    height: 400,
    layout: 'fit',
    buttonAlign:'center',
    closeAction:'hide',
    items: [enhGrid],
    buttons: [{
        text: '关闭',
        handler:function(){
    		enHistoryWin.hide(); 
	    }
    }]
});
function showEnHistory(swdjzh){
	enhStore.baseParams.swdjzh=swdjzh;
	enhStore.load();
	enHistoryWin.show();
}
function save(acType){
	var iid =  commonForm.getForm().findField("iid").getValue();
	if(!iid || iid == ''){
		Ext.Msg.alert('系统提示','请先选择申报项目！');
        return;
	}
	var allRows=new Array();
	for(var i=0;i<app_grid.getStore().getCount();i++){
		var rs = app_grid.getStore().getAt(i);
	    var fields=rs.data;
	    if(fields["itemcont"]&& fields["itemcont"]== '' ){
	    	Ext.Msg.alert('系统提示','企业申报记录中，项目名称不能为空');
	    	return;
   	    }
	    if(fields["approvaldate"]&& fields["approvaldate"]== ''){
	    	Ext.Msg.alert('系统提示','企业申报记录中，申报日期不能为空');
	    	return;
   	    }
	    if(fields["money"]&& fields["money"]== ''){
	    	Ext.Msg.alert('系统提示','企业申报记录中，预资助金额不能为空');
	    	return;
   	    }
   	    if(!fields["swdjzh"]||fields["swdjzh"]==''){
	   	    Ext.Msg.alert("系统提示","第"+(i+1)+"行的税号不能为空，删除该记录或手工匹配！");
			return;
	   	}
   	 	if(!fields["qymc"]||fields["qymc"]==''){
	   	    Ext.Msg.alert("系统提示","第"+(i+1)+"行的企业名称不能为空，删除该记录或手工匹配！");
			return;
	   	}
	    allRows.push(fields["xh"]);
	}
	Ext.Msg.wait("正在保存...");
	var saveRows =new Array();
	var modifiedRows = app_grid.getStore().getCount();
	for(var i=0;i<app_grid.getStore().getCount();i++){
		var row=new Object();
		var rs = app_grid.getStore().getAt(i);;
	    var fields=rs.data;
	    row.xh=fields["xh"];
	    row.swdjzh=fields["swdjzh"];
	    saveRows.push(row);
	}
	DataHandler.saveTempApps(Number(iid),Ext.encode(saveRows),function(data){   
    	var obj = Ext.util.JSON.decode(data);
    	if(obj.result){
    		firstSave = true;
			if(acType==1) {
				generate(iid,allRows.join());
			}else{
				Ext.Msg.hide();
				Ext.Msg.alert('信息','修改内容保存成功！');
				app_grid.getStore().reload();
			}
	 	}else{
	 	    Ext.Msg.alert('信息','修改内容保存失败！');
	 	}
	});	      
}
function generate(iid,xhs){
	DataHandler.saveFormalApps(Number(iid),xhs,function(fdata){   
	    var obj = Ext.util.JSON.decode(fdata);
	    if(obj.result){
	    	Ext.Msg.hide();
    	 	Ext.Msg.alert('信息',"保存申报操作成功！"+obj.info);
    	 	//project_tg.setValue(""); 
    	 	//commonForm.getForm().findField("iid").setValue(""); 
    	 	//commonForm.getForm().findField("appCount").setValue(""); 
    	 	//Ext.getCmp("appliedDetail").disable();
    	 	//var obj = document.getElementById("filepath");        
            //obj.outerHTML = obj.outerHTML;
    	 	app_grid.getStore().reload();
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
			opt.title =app_grid.title;
			app_grid.exportExcel(opt);
	        winFormat.hide(); 
		}
    },{
        text: '取消',
        handler:function(){
	        winFormat.hide(); 
	    }
    }]
});
//手工匹配使用的弹出窗体，现有企业列表，提供模糊查找，选中后，写入下部grid,并改写相同企业和项目的已有申报数
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
	{name: 'dz', type: 'string'},
	{name: 'contribute_lst', type: 'float'},
	{name: 'contribute', type: 'float'},
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
			var rcs = en_grid.getSelectionModel().getSelections();// 返回值为 Record 类型
			if(!rcs||rcs<1){
				Ext.Msg.alert("提示","请先选择企业!");
				return;
			}
			//及时更新后台
			DataHandler.matchEn(Number(cIid),matchRecord.get("xh"),rcs[0].get("swdjzh"),function(data){
		    	var obj = Ext.util.JSON.decode(data);
		    	if(obj&&obj.result){
		    		enWin.hide();
		    		matchRecord.set("swdjzh",rcs[0].get("swdjzh"));
		    		matchRecord.set("qymc",rcs[0].get("mc"));
		    		var appYear = commonForm.getForm().findField("appYear").getValue();
		    		DataHandler.queryContributeOfEn(''+appYear,rcs[0].get("swdjzh"),function(data){
		    			var dt = Ext.util.JSON.decode(data);
		    			if(dt&&dt.result){
		    				matchRecord.set("contribute",dt.contribute);
				    		matchRecord.set("contribute_lst",dt.contribute_lst);
		    			}else{
		    				matchRecord.set("contribute",0);
				    		matchRecord.set("contribute_lst",0);
		    			}
				    });
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
	        height:150,
	        border: false,
			items: commonForm
	   	},{  
			region:'center',  
			layout:'fit',  
			split:true,  
			autoScroll:true, //自动滚动条
			items: app_grid  
		}
	    ]
	});
	/*DataHandler.getAppTemplate(node[0].id,function(data){
		var result = Ext.decode(data);
		if(result&&result.columnModel){//模板重新加载
			var appstore,appliedstore;
			var appccm,appliedccm;
			app_grid_ds.reader.recordType = Ext.data.Record.create(result.store);
			applied_ds.reader.recordType = Ext.data.Record.create(result.store);
			var cols = result.columnModel;
			appccm = new Ext.grid.ColumnModel({
				columns: [ssm,{
				    id:'adjust',
				    header:'手工匹配',
				    width: 70,
				    align:'left',
				    dataIndex:'NOMATCH',
				    renderer: renderMatch
				},{
				    header:'已申报',
				    width: 70,
				    align:'left',
				    dataIndex:'ENAPPCOUNT',
				    renderer: function(v,r,p){
				    	if(v!=""&&v>0){
				    		p.title = "当前项目、当前企业已有"+v+"条申报记录。";
				    	}
				    	return v;
					}
				}].concat(cols)
			});
			appliedccm = new Ext.grid.ColumnModel({
				columns: [appliedSsm].concat(cols)
			});
			appstore= app_grid_ds;
			appliedstore= applied_ds;
			app_grid.reconfigure(appstore,appccm);
			appliedGrid.reconfigure(appliedstore,appliedccm);
		}
	});*/	
});
</script>
</head>
<body>
</body>
</html>