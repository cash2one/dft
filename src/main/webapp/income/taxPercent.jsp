<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
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
	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	String today = sdf.format(new Date());
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
<script type="text/javascript"><!--

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
var cSwdjzh = "";
var cDzdah = "0";
var cQynm = "0";
var pMc = "";
var treeWin;
function showTreeWin(table,cVal,callback){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn: CheckHandler.getBmCodesTree,
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
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
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
	    },{
	        header: "当前/最近规则",
	        dataIndex: 'rule',
	        width: 250,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业内码",
	        dataIndex: 'qynm',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "电子档案号",
	        dataIndex: 'dzdah',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'xh', type: 'int'},
	{name: 'swdjzh', type: 'string'},
	{name: 'qynm', type: 'string'},
	{name: 'dzdah', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'dz', type: 'string'},
	{name: 'czfpbm_bm', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'inuse', type: 'int'},
	{name: 'rule',type: 'string'}
]);
var fcEnDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: IncomeHandler.getPRuleEns,
		paramOrder: ['start','limit'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'swdjzh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
var view = new Ext.grid.GridView({
	getRowClass : function(r, rowIndex){
		if(r.get("inuse")!=1){
			return "invalid";
		}
	}
});
var fcEnGrid = new Ext.grid.GridPanel({
		title:'税额分成',
		store: fcEnDs,
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
			text: '新增分成企业',
			iconCls: 'add',
            handler : function(){
            	enWin.show();
			}
		},{
			text: '分成规则详情',
			iconCls: 'details',
            handler : function(){
				var records = fcEnGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择企业!");
					return;
				}
				cSwdjzh = records[0].get("swdjzh");
				cDzdah = records[0].get("dzdah");
				cQynm = records[0].get("qynm");
            	rulesWin.show();
			}
		},{
			text: '删除分成企业',
			iconCls: 'remove',
            handler : function(){
				var records = fcEnGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除规则的企业!");
					return;
				}
		        for(var i=0;i<records.length;i++){
					var qybj = records[i].get("inuse");
					if(qybj==1){
						Ext.Msg.alert("提示","该企业有启用的规则，请先确保停用该企业的所有规则，再删除!");
						return;
					}
				}
		        Ext.MessageBox.confirm('确认', '本操作会删除该企业的所有分成规则，但不会删除登记信息，是否执行?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var dzdahs = new Array();
						var qynms = new Array();
						for(var i=0;i<records.length;i++){
							var d = records[i].get("dzdah");
							var q = records[i].get("qynm");
							dzdahs.push(d);
							qynms.push(q);
						}
						IncomeHandler.deletePRulesOfEn(qynms.join(),dzdahs.join(),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
								fcEnDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中企业的所有规则已删除！");
							}else{
								Ext.Msg.alert("提示","企业分成规则删除操作时发生错误，删除失败!");
							}
						});	
					}
				});
			}
		}],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: fcEnDs,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
