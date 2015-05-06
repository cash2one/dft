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
		paramsAsHash : false
	}),
	reader : new Ext.data.JsonReader({
		idProperty : 'field'
	}, fldfltRecord)
});
var fldsfltGrid = new Ext.grid.GridPanel({
	title : '字段列表',
	store : fldfltDs,
	cm : fltcm,
	frame : false,
	stripeRows : true,
	loadMask : {msg : '正在加载数据....'},
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	selModel: fltssm,
	stripeRows : true
});
// 选中显示的字段
var sfssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
sfssm.handleMouseDown = Ext.emptyFn;
var showFldsCm = new Ext.grid.ColumnModel({
	columns : [{
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
	enableColumnMove : false,
	view : new Ext.grid.GridView(),
	selModel: sfssm,
	stripeRows : true
});
var fltPanel = new Ext.Panel({
	region: 'north',
	height : 200,
	header : true,
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
				var record = showFldsGrid.getSelectionModel().selection.record;
				var store = showFldsGrid.getStore();
				if (!record) {
					return;
				}
				var index = store.indexOf(record);
				if (index > 0) {
					store.removeAt(index);
					store.insert(index - 1, record);
					showFldsGrid.getSelectionModel().select(index - 1,1);
				}
			}
		}, {
			iconCls : 'icon-right',
			scope : this,
			handler : function() {
				var record = fldsfltGrid.getSelectionModel().getSelected();
				if (!record) {
					return;
				}
				var idx = showFldsGrid.getStore().find('field',record.data.field);
				if (idx < 0) {
					showFldsGrid.getStore().loadData(record.data, true);
					fldsfltGrid.remove(record);
				}
			}
		}, {
			iconCls : 'icon-left',
			scope : this,
			handler : function() {
				var record = showFldsGrid.getSelectionModel().selection.record;
				if (!record) {
					return;
				}
				fldsfltGrid.getStore().add(record);
				showFldsGrid.getStore().remove(record);
			}
		}, {
			iconCls : 'icon-down',
			scope : this,
			handler : function() {
				var record = showFldsGrid.getSelectionModel().selection.record;
				var store = showFldsGrid.getStore();
				if (!record) {
					return;
				}
				var index = store.indexOf(record);
				if (index < store.getCount() - 1) {
					store.removeAt(index);
					store.insert(index + 1, record);
					showFldsGrid.getView().refresh();
					showFldsGrid.getSelectionModel().select(index + 1,1);
				}
			}
		}]
	},{
		flex : 3,
		items:[showFldsGrid]
	}]
});
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
    	paramsAsHash : false
    }),
    reader : new Ext.data.JsonReader({
        idProperty : 'field'
    }, fltCdtRecord)
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

var cdtCm = new Ext.grid.ColumnModel({
	columns : [{
		id : 'field',
		header : "字段",
		dataIndex : 'field',
		width : 80,
		editor : cb_flds2filter,
		renderer : function(v, p, r) {
			var index = fltCdtDs.find('field', v);
			var cbRec = fltCdtDs.getAt(index);
			if (cbRec) {
				return cbRec.data.mc;
			} else {
				return v;
			}
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
			} else {
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
		})
	}, {
		id : 'treebt',
		header : "",
		dataIndex : 'treebt',
		width : 80,
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
    {name : 'field',type : 'string'}, 
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
	height : 220,
	frame : true,
	clicksToEdit : 1,
	store : cdtStore,
	cm : cdtCm
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
	var czd = e.record.get("field");
	var idx = fltCdtDs.indexOfId(czd);
	var valRenderMod = fltCdtDs.getAt(idx).get("var_src");
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
	if (field == "value") {
		e.record.set('hValue', e.record.get("value"));
	}
});
// Ext.Window=============================================================
var fltWin = new Ext.Window({
	width : 550,
	height : 400,
	title : "筛选条件",
	layout : 'border',
	items : [fltPanel,cdtGrid],
	buttons : [
	{
		text: "查看SQL",
	    handler: function(){
	    }
	},{
		text : "载入查询",
		handler : function() {
			var tb = new Object();
			var rows = new Array();
			if (cdtGrid.getStore().getCount() == 0) {
				win.hide();
			} else {
				var rds = cdtGrid.getStore().getRange();
				for (var i = 0; i < rds.length; i++) {
					var rs = rds[i];
					var row = new Object();
					var fields = rs.data;
					var connection = '_and';
					if (i = rds.length - 1) {
						connection = '';
					}
					if (fields["hValue"] == null || fields["hValue"] == '') {
						continue;
					}
					if (fields["connection"] != null&& fields["connection"] != '') {
						connection = fields["connection"];
					}
					rows.push({
						"tbName" : fields["tbName"],
						"fldName" : fields["bm"],
						"fldValue" : fields["hValue"],
						"relation" : "equ",
						"connection" : connection,
						"valueType" : fields["valueType"]
					});
				}
				tb.rows = rows;
				conditions = Ext.encode(tb);
				cdtGrid.getStore().removeAll();
				fltWin.hide();
				if (tb.rows.length <= 0) {
					conditions = "";
				}
			}
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
fltWin.on("show",function(){
	fldfltDs.load();
});
