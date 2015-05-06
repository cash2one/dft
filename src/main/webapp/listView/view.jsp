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
	String rptID = request.getParameter("rptID");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	Report rpt = TemplatesLoader.getTemplatesLoader().getReportTemplate(rptID);
	StringBuffer strHead = new StringBuffer("");
	StringBuffer strFoot = new StringBuffer("");
	boolean hasHead = false, hasFoot = false ,titleInHead = false;
	if(rpt!=null){
		if(rpt.getHead()!=null){
			hasHead = true;
			Head head = rpt.getHead();
			titleInHead = head.getTitleInHead()==1;
			if(head.getSubTitle()==null&&head.getTitleInHead()==1){//无副标题且标题在表头
				strHead.append("<p id='headTitle' class='>").append(head.getStyle()).append("'>").append(rpt.getTitle().getTitleExp()).append("</p>");
			}else{//有副标题
				SubTitle st = head.getSubTitle();
				strHead.append("<table width='100%' border='0' cellspacing='0' cellpadding='0'><tr><td colspan=3 align='center'>");
				strHead.append("<span id='headTitle' class='").append(head.getStyle()).append("'>").append(rpt.getTitle().getTitleExp());
				strHead.append("</span></td></tr><tr><td width='33%' align='left><span id='headSLeft' class='").append(st.getlStyle()).append("'>");
				strHead.append(StringUtils.isEmpty(st.getLeftExp())?"":st.getLeftExp());
				strHead.append("</span></td><td width='33%' align='center'><span id='headSCenter' class='").append(st.getcStyle()).append("'>");
				strHead.append(StringUtils.isEmpty(st.getCenterExp())?"":st.getCenterExp());
				strHead.append("</span></td><td width='33%' align='right'><span id='headSRight' class='>").append(st.getrStyle()).append("'>");
				strHead.append(StringUtils.isEmpty(st.getRightExp())?"":st.getRightExp()).append("</span></td></tr></table>");
			}
		}
		if(rpt.getFoot()!=null){
			hasFoot = true;
			Foot foot = rpt.getFoot();
			strFoot.append("<table width='100%' border=0' cellspacing='0' cellpadding='0'><tr height='").append(foot.getHeight());
			strFoot.append("'><td width='33%' align='left' ><span id='footLeft' class='").append(foot.getlStyle()).append("'>");
			strFoot.append(StringUtils.isEmpty(foot.getLeftExp())?"":foot.getLeftExp());
			strFoot.append("</span></td><td width='33%' align='center'><span id='footCenter' class='").append(foot.getcStyle()).append("'>");
			strFoot.append(StringUtils.isEmpty(foot.getCenterExp())?"":foot.getCenterExp());
			strFoot.append("</span></td><td width='33%' align='right'><span id='footRight' class='").append(foot.getrStyle()).append("'>");
			strFoot.append(StringUtils.isEmpty(foot.getRightExp())?"":foot.getRightExp()).append("</span></td></tr></table>");
		}
	}
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
<script type = "text/javascript">
var rptID="<%=rptID%>";
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/LockingGridView.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/query.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ParamTreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/DynamicGrid.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/render.js"></script>
<style type="text/css">
.rptGrid .x-grid3-row {
	border-bottom : 1px none;
}
</style>
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
Ext.Ajax.timeout = 120000;
App.ux.defaultPageSize=<%=cg.getString("pageSize","40")%>;
var RPTROOT="<%=request.getContextPath()%>/listView/view.jsp?isLink=1";
var cFltParam = '';
var cOperator = '';
var cFltRecord ;
//var cMetaDataLoaded;
var cLoadUserMeata = true;
var cLoadDefaultMeata = false;
var cQPid = -1;
var cQPRecord;
var defaultUnit = "<%=rpt.getDefaultUnit()%>";
var showUnit = <%=rpt.getMultiUnit()%>;
var condition ={};
var titleInHead = <%=titleInHead%>;
/********高级筛选中公共的combobox，作为交互renderType=1时的公共编辑器。*****/
var commonCbRcd = Ext.data.Record.create([
    {name : 'bm',type : 'string'}, 
    {name : 'name',type : 'string'}
]);
var cbStore = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
    	directFn : QueryHandler.getOptionItems,
    	paramOrder: ['rptID','pName','affectedBy'],
		paramsAsHash: false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'bm'
    }, commonCbRcd)
});