/****************************某企业的规则详情*************************************/
var xqssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
xqssm.handleMouseDown = Ext.emptyFn;
var xqcm = new Ext.grid.ColumnModel({
	columns: [
		xqssm,
		new Ext.grid.RowNumberer(),
		{
	        header: "内容",
	        dataIndex: 'description',
	        width: 180,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "起始时间",
	        dataIndex: 'begindate',
	        width: 70,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "终止时间",
	        dataIndex: 'enddate',
	        width: 70,
	        align: 'left',
	        renderer: renderFoo
	    },{
	    	header: "启用/禁用",
	    	width: 80,
	    	renderer : function(v,p,r){
		    	var rStr = "<INPUT type='button' value='"+(r.get("qybj")==1?"禁用":"启用");
		    	rStr += "' onclick=toggleQybj("+r.get("ruleid")+","+r.get("qybj")+")>";
		    	return rStr;
	    	}
	    }
	],
	defaultSortable: false
});
var xqRecord = Ext.data.Record.create([  
	{name: 'description', type: 'string'},
	{name: 'qybj', type: 'int'},
	{name: 'ruleid', type: 'int'},
	{name: 'begindate', type: 'int'},
	{name: 'enddate', type: 'int'}
]);
var xqDs = new Ext.data.GroupingStore({
	proxy: new Ext.data.DirectProxy({
		directFn: IncomeHandler.getPRulesOfEn,
		paramOrder: ['qynm','dzdah'],
		paramsAsHash: false
	}),
	reader: new Ext.data.JsonReader({},xqRecord)
});
xqDs.on("beforeload",function(){
	xqDs.baseParams.qynm=cQynm;
	xqDs.baseParams.dzdah=cDzdah;
});
var xqGrid = new Ext.grid.GridPanel({
	title:'',
	store: xqDs,
	cm: xqcm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view: new Ext.grid.GridView({}),
	selModel: xqssm,
	stripeRows: true,
	tbar: [
	{
        text: '增加新规则',
        iconCls: 'add',
        handler : function(){
			dtOfRuleWin.show();	
        }
    },{
        text: '删除规则',
        iconCls: 'remove',
        handler : function(){
	    	var records = xqGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的规则!");
				return;
			}
	        for(var i=0;i<records.length;i++){
				var qybj = records[i].get("qybj");
				if(qybj==1){
					Ext.Msg.alert("提示","不能删除启用的规则，请先停用规则，再删除!");
					return;
				}
			}
	        Ext.MessageBox.confirm('确认', '是否确定要删除选中的规则?', function(btn){
				if(btn == 'yes') {// 选中了是按钮
					var rids = new Array();
					for(var i=0;i<records.length;i++){
						var rid = records[i].get("ruleid");
						rids.push(rid);
					}
					IncomeHandler.deletePRulesByID(rids.join(),function(data){
						var obj = Ext.decode(data);
						if(obj&&obj.result){
							xqDs.load();
							Ext.Msg.alert("提示","选中的规则已删除！");
						}else{
							Ext.Msg.alert("提示","分成规则删除操作时发生错误，删除失败!");
						}
					});	
				}
			});	
        }
    }]
});
var rulesWin = new Ext.Window({
	id : 'rulesWin',
	title : '企业分成规则',
	items : [xqGrid],
	layout : 'fit',
	width : 480,
	height : 360,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "关闭",
		handler : function() {
			rulesWin.hide();
		}
	}]
});	
rulesWin.on("show",function(){
	xqDs.load();
});
function toggleQybj(rid,qybj){
	IncomeHandler.toggleRuleQybj(rid,qybj,function(data){
		var obj = Ext.decode(data);
		if(obj&&obj.result){
			Ext.Msg.alert("提示","规则已"+(qybj==1?"禁用！":"启用！"));
			xqDs.load();
			fcEnGrid.reload();
		}else{
			Ext.Msg.alert("提示",(qybj==1?"禁用":"启用")+"规则时发生错误:"+obj.info);
		}
	});	
}
/*************************查找企业*********************************/
var enssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
enssm.handleMouseDown = Ext.emptyFn;
var encm = new Ext.grid.ColumnModel({
	columns: [
		enssm,
	    {
	        header: "税号",
	        dataIndex: 'swdjzh',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "企业名称",
	        dataIndex: 'mc',
	        width: 150,
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
	    },{
	        header: "企业内码",
	        dataIndex: 'qynm',
	        width: 150,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "电子档案号",
	        dataIndex: 'dzdah',
	        width: 120,
	        align: 'left',
	        renderer: renderFoo
	    }
	],
	defaultSortable: false
});
var enRecord = Ext.data.Record.create([  
	{name: 'xh', type: 'int'},
	{name: 'dzdah', type: 'string'},
	{name: 'qynm', type: 'string'},
	{name: 'swdjzh', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'dz', type: 'string'},
	{name: 'czfpbm', type: 'string'},
	{name: 'czfpbm_bm', type: 'string'},
	{name: 'hasrule', type:'int'}
]);
var enDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: IncomeHandler.getEnsToAddPRules,
		paramOrder: ['start','limit','paraMc'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'xh',
		root: 'rows',
		totalProperty: 'totalCount'
	}, enRecord)
});
enDs.on("beforeload",function(){
	enDs.baseParams.paraMc=pMc;
});
var enview = new Ext.grid.GridView();
var enGrid = new Ext.grid.GridPanel({
		title:'',
		store: enDs,
	    cm: encm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : enview,
		selModel: enssm,
		stripeRows: true,
		tbar: [
		{
			text:'企业名称：',
			xtype: 'label'
		},{
			xtype: 'textfield',
			id: 'paras',
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
function doConditions(value){
	pMc = value;
	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
}
var enWin = new Ext.Window({
	id : 'enWin',
	title : '企业列表',
	items : [enGrid],
	layout : 'fit',
	width : 560,
	height : 420,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var records = enGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要添加规则的企业！");
				return;
			}
			if(records[0].get("hasrule")==1){
				return;
			}
			cSwdjzh =records[0].get("swdjzh");
			cDzdah = records[0].get("dzdah");
			cQynm = records[0].get("qynm");
			enWin.hide();
			dtOfRuleWin.show();
		}
	},{
		text : "取消",
		handler : function() {
			enWin.hide();
		}
	}]
});	
enWin.on("show",function(){
	Ext.getCmp("paras").setValue("");
	enDs.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
});
/****************可编辑窗体，单个规则的明细内容。****************/
var cRdtRecord ;
function setPzDetailBm(tField,hField){
	return function(value){
		cRdtRecord.set(hField,value.id);
		cRdtRecord.set(tField,value.text);
	}
};
var czfp_tg_pz = new Ext.form.TriggerField({
	width:90,
	editable: false,
    id :'czfp_tg_pz',
    name: 'czfp_tg_pz',
    value:''
});
czfp_tg_pz.onTriggerClick=czfpPzFun;
function czfpPzFun(e){
	var cVal = cRdtRecord.get("czfpbm");
	showTreeWin("BM_CZFP",cVal,setPzDetailBm("czfpmc","czfpbm"));
}

