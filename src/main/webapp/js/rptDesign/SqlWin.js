Ext.namespace('Query.widget');
Query.widget.SqlWindow = Ext.extend(Ext.Window, {
	sql: null,
	setSql : function(v) {
		this.sql = v;
	},
	getSql: function() {
		return this.sql ;
	},
	constructor : function(config) {
		Ext.apply(this, config);
		Query.widget.SqlWindow.superclass.constructor.call(this, config);
	}
});
var sqlForm = new Ext.FormPanel({
	frame: true,
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		hideLabel: true,
		xtype: 'textarea',
		name: 'sql',
		width: 370,
		height: 215
	}]
});

var sqlWindow = new Query.widget.SqlWindow({
	title : 'SQL',
	layout: 'fit',
	width : 400,
	height : 300,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	items : [sqlForm],
	buttons : [
	{
		text : "确定",
		handler : function(){
			sqlWindow.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			sqlWindow.onHideSetValue=Ext.emptyFn;
			sqlWindow.hide();
		}
	}],
	onHideSetValue : Ext.emptyFn
});
sqlWindow.on("show",function(){
	sqlForm.getForm().findField("sql").setValue(sqlWindow.getSql());
});
sqlWindow.on("hide",function(){
	var s = sqlForm.getForm().findField("sql").getValue();
	sqlWindow.setSql(s);
	sqlWindow.onHideSetValue.call(this.scope, s);
});

