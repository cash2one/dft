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
	Map fldsMapShowInList = cg.getDJFieldsShowInList();
    List fldsInList = fldsMapShowInList==null?new ArrayList(): (List)fldsMapShowInList.get("DJ_CZ");
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
var cMode = "add";
var conditions = {};
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "虚拟税号/机构号",
	        dataIndex: 'swdjzh',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "地址",
	        dataIndex: 'dz',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "财政分片",
	        dataIndex: 'czfpbm',
	        width: 100,
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
	{name: 'dz', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'czfpbm_bm', type: 'string'}
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
	conditions.qysx=3;
	enDs.baseParams.conditions=Ext.encode(conditions);
});
enDs.on("load",function(){
	conditions = {};
});
var view = new Ext.grid.GridView();
var enGrid = new Ext.grid.GridPanel({
		title:'虚拟企业',
		store: enDs,
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
			xtype:'label',
			text:'企业名称：'
		},{
			xtype:'textfield',
			id:'paras',
			width:120,
			enableKeyEvent:true,
			name:'paras',
			hideLabel:true
			,listeners:{   
				specialkey:function(field,e){   
					if (e.getKey()==Ext.EventObject.ENTER){  
						var value = Ext.getCmp('paras').getValue();
						doConditions(value);
					}   
				}   
			}   
		},{
            text: '搜索',
            iconCls: 'filter',
            handler : function(){
				var value = Ext.getCmp('paras').getValue();
				doConditions(value);
            }
    	},new Ext.Toolbar.Separator(),
    	{
			text: '增加',
			iconCls: 'add',
            handler : function(){
            	cMode = "add";
            	enWin.show();
			}
		},{
			text: '修改',
			iconCls: 'edit',
            handler : function(){
				//当前选中记录
            	var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择修改的企业!");
					return;
				}
				cMode = "modify";
				enWin.show();
			}
		},{
			text: '删除',
			iconCls: 'remove',
            handler : function(){
				var records = enGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除的企业!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '是否确定要删除选中的虚拟企业?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var ens = new Array();
						for(var i=0;i<records.length;i++){
							var sh = records[i].get("xh");
							ens.push(sh);
						}
						EnHandler.deleteVEn(ens.join(),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
								conditions = {};
					           	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中企业已删除！");
							}else{
								Ext.Msg.alert("提示",obj.info);
							}
						});	
					}
				});
			}
		}
		],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: enDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
function doConditions(value){
	conditions = conditions||{};
	if(!value||value==""){
		enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		return;
	}
	var fldNames = new Array();
	var fldValues = new Array();
	var relations = new Array();
	var connections = new Array();
	fldNames.push("mc");
	fldValues.push(value);
	relations.push("like");
	connections.push("empty");
	conditions.fldNames=fldNames.join();
	conditions.fldValues=fldValues.join();
	conditions.relations=relations.join();
	conditions.connections=connections.join();
	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
}
function setEnBm(tField,hField){
	return function(value){
		enForm.getForm().findField(hField).setValue(value.id);
		enForm.getForm().findField(tField).setValue(value.text);
	}
};
var treeWin;
function showTreeWin(table,cVal,callback){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn: CheckHandler.getBmCodesTree,
			onlyLeafCheckable: true,
			codeTable: table,
			defaultValue: cVal
		});
	}
	var p = {table: table,selectedVals: cVal};
	treeWin.onSelect = callback;
	treeWin.setTreeParams( p);
	treeWin.refreshTree();
	treeWin.show();
}
var zt_tg = new Ext.form.TriggerField({
	fieldLabel:'状态',
	width:120,
	editable: false,
	id:'zt_tg',
	name:'zt_tg'
});

zt_tg.onTriggerClick=ztFun;
function ztFun(e){
	var cv = enForm.getForm().findField("ztbm_bm").getValue();
	showTreeWin("BM_ZT",cv,setEnBm("zt_tg","ztbm_bm"));
}