var commonCombo = new Ext.form.ComboBox({ 
	displayField : 'name',
	valueField : 'bm',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	emptyText : '选择...',
	selectOnFocus : true,
	editable : false,
	store : cbStore,
	destroy : Ext.emptyFn
});
/***金额单位切换***********************************************/
var unCbRcd = Ext.data.Record.create([
    {name : 'id',type : 'string'}, 
    {name : 'name',type : 'string'},
    {name : 'renderFun',type : 'string'}
]);
var unStore = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
    	directFn : QueryHandler.getUnits,
    	paramOrder: ['rptID'],
		paramsAsHash: false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'id'
    },unCbRcd)
});
unStore.on("beforeload",function(){
	unStore.baseParams.rptID = rptID;
});
unStore.on("load",function(){
	for(var i=0;i<unStore.getCount();i++){
		var urd = unStore.getAt(i);
		if(urd.get("id")==defaultUnit){
			unitsCombo.setValue(urd.data.id);
			break;
		}
	}
});
unStore.load();
var unitsCombo = new Ext.form.ComboBox({ 
	displayField : 'name',
	valueField : 'id',
	typeAhead : true,
	width: 105,
	id: 'unitsCombo',
	mode : 'local',
	triggerAction : 'all',
	emptyText : '选择...',
	selectOnFocus : true,
	editable : false,
	store : unStore,
	destroy : Ext.emptyFn,
	listeners:{
		select: function(combo, record, index) {
			var uf = unStore.getAt(index).get("renderFun");
			var renderer = App.rpt.Renders[uf];
			var cols = grid.getColumnModel().config;
			for(var i=0;i<cols.length;i++){
    			if(cols[i].isMultiUnit&&cols[i].isMultiUnit>0){
					cols[i].renderer=renderer;
    			}
			}
			grid.view.refresh();
		}
	}
});
/********高级筛选中公共的树窗体构件+triggerField，作为交互renderType=2时的公共编辑器***/
var commonTrigger = new Ext.form.TriggerField({
	fieldLabel:'',
	editable: false,
	destroy : Ext.emptyFn
});
var fltTreeSingleWin;
var fltTreeMultiWin;
var fltTreeWin;
commonTriggerClick=function(){
	var tmpfld = fltCdtDs.getById(cFltParam);
	var tmpAffectby = tmpfld.get("affectedByParas");
	var affectedBy = getParaValsAffect(tmpAffectby);
	var tmpPost =new Object();
	tmpPost.macroParams = affectedBy||{};
	if(cOperator=='in'){
		if(!fltTreeMultiWin){
			fltTreeMultiWin = new App.widget.ParamTreeWindow({
				directFn: QueryHandler.getOptionItemsOfTree,
				checkModel : 'multiple',
				treeId: 'm_'+ cFltParam,
				rptID: rptID,
				onlyLeafCheckable: tmpfld.get("leafOnly"),
				codeTable: cFltParam,
				affectedBy: Ext.encode(tmpPost),
				defaultValue: '',
				canSetNull: true
			});
		}
		fltTreeWin = fltTreeMultiWin;
	}else{
		if(!fltTreeSingleWin){
			fltTreeSingleWin = new App.widget.ParamTreeWindow({
				directFn: QueryHandler.getOptionItemsOfTree,
				checkModel : 'single',
				treeId: 's_'+ cFltParam,
				rptID: rptID,
				onlyLeafCheckable: tmpfld.get("leafOnly"),
				codeTable: cFltParam,
				affectedBy: affectedBy,
				defaultValue: '',
				canSetNull: true
			});
		}
		fltTreeWin = fltTreeSingleWin;
	}
	var p = {rptID: rptID,pName: cFltParam, affectedBy: affectedBy||""};
	fltTreeWin.onSelect = function(value){
		if(!value)return;
		cFltRecord.set("fldValue",value.text); 
		cFltRecord.set("hValue",value.id);
	};
	fltTreeWin.setTreeParams(p);
	fltTreeWin.refreshTree();
	fltTreeWin.show();
};
 
