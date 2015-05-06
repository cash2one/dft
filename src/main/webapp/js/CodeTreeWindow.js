Ext.namespace('App.widget');
App.widget.CodeTreeWindow = Ext.extend(App.widget.TreeWindow, {
	codeTable : null,
	checkModel : 'single',
	onlyLeafCheckable : false,
	directFn : null,
	paramOrder : ['table', 'selectedVals'],
	baseParams : {
		table : this.codeTable,
		selectedVals : this.defaultValue
	},
	constructor : function(config) {
		Ext.apply(this, config);
		this.baseParams = {
			table : this.codeTable,
			selectedVals : this.defaultValue
		}
		App.widget.CodeTreeWindow.superclass.constructor.call(this, config);

		this.tree.on("load", function(node) {
			var params = this.baseParams;
			var tTree = this.tree;
			if (params.selectedVals != null && params.selectedVals != "") { // 展开到
				CheckHandler.getBmPath(params.table, params.selectedVals,function(data) {
					var result = Ext.util.JSON.decode(data);
					if (result&&result.pathes) {
						for(var i=0;i<result.pathes.length;i++){
							var path = result.pathes[i];
							tTree.expandPath(tTree.getId() + "/"+ tTree.getRootNode().id + "/"+ path, 'id', function(bSuccess,oLastNode) {
								if (!bSuccess)
									return;
								if (oLastNode) {
									oLastNode.getUI().toggleCheck(true);
								}
							});
						}
					}
				});
			}
		}, this);
	}
});