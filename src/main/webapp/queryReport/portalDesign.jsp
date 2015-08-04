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
<title>Portal</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<link href="<%=request.getContextPath()%>/css/Portal.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/query.js"></script>
<script type="text/javascript">
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.query.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.query.REMOTING_API);

var cMode = "add";
var cPid = "";
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
		{
	        header: "<div style=text-align:center>ID</div>",
	        dataIndex: 'id',
	        width: 100,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>名称</div>",
	        dataIndex: 'name',
	        width: 150,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>描述</div>",
	        dataIndex: 'remark',
	        width: 200,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>portlet数量</div>",
	        dataIndex: 'total',
	        width: 70,
	        align: 'left'
	    },{
	        header: "<div style=text-align:center>列数</div>",
	        dataIndex: 'colCount',
	        width: 70,
	        align: 'left'
	    }
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'id', type: 'string'},
	{name: 'name', type: 'string'},
	{name: 'remark', type: 'string'},
	{name: 'total', type: 'int'},      
	{name: 'colCount', type: 'int'}
]);
var ds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: PortalHandler.getPortals,
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'id'
	}, cRecord)
});
var view = new Ext.grid.GridView();
var grid = new Ext.grid.GridPanel({
		title:'portal设计列表',
		store: ds,
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
			text: '增加',
			iconCls: 'add',
            handler : function(){
            	cMode = "add";
            	cPid = "";
            	pWin.show();
			}
		},{
			text: '修改',
			iconCls: 'edit',
            handler : function(){
            	var records = grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择修改的记录!");
					return;
				}
		        cPid = records[0].get("id");
				cMode = "modify";
				pWin.show();
			}
		},{
			text: '删除',
			iconCls: 'remover',
            handler : function(){
				var records = grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除的记录!");
					return;
				}
		        cPid = records[0].get("id");
		        PortalHandler.deletePortalDesign(cPid,function(data){
		        	alert(data);
		        	var rslt = Ext.decode(data);
					if(rslt&&rslt.result){
						Ext.Msg.alert("提示","记录已删除！");
						ds.reload();
					}else{
						Ext.Msg.alert("提示","记录删除过程发生错误！");
					}
		        }); 
			}
		},{
			text: '预览',
			iconCls: 'detail',
            handler : function(){
            	var records = grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要预览的portal设计文件!");
					return;
				}
		        cPid = records[0].get("id");
				//新打开一个页面，预览portal布局，不需要加载内容
			}
		}
		],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: ds,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
