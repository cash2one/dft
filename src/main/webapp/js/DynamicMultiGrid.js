Ext.ns('App.ux');
App.ux.defaultPageSize=40;
App.ux.DynamicGridPanel = function(config) {
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
		scope : this,
		handler : function(btn, e) {
			this.exportExcel();
			//var expUrl='getRptData.query?doType=toExcel&rptID='+this.id;
	        //window.open(expUrl,"","scrollbars=auto,toolbar=yes,location=no,directories=no,status=no,menubar=yes,resizable=yes,width=780,height=500,left=10,top=50");
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
				/*listeners : {
					beforechange : function(self, params) {
						Ext.applyIf(params, this.store.lastOptions.params);
						return true;
					}
				}*/
			}
		});
	}
	App.ux.DynamicGridPanel.superclass.constructor.call(this, config);
};

Ext.extend(App.ux.DynamicGridPanel, Ext.grid.GridPanel, {
	columns : [],
	metaDataLoaded: false,
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

Ext.reg('DynamicGridPanel', App.ux.DynamicGridPanel);

App.ux.columnTipRender = function(value, p, record) {
	if (value) {
		p.attr = 'title="' + value + '"';
	}
	return value;
};

/**
 * @class App.ux.DynamicGridPanelMulti
 * @overrides onDataChange
 * @author jlong
 * @date 2013-04-15 Adds support the grid to assign the column width by the
 *       column content
 */
App.ux.DynamicGridPanelMulti = Ext.extend(App.ux.DynamicGridPanel, {
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
			var _hzcount = 0;// 大宽度字母的总个数
			var _zmcount = 0;// 小宽度字母字母的总个数
			var _sum_length = 0; // 总字符串长
			var _rownums;// 总字符个数
			var _avg; // 每列平均长度
			var _tmp; // 临时初始值
			var _replace;// 替换后的值
			var _indx; // 列头位索引
			var _head; // 列头宽度

			for (var _coli = 0; _coli < _len; _coli++) {
				columns[_coli].renderer = renderFoo;
				_indx = columns[_coli].dataIndex;
				_head = columns[_coli].header.length;
				_hzcount = 0;
				_zmcount = 0;
				_tmp = "";
				for (var k = 0;; k++) {
					if (typeof this.ds.getAt(k) != "undefined") {
						// 获取临时串
						_tmp = _tmp + String(this.ds.getAt(k).get(_indx));
					} else {
						_rownums = k;
						break;
					}
				}
				_replace = _tmp.replace(/[0-9a-z:\.<>=]/gi, "");
				_hzcount = _replace.length;
				_zmcount = _tmp.length - _hzcount;
				// 一个小宽度是长10,一个大宽度是长15
				var _sum_length = _hzcount * 15 + _zmcount * 10;
				_rownums = _rownums == 0 ? 1 : _rownums;
				_avg = Math.round(_sum_length / _rownums);
				// 计算表头的长度
				_head = _head * 13 + 5;
				if (_head > _avg) {
					_avg = _head;
				}
				// 对于日期特殊处理
				if (typeof columns[_coli].format == "string") {
					if (columns[_coli].format == 'Ymd H:i:s') {
						_avg = 130;

					} else if (columns[_coli].format == 'Ymd') {
						_avg = 80;
					} else if (columns[_coli].format == 'H:i:s') {
						_avg = 80;
					}
				}
				// 对于靠右特殊处理
				if (typeof columns[_coli].align == "string") {
					if (columns[_coli].align == 'right') {
						// 小于100,都归于100
						if (_avg < 100) {
							_avg = 100;
						}
					}
				}
				// 长度大于500的,都归于500处理
				if (_avg > 500) {
					_avg = 500;
				}
				if (columns[_coli].width < _avg) {
					columns[_coli].width = _avg;
				}
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
							showQparamTree(it.rptID,this.id.substring(3+this.rptID.length),this.isMulti);
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
						buildCondition(tmpId); 
					}
				});
			}
			
			this.metaDataLoaded = true;
			this.grid.getTopToolbar().doLayout();
			this.refresh();
			this.updateHeaderSortState();
			this.syncFocusEl(0);
		}
	},
	loadMask : true
});

Ext.reg('DynamicGridPanelMulti', App.ux.DynamicGridPanelMulti);