var sz_tg_pz = new Ext.form.TriggerField({
	width:90,
	editable: false,
    id :'sz_tg_pz',
    name: 'sz_tg_pz',
    value:''
});
sz_tg_pz.onTriggerClick=szPzFun;
function szPzFun(e){
	var cVal = cRdtRecord.get("szbm");
	showTreeWin("BM_SZ",cVal,setPzDetailBm("szmc","szbm"));
}
var dtOfRuleForm = new Ext.FormPanel({
    id: 'dtOfRuleForm',
    border:false,
    frame:true,
    labelAlign: 'right',
	layout:'form',
    items: [
    	new Ext.form.Radio({ 
   			fieldLabel: '规则作用时间',
   			boxLabel: '全部',
   			id:'time_all',
   			inputValue : "0", 
   			name: 'validtime',
   			checked :true,
   			listeners : {
                check : function(checkbox, checked) {
                    if (checked) {
                        dtOfRuleForm.getForm().findField("enddate").disable();
                        dtOfRuleForm.getForm().findField("startdate").disable();
                    }
                }
            }
   		}),new Ext.form.Radio({ 
	    	//hideLabel : true,
			boxLabel: '指定时间段',
			id:'time_fix',
			inputValue : "1", 
			checked :false,
			name: 'validtime',
			listeners : {
	            check : function(checkbox, checked) {
	                if (checked) {
	                	dtOfRuleForm.getForm().findField("enddate").enable();
	                	dtOfRuleForm.getForm().findField("startdate").enable();
	                }
	            }
        	}
		}),
   		{
	       	layout:'column',
	       	frame:false,
	        border:false,
	       	items:[
	       	{
	       		layout:'form',
				columnWidth: .5,
				labelWidth :80,
				frame:false,
				border:false,
				items:[
					new Ext.form.DateField({ 
						fieldLabel: '起始日期',
						width:100,
						disabled :true,
						allowBlank: true,
						value: '<%=today%>' ,
						format: 'Ymd',
						name: 'startdate'
					})
				]
	        },
	        {
	            layout:'form',
				columnWidth: .5,
				labelWidth :80,
				frame:false,
				border:false,
				items:[
				    new Ext.form.DateField({ 
						fieldLabel: '终止日期',
						width:100,
						disabled :true,
						allowBlank: true,
						value: '<%=today%>' ,
						format: 'Ymd',
						name: 'enddate'
					})
				]
	        }]
    	}
	]
});
var checkmodel = new Ext.grid.CheckboxSelectionModel();
checkmodel.handleMouseDown = Ext.emptyFn;
var rdt_cm = new Ext.grid.ColumnModel({
	columns: [
		checkmodel,
	    {
	       header: "财政分片",
	       dataIndex: 'czfpmc',
	       width: 150,
	       align:'left',
	       editor: czfp_tg_pz 
	    }/*2014-10-08去掉税种，固定的写一个值-1
	    ,{
		       header: "税种",
		       dataIndex: 'szmc',
		       width: 90,
		       align:'left',
		       editor: sz_tg_pz 
		}*/,{
		    header: "税额比例（%）",
		    dataIndex: 'sebl',
		    width: 100,
		    align: 'right',
		    editor: new Ext.form.NumberField({selectOnFocus:true,maxLength:16,decimalPrecision:2}),
			renderer :function(v,p,r){
				return reg2Decimal(v,p,r);
			}
		}
	],
	defaultSortable: false
});
var rdt_Record = Ext.data.Record.create([   
       {name: 'czfpmc', type: 'string'},
       {name: 'czfpbm', type: 'string'},
       {name: 'szmc', type: 'string'},
       {name: 'szbm', type: 'string'},
       {name: 'sebl',type:'float'}
 ]);