/******可编辑grid，用于高级筛选中编辑条件****************/
var fltCdtRecord = Ext.data.Record.create([
    {name: 'name',type : 'string'}, 
    {name: 'desc',type : 'string'},
    {name: 'renderType', type: 'int' },
    {name: 'dateFormat', type: 'string' },
    {name: 'affectedByParas', type: 'string' },
    {name: 'filterFld',type : 'string'}, 
    {name: 'dataType', type: 'int' },
    {name: 'leafOnly', type: 'int' }
]);
var fltCdtDs = new Ext.data.Store({
    proxy : new Ext.data.DirectProxy({
    	directFn : QueryHandler.getFieldsOfComplexFilter,
    	paramOrder: ['rptID'],
    	paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'name'
    }, fltCdtRecord)
});
fltCdtDs.on("beforeload",function(){
	fltCdtDs.baseParams.rptID = rptID;
});
fltCdtDs.load();
var cb_flds2filter = new Ext.form.ComboBox({
	displayField : 'desc',
	valueField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	emptyText : '字段',
	selectOnFocus : true,
	editable : false,
	store : fltCdtDs
});
function resetEditor(record){
	var pName = record.get("name");
	var rType = record.get("renderType");
	var dType = record.get("dataType");
	var df = record.get("dateFormat")||"Y-m-d";
	var edtCb = null;
	if(rType == 1){
		cbStore.baseParams.pName = pName;
		cbStore.baseParams.rptID = rptID;
		if(record.get("affectedByParas")){
			var aps = record.get("affectedByParas");
			var objVals = getParaValsAffect(aps)
			var tmpPost =new Object();
			tmpPost.macroParams = objVals||{};
			cbStore.baseParams.affectedBy = Ext.encode(tmpPost);
		}else{
			cbStore.baseParams.affectedBy = "";
		}
		cbStore.baseParams.selectedVals="";
		cbStore.load();
		var commonCombo = new Ext.form.ComboBox({ 
			displayField : 'name',
			valueField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			emptyText : '选择...',
			selectOnFocus : true,
			editable : false,
			store : cbStore,
			listeners: {
				select: function(combo, record, index) {
					var hv = record.get("bm");
					cFltRecord.set("hValue",hv);
				}
			}
		});
		edtCb = commonCombo;
	}else if(rType == 2){
		var commonTrigger = new Ext.form.TriggerField({
			fieldLabel:'',
			editable: false
		});
		edtCb =  commonTrigger ;
		commonTrigger.onTriggerClick = commonTriggerClick;
	}else if(rType == 3){
		var commonDateFld = new Ext.form.TextField({
			format: df,
			value: new Date().format(df)
		});
		edtCb = new Ext.grid.GridEditor(commonDateFld);
	}else {
		if(dType == 1){
			var commontIntFld = new Ext.form.NumberField({selectOnFocus:true,maxLength:9,decimalPrecision:0});
			edtCb = new Ext.grid.GridEditor(commontIntFld);
		}else if(dType==2){
			var commontDoubleFld = new Ext.form.NumberField({selectOnFocus:true,maxLength:16,decimalPrecision:4});
			edtCb = new Ext.grid.GridEditor(commontDoubleFld);
		}else{
			var commonTextFld = new Ext.form.TextField({selectOnFocus : true,maxLength : 200});
			edtCb = new Ext.grid.GridEditor(commonTextFld);
		}
	}
	cdtCm.setEditor(3,edtCb); 
}
//参数联动时，解析“被动”参数所需要的“主动”参数的值。
function getParaValsAffect(affectParas){
	if(!affectParas){
		return;
	}
	var arrPs = affectParas.split(",");
	var pvals = new Object();
	for(var i = 0;i<arrPs.length;i++){
		var p = arrPs[i];
		var val = "";
		var conRow = cdtStore.getById(p);
		var fltFld = fltCdtDs.getById(p);
		if(fltFld&&fltFld.get("renderType")==2){
			val=conRow?"":conRow.get("hValue");
		}else if(fltFld){
			val = conRow?"":conRow.get("fldValue");
		}else{
			val="";
		}
		pvals[p]=val;
	}
	return pvals;
}
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
			var index = fltCdtDs.find('name', v);
			var cbRec = fltCdtDs.getAt(index);
			var newval= v;
			if (cbRec) {
				newval= cbRec.data.desc;
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
	},{
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
		idProperty : 'fld'
	}, cdtRecord)
});

