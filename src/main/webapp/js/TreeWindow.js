Ext.namespace('App.widget');
App.widget.TreeWindow = Ext.extend(Ext.Window, {
	/**
	 * @cfg {String} checkModel 树的单选多选模式,'multiple':多选; 'single':单选;
	 *      'cascade':级联多选
	 */
	checkModel : 'multiple',
	/**
	 * @cfg {String} onlyLeafCheckable 只对树的叶子进行选择
	 */
	onlyLeafCheckable : false,
	/**
	 * @cfg {String} directFn 加载子节点的远程方法
	 */
	directFn : null,
	/**
	 * @cfg {Ext.tree.TreePanel} tree 弹出框中的树
	 */
	treeId : 'wTree',
	treeRootId : '-1',
	tree : null,
	rootVisible : true,
	/**
	 * @cfg {String} value 默认值
	 */
	defaultValue : null,
	
	value: null,
	/**
	 * @cfg {Object} baseParams 向后台传递的参数集合
	 */
	baseParams : {},
	paramOrder : ['table', 'selectedVals'],
	/**
	 * @cfg {boolean} canSetNull 是否有置空按钮
	 */
	canSetNull: true,
	title : '',
	width : 360,
	height : 420,
	constructor : function(config) {
		Ext.apply(this, config);
		this.tree = this.createTree();
		if(this.canSetNull){
			Ext.applyIf(config, {
				modal : true,
				autoScroll : true,
				bodyStyle : 'padding:5px;',
				layout : 'fit',
				closeAction : 'hide',
				items : [this.tree],
				buttons : [{
					text : "确定",
					handler : this.select.createDelegate(this)
				},{
					text : "置空",
					handler : this.setNull.createDelegate(this)
				}, {
					text : "取消",
					handler : this.cancel.createDelegate(this)
				}]
			});
		}else{
			Ext.applyIf(config, {
				modal : true,
				autoScroll : true,
				bodyStyle : 'padding:5px;',
				layout : 'fit',
				closeAction : 'hide',
				items : [this.tree],
				buttons : [{
					text : "确定",
					handler : this.select.createDelegate(this)
				},{
					text : "取消",
					handler : this.cancel.createDelegate(this)
				}]
			});
		}
		App.widget.TreeWindow.superclass.constructor.call(this, config);

		this.tree.on('click', function(node) {
			if (!node.isLeaf()) {
				node.toggle();
			}
		});
	},
	select : function() {
		var value = {
			id : this.tree.getChecked('id').join(),
			text : this.tree.getChecked('text').join()
		}
		if (this.checkModel === 'single') {
			value.id = this.tree.getChecked('id')[0] || '';
			value.text = this.tree.getChecked('text')[0] || '';
		}
		this.setValue(value);
		this.onSelect.call(this.scope, value);
		this.hide();
	},
	onSelect : Ext.emptyFn,
	cancel : function() {
		this.hide();
	},
	setNull: function(){
		var value = {
			id : "",
			text : ""
		}
		this.setValue(value);
		this.onSelect.call(this.scope, value);
		this.hide();
	},
	createTree : function() {
		var tree = new Ext.tree.TreePanel({
			id : this.treeId,
			checkModel : this.checkModel, // 树节点是否多选
			onlyLeafCheckable : this.onlyLeafCheckable,// 对树所有结点都可选
			autoScroll : true,
			animate : true,
			containerScroll : true,
			rootVisible : this.rootVisible,
			loader : new Ext.tree.TreeLoader({
				directFn : this.directFn,
				//nodeParameter : 'node',
				baseAttrs : {
					uiProvider : Ext.tree.TreeCheckNodeUI
				},
				baseParams : this.baseParams,
				paramOrder : this.paramOrder
			}),
			root : new Ext.tree.AsyncTreeNode({
				id : this.treeRootId,
				text : '全部'
			})
		});
		return tree;
	},
	setValue : function(v) {
		this.value = v;
	},
	setTreeParams : function(p) {
		this.baseParams = p;
		this.tree.loader.baseParams = this.baseParams;
	},
	refreshTree : function() {
		this.tree.getRootNode().reload();
	}
});