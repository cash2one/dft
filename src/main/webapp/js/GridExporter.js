Ext.ns('Ext.ux.grid');

/**
 * Plugin (ptype = 'gridexporter') that adds export functionality for the grid.
 * 
 * @class Ext.ux.grid.GridExporter
 * @extends Ext.util.Observable
 * @author WangXiaohui
 * @ptype gridexporter
 */
Ext.ux.grid.GridExporter = Ext.extend(Ext.util.Observable, {

  mode : 'remote', // 'local','relay'
  mask : true,
  maxExportRows : 0,
  maskMsg : '导出中，请稍候……',
  exportExcelIconCls : 'icon-excel',
  exportPdfIconCls : 'icon-pdf',
  
  exportExcelText : '导出Excel',
  exportPdfText : '导出PDF',

  constructor : function(config) {
    Ext.apply(this, config);

    this.addEvents(
        /**
         * @event beforeexport
         */
        'beforeexport',
        /**
         * @event export
         */
        'export',
        /**
         * @event exportexception
         */
        'exportexception');

    Ext.ux.grid.GridExporter.superclass.constructor.call(this);
  },

  init : function(grid) {
    this.grid = grid;

    Ext.apply(grid, {
      exportExcel : this.exportExcel.createDelegate(this),
      exportPdf : this.exportPdf.createDelegate(this)
    });
  },

  buildColumns : function(cm) {
    var columns = [];
    for (var i = 0; i < cm.getColumnCount(); i++) {
      var hidden = false;
      if (cm.getColumnId(i) == 'numberer' || cm.getColumnId(i) == 'checker') {
        hidden = true;
      } else {
        hidden = cm.isHidden(i);
      }
      var col = {
        hidden : hidden,
        header : cm.getColumnHeader(i),
        dataIndex : cm.getDataIndex(i),
        width : cm.getColumnWidth(i),
        dataType : cm.config[i].dataType || '',
        format : cm.config[i].format || '',
        align : cm.config[i].align || '',
        isGroup : cm.config[i].isGroup || '0',
        isMultiUnit: cm.config[i].isMultiUnit || '0',
        renderer : cm.config[i].renderStr||""
      }
      columns.push(col);
    }
    return columns;
  },
  buildHeaderRows : function(cm) {
	if(!cm||!cm.rows){
		return "";
	}
	var rows = [];
	for (var i = 0; i < cm.rows.length; i++) {
		var r = cm.rows[i], cells = [];
		for (var j = 0; j < r.length; j++) {
			if (cm.getColumnId(j) == 'numberer' || cm.getColumnId(j) == 'checker') {
		        continue;
		     } 
			var c = r[j];
			c.colspan = c.colspan || 1;
			var col = {
			    colspan : c.colspan || 1,
			    header : c.header||""
			}
			cells.push(col);
		}
		rows.push(cells);
	}
	return rows;
  },
  buildData : function(store, cm) {
    var data = [];
    var lastOptions = store.lastOptions;
    store.load({
      params : {
        start : 0,
        limit : 60000
      }
    });
    for (var j = 0, len = store.getCount(); j < len; j++) {
      var rec = store.getAt(j);
      var row = {};
      for (var i = 0; i < cm.getColumnCount(); i++) {
        if (!(cm.isHidden(i) || cm.getColumnId(i) == 'numberer' || cm.getColumnId(i) == 'checker')) {
          var renderer = cm.getRenderer(i);
          var name = cm.getDataIndex(i);
          var value = rec.data[name];
          var p = {};
          try {
            value = renderer.call(cm.config[i], value, p, rec, j, i, store);
          } catch (err) {

          }
          if (typeof value === 'string') {
            value = value.replace(/<[^>]*>/g, '');
          }
          row[name] = value;
        }
      }
      data.push(row);
    }
    store.load(lastOptions);
    return data;
  },

  submitAsTarget : function(submitCfg) {
    var opt = submitCfg || {}, D = document, form = Ext.getDom(opt.form ? opt.form.form || opt.form : null, false, D)
        || Ext.DomHelper.append(D.body, {
          tag : 'form',
          cls : 'x-hidden x-export-form',
          encoding : 'multipart/form-data'
        }), formFly = Ext.fly(form, '_dynaForm'), formState = {
      target : form.target || '',
      method : form.method || '',
      encoding : form.encoding || '',
      enctype : form.enctype || '',
      action : form.action || ''
    }, encoding = opt.encoding || form.encoding, method = opt.method || 'POST';
    formFly.set({
      target : opt.target || '__download',// this.dom.name,
      method : method,
      encoding : encoding,
      action : opt.url
          || opt.action
          || form.action
          || Ext.ux.grid.GridExporter.PATH
    });
    if (method == 'POST' || !!opt.enctype) {
      formFly.set({
        enctype : opt.enctype || form.enctype || encoding
      });
    }
    var hiddens, hd, ps;
    if (opt.params && (ps = Ext.isFunction(opt.params) ? opt.params() : opt.params)) {
      hiddens = [];

      Ext.iterate(ps = typeof ps == 'string' ? Ext.urlDecode(ps, false) : ps, function(n, v) {
        Ext.fly(hd = D.createElement('input')).set({
          type : 'hidden',
          name : n,
          value : v
        });
        form.appendChild(hd);
        hiddens.push(hd);
      });
    }

    (function() {

      form.submit();
      hiddens && Ext.each(hiddens, Ext.removeNode, Ext);
      if (formFly.hasClass('x-export-form')) {
        formFly.remove();
      } else {
        formFly.set(formState);
      }
      formFly = null;
      this.grid.bwrap.unmask();
      this.fireEvent('export', this, submitCfg);
    }).defer(100, this);

  },

  exportGrid : function(exportCfg) {
    var opt = exportCfg || {};
    var grid = this.grid;
    if (grid && grid.getStore()&& grid.getStore().getCount() > 0) {
        return this.doExport(exportCfg);
    } else {
      Ext.Msg.alert('无法导出', '没有需要导出的数据');
      return false;
    }
  },

  doExport : function(exportCfg) {
    var opt = exportCfg || {};
    var maxExportRows = opt.maxExportRows || this.maxExportRows || 0;
    var totalCount = this.grid.getStore().getTotalCount();
    if ((maxExportRows > 0) && (totalCount > maxExportRows)) {
      Ext.Msg.confirm('导出数据限制', '系统最多能导出' + maxExportRows + '条记录，是否继续？', function(btn, text) {
        if (btn == 'yes') {
          return this.exportData(exportCfg);
        } else {
          return false;
        }
      }, this);
    } else {
      return this.exportData(exportCfg);
    }

  },

  exportData : function(exportCfg) {
    var opt = exportCfg || {};
    var grid = this.grid;
    if (grid && grid.getStore() /* && grid.getStore().getCount() > 0 */) {
      this.fireEvent('beforeexport', this, exportCfg);
      if (this.mask) {
        grid.bwrap.mask(this.maskMsg);
      }
      var mode = opt.mode || this.mode || 'local';
      var store = grid.getStore();
      var cm = grid.getColumnModel();
      var params = {
        format : opt.format || 'excel',
        filename : opt.filename || opt.title ||this.filename || grid.title || (grid.ownerCt ? grid.ownerCt.title : 'export')
            || 'export',
        columns : opt.columns || Ext.encode(this.buildColumns(cm)),
        groupRows : opt.groupRows ||Ext.encode(this.buildHeaderRows(cm)),
        maxExportRows : opt.maxExportRows || this.maxExportRows || 0,
        title : opt.title || grid.title || '',
        subject : opt.subject || this.subject || '',
        description : opt.description || this.description || '',
        rows : cm.rows ? Ext.encode(cm.rows) : '',
        url : opt.url || '',
        rangeMode: opt.rangeMode|| 0,
        expStart: opt.expStart|| 0,
        expEnd: opt.expEnd||opt.maxExportRows|| this.maxExportRows || 0,
        subTitle :opt.subTitle || '',
        foot :opt.foot || '',
        target : opt.target || self.name||'ifmExport' 
      };

      if (mode == 'local') {
        params.data = Ext.encode(this.buildData(store, cm));
        this.submitAsTarget({
          params : params,
          target : params.target
        });

      } else if (mode == 'remote') {
        var directFn = store.proxy.api[Ext.data.Api.actions.read] || store.proxy.directFn || store.directFn;
        params.action = opt.action || directFn.directCfg.action;
        params.method = opt.method || directFn.directCfg.method.name;
        params.paramOrder = store.proxy.paramOrder;
        var lastOptions = store.lastOptions || {};
        var p = lastOptions.params || {};
        p = Ext.apply({}, p, store.baseParams);
        p = opt.params || p || {};
        params.params = Ext.encode(p);
        this.submitAsTarget({
          params : params,
          url : params.url,
          target : params.target
        });
      } else {
        if (!this.exportStore) {
          this.exportStore = new store.constructor({
            proxy : store.proxy,
            reader : store.reader,
            listeners : {
              scope : this,
              load : function(ds, recs, opts) {
                params.data = Ext.encode(this.buildData(ds, cm));
                this.submitAsTarget({
                  params : params,
                  target : params.target
                });
                ds.removeAll();
              },
              loadexception : function() {
                this.fireEvent('exportexception', this);
              }
            }
          });

        }

        var lastOptions = Ext.apply({}, store.lastOptions, {
          params : {}
        });
        lastOptions.params.start = 0;
        lastOptions.params.limit = this.maxExportRows;
        this.exportStore.load(lastOptions);
      }
    } else {
      Ext.Msg.alert('无法导出', '没有需要导出的数据');
    }
  },

  exportPdf : function(exportPdfCfg) {
    var optPdfCfg = exportPdfCfg || {};
    Ext.apply(optPdfCfg, {
      format : "pdf"
    });
    this.exportGrid(optPdfCfg);
  },
  exportExcel : function(exportExcelCfg) {
    var optExcelCfg = exportExcelCfg || {format : "excel"};
    this.exportGrid(optExcelCfg);
  },
  exportCsv : function(exportCsvCfg) {
    var optCsvCfg = exportCsvCfg || {};
    Ext.apply(optCsvCfg, {
      format : "csv"
    });
    this.exportGrid(optCsvCfg);
  },

  destroy : function() {
    if (this.exportStore) {
      Ext.destroy(this.exportStore);
    }
  }

});
if (Ext.version >= 3) {
  Ext.preg('gridexporter', Ext.ux.grid.GridExporter);
}
Ext.ux.grid.GridExporter.PATH = 'utils/export.mt?doType=export';
Ext.ux.grid.GridExporter.fileType = {
  'pdf' : 'pdf',
  'excel' : 'xls'//,
  //'csv' : 'csv'
};