var cdtGrid = new Ext.grid.EditorGridPanel({
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
	        	fld: fltCdtDs.getCount()>0?fltCdtDs.getAt(0).get("name"):'',
	        	ops: 'equ',
	        	fldValue: '',
	        	connection: 'empty',
	        	hValue :''
	        });
	        cdtGrid.stopEditing();
	        cdtStore.insert(cdtStore.getCount(), cdt);
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
cdtGrid.on('beforeedit',function(e){ 
	cFltRecord = e.record;
	cFltParam = e.record.get("fld");
	cOperator =  e.record.get("ops");
	if(e.field=="fldValue"){
		var fRecord= fltCdtDs.getById(cFltParam);
		resetEditor(fRecord);
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

var fltWin = new Ext.Window({
	width : 550,
	height : 420,
	title : "筛选条件",
	layout : 'fit',
	closeAction :"hide",
	items : [cdtGrid],
	buttons : [
	{
		text : "载入查询",
		handler : function() {
			buildCondition();
			grid.getStore().load({params:{rptID:'<%=rptID%>',start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	},{
		text: "清空",
	    handler: function(){
			cdtGrid.getStore().removeAll();
	    }
	},{
		text : "关闭",
		handler : function() {
			fltWin.hide();
		}
	}],
	buttonAlign : "center"
});
function buildCondition(){
	//附加条件分两类，一类用于筛选，一类是用于宏替换参数。
	var dParams = new Object();
	//筛选。1、来自高级筛选窗体grid
	var cdts = new Object();
	var fldNames = new Array();
	var fldValues = new Array();
	var relations = new Array();
	var connections = new Array();
	var rds = cdtStore;
	for (var i = 0; i < rds.getCount(); i++) {
		var rs = rds.getAt(i);
		var fields = rs.data;
		if(!fields){
			continue;
		}
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
		var ffrd = fltCdtDs.getById(fields["fld"]);
		if(!ffrd){
			continue;
		}
		fldNames.push(ffrd.get("filterFld"));
		fldValues.push(value);
		relations.push(fields["ops"]);
		connections.push(connection);
	}
	var fltIndex = connections.length-1; 
	//筛选。2、来自工具条
	var tbItems = grid.getTopToolbar().items;
	for(var i = 0;i<tbItems.length;i++){
		var it = tbItems.item(i);
		if(!it.filterFld||it.filterFld==""){
			continue;
		}
		var val ="";
		if(it.xtype=="textfield"||it.xtype=="datefield"){
			val =it.getValue();
		}else if(it.xtype=="trigger"||it.xtype=="combo"){
			val =Ext.getCmp("q_h_"+it.id.substring(2)).getValue();
		}else{
			continue;
		}
		if(!val||val==""){
			continue;
		}
		fldNames.push(it.filterFld);
		fldValues.push(val);
		relations.push(it.vop?it.vop:"equ");
		connections.push("_and");
	}
	if(connections.length>fltIndex){
		connections[fltIndex]="_and";
	}
	if(connections.length>0){
		connections[connections.length-1]="empty"; 
	}
	cdts.fldNames=fldNames.join();
	cdts.fldValues=fldValues.join();
	cdts.relations=relations.join();
	cdts.connections=connections.join();
	dParams.filter = cdts;
	//工具条中，用于宏替换的
	var mps = new Object();
	for(var i = 0;i<tbItems.length;i++){
		var it = tbItems.item(i);
		if(it.filterFld&&it.filterFld!=""){
			continue;
		}
		var val ="";
		if(it.xtype=="textfield"||it.xtype=="datefield"){
			val =it.getValue();
		}else if(it.xtype=="trigger"||it.xtype=="combo"){
			val =Ext.getCmp("q_h_"+it.id.substring(2)).getValue();
			var textVal= Ext.getCmp("q_"+it.id.substring(2)).getRawValue();
			mps[it.id.substring(2)+"_desc"]=textVal;
		}else{
			continue;
		}
		mps[it.id.substring(2)]=val;
	}
	//窗体中，用于宏替换
	if(paramForm){
		var fits =paramForm.items;
		for(var i=0;i<fits.getCount();i++){
			var f = fits.itemAt(i);
			var val ="";
			if(!f){
				continue;
			}
			if(f.xtype=="textfield"||f.xtype=="datefield"){
				val =f.getValue();
			}else if(f.xtype=="trigger"||f.xtype=="combo"){
				val =Ext.getCmp("q_h_"+f.id.substring(2)).getValue();
				var textVal= Ext.getCmp("q_"+f.id.substring(2)).getValue();
				mps[f.id.substring(2)+"_desc"]=textVal;
			}else{
				continue;
			}
			mps[f.id.substring(2)]=val;
		}
	}
	dParams.macroParams = mps;
	condition=dParams;
}
//工具条中的triggerField的触发函数
var qpTreeSingleWin;
var qpTreeMultiWin;
var qpTreeCascWin;
var qpTreeWin;
function showQparamTree(cQueryParam,cMulti,cOnlyLeaf){
	if(cMulti==1){
		if(!qpTreeMultiWin){
			qpTreeMultiWin = new App.widget.ParamTreeWindow({
				directFn: QueryHandler.getOptionItemsOfTree,
				checkModel : 'multiple',
				treeId: 'm_'+ cQueryParam,
				rptID: rptID,
				onlyLeafCheckable: cOnlyLeaf,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeMultiWin;
	}else if(cMulti==2){
		if(!qpTreeCascWin){
			qpTreeCascWin = new App.widget.ParamTreeWindow({
				directFn: QueryHandler.getOptionItemsOfTree,
				checkModel : 'cascade',
				treeId: 'c_'+ cQueryParam,
				rptID: rptID,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeCascWin;
	}else{
		if(!qpTreeSingleWin){
			qpTreeSingleWin = new App.widget.ParamTreeWindow({
				directFn: QueryHandler.getOptionItemsOfTree,
				checkModel : 'single',
				treeId: 's_'+ cQueryParam,
				rptID: rptID,
				onlyLeafCheckable: cOnlyLeaf,
				codeTable: cQueryParam,
				defaultValue: '',
				canSetNull: true
			});
		}
		qpTreeWin = qpTreeSingleWin;
	}
	var cmpPara = Ext.getCmp("q_"+cQueryParam);
	var tmpPost={},mps = {};
	if(cmpPara){
		var aBy = cmpPara.affectedBy;
		if(aBy){
			var aparas = aBy.split(",");
			mps = new Object();
			for(var i = 0;i<aparas.length;i++){
				var tp = aparas[i];
				var tcmp = Ext.getCmp("q_h_"+tp);
				if(tcmp){
					var val =tcmp.getValue();
					mps[aparas]=val;
				}
			}
			tmpPost.macroParams = mps;
		}
	}
	var p = {rptID: rptID,pName: cQueryParam, affectedBy: Ext.encode(tmpPost)};
	qpTreeWin.onSelect = function(value){
		if(!value)return;
		Ext.getCmp("q_h_"+cQueryParam).setValue(value.id); 
		Ext.getCmp("q_"+cQueryParam).setValue(value.text); 
	};
	qpTreeWin.setTreeParams(p);
	qpTreeWin.refreshTree();
	qpTreeWin.show();
};
function showFilter(){
	fltWin.show();
}
function showParamsWin(){
	paramWin.show();
}
var paramForm;
var paramWin = new Ext.Window({
	width : 450,
	height : 320,
	title : "参数",
	layout : 'fit',
	closeAction :"hide",
	items : [
	],
	buttons : [
	{
		text : "载入查询",
		handler : function() {
			buildCondition();
			grid.getStore().load({params:{rptID:'<%=rptID%>',start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	},{
		text : "关闭",
		handler : function() {
			paramWin.hide();
		}
	}],
	buttonAlign : "center"
});
function saveQueryPlan(qpid,onlyClob){
	var qpname = sQForm.getForm().findField("qpname").getValue();
	qpname = qpname?qpname:"";
	var remark = sQForm.getForm().findField("remark").getValue();
	remark = remark?remark:"";
	var isdefault = sQForm.getForm().findField("isdefault").getValue();
	isdefault = isdefault?isdefault:0;
	var cfgs = grid.getColumnModel().config;
	var cols = [];
	for(var i = 0; i<cfgs.length;i++){
		var c = cfgs[i];
		cols.push(Ext.copyTo({},c,'id,align,dataIndex,dataType,header,hidden,hideable,sortable,tooltip,width,isMultiUnit,renderer,isLink,linkParams,target,linkTo,isGroup'));
	}
	buildCondition();
	var tmpCdts = Ext.apply({},condition);
	var sinfo = grid.getStore().getSortState();
	tmpCdts.sort=sinfo&&sinfo.field?sinfo.field:"";
	tmpCdts.dir=sinfo&&sinfo.direction?sinfo.direction:"";
	tmpCdts.moneyUnit = showUnit>0?unitsCombo.getValue():"";
	var setting = {
		cols : cols,
		condition: Ext.encode(tmpCdts)
	}
	Ext.Msg.wait("正在保存...");
	QueryHandler.saveQueryPlan(Number(qpid),onlyClob,rptID,qpname,remark,Number(isdefault),Ext.encode(setting),function(data){
		Ext.Msg.hide();
		var obj = Ext.decode(data);
		if(obj){
			if(obj.result){
				cQPid = obj.result;
				if(cQPRecord){
					qpDs.load();
				}
				Ext.Msg.alert("信息","当前的查询方案已保存");
			}else{
				Ext.Msg.alert("错误","保存查询方案时发生错误！");
			}
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
		    if(titleInHead&&document.getElementById('headTitle')){
		    	opt.title = document.getElementById('headTitle').innerHTML;
			}else{
				opt.title =grid.title;
			}
		    var subTitle = document.getElementById('headSLeft')?document.getElementById('headSLeft').innerHTML:"";
		    subTitle += document.getElementById('headSCenter')?document.getElementById('headSCenter').innerHTML:"";
		    subTitle += document.getElementById('headSRight')?document.getElementById('headSRight').innerHTML:"";
		    var foot = document.getElementById('footLeft')?document.getElementById('footLeft').innerHTML:"";
		    foot += document.getElementById('footCenter')?document.getElementById('footCenter').innerHTML:"";
		    foot += document.getElementById('footRight')?document.getElementById('footRight').innerHTML:"";
		    opt.subTitle = subTitle;
		    opt.foot = foot;
	        grid.exportExcel(opt);
	        winFormat.hide(); 
		}
    },{
        text: '取消',
        handler:function(){
	        winFormat.hide(); 
	    }
    }]
});
var sQForm = new Ext.FormPanel({
    frame: true,
    labelAlign: 'right',
    labelWidth : 60,
    width: 300,
    height: 250,
    layout: 'form', 
    items: [
	{
		fieldLabel:'名称',
		name: 'qpname', 
		xtype:'textfield', 
		width: 180,
		maxLenth: 50
	},{
	    fieldLabel: '说明',
	    name: "remark",
	    xtype:'textarea',   
	    width: 180,
		height: 80,
		maxLenth: 200
	},{ 
	    name: 'isdefault',
	    xtype: 'hidden',
	    value: 0
	},{
	    fieldLabel: '默认方案',
	    name :'ckdefault',
    	checked: false,
	    xtype:'checkbox',   
	    width: 120,
	    listeners:{
		    check: function(obj,checked){
				if(checked){
					sQForm.getForm().findField("isdefault").setValue(1);
				}else{
					sQForm.getForm().findField("isdefault").setValue(0);
				}
		    }
	    }
	}
	]
}); 
var saveQWin = new Ext.Window({
    title: '保存查询方案',
    width: 300,
    height: 250,
    layout: 'fit',
    buttonAlign:'center',
    items: sQForm,
    buttons: [{
        text: '确定',
        handler:function(){
        	var qpid = -1;
        	if(cQPRecord){
        		qpid = cQPRecord.get("id");
        	}
    		saveQueryPlan(qpid,false);
    		saveQWin.hide(); 
		}
    },{
        text: '取消',
        handler:function(){
    		saveQWin.hide(); 
	    }
    }]
});
saveQWin.on("show",function(){
	if(cQPRecord){
		sQForm.getForm().findField("qpname").setValue(cQPRecord.get("name"));
		sQForm.getForm().findField("remark").setValue(cQPRecord.get("remark"));
		sQForm.getForm().findField("ckdefault").setValue(cQPRecord.get("isdefault"));
		sQForm.getForm().findField("isdefault").setValue(cQPRecord.get("isdefault"));
	}else{
		sQForm.getForm().findField("qpname").setValue("");
		sQForm.getForm().findField("remark").setValue("");
		sQForm.getForm().findField("ckdefault").setValue(false);
		sQForm.getForm().findField("isdefault").setValue(0);
	}
});
var qpssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
qpssm.handleMouseDown = Ext.emptyFn;
var qpcm = new Ext.grid.ColumnModel({
	columns: [
		qpssm,
	    {
	        header: "方案名称",
	        dataIndex: 'name',
	        width: 190,
	        align: 'left',
	        renderer: renderFoo
	    },{
	        header: "方案说明",
	        dataIndex: 'remark',
	        width: 200,
	        align: 'left',
	        renderer: renderFoo    
	    },{
	        header: "保存时间",
	        dataIndex: 'savetime',
	        width: 150,
	        align: 'left'
	    }
	],
	defaultSortable: false
});
var qpRecord = Ext.data.Record.create([  
	{name: 'id', type: 'int'},
	{name: 'name', type: 'string'},
	{name: 'remark', type: 'string'},
	{name: 'savetime', type: 'string'},
	{name: 'isdefault', type: 'int'}
]);
var qpDs = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: QueryHandler.getQueryPlans,
		paramOrder: ['rptID'],
		paramsAsHash: false
	}), 
	reader: new Ext.data.JsonReader({}, qpRecord)
});
qpDs.on("beforeload",function(){
	qpDs.baseParams.rptID=rptID;
});
var qplanGrid = new Ext.grid.GridPanel({
	store: qpDs,
	cm: qpcm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : new Ext.grid.GridView(),
	selModel: qpssm,
	stripeRows: true,
	tbar: [
	{
        text: '载入方案',
        iconCls: 'loadSchema',
        handler : function(){
        	//选择一个查询方案
        	var records = qplanGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要载入的查询方案!");
				return;
			}
	        Ext.MessageBox.confirm('确认', '是否载入选中的查询方案?', function(btn){
				if(btn == 'yes') {// 选中了是按钮
					cQPid = records[0].get("id");
					grid.metaDataLoaded = false;
					cLoadDefaultMeata = false;
					cLoadUserMeata = true;
					//QueryHandler.loadQueryPlans(pid,function(){
					grid.getStore().load({params:{rptID:'<%=rptID%>',start:0, limit:<%=cg.getString("pageSize","40")%>}});
					//});
				}
			});
        }
    },{
    	text: '修改',
        iconCls: 'edit',
        handler : function(){
	    	var records = qplanGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要修改的查询方案!");
				return;
			}
	        cQPRecord=records[0];
	        saveQWin.show();
    	}
    },{
    	text: '删除方案',
        iconCls: 'remove',
        handler : function(){
	    	var records = qplanGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的查询方案!");
				return;
			}
	        Ext.MessageBox.confirm('确认', '是否删除选中的查询方案?', function(btn){
				if(btn == 'yes') {// 选中了是按钮
					var qpid = records[0].get("id");
					Ext.Msg.wait("正在删除...");
					QueryHandler.deleteQueryPlan(Number(qpid),function(data){
						Ext.Msg.hide();
						var obj = Ext.decode(data);
						if(obj){
							if(obj.result){
								cQPRecord=null;
								qpDs.load();
								Ext.Msg.alert("信息","当前的查询方案已删除");
							}else{
								Ext.Msg.alert("错误","保存查询方案时发生错误！");
							}
						}
					});
				}
	        });
    	}
    },{
    	text: '恢复原始设置',
        iconCls: 'loadDefault',
        handler : function(){
    		grid.metaDataLoaded =  false;
			cLoadDefaultMeata = true;
			cLoadUserMeata = false;
			QPid = -1;
			grid.getStore().load({params:{rptID:'<%=rptID%>',start:0, limit:<%=cg.getString("pageSize","40")%>}});
    	}
    }]
});
var loadQWin = new Ext.Window({
    title: '查询方案',
    width: 580,
    height: 380,
    layout: 'fit',
    buttonAlign:'center',
    items: qplanGrid,
    closable: false,
    buttons: [{
        text: '关闭',
        handler:function(){
    		loadQWin.hide(); 
		}
    }]
});
loadQWin.on("show",function(){
	qpDs.load();
});
//动态构造columnModel和Store，带导出Excel的框
var grid = new App.ux.DynamicGridPanelAuto({
	cls : 'rptGrid',
	columns : [{}],
	<%if(hasHead||hasFoot){%>
	region: 'center',
	<%}%>
	title: "<%=titleInHead?"":rpt.getTitle().getTitleExp()%>",
	enableColumnMove :true,
	stripeRows: true,
	store : new Ext.data.DirectStore({
		directFn : QueryHandler.queryGeneralDataDynamic,
		paramsAsHash : false,
		remoteSort: <%=rpt.getRemoteSort()==1?"true":"false"%>,
		paramOrder: ['rptID','start','limit','condition'],
		fields : []
	}),
	view: new Ext.ux.grid.LockingGridView(App.ux.DynamicGridPanelAuto.prototype.viewConfig),
	tbar: [
	{
		iconCls: 'viewMenu',
        text: '查询方案',
        menu: new Ext.menu.Menu({
            id: 'queryPlanMn',
            items: [
			{
				text: '保存',
		        iconCls: 'save',
		        handler : function(){
		        	if(!cQPid||cQPid<0){
		        		cQPRecord=null;
			        	saveQWin.show();
		        	}else {
		        		saveQueryPlan(cQPid,true);
		        	}
				}
			},{
				text: '另存为...',
		        iconCls: 'saveAs',
		        handler : function(){
					cQPRecord=null;
		        	saveQWin.show();
				}
			},{
				text: '方案...',
		        iconCls: 'schema',
		        handler : function(){
			    	loadQWin.show();
				}
			}]
        })
	},new Ext.Toolbar.Separator()]
});

grid.getStore().on("beforeload",function(ds,op){
	//组织参数
	ds.baseParams.rptID = rptID;
	var tmpCdts = Ext.apply({},condition);
	tmpCdts.metaDataLoaded = grid.metaDataLoaded;
	tmpCdts.loadDefaultMeata = cLoadDefaultMeata;
	tmpCdts.loadUserMeata = cLoadUserMeata;
	tmpCdts.moneyUnit = showUnit>0?unitsCombo.getValue():"";
	tmpCdts.QPid = cQPid
	Ext.copyTo(tmpCdts,op.params,'sort,dir');
	delete op.params.sort;
	delete op.params.dir;
	op.params.condition = Ext.encode(tmpCdts);
});
//grid.getStore().on("load",function(){gridSpan(grid,"row");});  
function getRenderer(fname){
	if(fname =="wan2Decimals"){
		return regWan2Decimals;
	}else if(fname =="yi2Decimals"){
		return regYi2Decimals;
	}else{
		return renderFoo;
	}
}
function zeroHideShow(){
	App.rpt.HIDEZERO=!App.rpt.HIDEZERO;
	Ext.getCmp("btnHideZero").setText(App.rpt.HIDEZERO?"显示零":"隐藏零");
	//var uf = unStore.getAt(index).get("renderFun");
	var uid = unitsCombo.getValue();
	var uf = unStore.getById(uid).get("renderFun")
	var renderer = App.rpt.Renders[uf];
	var cols = grid.getColumnModel().config;
	for(var i=0;i<cols.length;i++){
		if(cols[i].isMultiUnit&&cols[i].isMultiUnit>0){
			cols[i].renderer=renderer;
		}else if(cols[i].hideZero&&cols[i].hideZero>0){
			cols[i].renderer=App.rpt.Renders["hideZero"]
		}
	}
	grid.view.refresh();
}
function gridSpan(grid, rowOrCol, cols, sepCol){  
    var array1 = new Array();  
    var arraySep = new Array();  
    var count1 = 0;  
    var count2 = 0;  
    var index1 = 0;  
    var index2 = 0;  
    var aRow = undefined;  
    var preValue = undefined;  
    var firstSameCell = 0;  
    var allRecs = grid.getStore().getRange();  
    if(rowOrCol == "row"){  
        count1 = grid.getColumnModel().getColumnCount();  
        count2 = grid.getStore().getCount();  
    } else {  
        count1 = grid.getStore().getCount();  
        count2 = grid.getColumnModel().getColumnCount();  
    }  
    for(i = 0; i < count1; i++){  
        if(rowOrCol == "row"){  
            var col= grid.getColumnModel().config[i];
    		if(col.isGroup==0){
    			continue;
    		}
        }  
        preValue = undefined;  
        firstSameCell = 0;  
        array1[i] = new Array();  
        for(j = 0; j < count2; j++){  
            if(rowOrCol == "row"){  
                index1 = j;  
                index2 = i;  
            } else {  
                index1 = i;  
                index2 = j;  
            }  
            var colName = grid.getColumnModel().getDataIndex(index2);  
            if(sepCol && colName == sepCol)  
            arraySep[index1] = allRecs[index1].get(sepCol);  
            var seqOldValue = seqCurValue = "1";  
            if(sepCol && index1 > 0){  
                seqOldValue = arraySep[index1 - 1];  
                seqCurValue = arraySep[index1];  
            }  
            if(allRecs[index1].get(colName) == preValue && (colName == sepCol || seqOldValue == seqCurValue)){  
                 allRecs[index1].set(colName, "");  
                 array1[i].push(j);  
                 if(j == count2 - 1){  
                     var index = firstSameCell + Math.round((j + 1 - firstSameCell) / 2 - 1);  
                     if(rowOrCol == "row"){  
                         allRecs[index].set(colName, preValue);  
                       } else {  
                           allRecs[index1].set(grid.getColumnModel().getColumnId(index), preValue);  
                       }  
                   }  
               } else {  
                   if(j != 0){  
                       var index = firstSameCell + Math.round((j + 1 - firstSameCell) / 2 - 1);  
                       if(rowOrCol == "row"){  
                           allRecs[index].set(colName, preValue);  
                       } else {  
                           allRecs[index1].set(grid.getColumnModel().getColumnId(index), preValue);  
                    }  
               }  
               firstSameCell = j;  
               preValue = allRecs[index1].get(colName);  
               allRecs[index1].set(colName, "&nbsp;");  
               if(j == count2 - 1){  
                   allRecs[index1].set(colName, preValue);  
               }  
           }  
        }  
    }  
    grid.getStore().commitChanges();  
    //添加所有分隔线  
    var rCount = grid.getStore().getCount();  
    for(i = 0; i < rCount; i ++){  
        for(j = 0; j < grid.getColumnModel().getColumnCount(); j ++){
            aRow = grid.getView().getCell(i,j);  
            if(i == 0){  
                aRow.style.borderTop = "none";  
                //aRow.style.borderLeft = "1px solid #ccc";  
            }else if(i == rCount - 1){  
                aRow.style.borderTop = "1px solid #eceff6";  
                //aRow.style.borderLeft = "1px solid #ccc";  
                aRow.style.borderBottom = "1px solid #ccc";  
            }else{  
                aRow.style.borderTop = "1px solid #eceff6";  
                //aRow.style.borderLeft = "1px solid #ccc";  
            }  
            if(j == grid.getColumnModel().getColumnCount()-1)  
                aRow.style.borderRight = "1px solid #eceff6";  
            if(i == rCount-1)       
            	aRow.style.borderBottom = "1px solid #eceff6";  
        }
    }
    //去除合并的单元格的分隔线  
    for(i = 0; i < array1.length; i++){  
    	if(!Ext.isEmpty(array1[i])){  
    		for(j = 0; j < array1[i].length; j++){  
            	if(rowOrCol == "row"){ 
					aRow = grid.getView().getCell(array1[i][j],i);  
					aRow.style.borderTop = "none";  
				} else {  
					aRow = grid.getView().getCell(i, array1[i][j]);  
					aRow.style.borderLeft = "none";  
				}
			}  
		}  
	}  
} 
var head = new Ext.Panel({
    region: 'north',
    height: <%=rpt.getHead()==null?50:rpt.getHead().getHeight()%>, 
    frame : true,
    html: "<%=strHead%>"
});
var foot = new Ext.Panel({
    region: 'south',
    height: <%=rpt.getFoot()==null?50:rpt.getFoot().getHeight()%>, 
	frame : true,
	html: "<%=strFoot%>"
});
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		<%if(!hasHead&&!hasFoot){%>
		layout:'fit',
        items:[grid]
        <%}else{%>
        layout: 'border',
        items:[
        	<%if(hasHead){%>
        	head,
        	<%}
			if(hasFoot){%>
        	foot,
        	<%}%>
			grid
		]
        <%}%>
	});
	grid.getStore().load({params:{rptID:'<%=rptID%>',start:0, limit:<%=cg.getString("pageSize","40")%>,condition:''}});
}); 
var REPORTGRID = grid;
</script>
</head>
<body>
<iframe name='ifmExport' src='' frameborder=0  marginwidth=0></iframe>
</body>
</html>