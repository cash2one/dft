Ext.ns('App.ux');

App.ux.ExportEditorGridPanel = function(config) {
	config = config || {};
	Ext.apply(this, config);

	var plugins = [new Ext.ux.grid.GridExporter({
		mode : this.mode ? this.mode : 'remote',
		maxExportRows :30000		
	})];
	if (config.plugins) {
		plugins = plugins.concat(config.plugins);
	}

	Ext.apply(config, {
		plugins : plugins
	});
	if (!this.sm&&!this.selModel) {
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
		this.colModel = new Ext.grid.ColumnModel({
			columns : this.colModel,
			defaults : {
				sortable : false,
				menuDisabled : true
			}
		});
	}
	this.enableColumnMove = config.enableColumnMove
			? config.enableColumnMove
			: false;
	this.actExportXLS = new Ext.Action({
		text : '导出',
		iconCls : 'expExcel',
		id :'expExcel',
		scope : this,
		handler : function(btn, e) {
			this.exportExcel();
		}
	});
	this.actExportPDF = new Ext.Action({
		text : '打印',
		iconCls : 'expPdf',
		id :'expPdf',
		scope : this,
		handler : function(btn, e) {
			this.exportPdf();
		}
	});
	var btns = [this.actExportXLS];
	if (config.tbar) {
		btns = config.tbar.concat(btns);
	}
	Ext.apply(config, {
		tbar : btns
	});
	if (this.bbar || (this.disablePaging && this.disablePaging == true)) {
	} else {
		var count =0;
		if(this.pageCount) {
			count = this.pageCount;
		}
		Ext.apply(config, {
			bbar : {
				xtype : 'paging',
				pageSize : count,
				displayInfo : true,
				store : this.store,
				displayMsg : '当前 {0} - {1} &nbsp;&nbsp; 共 {2}条',
				emptyMsg : '没有数据',
				listeners : {
					beforechange : function(self, params) {
						Ext.applyIf(params, this.store.lastOptions.params);
						return true;
					}
				}
			}
		});
	}
	App.ux.ExportEditorGridPanel.superclass.constructor.call(this, config);
};

Ext.extend(App.ux.ExportEditorGridPanel, Ext.grid.EditorGridPanel, {
	columns : [],
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

Ext.reg('ExportEditorGridPanel', App.ux.ExportEditorGridPanel);