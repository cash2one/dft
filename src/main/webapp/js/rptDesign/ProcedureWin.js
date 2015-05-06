Ext.namespace('Query.widget');
Query.widget.ProcedureWindow = Ext.extend(Ext.Window, {
	proInfo: null,
	setProInfo : function(v) {
		this.proInfo = v;
	},
	getProInfo : function() {
		return this.proInfo ;
	},
	constructor : function(config) {
		Ext.apply(this, config);
		Query.widget.ProcedureWindow.superclass.constructor.call(this, config);
	}
});
var procedureForm = new Ext.FormPanel({
	labelWidth: 100,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .5,
			layout : 'form',
			items : [
			{
				fieldLabel: '过程名称',
				xtype: 'textfield',
				name: 'proName',
				width: 120
			},{
				fieldLabel: '总记录数索引号',
				xtype: 'numberfield',
				name: 'totalIndex',
				width: 120
			}]
		},{
			columnWidth : .5,
			layout : 'form',
			items : [
			{
				fieldLabel: '记录集索引号',
				xtype: 'numberfield',
				name: 'dataSetIndex',
				width: 120
			},{
				fieldLabel: '输出串索引号',
				xtype: 'numberfield',
				name: 'outPutInfoIndex',
				width: 120
			}]
		}]
	}]
});
var propi_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var propi_cm = new Ext.grid.ColumnModel({
	columns: [
	propi_sm,
	{
	    id:'referMode',
	    header: "引用参数",
	    dataIndex: 'referMode',
	    menuDisabled:true,
	    width: 60,
	    align:'left',
	    renderer: function(v,p,r){
			if(v==1){
				return "是";
			}else{
				return "否";
			}
		}
	},{
	    id:'referTo',
	    header: "引用参数名",
	    dataIndex: 'referTo',
	    menuDisabled:true,
	    width: 90,
	    align: 'left'
	},{
	    id:'value',
	    header: "静态值",
	    dataIndex: 'value',
	    menuDisabled:true,
	    width: 90,
	    align: 'left'
	},{
	    id:'dataType',
	    header: "类型",
	    dataIndex: 'dataType',
	    menuDisabled:true,
	    width: 60,
	    align: 'left',
	    renderer: function(v,p,r){
			if(v==1){
				return "整数";
			}else if(v==2){
				return "小数";
			}else if(v==3){
				return "游标";
			}else{
				return "字符串";
			}
		}
	}],
	defaultSortable: false
});
var propi_Record = Ext.data.Record.create([                             
    {name: 'referMode', type: 'int'},
    {name: 'referTo', type: 'string'},     
    {name: 'value',type:'string'},
    {name: 'dataType',type:'int'}
]);
var propi_ds = new Ext.data.Store({
    reader: new Ext.data.JsonReader({
	}, propi_Record)
});
var proPIGrid = new Ext.grid.GridPanel({
	title:'输入参数',
	store: propi_ds,
    cm: propi_cm,
    height:270,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: propi_sm,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加参数',
		iconCls: 'add',
        handler : function(){
			var pif = propiForm.getForm();
			pif.findField("referMode").setValue("1");
			pif.findField("referTo").setValue("");
			pif.findField("value").setValue("");
			pif.findField("value").disable();
			pif.findField("dataType").setValue("");
			propiWin.addMode="add";
			propiWin.show();
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = proPIGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的参数!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					propi_ds.getModifiedRecords().remove(records[rc]);
					propi_ds.remove(records[rc]);
				}
	        }
		}
	},{
		text: '修改',
		iconCls: 'edit',
        handler : function(){
			//当前记录
			var records = proPIGrid.getSelectionModel().getSelections();
			if (!records||records.length==0) {
				return;
			}
			var record = records[0];
			var pif = propiForm.getForm();
			pif.findField("referMode").setValue(record.get("referMode"));
			pif.findField("referTo").setValue(record.get("referTo"));
			pif.findField("value").setValue(record.get("value"));
			pif.findField("dataType").setValue(record.get("dataType"));
			propiWin.addMode="modify";
			propiWin.show();
		}
	}]
});
//输出参数
var editor_propType=new Ext.form.ComboBox({
	fieldLabel:'参数类型',
	name: 'cb_proDataType',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '字符串'], 
			[1, '整数'],
			[2, '小数'],
			[3, '游标']
		] 
	}),
	mode: 'local'
});