var rdtDs = new Ext.data.Store({ 
	proxy: new Ext.data.DirectProxy({
		//directFn: EnHandler.getNewPzDetail,
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({},rdt_Record)
});
var dtOfRuleGrid = new Ext.grid.EditorGridPanel({
	title:'分成比例',
	store: rdtDs,
    cm: rdt_cm,
    frame:false,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    clicksToEdit:1,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
    selModel: checkmodel,
    tbar: [
    {
	    text: '增加行',
	    id:'addBtn',
	    iconCls: 'add',
	    handler : function(){
			var rdt = new rdt_Record({
	            czfpbm:"",
	            czfpmc:"",
	            szbm: "-1",
	            szmc: "",
	            sebl: 0
	        });
			var cc = rdtDs.getCount();
			dtOfRuleGrid.stopEditing();
			dtOfRuleGrid.getStore().insert(cc, rdt);
			dtOfRuleGrid.startEditing(cc, 0);
			dtOfRuleGrid.getStore().commitChanges();
	    }
	},{
	    text: '删除行',
	    id:'removeBtn',
	    iconCls: 'remove',
		handler :function(){
	        var records = dtOfRuleGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的行!");
				return;
			}	
			if(records){
				Ext.MessageBox.confirm('确认删除', '你真的要删除所选行吗?', 
			    	function(btn){
					    if(btn == 'yes') {// 选中了是按钮							    	    										 
							for(var rc=0;rc<records.length;rc++){						    	    	
								rdtDs.getModifiedRecords().remove(records[rc]);
								rdtDs.remove(records[rc]);
							}
						}
					}
				);
			}
	    }
	}
    ]
});
dtOfRuleGrid.on('beforeedit',function(e){ 
	cRdtRecord = e.record;
    return;   
}); 
var dtOfRuleWin = new Ext.Window({
	id : 'rdtWin',
	title : '规则明细',
	layout : 'border',
	width : 500,
	height : 420,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	items:[{	
	    id:'north',
	    region:'north',
	    height:120,
	    frame:true,
	    layout:'fit',
	   	title:"",
	   	items: dtOfRuleForm
	 },{  
	   	region:'center',  
	   	layout:'fit',  
	   	margins:'0 0 0 0',  
	   	items: dtOfRuleGrid  
	}],
	buttons : [
	{
		text : "确定",
		handler : function(){
			saveDetails();
		}
	},{
		text : "取消",
		handler : function() {
			dtOfRuleWin.hide();
		}
	}]
});	
dtOfRuleWin.on("show",function(){
	Ext.getCmp("time_all").setValue(true);
	dtOfRuleForm.getForm().findField("startdate").setValue('<%=today%>');
	dtOfRuleForm.getForm().findField("enddate").setValue('<%=today%>');
	rdtDs.removeAll();
});
	function saveDetails(){
		var cc = rdtDs.getCount();
		if(cc==0){
			Ext.Msg.alert('信息','请录入规则内容！');
			return;
		}
		var sebl = 0;
		var iniSZ = rdtDs.getAt(0).get("szbm");
		for(var i=0;i<cc;i++){
			var rc = rdtDs.getAt(i);
			if(rc.get("czfpbm")==null||rc.get("czfpbm")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的分片为空，请录入！");
				return;
			}
			if(rc.get("sebl")==null||rc.get("sebl")==""){
				Ext.Msg.alert('信息','第'+(i+1)+"行的分成比例为空，请录入！");
				return;
			}
			for(var j=0;j<cc;j++){
				var tr = rdtDs.getAt(j);
	    		if(i!=j&&tr.get("czfpbm") == rc.get("czfpbm")){
	    			Ext.Msg.alert('信息',"第"+(j+1)+"行与第"+(i+1)+"行的财政分片重复！");
	    			return;
	    		}
	    	}
	    	sebl += Number(rc.get("sebl"));
		}
		if(sebl!=100){
			Ext.Msg.alert('信息','税额比例的和不等于100！');
	    	return; 
	    }
	    var stdate="",edate="";
	    var d_sdate,d_edate;
		var val_stdate = dtOfRuleForm.getForm().findField("startdate").getValue();
		var val_edate = dtOfRuleForm.getForm().findField("enddate").getValue();
		if(val_stdate){
			d_sdate = new Date(val_stdate);
			stdate = d_sdate.format('Ymd');
		}else{
			Ext.Msg.alert('信息','请输入起始日期！');
	    	return; 
		}
		if(val_edate){
			d_edate = new Date(val_edate);
			edate = d_edate.format('Ymd');
		}else{
			Ext.Msg.alert('信息','请输入终止日期！');
	    	return; 
		}
		var v_timeAll = Ext.getCmp("time_all").getValue();
		var v_timeFix = Ext.getCmp("time_fix").getValue(); 
		var timeType = v_timeAll? "0":"1";
	    if(timeType=="0"&&d_sdate > d_edate){
	    	Ext.Msg.alert('信息',"起始日期不能大于终止日期！");
	    	return;
	    }
		Ext.MessageBox.confirm('确认保存', '确认要保存规则明细内容?', function(btn){
			if(btn == 'yes') {// 选中了是按钮
			    var rows=new Array();
			    rdtDs.each(function(rs){ 		
		   			var row=new Object();
		   			row.czfpbm=rs.get("czfpbm");
		   			row.szbm=rs.get("szbm");
		   			row.sebl=rs.get("sebl");
		   	    	rows.push(row);
		   	    });  	
			    var rules = Ext.util.JSON.encode(rows);
			    IncomeHandler.saveRuleDetails(cQynm,cDzdah,cSwdjzh,timeType,stdate,edate,rules,function(data){
					var obj = Ext.decode(data);
					if(!obj)return;
					if(obj.result){
						dtOfRuleWin.hide();
						Ext.Msg.alert("提示","规则保存成功!");
						fcEnDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
						xqDs.load();
					}else{
						Ext.Msg.alert("提示","规则保存失败!");
					}
				});
			}
		});	
	}

/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[fcEnGrid]
	});
	fcEnDs.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
</script>
</head>
<body>
</body>
</html>