var pForm = new Ext.FormPanel({
	frame: true,
	api: {
        submit: PortalHandler.savePortal
    },
    layout : 'form',
    labelWidth: 50,
	labelAlign:'right',
	items:[
	{
		layout : 'column',
		items: [
		{
			layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			labelWidth: 50,
			items:[
			new Ext.form.TextField({	
				name:'id',
				fieldLabel : 'ID',
				width:150,
				maxLength: 20
			}),new Ext.form.TextField({	
				name:'name',
				fieldLabel : '名称',
				width:150,
				maxLength: 100
			})
			]
		},{
			layout:'form',
			columnWidth: .5,
			frame:false,
			border:false,
			labelWidth: 70,
			items:[
			new Ext.form.NumberField({
				name:'colCount',
				fieldLabel : '布局列数',
				width:110
			}),new Ext.form.NumberField({	
				name:'defaultHeight',
				fieldLabel : '默认面板高',
				width:110
			})]
		}]
	},new Ext.form.TextArea({
		name:'remark',
		fieldLabel : '备注',
		width: 360,
		height:50
	})
	]
});
var pssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
pssm.handleMouseDown = Ext.emptyFn;
var pcm = new Ext.grid.ColumnModel({
	columns: [
		pssm,
		{
	        header: "ID",
	        dataIndex: 'id',
	        width: 90,
	        align: 'left',
	        editor: new Ext.form.TextField({selectOnFocus:true,maxLength:30})
	    },{
	        header: "面板标题",
	        dataIndex: 'title',
	        width: 150,
	        align: 'left',
	        editor: new Ext.form.TextField({allowBlank: false,selectOnFocus:true,maxLength:50})
	    },{
	        header: "面板高度",
	        dataIndex: 'height',
	        width: 60,
	        align: 'left',
	        editor: new Ext.form.NumberField({selectOnFocus:true,maxLength:4})
	    },{
	        header: "类型",
	        dataIndex: 'type',
	        width: 90,
	        align: 'left',
	        editor: new Ext.form.ComboBox({
	    		width: 90,
	    	    mode : 'local', 
	            triggerAction : 'all', 
	            hiddenName:'ismanager',
	            forceSelection:true,
	            value: '0',
	            store : new Ext.data.SimpleStore({ 
	            	fields : ["id", "text"], 
	            	data : [
	            	['0', '报表'], 
	            	['1', '图表'],
	            	['2', '文本'],
	             	] 
	            }),
	            valueField : "id", 
	            displayField : "text", 
	            editable: false
			}),
			renderer: function(v,p,r){
				if(v=="1"){
					return "图表"; 
				}else if(v=="2"){
					return "文本"; 
				}else{
					return "报表"; 
				}
			}
	    },{
	        header: "内容",
	        dataIndex: 'content',
	        width: 150,
	        align: 'left',
	        editor: new Ext.form.TextField({allowBlank: false,selectOnFocus:true,maxLength:500})
	    }
	],
	defaultSortable: false
});
var ptRecord = Ext.data.Record.create([  
	{name: 'id', type: 'string'},
	{name: 'title', type: 'string'},
	{name: 'height', type: 'string'},
	{name: 'type', type: 'string'},
	{name: 'content', type: 'string'} 
]);
var ptds = new Ext.data.Store({
	/*proxy: new Ext.data.DirectProxy({
		directFn: PotalHandler.getGckProcess,
		paramsAsHash: false,
		paramOrder: ['iid']
	}), */
	reader: new Ext.data.JsonReader({
		idProperty : 'id'
	}, ptRecord)
});
var ptgrid = new Ext.grid.EditorGridPanel({
	title:'',
	store: ptds,
	cm: pcm,
	clicksToEdit:1,
	frame: false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	view : new Ext.grid.GridView({}),
	selModel: pssm,
	stripeRows: true,
	tbar: [
	{
	   	text: '添加行',
	   	iconCls: 'add',
	   	handler : function(){
			var cc = ptds.getCount();  
	   	    var rd = new ptRecord({
	   	    	id :'',
	   	    	title:'',
	   	    	height:'',
	   	    	type:'0',  
	   	    	content:''
	   	    });
	   	 	ptgrid.stopEditing();
	   	 	ptds.insert(cc, rd);
	   	 	ptgrid.startEditing(cc, 0);
	   	}
	},{
	   	text: '删除',
	   	iconCls: 'remove',
	   	handler :function(){
	   	    var records = ptgrid.getSelectionModel().getSelections();
	   	    if(!records||records.length<1){
	   			Ext.Msg.alert("提示","请先选择要删除的行!");
	   			return;
	   		}	
	   	 	Ext.MessageBox.confirm('确认', '确定要删除指定行?', function(btn){
				if(btn == 'yes') {
					var rl = records.length;
			        for(var rc=0;rc<rl;rc++){						    	    	
						ptds.getModifiedRecords().remove(records[rc]);
						ptds.remove(records[rc]);
					}
				}
			});
	   	}
	}]
});
var pWin = new Ext.Window({
    title : 'portal属性设置',
    width : 480,
    height : 400,
    layout:'border',
    items:[{
    	id:'north',
    	layout:'fit',
        region:'north',	
        height:130,
        frame:false,
	    border:false,
		items: pForm
	},{	
		region:'center',	
        layout:'fit',
        frame:false,
	    border:false,
		items: ptgrid
	}],
	closeAction:'hide',
    buttons : [
    {
    	text : "关闭",
	    handler:function(){
    		pWin.hide();
	    }
    },{
    	text: '保存',
    	handler: function(){
    		var tmpid = pForm.getForm().findField("id").getValue();
			if(!tmpid||tmpid==""){
				Ext.Msg.alert("提示","请输入ID!");
				return;
			}
			var tname = pForm.getForm().findField("name").getValue();
			if(!tname||tname==""){
				Ext.Msg.alert("提示","请输入名称!");
				return;
			}
			var cc = ptds.getCount(),cids = ptds.collect("id").length;
			if(cc>cids){
				Ext.Msg.alert("提示","每个面板的id不能重复，请检查!");
				return;
			}
			if(cMode=="modify"){
				savePortalinfo(tmpid);
			}else{
				PotalHandler.checkPortalid(tmpid,function(result){
					var rslt = Ext.decode(result);
					if(rslt&&!rslt.duplicate){
						savePortalinfo(tmpid);
					}else{
						Ext.Msg.alert("提示","当前portal设计ID已存在，不能重复！");
					}
				});
			}
    	}
    }]
});
function savePortalinfo(tmpid){
	var portlets = new Array();
	var pc = ptgrid.getStore().getCount();
	for(var i=0;i<pc;i++){
		var p = new Object();
		var pt = ptgrid.getStore().getAt(i);
		p.id = pt.get("id");
		p.title = pt.get("title");
		p.height = pt.get("height");
		p.type = pt.get("type");
		p.content = pt.get("content");
		portlets.push(p);
	}
	pForm.getForm().submit({
		//行解析
	    params: {cMode: cMode,portalid: tmpid,rows:Ext.encode(portlets)},
	    success :function(form,action){
			var obj = action.result;
			if(obj&&obj.infos){
				Ext.Msg.show({title:'成功',
					msg: obj.infos.msg,
					buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.INFO});
	           	ds.load();
				pWin.hide();
			}else{
				Ext.Msg.alert("提示","portal配置信息保存失败!");
			}
	    },
	    failure : function(form,action) {
	        Ext.Msg.alert('失败', "portal配置信息保存失败！");
	    }
	});
}
pWin.on("beforeshow",function(){
	var pf = pForm.getForm();
	if(cMode=="modify"){
		PortalHandler.getPortalDesign(cPid,function(data){
			var obj = Ext.decode(data);
			if(obj&&obj.portalInfo){
				var po = obj.portalInfo;
				pf.findField("id").setValue(po.id);
				pf.findField("name").setValue(po.name);
				pf.findField("remark").setValue(po.remark);
				pf.findField("defaultHeight").setValue(po.defaultHeight);
				pf.findField("colCount").setValue(po.colCount);
				pf.findField("id").disable();
				//加载详情内容
				ptgrid.getStore().loadData(obj.portlets);
			}
		});
	}else{
		pf.findField("id").enable();
	}
});
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[grid]
	});
	ds.load();
});
</script>
</head>
<body>
</body>
</html>