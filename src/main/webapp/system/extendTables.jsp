<%@ page contentType="text/html; charset=UTF-8"%>
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
Ext.Direct.addProvider(Ext.datapro.REMOTING_API);
var cTb = {};
var edtCol=new Array(),edtDesc=new Array(),edtType=new Array(),newCols=new Array();
var cMode="add";
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "表名",
	        dataIndex: 'tbname',
	        width: 100,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "中文名",
	        dataIndex: 'tbdesc',
	        width: 120,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "类型",
	        dataIndex: 'ttype',
	        width: 80,
	        align: 'left',
	        renderer: function(v,p,r){
	        	if(v=="1"){
	        		p.attr = 'title=月度表('+v+")";
		        	return "月度表";
	        	}else if(v=="2"){
	        		p.attr = 'title=季度表('+v+")";
		        	return "季度表";
	        	}else if(v=="3"){
	        		p.attr = 'title=年度表('+v+")";
		        	return "年度表";
	        	}else {
	        		p.attr = 'title=一次性('+v+")";
		        	return "一次性";
	        	} 
	    	}
	    },{
	    	header: "备注",
	        dataIndex: 'remark',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo
		}
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([ 
	{name: 'tid', type: 'int'},
	{name: 'tbname', type: 'string'},
	{name: 'tbdesc', type: 'string'},
	{name: 'ttype', type: 'string'},
	{name: 'remark', type: 'string'},
	{name: 'proname', type: 'string'}
]);
var ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getTbs,
		paramOrder: ['start','limit'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty: 'tbname',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
var grid = new Ext.grid.GridPanel({
	title:'扩展表信息',
	store: ds,
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
		text: '新增',
		iconCls: 'add',
        handler : function(){
    		cMode="add";
            tbWin.show();
		}
	},{
		text: '详情',
		iconCls: 'details',
        handler : function(){
			cMode="detail";
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要查看的表!");
				return;
			}
			cTb = records[0];
			edtCol=new Array();
			edtDesc=new Array();
			edtType=new Array();
			newCols=new Array();
            tbWin.show();
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = grid.getSelectionModel().getSelections();
		    if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的表!");
				return;
			}
			var tbs = new Array();
			for(var i=0;i<records.length;i++){
				var rd = records[i];
				tbs.push(rd.get("tbname"));
			}
		    Ext.MessageBox.confirm('确认', '本操作会删除该表以及相关的Excel导入模板信息，是否继续?', function(btn){
				if(btn == 'yes') {
					Ext.Msg.wait("正在删除...");
					DataHandler.deleteTb(tbs.join(),function(data){
						Ext.Msg.hide();
						var obj = Ext.decode(data);
						if(obj&&obj.result){
							ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							Ext.Msg.alert("提示","选中企业的表的所有信息已删除！");
						}else{
							Ext.Msg.alert("提示","删除表操作时发生错误，删除失败!");
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
var tbForm = new Ext.FormPanel({
	frame:true,
	buttonAlign :'center',
    id: 'tbForm',
    labelAlign:'right',
    bodyStyle: 'padding:5px',
    border: false,
    layout : 'absolute', 
    items: [
	{
        x: 5,
        y: 10,
        xtype:'label',
        text: '表名:'
	},{
		x: 40,
        y: 5,
		xtype: 'textfield',
		width: 150,
		maxLength: 30,
		allowBlank:false,
		name: 'tbname'
	},{
        x: 250,
        y: 10,
        xtype:'label',
        text: '中文名:'
	},{
		x: 300,
        y: 5,
		xtype: 'textfield',
		width: 150,
		allowBlank:false,
		name: 'tbdesc'
	},{
        x: 5,
        y: 40,
        xtype:'label',
        text: '类型:'
	},{
		x: 40,
        y: 35,
		xtype: 'combo',
		width: 150,
		allowBlank: false,
		name: 'ttype',
		mode : 'local', 
        triggerAction : 'all', 
        hiddenName: 'tbtype',
        forceSelection:true,
        store : new Ext.data.SimpleStore({
        	fields : ['mc', 'bm'],
        	data : [['一次性', "0"],['月度表', '1'],['季度表', '2'],['年度表', '3']]
        }),
        valueField : "bm", 
        displayField : "mc", 
        editable: false
	},{
        x: 225,
        y: 40,
        xtype:'label',
        text: '导入后执行:'
	},{
		x: 300,
        y: 35,
		xtype: 'textfield',
		width: 150,
		allowBlank: true,
		name: 'proname' 
	},{
        x: 5,
        y: 70,
        xtype:'label',
        text: '备注:'
	},{
		x: 40,
        y: 65,
		xtype: 'textfield',
		width: 410,
		allowBlank: true,
		name: 'remark' 
	},{
		xtype: 'hidden',
		name: 'tid' 
	}]
});
var tssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
tssm.handleMouseDown = Ext.emptyFn;
var tcm = new Ext.grid.ColumnModel({
	columns: [
		tssm,
	    {
	        header: "列名",
	        dataIndex: 'colname',
	        width: 150,
	        align: 'left',
	        editor: new Ext.form.TextField({allowBlank: false,selectOnFocus:true,maxLength:30}) 
	    },{
	        header: "列中文名",
	        dataIndex: 'coldesc',
	        width: 180,
	        align: 'left',
	        editor: new Ext.form.TextField({allowBlank: false,selectOnFocus:true,maxLength:30}) 
	    },{
	        header: "数据类型",
	        dataIndex: 'coltype',
	        width: 80,
	        align: 'left',
	        editor: new Ext.form.ComboBox({
	        	displayField:'text',
				valueField:'id',
				editable: false, 
				triggerAction : 'all',
				allowBlank:true,
				value:'',
				mode: 'local',
				store : new Ext.data.SimpleStore({ 
				    fields : ["id", "text"], 
				    data : [ 
				    	['0', '文本'], 
				        ['1', '整数'],
				        ['2', '长文本'],
				        ['3', '金额']	
				    ] 
				}) 
			}),
			renderer :function(v,p,r){
				if(v == '0'){
					return "文本";
				}else if(v == '1'){
					return "整数";
				}else if(v == '2'){
					return "长文本";
				}else if(v == '3'){
					return "金额";
				}else{
				    return "";
				}
			}
	    }
	],
	defaultSortable: false
});
var tbRecord = Ext.data.Record.create([ 
    {name: 'isold',type:'int'},
    {name: 'o_colname', type: 'string'},
	{name: 'colname', type: 'string'},
	{name: 'coldesc', type: 'string'},
	{name: 'coltype', type: 'int'},
	{name: 'showorder',type:'int'}
]);
var tbds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: DataHandler.getTbCols,
		paramOrder: ['tbname'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty: 'colname'
	}, tbRecord)
});
var tbInfoGrid = new Ext.grid.EditorGridPanel({
	title: '列信息',
	store: tbds,
	cm: tcm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : new Ext.grid.GridView(),
	selModel: tssm,
	clicksToEdit:1,
	stripeRows: true,
	tbar: [
    {
		text: '增加',
		id: 'addColumn',
		iconCls: 'add',
        handler : function(){
        	var btindex = tbds.getCount();
	    	var col = new tbRecord({
	    		isOld: 0,
	    		colname: '',
	    		o_colname :'',
	    		coldesc: '',
	    		coltype: '0',
	    		showorder: btindex
	        });
	    	tbInfoGrid.stopEditing();
	    	tbds.insert(btindex, col);
	    	tbInfoGrid.startEditing(btindex, 0);
		}
	},{
		text: '删除',
		id: 'delColumn',
		iconCls: 'remove',
        handler : function(){
			var records = tbInfoGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的列!");
				return;
			}	
			if(records){
				if(cMode=="add"){
					for(var rc=0;rc<records.length;rc++){						    	    	
		    	    	tbds.getModifiedRecords().remove(records[rc]);
		    	    	tbds.remove(records[rc]);
				    }
				}else{
					Ext.MessageBox.confirm('确认删除', '确定要从表中删除这个列吗?', function(btn){
						if(btn == 'yes') {// 选中了是按钮
							var c = records[0].colname;
							DataHandler.deleteColumn(cTb.get("tbname"),c,function(data){
								var obj = Ext.decode(data);
								if(obj&&obj.result){
									Ext.Msg.alert("提示","指定的列已删除!");
									tbds.reload();
								}else if(obj&&obj.info){
									Ext.Msg.alert("错误","删除列过程中发生错误。"+obj.info);
								}
							}); 	
						}
					});
				}
			}
		}
	},{
		text: '保存',
		id: 'saveColumns',
		iconCls: 'save',
        handler : function(){
			saveTbInfos();
		}
	}]
});
tbInfoGrid.on('afteredit',function(e){ 
	var r = e.record;
	//如果是编辑模式。记录下非新增的，即修改的信息
 	if(cMode !="add"&&r.isold==1){
 		var edtInfo = {
 			col: r.o_colname,
 			newVal: e.value	
 		};
 		if(e.field=="colname"){
 			edtCol.push(edtInfo) ;
 		}else if(e.field=="coldesc"){
 			edtDesc.push(edtInfo) ;
 		}else if(e.field=="coltype"){
 			edtType.push(edtInfo);
 		}
 	}
});
var tbWin= new Ext.Window({
	id : 'tbWin',
	title : '扩展表详情',
	layout : 'border',
	width : 495,
	height : 420,
	modal : true,
	closable: false,
	closeAction:'hide',
	items:[{	
	    region:'north',
	    height:120,
	    frame:true,
	    layout:'fit',
	   	items: tbForm
	 },{  
	   	region:'center',  
	   	layout:'fit',  
	   	items: tbInfoGrid  
	}],
	buttons : [
	{
		text : "确定",
		handler : function(){
			saveTbInfos();
		}
	},{
		text : "取消",
		handler : function() {
			tbWin.hide();
		}
	}]
});	
tbWin.on("show",function(){
	if(cMode=="add"){
		//启用所有可编辑部分
		tbForm.getForm().findField("tbname").setDisabled(false);
		tbForm.getForm().findField("tbtype").setDisabled(false);
		tbForm.getForm().findField("tbname").setValue("");
		tbForm.getForm().findField("tbdesc").setValue("");
		tbForm.getForm().findField("tbtype").setValue("0");
		tbForm.getForm().findField("proname").setValue("");
		tbForm.getForm().findField("remark").setValue("");
		tbForm.getForm().findField("tid").setValue("-1");
		tbds.removeAll();
	}else{
		//加载扩展表信息，禁用除对应excel列外的所有内容
		tbForm.getForm().findField("tbname").setDisabled(true);
		tbForm.getForm().findField("tbtype").setDisabled(true);
		tbForm.getForm().findField("tbname").setValue(cTb.get("tbname"));
		tbForm.getForm().findField("tbdesc").setValue(cTb.get("tbdesc"));
		tbForm.getForm().findField("tbtype").setValue(cTb.get("ttype"));
		tbForm.getForm().findField("proname").setValue(cTb.get("proname"));
		tbForm.getForm().findField("remark").setValue(cTb.get("remark"));
		tbForm.getForm().findField("tid").setValue(cTb.get("tid"));
		tbds.baseParams.tbname=cTb.get("tbname");
		tbds.load();
	}
});
function saveTbInfos(){
	var tb = buildTb();	
	var tn = tb.tbname,td=tb.tbdesc;
	if(!tn||tn==""){
		Ext.Msg.alert("提示","表名不能为空!");
		return;
	}
	if(!td||td==""){
		Ext.Msg.alert("提示","表名的中文名不能为空!");
		return;
	}
	if(tn.length>26){
		Ext.Msg.alert("提示","表名长度不应超过26!");
		return;
	}
	if(!oraObjectName(tn)){
		Ext.Msg.alert("提示","表名格式应符合:字母开头，内容为字母、数字、下划线!");
		return;
	}
	if(!tb.cols||tb.cols.length==0){
		Ext.Msg.alert("提示","请输入表的列定义!");
		return;
	}
	for(var i=0;i<tb.cols.length;i++){
		var cl = tb.cols[i];
		if(!cl.colname||cl.colname==""){
			Ext.Msg.alert("提示","第"+(i+1)+"行的列名不能为空!");
			return;
		}
		if(cl.colname.length>30){
			Ext.Msg.alert("提示","第"+(i+1)+"行的列名长度不应超过30!");
			return;
		}
		if(!oraObjectName(cl.colname)){
			Ext.Msg.alert("提示","第"+(i+1)+"行的列名格式应符合:字母开头，内容为字母、数字、下划线!");
			return;
		}
		if(!cl.coldesc||cl.coldesc==""){
			Ext.Msg.alert("提示","第"+(i+1)+"行的列中文名不能为空!");
			return;
		}
	}
	if(cMode=="add"){
		Ext.MessageBox.confirm('确认', '你真的要在数据库中增加一个表吗?', function(btn){
			if(btn == 'yes') {	
				Ext.Msg.wait("正在增加...");
				DataHandler.CheckTableName(tb.tbname,function(data){
					var obj = Ext.decode(data);
					Ext.Msg.hide();
					if(obj&&!obj.duplicate){
						DataHandler.addExtendTables(Ext.encode(tb),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
								Ext.Msg.alert("提示","新增表添加完成!");
								tbds.commitChanges();
								tbWin.hide();
								ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
							}else{
								Ext.Msg.alert("错误","新增表过程中发生错误:"+obj.info);
							}
							edtCol=new Array();
							edtDesc=new Array();
							edtType=new Array();
							newCols=new Array();
						});
					}else if(obj.duplicate){
						Ext.Msg.alert("错误","表名已存在!");
					}else{
						Ext.Msg.alert("错误","检查表名重复时发生错误!");
					}
				});
			}
		});
	}else{
		Ext.Msg.wait("正在保存...");
		var edtInfo = {
			edtCol:edtCol,
			edtDesc:edtDesc,
			edtType:edtType,
			newCols:newCols
		};
		DataHandler.saveExtendTables(tn,Ext.encode(tb),Ext.encode(edtInfo),function(data){
			var obj = Ext.decode(data);
			Ext.Msg.hide();
			if(obj&&obj.result){
				Ext.Msg.alert("提示","表信息保存成功!");
				tbds.commitChanges();
				tbWin.hide();
				edtCol=new Array();
				edtDesc=new Array();
				edtType=new Array();
				newCols=new Array();
				ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
			}else{
				Ext.Msg.alert("错误","保存表信息过程中发生错误!");
			}
		});
	}
}
function buildTb(){
	var rds = tbds.getRange();
	var cc = tbds.getCount();
	var tb = new Object();
	var cols = new Array();
	for(var i=0;i<cc;i++){
		var rd = rds[i];
		var col = {
			colname: rd.get("colname"),
			coldesc: rd.get("coldesc"),
			coltype: rd.get("coltype"),
			showorder: rd.get("showorder")
		};
		cols.push(col);
		//保存新增列，主要用于编辑模式下，对新增列的特殊处理
		if(rd.get("isold")==0){
			newCols.push(col);
		}
	}
	tb.tbname = tbForm.getForm().findField("tbname").getValue(); 
	tb.tbdesc = tbForm.getForm().findField("tbdesc").getValue();
	tb.ttype = tbForm.getForm().findField("tbtype").getValue();
	tb.proname = tbForm.getForm().findField("proname").getValue();
	tb.remark = tbForm.getForm().findField("remark").getValue();
	tb.tid = tbForm.getForm().findField("tid").getValue();
	tb.cols = cols;
	return tb;
}
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[grid]
	});
	ds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});

function oraObjectName(str){
	var r = /^[a-zA-Z][a-zA-Z0-9_]*$/ ;
	return r.test(str);
}
</script>
</head>
<body>
</body>
</html>