Ext.namespace('Query.widget');
Query.widget.ImplClassWindow = Ext.extend(Ext.Window, {
	clsPath: null,
	setClsPath : function(v) {
		this.clsPath = v;
	},
	getClsPath: function() {
		return this.clsPath ;
	},
	constructor : function(config) {
		Ext.apply(this, config);
		Query.widget.SqlWindow.superclass.constructor.call(this, config);
	}
});
var implClsForm = new Ext.FormPanel({
	frame: true,
	border: false,
	autoScroll:true,
  	layout : 'form',
  	labelWidth: 60,
	items:[{
		fieldLabel: "类路径",
		xtype: 'textfield',
		name: 'clsPath',
		width: 260
	}]
});

var implClassWindow = new Query.widget.ImplClassWindow({
	title : 'SQL',
	layout: 'fit',
	width : 400,
	height : 200,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	items : [implClsForm],
	buttons : [
	{
		text : "确定",
		handler : function(){
			implClassWindow.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			implClassWindow.onHideSetValue=Ext.emptyFn;
			implClassWindow.hide();
		}
	}],
	onHideSetValue : Ext.emptyFn
});
implClassWindow.on("show",function(){
	implClsForm.getForm().findField("clsPath").setValue(implClassWindow.getClsPath());
});
implClassWindow.on("hide",function(){
	var s = implClsForm.getForm().findField("clsPath").getValue();
	implClassWindow.setClsPath(s);
	implClassWindow.onHideSetValue.call(this.scope, s);
});

