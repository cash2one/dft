Ext.namespace('App.widget');
App.widget.ParamTreeWindow = Ext.extend(App.widget.TreeWindow, {
	rptID : null,
	codeTable : null,
	affectedBy : null,
	checkModel : 'single',
	onlyLeafCheckable : false,
	directFn : null,
	paramOrder : ['rptID','pName','affectedBy'],
	baseParams : {
		rptID : this.rptID,
		pName : this.codeTable,
		affectedBy: this.affectedBy,
		selectedVals : this.defaultValue
	},
	constructor : function(config) {
		Ext.apply(this, config);
		this.baseParams = {
			rptID : this.rptID,
			pName : this.codeTable,
			affectedBy: this.affectedBy,
			selectedVals : this.defaultValue
		}
		App.widget.ParamTreeWindow.superclass.constructor.call(this, config);
	}
});