var hy_tg = new Ext.form.TriggerField({
	fieldLabel:'行业',
	width:120,
	editable: false,
	id:'hy_tg',
	name:'hy_tg'
 });
hy_tg.onTriggerClick=hyFun;
function hyFun(e){
	var cv = enForm.getForm().findField("hybm_bm").getValue();
	showTreeWin("BM_HY",cv,setEnBm("hy_tg","hybm_bm"));
}
var jjxz_tg = new Ext.form.TriggerField({
	fieldLabel:'经济性质',
	width:120,
	editable: false,
	id:'jjxz_tg',
	name:'jjxz_tg'
 });
jjxz_tg.onTriggerClick=jjxzFun;
function jjxzFun(e){
	var cv = enForm.getForm().findField("jjxzbm_bm").getValue();
	showTreeWin("BM_JJXZ",cv,setEnBm("jjxz_tg","jjxzbm_bm"));
}

var czfp_tg = new Ext.form.TriggerField({
	fieldLabel:'财政分片',
	width:120,
	editable: false,
	id:'czfp_tg',
	name:'czfp_tg'
 });
czfp_tg.onTriggerClick=czfpFun;
function czfpFun(e){
	var cv = enForm.getForm().findField("czfpbm_bm").getValue();
	showTreeWin("BM_CZFP",cv,setEnBm("czfp_tg","czfpbm_bm"));
}
var enForm = new Ext.FormPanel({
	layout : 'column',
	id : 'enForm',
	api: {
        submit: EnHandler.saveVirtualEn
    },
	frame: true,
	items:[
	{
		columnWidth : .5,
		layout : 'form',
		labelWidth : 90,
		border : false,
		labelAlign : 'left',
		items : [
		{
			name:'swdjzh',
			id: "swdjzh",
			xtype:"textfield",
			width:120,
		    fieldLabel : '税号/机构代码',
		    maxLength: 20
		},{
			name:'dz',
			id: "dz",
			xtype:"textfield",
			width:120,
		    fieldLabel : '地址',
		    maxLength: 100
		},zt_tg,
		hy_tg,
		czfp_tg,{
			name:'czfpbm_bm',
			id: "czfpbm_bm",
			xtype:"hidden",
		    fieldLabel : ''
		},{
			name:'hybm_bm',
			id: "hybm_bm",
			xtype:"hidden",
		    fieldLabel : ''
		},{
			name:'ztbm_bm',
			id: "ztbm_bm",
			xtype:"hidden",
		    fieldLabel : ''
		}]
	},{
		columnWidth : .5,
		layout : 'form',
		labelWidth : 90,
		border : false,
		labelAlign : 'left',
		items : [
		{
			name:'mc',
			id: "mc",
			xtype:"textfield",
			width:120,
		    fieldLabel : '企业名称',
		    maxLength: 100
		},{
			name:'fddbr',
			id: "fddbr",
			xtype:"textfield",
			width: 120,
		    fieldLabel : '法人代表'
		},{
			name:'bgrq',
			id: "bgrq",
			xtype:"textfield",
			width:120,
		    fieldLabel : '变更日期',
		    xtype:"datefield",
			format : 'Ymd',
		    maxLength: 20
		},
		jjxz_tg,
		{
			name:'jjxzbm_bm',
			id: "jjxzbm_bm",
			xtype:"hidden",
		    fieldLabel : ''
		},{
			name:'xh',
			xtype:"hidden",
		    fieldLabel : ''
		}
		]
	}
	]
});	
var enWin = new Ext.Window({
	id : 'enWin',
	title : '虚拟企业',
	items : [enForm],
	layout : 'fit',
	width : 550,
	height : 300,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var fxh = enForm.getForm().findField("xh").getValue();
			var txh = Number(fxh);
			var tsh = enForm.getForm().findField("swdjzh").getValue();
			var tmc = enForm.getForm().findField("mc").getValue();
			var tczfp = enForm.getForm().findField("czfpbm_bm").getValue();
			if(!tsh){
				Ext.Msg.alert("提示","税号/机构代码不能为空！");
				return;
			}
			if(!tmc){
				Ext.Msg.alert("提示","企业名称不能为空！");
				return;
			}
			if(!tczfp){
				Ext.Msg.alert("提示","财政分片不能为空！");
				return;
			}
			EnHandler.checkSwdjzh(txh,tsh,function(result){
				var rslt = Ext.decode(result);
				if(rslt&&!rslt.duplicate){
					enForm.getForm().submit({
						timeout: 10*60*1000,
						params:{cMode: cMode},
						success: function(form, action) {
							var obj = action.result;
			    		    if(obj.success){
								conditions = {};
				           		enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","企业信息已保存！");
								enWin.hide();
							}else{
								Ext.Msg.alert("提示","企业信息保存失败!");
							}
		    		    },
		    		    failure: function(form,action){
		    		    	var obj = action.result;
							if(obj&&obj.errors){
								Ext.Msg.alert("警告",obj.errors.msg);
							}
						}
					});
				}else{
					Ext.Msg.alert("提示","当前税号/机构代码已存在，不能重复！");
				}
			});
		}
	},{
		text : "取消",
		handler : function() {
			enWin.hide();
		}
	}]
});	
enWin.on("show",function(){
	if(cMode == "modify"){
		var records = enGrid.getSelectionModel().getSelections();
        if(!records||records.length<1){
			Ext.Msg.alert("提示","请先选择要修改的企业!");
			return;
		}
		var rd = records[0];
		var swdjzh = rd.get("swdjzh");
		EnHandler.getVirtualEn(swdjzh,function(data){
			var obj = Ext.decode(data);
			if(obj&&obj.result){
				var en = obj.en;
				if(en){
					enForm.getForm().findField("xh").setValue(en.XH);
					enForm.getForm().findField("mc").setValue(en.MC);
					enForm.getForm().findField("czfpbm_bm").setValue(en.CZFPBM_BM);
					enForm.getForm().findField("hybm_bm").setValue(en.HYBM_BM);
					enForm.getForm().findField("ztbm_bm").setValue(en.ZTBM_BM); 
					enForm.getForm().findField("jjxzbm_bm").setValue(en.JJXZBM_BM); 
					enForm.getForm().findField("czfp_tg").setValue(en.CZFPBM);
					enForm.getForm().findField("hy_tg").setValue(en.HYBM);
					enForm.getForm().findField("zt_tg").setValue(en.ZTBM); 
					enForm.getForm().findField("jjxz_tg").setValue(en.JJXZBM); 
					enForm.getForm().findField("swdjzh").setValue(en.SWDJZH); 
					enForm.getForm().findField("dz").setValue(en.DZ);  
					enForm.getForm().findField("fddbr").setValue(en.FDDBR);  
					enForm.getForm().findField("bgrq").setValue(en.BGRQ); 
				}
			}else{
				Ext.Msg.alert("警告","获取单位信息时发生错误！");
			}
		});
	}else{
		enForm.getForm().findField("xh").setValue("");
		enForm.getForm().findField("mc").setValue("");
		enForm.getForm().findField("czfpbm_bm").setValue("");
		enForm.getForm().findField("hybm_bm").setValue("");
		enForm.getForm().findField("ztbm_bm").setValue(""); 
		enForm.getForm().findField("jjxzbm_bm").setValue("");
		enForm.getForm().findField("czfp_tg").setValue("");
		enForm.getForm().findField("hy_tg").setValue("");
		enForm.getForm().findField("zt_tg").setValue(""); 
		enForm.getForm().findField("jjxz_tg").setValue("");
		enForm.getForm().findField("swdjzh").setValue(""); 
		enForm.getForm().findField("dz").setValue("");  
		enForm.getForm().findField("fddbr").setValue("");  
		enForm.getForm().findField("bgrq").setValue(""); 
	}
});
/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[enGrid]
	});
	enDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>