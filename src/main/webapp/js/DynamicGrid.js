Ext.ns('App.ux');

App.ux.DynamicGridPanel = function(config) {
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
	this.actExportXLS = new Ext.Action({
		text : '导出',
		iconCls : 'expExcel',
		scope : this,
		handler : function(btn, e) {
			winFormat.show();
			//this.exportExcel();
		}
	});
	this.actExportPDF = new Ext.Action({
		text : '导出',
		iconCls : 'expPdf',
		scope : this,
		handler : function(btn, e) {
			this.exportPdf();
		}
	});
	//var btns = [this.actExportXLS,this.actExportPDF];
	var btns = [this.actExportXLS];
	if (config.tbar) {
		btns = config.tbar.concat(btns);
	}
	Ext.apply(config, {
		tbar : btns
	});
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
	App.ux.DynamicGridPanel.superclass.constructor.call(this, config);
};

Ext.extend(App.ux.DynamicGridPanel, Ext.grid.GridPanel, {
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

Ext.reg('DynamicGridPanel', App.ux.DynamicGridPanel);

App.ux.columnTipRender = function(value, p, record) {
	if (value) {
		p.attr = 'title="' + value + '"';
	}
	return value;
};

/**
 * @class App.ux.DynamicGridPanelAuto
 * @overrides onDataChange
 * @author jlong
 * @date 2013-04-15 Adds support the grid to assign the column width by the
 *       column content
 */
App.ux.DynamicGridPanelAuto = Ext.extend(App.ux.DynamicGridPanel, {
	columns : [],
	viewConfig : {
		emptyText : "没有数据",
		onDataChange : function() {
			var columns = this.cm.config;
			if (this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.columns) {
				columns = this.ds.reader.jsonData.metaData.columns;
			}
			if(!columns || columns.length == 0){
				return;
			}
			var ISMU;
			if(this.ds.reader.jsonData.metaData&&this.ds.reader.jsonData.metaData.multiUnit){
				ISMU=true;
			}
			if(this.ds.reader.jsonData.metaData&&this.ds.reader.jsonData.metaData.unit){
				defaultUnit=this.ds.reader.jsonData.metaData.unit;
			}
			if(this.ds.reader.jsonData.metaData&&this.ds.reader.jsonData.metaData.cQPid){
				cQPid = this.ds.reader.jsonData.metaData.cQPid;
			}
			
			var ttbars;
			var moreParas;
			if(!this.metaDataLoaded){//如果是重组元数据
				//先删除工具栏项目，保留前4项
				var tbitems = this.grid.getTopToolbar().items;
				while(tbitems.length>4){
					//alert(tbitems.get(tbitems.length-1).xtype);
					//alert(tbitems.length);
					if(tbitems.get(tbitems.length-1)){
						this.grid.getTopToolbar().remove(tbitems.get(tbitems.length-1));
					}
					//this.grid.getTopToolbar().removeAll();
				}
				if (this.ds.reader.jsonData.metaData
						&& this.ds.reader.jsonData.metaData.ttbars) {
					ttbars = this.ds.reader.jsonData.metaData.ttbars;
				}
			}
			var _len = columns.length;
			
			for (var _coli = 0; _coli < _len; _coli++) {
				if(ISMU&&defaultUnit&&columns[_coli].isMultiUnit>0){
					var rfun = unStore.getById(defaultUnit);
					columns[_coli].renderer=rfun?App.rpt.Renders[rfun.get("renderFun")]:null;
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
			if(!this.metaDataLoaded&&ttbars&&ttbars.length>0){
				for(var i=0;i<ttbars.length;i++){
					var it = ttbars[i];
					if(it.xtype=="combo"){
						it.store = cbStore;
						it.mode = "remote";
						it.listeners = {
						    beforequery: function(qe){
								cbStore.baseParams.pName = qe.combo.getName();
								cbStore.baseParams.rptID = rptID;
								var tmpPost={},mps = {};
								var aBy = qe.combo.affectedBy;
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
								cbStore.baseParams.affectedBy = Ext.encode(tmpPost);
								cbStore.load();
							},
							select: function(combo, record, index) {
								Ext.getCmp('q_h_'+this.id.substring(2)).setValue(record.get('bm'));
								var affs = combo.affect;
								var arrCmps = affs?affs.split(","):[];
								for(var i=0;i<arrCmps.length;i++){
									var cp = Ext.getCmp("q_"+arrCmps[i]);
									if(cp){
										cp.setValue("");
									}
									var hcp = Ext.getCmp("q_h_"+arrCmps[i]);
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
							showQparamTree(this.id.substring(2),this.isMulti,this.onlyLeaf);
						};
					}
					this.grid.getTopToolbar().add(it);
					if(it.xtype!="label"&&it.xtype!="hidden"){
						this.grid.getTopToolbar().addSeparator();
					}
				}
				//this.id = tmpId;
				this.grid.getTopToolbar().addButton({
					text: '查询',
		            iconCls: 'filter',
		            handler : function(){
		            	buildCondition();
		            	grid.getStore().load({params:{rptID:rptID,start:0, limit:App.ux.defaultPageSize}});
		            }
				});
			}
			if(!this.metaDataLoaded&&this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.hasComplexParams){
				if(ttbars&&ttbars.length>0){
					this.grid.getTopToolbar().addSeparator();
				}
				this.grid.getTopToolbar().addButton({
					text: '更多参数',
		            iconCls: 'morePara',
		            handler : showParamsWin
				});
				moreParas = this.ds.reader.jsonData.metaData.paramsInForm;
				if(!paramForm){
					paramForm = new Ext.FormPanel({
				        frame: true,
				        labelAlign: 'left',
				        bodyStyle:'padding:5px',
				        width: 450,
				        height: 320,
				        autoScroll: true,
				        layout: 'form'
					});
				}
				for(var i=0;i<moreParas.length;i++){
					var it = moreParas[i];
					if(it.xtype=="combo"){
						it.store = cbStore;
						it.mode = "remote";
						it.listeners = {
						    beforequery: function(qe){
								cbStore.baseParams.pName = qe.combo.getName();
								cbStore.baseParams.rptID = rptID;
								var tmpPost={},mps = {};
								var aBy = qe.combo.affectedBy;
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
								cbStore.baseParams.affectedBy = Ext.encode(tmpPost);
								cbStore.load();
							},
							select: function(combo, record, index) {
								Ext.getCmp('q_h_'+this.id.substring(2)).setValue(record.get('bm'));
								var affs = combo.affect;
								var arrCmps = affs?affs.split(","):[];
								for(var i=0;i<arrCmps.length;i++){
									var cp = Ext.getCmp("q_"+arrCmps[i]);
									if(cp){
										cp.setValue("");
									}
									var hcp = Ext.getCmp("q_h_"+arrCmps[i]);
									if(hcp){
										hcp.setValue("");
									}
								}
							}
						}
					}else if(it.xtype=="trigger"){
						it.editable = false;
						//it.destroy = Ext.emptyFn;
						it.onTriggerClick=function(){
							showQparamTree(this.id.substring(2),this.isMulti,this.onlyLeaf);
						};
					}
					paramForm.add(Ext.create(it));
				}
				paramForm.doLayout();
				paramWin.add(paramForm);
			}
			if(!this.metaDataLoaded&&this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.hasComplexFlt){
				if(ttbars&&ttbars.length>0){
					this.grid.getTopToolbar().addSeparator();
				}
				this.grid.getTopToolbar().addButton({
					text: '高级筛选',
		            iconCls: 'complexFilter',
		            handler : showFilter
				});
				if(this.ds.reader.jsonData.metaData.filters){
					var cdts = Ext.encode(this.ds.reader.jsonData.metaData.filters);
					var fns = cdts.fldNames?cdts.fldNames.split(","):"";
					var fvs = cdts.fldValues?cdts.fldValues.split(","):"";
					var rlts = cdts.relations?cdts.relations.split(","):"";
					var conns = cdts.connections?cdts.connections.split(","):"";
					for(var i=0;i<fns.length;i++){
						var cdt = new cdtRecord({
				        	fld: fns[i],
				        	ops: rlts[i],
				        	fldValue: fvs[i],
				        	connection: conns[i],
				        	hValue :fvs[i].replace("|",",")
				        });
				        cdtStore.insert(cdtStore.getCount(), cdt);
					}
				}
			}
			if(!this.metaDataLoaded&&this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.multiUnit){
				if(ttbars&&ttbars.length>0){
					this.grid.getTopToolbar().addSeparator();
				}
				//增加单位的下拉框
				this.grid.getTopToolbar().add({
					xtype: "label",
			    	text: "金额单位："
			    });
				this.grid.getTopToolbar().add(unitsCombo);
			}
			if(!this.metaDataLoaded&&this.ds.reader.jsonData.metaData
					&& this.ds.reader.jsonData.metaData.zeroCanHide){
				if(ttbars&&ttbars.length>0){
					this.grid.getTopToolbar().addSeparator();
				}
				//增加隐藏零值的按钮
				this.grid.getTopToolbar().addButton({
					text: '隐藏零',
					id: 'btnHideZero',
		            iconCls: 'zeroHideShow',
		            handler : zeroHideShow
				});
			}
			this.metaDataLoaded = true;
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.title){
				if(titleInHead&&document.getElementById('headTitle')){
					document.getElementById('headTitle').innerHTML=this.ds.reader.jsonData.title;
				}else{
					this.grid.setTitle(this.ds.reader.jsonData.title);
				}
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.subTitleLeft&&document.getElementById('headSLeft')){
				document.getElementById('headSLeft').innerHTML=this.ds.reader.jsonData.subTitleLeft;
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.subTitleCenter&&document.getElementById('headSCenter')){
				document.getElementById('headSCenter').innerHTML=this.ds.reader.jsonData.subTitleCenter;
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.subTitleRight&&document.getElementById('headSRight')){
				document.getElementById('headSRight').innerHTML=this.ds.reader.jsonData.subTitleRight;
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.footLeft&&document.getElementById('footLeft')){
				document.getElementById('footLeft').innerHTML=this.ds.reader.jsonData.footLeft;
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.footCenter&&document.getElementById('footCenter')){
				document.getElementById('footCenter').innerHTML=this.ds.reader.jsonData.footCenter;
			}
			if(this.ds.reader.jsonData&&this.ds.reader.jsonData.footRight&&document.getElementById('footRight')){
				document.getElementById('footRight').innerHTML=this.ds.reader.jsonData.footRight;
			}
			this.grid.getTopToolbar().doLayout();
			this.refresh();
			this.updateHeaderSortState();
			this.syncFocusEl(0);
		}
	},
	loadMask : true
});

Ext.reg('DynamicGridPanelAuto', App.ux.DynamicGridPanelAuto);