var propo_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var propo_cm = new Ext.grid.ColumnModel({
	columns: [
	propo_sm,
	{
	    header: "数据类型",
	    dataIndex: 'dataType',
	    menuDisabled:true,
	    width: 100,
	    align:'left',
	    editor : editor_propType,
	    renderer: function(v,p,r){
			if(v==1){
				return "整数";
			}else if(v==2){
				return "小数";
			}else if(v==3){
				return "游标";
			}else{
				return "字符串";
			}
		}
	}],
	defaultSortable: false
});
var propo_Record = Ext.data.Record.create([                             
    {name: 'dataType', type: 'int'}
]);
var propo_ds = new Ext.data.Store({
    reader: new Ext.data.JsonReader({
	}, propo_Record)
});
var proPOGrid = new Ext.grid.EditorGridPanel({
	title:'输出参数',
	store: propo_ds,
    cm: propo_cm,
    height:270,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: propo_sm,
    clicksToEdit : 1,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加参数',
		iconCls: 'add',
        handler : function(){
			var cdt = new propo_Record({
				dataType: 0
	        });
			proPOGrid.stopEditing();
			propo_ds.insert(propo_ds.getCount(), cdt);
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = proPOGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的参数!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					propo_ds.getModifiedRecords().remove(records[rc]);
					propo_ds.remove(records[rc]);
				}
	        }
		}
	}]
});
var procedureWindow = new Query.widget.ProcedureWindow({
	id : 'procedureWindow',
	title : '存储过程',
	layout : 'border',
	width : 500,
	height : 420,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	items : [{
		region:"north",
		height:80,
		frame: true,
		items:[procedureForm]
	},{
		xtype: "panel",
		region:"center",
		frame: true,
		layout:'column',
		items:[
		{
			columnWidth : .65,
            layout : 'fit',
            border : true,
            autoHeight: true,
			items:[proPIGrid]
		},{
			columnWidth : .35,
			layout : 'fit',
            border : true,
			items:[proPOGrid]
		}]
	}],
	buttons : [
	{
		text : "确定",
		handler : function(){
			procedureWindow.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			procedureWindow.onHide=Ext.emptyFn;
			procedureWindow.hide();
		}
	}],
	onHideSetValue : Ext.emptyFn
});
procedureWindow.on("hide",function() {
	//encode成json串
	var info = new Object();
	var pf = procedureForm.getForm();
	info.name = pf.findField("proName").getValue();
	info.totalIndex = pf.findField("totalIndex").getValue();
	info.dataSetIndex = pf.findField("dataSetIndex").getValue();
	info.outPutInfoIndex = pf.findField("outPutInfoIndex").getValue();
	var ips = new Array();
	for(var i=0;i< propi_ds.getCount();i++){
		var rd = propi_ds.getAt(i);
		var ip = new Object();
		ip.referMode = rd.get("referMode");
		ip.referTo = rd.get("referTo");
		ip.value=rd.get("value");
		ip.dataType=rd.get("dataType");
		ips.push(ip);
	}
	var ops = new Array();
	for(var i=0;i< propo_ds.getCount();i++){
		var rd = propo_ds.getAt(i);
		var op = new Object();
		op.dataType = rd.get("dataType");
		ops.push(op);
	}
	info.inParas =ips;
	info.outParas = ops;
	var jv = Ext.encode(info)
	procedureWindow.setProInfo(jv);
	procedureWindow.onHideSetValue.call(this.scope, jv);
});

procedureWindow.on("show",function(){
	//解析proInfo的json串，写入控件
	var strInfo = procedureWindow.getProInfo();
	var info = Ext.decode(strInfo);
	if(info){
		var pf = procedureForm.getForm();
		pf.findField("proName").setValue(info.name);
		pf.findField("totalIndex").setValue(info.totalIndex);
		pf.findField("dataSetIndex").setValue(info.dataSetIndex);
		pf.findField("outPutInfoIndex").setValue(info.outPutInfoIndex);
		var pis = info.inParas;
		var pos = info.outParas;
		propi_ds.removeAll();
		propi_ds.loadData(pis);
		propo_ds.removeAll();
		propo_ds.loadData(pos);
	}
});
//单个输入参数属性
var cb_propType=new Ext.form.ComboBox({
	fieldLabel:'参数类型',
	name: 'cb_proDataType',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'dataType',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '字符串'], 
			[1, '整数'],
			[2, '小数'],
			[3, '游标']
		] 
	}),
	mode: 'local'
});

var propiForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 80,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		hideLabel: true,
		boxLabel: '是否引用参数',
		xtype: 'checkbox',
		name: 'referMode',
		width: 120,
		checked : true,
		listeners :{
			check: function(ckbox,checked){
				if(checked){
					propiForm.getForm().findField("referTo").enable();
					propiForm.getForm().findField("value").disable();
				}else{
					propiForm.getForm().findField("referTo").disable();
					propiForm.getForm().findField("value").enable();
				}
			}
		}
	},{
		fieldLabel: '引用参数名',
		xtype: 'textfield',
		name: 'referTo',
		width: 120
	},{
		fieldLabel: '静态值',
		xtype: 'textfield',
		name: 'value',
		disabled :true,
		width: 120
	},cb_propType]
});
var propiWin = new Ext.Window({
	id : 'propiWin',
	title : '输入参数',
	items : [propiForm],
	layout : 'fit',
	width : 280,
	height : 200,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var pif = propiForm.getForm();
			if(propiWin.addMode=="add"){
				//增加一条记录
				var rm=0,referTo="",v = "";
				var referMode = pif.findField("referMode").getValue();
				if(referMode=="on"||referMode){
					referTo = pif.findField("referTo").getValue();
					v="";
					rm =1;
				}else{
					v = pif.findField("value").getValue();
					referTo = "";
					rm = 0;
				}
				var cdt = new propi_Record({
					referMode: rm,
					referTo: referTo,
					value: v,
					dataType: pif.findField("dataType").getValue()
		        });
				proPIGrid.stopEditing();
				propi_ds.insert(propi_ds.getCount(), cdt);
			}else{
				var records = proPIGrid.getSelectionModel().getSelections();
				if (!records||records.length==0) {
					propiWin.hide();
					return;
				}
				var record = records[0];
				var rm=0,referTo="",v = "";
				var referMode = pif.findField("referMode").getValue();
				if(referMode=="on"||referMode){
					referTo = pif.findField("referTo").getValue();
					v="";
					rm=1;
				}else{
					v = pif.findField("value").getValue();
					referTo = "";
					rm=0;
				}
				record.set("referMode",rm);
				record.set("referTo",referTo);
				record.set("value",v);
				record.set("dataType",pif.findField("dataType").getValue());
			}
			propiWin.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			propiWin.hide();
		}
	}]
});
