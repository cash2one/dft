
App.ux.DynamicGridPanelNoExport = function(config) {
	config = config || {};
	Ext.apply(this, config);

	var plugins = [new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :60000
	})];
	if (config.plugins) {
		plugins = plugins.concat(config.plugins);
	}

	Ext.apply(config, {
		plugins : plugins
	});
	if (!this.sm) {
		if (this.checkboxSelect) {
			this.sm = new Ext.grid.CheckboxSelectionModel({
				singleSelect : config.singleSelect
						? config.singleSelect
						: false,
				listeners : config.listeners
			});
		} else {
			this.sm = new Ext.grid.RowSelectionModel({
				singleSelect : config.singleSelect ? config.singleSelect : true,
				listeners : config.listeners
			});
		}
	}

	if (this.columns && this.columns.length > 0 && this.checkboxSelect) {
		this.columns = [].concat(this.sm).concat(this.columns);
	}
	if (this.columns) {
		this.colModel = this.columns;
	}
	if (this.cm) {
		this.colModel = this.cm;
	}
	if (Ext.isArray(this.colModel)) {
		this.cm = new Ext.ux.grid.LockingColumnModel({
			columns : this.colModel,
			defaults : {
				sortable : false
			}
		});
	}
	this.enableColumnMove = config.enableColumnMove
			? config.enableColumnMove
			: false;
	if ((this.disablePaging && this.disablePaging == true)) {
	} else {
		var count = App.ux.defaultPageSize;
		if (this.pageCount) {
			count = this.pageCount;
		}
		Ext.apply(config, {
			bbar : {
				xtype : 'paging',
				pageSize : count,
				displayInfo : true,
				store : this.store,
				displayMsg : '当前 {0} - {1} &nbsp;&nbsp; 共 {2}条',
				emptyMsg : '没有数据'//,
			}
		});
	}
	App.ux.DynamicGridPanelNoExport.superclass.constructor.call(this, config);
};

Ext.extend(App.ux.DynamicGridPanelNoExport, Ext.grid.GridPanel, {
	columns : [],
	metaDataLoaded : false,
	viewConfig : {
		emptyText : "没有数据",
		onDataChange : function() {
			if (this.cm.getColumnCount() == 0
					&& this.ds.reader.jsonData.metaData.columns) {
				columns = this.ds.reader.jsonData.metaData.columns;
				if (this.grid.checkboxSelect) {
					columns = [].concat(this.grid.selModel).concat(columns);
				}
				this.cm.setConfig(columns);
				this.syncFocusEl(0);
				return;
			}
			this.refresh();
			this.updateHeaderSortState();
			this.syncFocusEl(0);
		}
	},
	loadMask : true
});

Ext.reg('DynamicGridPanelNoExport', App.ux.DynamicGridPanelNoExport);

App.ux.columnTipRender = function(value, p, record) {
	if (value) {
		p.attr = 'title="' + value + '"';
	}
	return value;
};
/**
 * @class App.ux.DynamicGridPanelPopup
 * @overrides onDataChange
 */
App.ux.DynamicGridPanelPopup = Ext.extend(App.ux.DynamicGridPanelNoExport, {
	columns : [],
	viewConfig : {
		emptyText : "没有数据",
		onDataChange : function() {
			var columns = this.cm.config;
			if (this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.columns) {
				columns = this.ds.reader.jsonData.metaData.columns;
			}
			var ttbars;
			if(!this.metaDataLoaded){
				if (this.ds.reader.jsonData.metaData
						&& this.ds.reader.jsonData.metaData.ttbars) {
					ttbars = this.ds.reader.jsonData.metaData.ttbars;
				}
			}
			var _len = columns.length;
			for (var _coli = 0; _coli < _len; _coli++) {
				columns[_coli].renderer = renderFoo;
				if(columns[_coli].renderer&&typeof(columns[_coli].renderer)=="string"){
					columns[_coli].renderStr = columns[_coli].renderer;
					columns[_coli].renderer=App.rpt.Renders[columns[_coli].renderer];
				}else if(columns[_coli].isLink>0){//no renderer,as a link column,it need renderer
					columns[_coli].renderer=App.rpt.Renders["renderFoo"];
				}
			}

			// 判断初始化多选框列
			if (this.grid.checkboxSelect) {
				columns = [].concat(this.grid.selModel).concat(columns);
			}
			this.cm.setConfig(columns);
			//2015-12复杂表头
			if (this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.headRows) {
				var hrows = this.ds.reader.jsonData.metaData.headRows;
				if(hrows.length>0){
					this.cm.rows = hrows;
					/*var plugins = [new Ext.ux.plugins.GroupHeaderGrid()];
					if (this.plugins) {
						this.plugins[0].init(this);
					}
					this.plugins = plugins;*/
				}
			}
			var tmpId="";
			if(ttbars&&ttbars.length>0){
				for(var i=0;i<ttbars.length;i++){
					var it = ttbars[i];
					tmpId = it.rptID;
					if(it.xtype=="combo"){
						it.store = cbStore;
						it.mode = "remote";
						it.listeners = {
						    beforequery: function(qe){
								cbStore.baseParams.pName = qe.combo.getName();
								cbStore.baseParams.rptID = this.rptID;
								var tmpPost={},mps = {};
								var aBy = qe.combo.affectedBy;
								if(aBy){
									var aparas = aBy.split(",");
									mps = new Object();
									for(var i = 0;i<aparas.length;i++){
										var tp = aparas[i];
										var tcmp = Ext.getCmp("q_h_"+this.rptID+"_"+tp);
										if(tcmp){
											var val =tcmp.getValue();
											mps[aparas]=val;
										}
									}
									tmpPost.macroParams = mps;
								}
								cbStore.baseParams.affectedBy = Ext.encode(tmpPost);
								cbStore.load();
							},
							select: function(combo, record, index) {
								Ext.getCmp('q_h_'+this.rptID+"_"+this.id.substring(3+this.rptID.length)).setValue(record.get('bm'));
								var affs = combo.affect;
								var arrCmps = affs?affs.split(","):[];
								for(var i=0;i<arrCmps.length;i++){
									var cp = Ext.getCmp("q_"+this.rptID+"_"+arrCmps[i]);
									if(cp){
										cp.setValue("");
									}
									var hcp = Ext.getCmp("q_h_"+this.rptID+"_"+arrCmps[i]);
									if(hcp){
										hcp.setValue("");
									}
								}
							}
						}
					}else if(it.xtype=="trigger"){
						it.editable = false;
						it.destroy = Ext.emptyFn;
						it.onTriggerClick=function(){
							showQparamTreeByRptID(it.rptID,this.id.substring(3+this.rptID.length),this.isMulti);
						};
					}
					this.grid.getTopToolbar().add(it);
					if(it.xtype!="label"&&it.xtype!="hidden"){
						this.grid.getTopToolbar().addSeparator();
					}
				}
				this.id = tmpId;
				this.grid.getTopToolbar().addButton({
					text: '查询',
		            iconCls: 'filter',
		            handler : function(){
						buildConditionByRptID(tmpId); 
					}
				});
			}
			this.metaDataLoaded = true;
			this.grid.getTopToolbar().doLayout();
			this.refresh(true);
			//this.updateHeaderSortState();
			this.syncFocusEl(0);
		}
	},
	loadMask : true
});

Ext.reg('DynamicGridPanelPopup', App.ux.DynamicGridPanelPopup);