<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.system.entity.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>DNFT</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<style type="text/css">
.greenNode {
	background-color:#C3FF8F !important;
	color:black;
	font-weight:bold;
}
.redNode {
	background-color:#ed6b27 !important;
	color:black;
	font-weight:bold;
}
/* Fixes for IE6/7 trigger fields */
.ext-ie6 .x-row-editor .x-form-field-wrap .x-form-trigger, .ext-ie7 .x-row-editor .x-form-field-wrap .x-form-trigger {
  top: 1px;
}
.STYLE1 {font-size: 12px}
</style>
<script>
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
var cTree;
var cNode ;//选中节点
var cMode = "add";
var cAddMode = 0;
var cWho = 9;
var gtb ="", dtb = "",ftb = "",gname="",dname="",fname="";
var mapDir=9;
//编码表选择
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect:true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
	    {
	        header: "编码表",
	        dataIndex: 'table_bm',
	        width: 110,
	        align: 'left'    
	    },{
	        header: "编码表名",
	        dataIndex: 'name',
	        width: 120,
	        align: 'left'    
	    }
	],
	defaultSortable: false
});
var tRecord = Ext.data.Record.create([
    {name: 'table_bm', type: 'string' },
    {name: 'name', type: 'string' }
]);
var tbStore = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
  		directFn: CodeHandler.getCodeTables,
  		paramOrder: ['who'],
  		paramsAsHash: false
  	}), 
    reader: new Ext.data.JsonReader({}, tRecord)
});
tbStore.on("beforeload",function(){
	tbStore.baseParams.who=cWho;
});
var view = new Ext.grid.GridView();
var tbGrid = new Ext.grid.GridPanel({
		title:'',
		store: tbStore,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		stripeRows: true,
		selModel: ssm
});
var tbWin = new Ext.Window({
	title : '编码表',
	width : 320,
	height : 280,
	autoScroll : true,
	layout : 'fit',
	items : [tbGrid],
	closeAction:'hide',
	buttons : [{
		text : "确定",
		handler:function(){
			var records = tbGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择编码!");
				return;
			}		
			var rd = records[0];
			//获取对应表，写入trigger
			var tb = rd.get("table_bm");
			CodeHandler.getTableMappingInfo(tb,cWho,1,function(data){
				if(data!=""){
				   	var result = Ext.util.JSON.decode(data);
				   	if(result){
					   	gtb = result.gs;
				   		dtb = result.ds;
				   		ftb = result.f;
				   		gname = result.gname;
				   		dname = result.dname;
				   		fname = result.fname;
				   		ds_tg.setValue(dname);
				   		gs_tg.setValue(gname);
				   		fn_tg.setValue(fname);
				   		dsTree.root.setText(dname);
				   		gsTree.root.setText(gname);
				   		fTree.root.setText(fname);
				   		reloadTrees();
				   	}
				}
			});
			tbWin.hide();
		}
	},{
        text : "取消",
        handler : function() {
			tbWin.hide();
        }
    }]
});
tbWin.on("show",function(){
	if(cWho==0){
		tbWin.setTitle("地税编码表");
	}else if(cWho==1){
		tbWin.setTitle("国税编码表");
	}else{
		tbWin.setTitle("财政编码表");
	}
	tbStore.load();
});
//三个triggerField
var ds_tg = new Ext.form.TriggerField({
    fieldLabel:'地税',
    width:100,
    editable: false,
	name:'ds_tg'
});
ds_tg.onTriggerClick=dsTrigFun;
function dsTrigFun(e){
	cWho=0;
	tbWin.show();
}
var gs_tg = new Ext.form.TriggerField({
    fieldLabel:'国税',
    width:100,
    editable: false,
	name:'gs_tg'
});
gs_tg.onTriggerClick=gsTrigFun;
function gsTrigFun(e){
	cWho=1;
	tbWin.show();
}
var fn_tg = new Ext.form.TriggerField({
    fieldLabel:'财政',
    width:100,
    editable: false,
	name:'fn_tg'
});
fn_tg.onTriggerClick=fnTrigFun;
function fnTrigFun(e){
	cWho=9;
	tbWin.show();
}
//-------------------------------------tree start -----------------------------------------------
//树形的右键菜单
var treeRightMenu = new Ext.menu.Menu({
	id: 'theContextMenu',
	items:[
	{
		id:'addNode',
		text:'添加',
		menu:[
			{
				id:'insertNode',
				text:'添加同级节点'
			},
			{
				id:'appendNode',
				text:'添加子节点'
			}
		]
	},'-',
	{
		id:'modifyNode',
		text:'修改'
	},'-',
	{
		id:'delNode',
		text:'删除'
	}
	]
});
//****绑定右键菜单事件*************
Ext.getCmp('appendNode').on('click',appendNode);
Ext.getCmp('delNode').on('click',deleteNode);
Ext.getCmp('insertNode').on('click',insertNode);
Ext.getCmp('modifyNode').on('click',modifyNode);
function moveNode(who,table_bm,node,oldParent,newParent){
	var msg = "当前节点“"+node.text+"”已从“"+oldParent.text+"”下移到了“"+newParent.text+"”下。";
	CodeHandler.moveCode(who,table_bm,node.id,oldParent.id,newParent.id,function(data){
		if(data&&data!=""){
			var result = Ext.util.JSON.decode(data);
			if(result&&result.success){
				Ext.Msg.alert("提示",msg);
				if(!oldParent.hasChildNodes()){
					oldParent.ui.updateExpandIcon();
				}
			}else{
				Ext.Msg.alert("警告","编码改变层级关系时发生错误！");
			}
		}
	});
}
//财政树型面板
var froot = new Ext.tree.AsyncTreeNode({ 
	id:'tree-root',
	allowDrag :false,
	text: fname
});
var fTree = new Ext.tree.TreePanel({   
    id: 'fTree', 
    layout:'fit',  
    root: froot,
    collapsible: true,
    split: true, 
    enableDD:true, 
    ddAppendOnly: true,   
    animate: false,   
    rootVisible: true,   
    autoScroll:true, 
    title:'',
    frame :true,
    loader: new Ext.tree.TreeLoader({
        directFn: CodeHandler.getFCodesTree,
        paramsAsHash: false,
        paramOrder: ['tbBm','pid','dir']
    }),
    tbar:[
 		{
           	xtype:'label',
           	text: '编码名称：'
        },{
		    xtype : 'textfield',
			id: 'searchForF',
			allowBlank: true,
			width: 120
 		},{
		    text: '查找',
			iconCls: 'filter',
		    handler : function(){
 				var searchFor = Ext.getCmp("searchForF").getValue();
 				cTree = fTree;
		    	searchForNode(searchFor);
 			}
	    }
	],
    tools:[{
		id:'refresh',
		handler:function(){
			var tree = Ext.getCmp('fTree');
			tree.root.reload();
			tree.body.mask('数据加载中……', 'x-mask-loading');
			tree.root.expand(false,false,function(){
				tree.body.unmask();
			});
		}
	}]   
});

fTree.on('beforemovenode',function(tree, node, oldParent, newParent, index ){
	moveNode(9,ftb,node, oldParent, newParent);
});
fTree.getLoader().on("beforeload", function(tl, node) {
	fTree.getLoader().baseParams.tbBm=ftb;
	fTree.getLoader().baseParams.pid=(!node||node==fTree.getRootNode())?"":node.id;
	fTree.getLoader().baseParams.dir=mapDir;
}, this);
fTree.getSelectionModel().on("beforeselect",function(sl,n,o){
	if(mapDir==9){
		n.getUI().addClass("greenNode"); 
		try{
			o.getUI().removeClass("greenNode"); 
		}catch(err){}
		var dssl = dsTree.getSelectionModel().getSelectedNode();
		if(dssl){
			try{
				dssl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
		var gssl = gsTree.getSelectionModel().getSelectedNode();
		if(gssl){
			try{
				gssl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
	}else{
		n.getUI().addClass("redNode"); 
		try{
			o.getUI().removeClass("redNode"); 
		}catch(err){}
	}
});
fTree.getSelectionModel().on('selectionchange', function(sm, node){ 
    if(!node)return;
	cTree=fTree;
	cNode = fTree.getSelectionModel().getSelectedNode();
	if(mapDir==9){
		expandMapping(9,fTree.getSelectionModel().getSelectedNode());
	}
});
fTree.on("checkchange",function(node,checked){
	//从未选中改变为选中，将其余选中的除掉
	if(checked){
		var fNodes = fTree.getChecked();
		if(fNodes&&fNodes.length >0){
			for(var i=0;i<fNodes.length;i++){
				var tnode = fNodes[i];
				if(tnode!=node){
					tnode.getUI().toggleCheck(false);
				}
			}
		}
	}
});
//地税树
var dsTree = new Ext.tree.TreePanel({   
    id: 'dsTree',   
    collapsible: true,
    split: true,    
    animate: false,   
    rootVisible: true, 
    autoScroll:true,
    enableDD:true,// 是否支持拖放   
    title:'',
    frame :true,
    loader: new Ext.tree.TreeLoader({
        directFn: CodeHandler.getTCodesTree,
        paramsAsHash: false,
        paramOrder: ['tbBm','who','pid','dir']
    }),
    tbar:[
   		{
             xtype:'label',
             text: '编码名称：'
        },{
  		    xtype : 'textfield',
  			id: 'searchForDs',
  			allowBlank: true,
  			width: 120
   		},{
  		    text: '查找',
  			iconCls: 'filter',
  		    handler : function(){
   				var searchFor = Ext.getCmp("searchForDs").getValue();
   				cTree = dsTree;
  		    	searchForNode(searchFor);
   			}
  	    }
  	],
  	tools:[{
		id:'refresh',
		handler:function(){
			var tree = Ext.getCmp('dsTree');
			tree.root.reload();
			tree.body.mask('数据加载中……', 'x-mask-loading');
			tree.root.expand(false,false,function(){
				tree.body.unmask();
			});
		}
	}]   
});
var dsroot = new Ext.tree.AsyncTreeNode({ 
	id:'tree-root-ds',
	text: dname
});
dsTree.setRootNode(dsroot);
dsTree.getLoader().on("beforeload", function(tl, node) {
	dsTree.getLoader().baseParams.tbBm=dtb;
	dsTree.getLoader().baseParams.pid=(!node||node==dsTree.getRootNode())?"":node.id;
	dsTree.getLoader().baseParams.who=0;
	dsTree.getLoader().baseParams.dir=mapDir;
}, this); 
dsTree.getSelectionModel().on("beforeselect",function(sl,n,o){
	if(mapDir==0){
		n.getUI().addClass("greenNode"); 
		try{
			o.getUI().removeClass("greenNode"); 
		}catch(err){}
		var fsl = fTree.getSelectionModel().getSelectedNode();
		if(fsl){
			try{
				fsl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
		var gssl = gsTree.getSelectionModel().getSelectedNode();
		if(gssl){
			try{
				gssl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
	}else{
		n.getUI().addClass("redNode"); 
		try{
			o.getUI().removeClass("redNode"); 
		}catch(err){}
	}
});
dsTree.getSelectionModel().on('selectionchange', function(sm, node){ 
    if(!node)return;
	cTree=dsTree;
	cNode = dsTree.getSelectionModel().getSelectedNode();
	if(mapDir==0){
		expandMapping(0,dsTree.getSelectionModel().getSelectedNode());
	}
});
dsTree.on('beforemovenode',function(tree, node, oldParent, newParent, index ){
	moveNode(0,dtb,node, oldParent, newParent);
});
//国税树
var gsTree = new Ext.tree.TreePanel({   
    id: 'gsTree', 
    layout:'fit',  
    collapsible: true,
    split: true,    
    animate: false,   
    rootVisible: true, 
    autoScroll:true,
    enableDD:true,// 是否支持拖放   
    title:'',
    frame :true,
    loader: new Ext.tree.TreeLoader({
        directFn: CodeHandler.getTCodesTree,
        paramsAsHash: false,
        paramOrder: ['tbBm','who','pid','dir']
    }),
    tbar:[
     	{
        	xtype:'label',
            text: '编码名称：'
         },{
    		xtype : 'textfield',
    		id: 'searchForGs',
    		allowBlank: true,
    		width: 120
     	},{
    		text: '查找',
    		iconCls: 'filter',
    		handler : function(){
     			var searchFor = Ext.getCmp("searchForGs").getValue();
     			cTree = gsTree;
    		    searchForNode(searchFor);
     		}
    	}
    	],
    tools:[{
		id:'refresh',
		handler:function(){
			var tree = Ext.getCmp('gsTree');
			tree.root.reload();
			tree.body.mask('数据加载中……', 'x-mask-loading');
			tree.root.expand(false,false,function(){
				tree.body.unmask();
			});
		}
	}]   
});
var gsroot = new Ext.tree.AsyncTreeNode({ 
	id:'tree-root-gs',
	text: gname
});
gsTree.setRootNode(gsroot);
gsTree.getLoader().on("beforeload", function(tl, node) {
	gsTree.getLoader().baseParams.tbBm=gtb;
	gsTree.getLoader().baseParams.pid=(!node||node==gsTree.getRootNode())?"":node.id;
	gsTree.getLoader().baseParams.who=1;
	gsTree.getLoader().baseParams.dir=mapDir;
}, this); 
gsTree.getSelectionModel().on("beforeselect",function(sl,n,o){
	if(mapDir==1){
		n.getUI().addClass("greenNode"); 
		try{
			o.getUI().removeClass("greenNode"); 
		}catch(err){}
		var dssl = dsTree.getSelectionModel().getSelectedNode();
		if(dssl){
			try{
				dssl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
		var fsl = fTree.getSelectionModel().getSelectedNode();
		if(fsl){
			try{
				fsl.getUI().removeClass("redNode"); 
			}catch(err){}
		}
	}else{
		n.getUI().addClass("redNode"); 
		try{
			o.getUI().removeClass("redNode"); 
		}catch(err){}
	}
});
gsTree.getSelectionModel().on('selectionchange', function(sm, node){ 
    if(!node)return;
	cTree=gsTree;
	cNode = gsTree.getSelectionModel().getSelectedNode();
	if(mapDir==1){
		expandMapping(1,gsTree.getSelectionModel().getSelectedNode());
	}
});
gsTree.on('beforemovenode',function(tree, node, oldParent, newParent, index ){
	moveNode(1,gtb,node,oldParent,newParent);
});
var itemSearch;
function searchForNode(searchFor){
	var chosenNode = cTree.getSelectionModel().getSelectedNode();
    var selectedNodeID='';
    if(chosenNode){
    	selectedNodeID = chosenNode.id;
    }
	if(searchFor=='') return;
	var startNodeID = null; //New search 新的查找
	if(itemSearch != '' && itemSearch == searchFor) {// Find next 查找下一个
		if(selectedNodeID && selectedNodeID.substr(0,9)!= 'tree-root'){
	 		startNodeID = selectedNodeID ;
		}
    }
    var table = ftb,cwho = 9;
    if(cTree == dsTree){
        table = dtb;
        cwho=0;
    }else if(cTree == gsTree){
    	table = gtb;
    	cwho=1;
    }
	itemSearch = searchFor;
	//调用服务端查找
	CodeHandler.searchForCode(table,cwho,searchFor,startNodeID,function(data){
		if(data&&data!=""){
			var result = Ext.util.JSON.decode(data);
			if(result.match=='no'){ 
				Ext.Msg.alert( "信息","没有匹配节点！");
			}else{//展开路径，注意Path是以节点id加上/来间隔的。
				if(cTree==dsTree){
					dsTree.expandPath('dsTree/tree-root-ds/' + result.path, 'id', onExpandChoose);
				}else if(cTree==gsTree){
					gsTree.expandPath('gsTree/tree-root-gs/' + result.path, 'id', onExpandChoose);
				}else{
					fTree.expandPath('fTree/tree-root/' + result.path, 'id', onExpandChoose);
				}
			}
		}
	});
}
function onExpandChoose(bSuccess, oLastNode) {
	if(!bSuccess) return;
	if(oLastNode){
  		oLastNode.ensureVisible();
  		oLastNode.select();
  		oLastNode.fireEvent('click', oLastNode);
	}
}
//根据选中的树节点展开映射
function expandMapping(mapDir,node){
	CodeHandler.getMappingPath(mapDir,ftb,""+node.id,function(data){
		if(data&&data!=""){
			var result = Ext.util.JSON.decode(data);
			if(mapDir<9){
				//清除之前选中
				var fNodes = fTree.getChecked();
				unCheckNodes(fNodes);
				//展开新映射
				fTree.expandPath('fTree/tree-root/' + result.fPath, 'id', onExpandPathComplete);
			}else{
				var dsPathes = result.dPaths;
				var gsPathes = result.gPaths;
				//清除之前选中
				var dNodes = dsTree.getChecked();
				unCheckNodes(dNodes);
				var gNodes = gsTree.getChecked();
				unCheckNodes(gNodes);
				//打开地税树
				if(dsPathes && dsPathes.length>0){
					for(var i=0;i<dsPathes.length;i++){
						var path = dsPathes[i];
						dsTree.expandPath('dsTree/tree-root-ds/' + path, 'id', onExpandPathComplete);
					}
				}
				//打开国税树
				if(gsPathes && gsPathes.length>0){
					for(var i=0;i<gsPathes.length;i++){
						var path = gsPathes[i];
						gsTree.expandPath('gsTree/tree-root-gs/' + path, 'id', onExpandPathComplete);
					}
				}
			}
		}
	});
}
function unCheckNodes(dNodes){
	if(dNodes&&dNodes.length >0){
		for(var i=0;i<dNodes.length;i++){
			var tnode = dNodes[i];
			tnode.getUI().toggleCheck(false);
		}
	}
}
function onExpandPathComplete(bSuccess, oLastNode) {
	if(!bSuccess) return;
	if(oLastNode){
		oLastNode.getUI().toggleCheck(true);
	}
}
//给tree添加事件
fTree.on('contextmenu',function(node,e){
	addMenu(fTree,node,e);
});
dsTree.on('contextmenu',function(node,e){
	addMenu(dsTree,node,e);
});
gsTree.on('contextmenu',function(node,e){
	addMenu(gsTree,node,e);
});
function addMenu(tree,node,e){
	if(node == tree.root){
		Ext.getCmp('delNode').hide();
		Ext.getCmp('addNode').show();
		Ext.getCmp('appendNode').show();
		Ext.getCmp('insertNode').hide();
		Ext.getCmp('modifyNode').hide;
	}else{
		Ext.getCmp('delNode').show();
		Ext.getCmp('addNode').show();
		Ext.getCmp('appendNode').show();
		Ext.getCmp('insertNode').show();
		Ext.getCmp('modifyNode').show;
	}
	e.preventDefault();
	node.select();
	cTree=tree;
	treeRightMenu.showAt(e.getXY());
}
function deleteNode(){
	var msg = "您真的要删除该编码吗?";
	var selectedNode = cTree.getSelectionModel().getSelectedNode();//选中节点
	var cNode=selectedNode;
	if(!selectedNode.leaf){
		msg = "您真的要删除该编码吗?此操作会删除其下所有子节点";
	}
	Ext.Msg.confirm("确认删除",msg,
		function(btn){
			if(btn=='yes'){
				var selectedNode = cTree.getSelectionModel().getSelectedNode();//得到选中的节点
				var cBm = ftb,cSource=9;
				if(cTree==dsTree){
					cBm = dtb;
					cSource=0;
				}else if(cTree==gsTree){
					cBm = gtb;
					cSource=1;
				}
				CodeHandler.deleteCode(ftb,cBm,selectedNode.id,cSource,selectedNode.attributes.pid,function(data){
					if(data&&data!=""){
						var result = Ext.util.JSON.decode(data);
	    			   	if(result&&result.success){
							Ext.Msg.alert('成功',"删除成功！！");
							if(selectedNode.parentNode.childNodes.length==1){
								selectedNode.parentNode.leaf = true;
								selectedNode.parentNode.cls = "file";
							}
							var pnode=selectedNode.parentNode;
							selectedNode.remove();
							pnode.select();
						}else{
							Ext.Msg.alert('失败',"删除节点过程中发生错误！");
						}
					}
				});
			}
		}
	);
}
function appendNode(){//增加子节点
	cNode = cTree.getSelectionModel().getSelectedNode();//选中节点
	nodeWin.show();
	var cBm = ftb,cName = fname;
	if(cTree==dsTree){
		cBm = dtb;
		cName=dname;
	}else if(cTree==gsTree){
		cBm = gtb;
		cName=gname;
	}
	Ext.getCmp('bm').enable();
	Ext.getCmp('bm').setValue("");
   	Ext.getCmp('mc').setValue("");
   	Ext.getCmp('qy_bj').setValue(1);
   	Ext.getCmp('isleaf').setValue(1);
	Ext.getCmp('table_bm').setValue(cBm);
   	Ext.getCmp('tablename').setValue(cName);
   	if(cNode.id.substr(0,9)=='tree-root'){
   		Ext.getCmp('codeLevel').setValue(1);
   	}else{
   		Ext.getCmp('codeLevel').setValue(cNode.attributes.codelevel+1);
   	}
   	Ext.getCmp('pid').setValue(cNode.id.substr(0,9)=='tree-root'?"":cNode.id);
   	cMode = "add";
   	cAddMode =0;
}
function insertNode(){//增加同级节点
	Ext.getCmp('bm').enable();
	cNode = cTree.getSelectionModel().getSelectedNode();//选中节点
	var cBm = ftb,cName = fname;
	if(cTree==dsTree){
		cBm = dtb;
		cName=dname;
	}else if(cTree==gsTree){
		cBm = gtb;
		cName=gname;
	}
	nodeWin.show();
	cMode = "add";
   	cAddMode =1;
   	Ext.getCmp('bm').setValue("");
   	Ext.getCmp('mc').setValue("");
   	Ext.getCmp('qy_bj').setValue(1);
   	Ext.getCmp('isleaf').setValue(1);
	Ext.getCmp('table_bm').setValue(cBm);
   	Ext.getCmp('tablename').setValue(cName);
   	Ext.getCmp('codeLevel').setValue(cNode.attributes.codelevel);
   	Ext.getCmp('pid').setValue(cNode.attributes.pid.substr(0,9)=='tree-root'?"":cNode.attributes.pid);
}
function modifyNode(){
	cMode = "update";
	cNode = cTree.getSelectionModel().getSelectedNode();//选中节点
	var cBm = ftb,cSource=9,cName=fname;
	if(cTree==dsTree){
		cBm = dtb;
		cName=dname;
		cSource=0;
	}else if(cTree==gsTree){
		cBm = gtb;
		cName=gname;
		cSource=1;
	}
	Ext.getCmp('bm').disable();
	CodeHandler.getCode(cBm,cSource,cNode.id,function(data){
		if(data){
		   	var obj = data;
		   	if(obj){
				Ext.getCmp('bm').setValue(obj.bm);
		       	Ext.getCmp('mc').setValue(obj.mc);
		       	Ext.getCmp('pid').setValue(obj.pid);
		       	Ext.getCmp('qy_bj').setValue(obj.qybj);
		       	Ext.getCmp('isleaf').setValue(obj.isleaf);
		    	Ext.getCmp('table_bm').setValue(cBm);
		       	Ext.getCmp('tablename').setValue(cName);
		       	Ext.getCmp('codeLevel').setValue(obj.codelevel);
		   	}else{
				Ext.getCmp('bm').setValue("");
		       	Ext.getCmp('mc').setValue("");
		       	Ext.getCmp('pid').setValue("");
		       	Ext.getCmp('qy_bj').setValue(1);
		       	Ext.getCmp('isleaf').setValue("");
		    	Ext.getCmp('table_bm').setValue("");
		       	Ext.getCmp('tablename').setValue("");
		       	Ext.getCmp('codeLevel').setValue(1);
		   	}
		}
	});
	nodeWin.show();
}
var iForm = new Ext.FormPanel({
    id: 'iForm',
    region: 'center',
    frame: true,
    bodyStyle:'padding:5px',
    labelWidth :70,
    labelAlign:'right',
    api: {
        submit: CodeHandler.saveCode
    },
    paramOrder: ['cMode','table_bm','bm','who'],
    buttonAlign : 'left',
    items: [
    	    {
	        	id: "bm",
	            name: "bm",
	            xtype:'textfield',
	            fieldLabel: '编码',
	            allowBlank :false,
	            width: 120
		    },{
	            id: "mc",
	            name: "mc",
	            xtype:'textfield',
	            width: 120,
	            fieldLabel: '名称',
	            allowBlank :false,
	            blankText : "请输入名称"
	        },{
	        	id: "pid",
	            name: "pid",
	            xtype:'hidden',
	            fieldLabel: '父编码',
                hideLabel: true
		    },{
	            id: "qy_bj",
	            name: "qy_bj",
	            xtype:'combo',
	            fieldLabel: '是否启用',
		        width:100,
		        mode : 'local', 
		        triggerAction : 'all', 
		        hiddenName:'qybj',
		        forceSelection:true,
		        store : new Ext.data.SimpleStore({ 
		        	fields : ["Id", "text"], 
		        	data : [ 
		        	['1', '是'], 
		        	['0', '否']
		         	] 
		        }),
		        valueField : "Id", 
		        displayField : "text", 
		        readOnly: false
			},{
	        	id: "isleaf",
	            name: "isleaf",
	            xtype:'hidden',
	            fieldLabel: '是否底级',
                hideLabel: true
		    },{
	        	id: "table_bm",
	            name: "table_bm",
	            xtype:'hidden',
	            allowBlank :false,
	            width: 100
		    },{
	        	id: "tablename",
	            name: "tablename",
	            xtype:'textfield',
	            style:'background:none;border:0px;',
	            fieldLabel: '所属编码表',
	            allowBlank :false,
	            width: 150
		    },{
				id: "codeLevel",
			    name: "codeLevel",
			    xtype:'hidden',
			    hideLabel: true
			}
    ]
});
var nodeWin=new Ext.Window({
	title : '编码表',
	width : 320,
	height : 220,
	autoScroll : true,
	layout : 'fit',
	items : [iForm],
	closeAction:'hide',
    buttons :[{
        xtype:'button',
        name: 'save',
        id: 'save',
        text: '确定',
        disabled :false,
        listeners: {'click':
            function save(){
			    if(!iForm.getForm().isValid()){
					return;
				}
				var tmpBm = iForm.getForm().findField("bm").getValue();
			    var cSource=9;
				if(cTree==dsTree){
					cSource=0;
				}else if(cTree==gsTree){
					cSource=1;
				}
			    iForm.getForm().submit({
			        params: {bm: tmpBm,cMode: cMode,who:cSource},
			        success : successfn,
			        failure : function(form,action) {
			            Ext.Msg.alert('失败', "保存节点信息失败！");
			        }
			    });			                         
			}
	    }
	},{
        text : "取消",
        handler : function() {
			nodeWin.hide();
        }
    }]
});
function successfn(form,action){
	if(cMode== "add"){	//如果是新增
		var obj = action.result;
		if(obj&&obj.infos){
			Ext.Msg.show({title:'成功',
				msg: obj.infos.msg,
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO});
			var newNode ;
			if(mapDir<9&&cTree==fTree){
				newNode = new Ext.tree.TreeNode({
					id: iForm.getForm().findField('bm').getValue(),
					text: iForm.getForm().findField('mc').getValue()+"("+iForm.getForm().findField('bm').getValue()+")",
					pid: iForm.getForm().findField('pid').getValue(),
					leaf: true,
				    cls:"file",
				    checked: false
				});
			}else if(mapDir==9&&cTree==dsTree){
				newNode = new Ext.tree.TreeNode({
					id: iForm.getForm().findField('bm').getValue(),
					text: iForm.getForm().findField('mc').getValue()+"("+iForm.getForm().findField('bm').getValue()+")",
					pid: iForm.getForm().findField('pid').getValue(),
					leaf: true,
				    cls:"file",
				    checked: false
				});
			}else if(mapDir==9&&cTree==gsTree){
				newNode = new Ext.tree.TreeNode({
					id: iForm.getForm().findField('bm').getValue(),
					text: iForm.getForm().findField('mc').getValue()+"("+iForm.getForm().findField('bm').getValue()+")",
					pid: iForm.getForm().findField('pid').getValue(),
					leaf: true,
				    cls:"file",
				    checked: false
				});
			}else{
				newNode = new Ext.tree.TreeNode({
					id: iForm.getForm().findField('bm').getValue(),
					text: iForm.getForm().findField('mc').getValue()+"("+iForm.getForm().findField('bm').getValue()+")",
					pid: iForm.getForm().findField('pid').getValue(),
					leaf: true,
				    cls:"file"
				});
			}
			if(cMode=="add"){
				if(cAddMode==0){//如果是增加下级节点
					cNode.leaf = false;
					cNode.appendChild(newNode);
					newNode.parentNode.collapse();				
					newNode.parentNode.expand(true,false,function(){
						cTree.getSelectionModel().select(newNode);
					});//将上级树形展开
				}else{
					var selectedParentNode = cNode.parentNode;//选中节点父节点
					if(selectedParentNode){
						selectedParentNode.insertBefore(newNode,cNode);
					}
				}
			}
		}
	}else{
		Ext.Msg.alert('成功', "更新编码成功！");
		cNode.id = iForm.getForm().findField('bm').getValue();
		cNode.text =iForm.getForm().findField('mc').getValue();
		cNode.setText(iForm.getForm().findField('mc').getValue()+"("+iForm.getForm().findField('bm').getValue()+")");
	}
	nodeWin.hide();
}
function reloadTrees(){
	fTree.root.reload();
	fTree.root.expand(false,false,function(){
		fTree.body.unmask();
	});
	dsTree.root.reload();
	dsTree.root.expand(false,false,function(){
		dsTree.body.unmask();
		if(noMapClick){
			noMapClick = false;
			dsTree.expandPath('dsTree/tree-root-ds/' + noMapPath, 'id', onExpandChoose);
		}
	});
	gsTree.root.reload();
	gsTree.root.expand(false,false,function(){
		gsTree.body.unmask();
		if(noMapClick){
			noMapClick = false;
			gsTree.expandPath('gsTree/tree-root-gs/' + noMapPath, 'id', onExpandChoose);
		}
	});
}
function saveMappingF2T(){
	if(mapDir!=9){return;}
	var fnode=fTree.getSelectionModel().getSelectedNode();
	if(!fnode){
		Ext.Msg.alert("提示","请选择要操作的财政编码!");
		return;
	}
	var fbm = fnode.id;
	var ocodes = new Object();
	var dsbms = new Array(),gsbms = new Array();
	var dsNodes = dsTree.getChecked();
	if(dsNodes&&dsNodes.length>0){
		for(var i=0;i<dsNodes.length;i++){
			dsbms.push(dsNodes[i].id);
		}
	}
	var gsNodes = gsTree.getChecked();
	if(gsNodes&&gsNodes.length>0){
		for(var i=0;i<gsNodes.length;i++){
			gsbms.push(gsNodes[i].id);
		}
	}
	ocodes.ds=dsbms;
	ocodes.gs=gsbms;
	Ext.Msg.wait("正在保存...");
	CodeHandler.saveMappingF2T(ftb,dtb,gtb,fbm,Ext.encode(ocodes),function(data){
		if(data!=""){
			Ext.Msg.hide();
		   	var result = Ext.util.JSON.decode(data);
		   	if(result&&result.success){
				Ext.Msg.alert('成功',"保存成功！！");
			}else{
				Ext.Msg.alert('失败',"保存过程中发生错误！");
			}
		   	CodeHandler.getNotMappingCount(function(data){
				if(data!=""){
				   	var result = Ext.util.JSON.decode(data);
				   	var ds = result.ds;
				   	var gs = result.gs;
				   	Ext.getCmp("lbDs").setText("地税:共"+ds+"个未映射编码");
				   	Ext.getCmp("lbGs").setText("国税:共"+gs+"个未映射编码");
				}
			});
		}
	});
}
function saveMappingT2F(){
	if(mapDir==9){return;}
	var tTable = (mapDir==1)?gtb:dtb;
	var taxTree = (mapDir==1)?gsTree:dsTree;
	var tnode=taxTree.getSelectionModel().getSelectedNode();
	if(!tnode){
		Ext.Msg.alert("提示","请选择要进行映射操作的税务编码!");
		return;
	}
	var t_bm = tnode.id;
	var fnodes=fTree.getChecked();
	var fbm = fnodes&&fnodes.length>0?fnodes[0].id:"";
	Ext.Msg.wait("正在保存...");
	CodeHandler.saveMappingT2F(mapDir,ftb,tTable,fbm,t_bm,function(data){
		Ext.Msg.hide();
		if(data!=""){
		   	var result = Ext.util.JSON.decode(data);
		   	if(result&&result.success){
				Ext.Msg.alert('成功',"保存成功！！");
			}else{
				Ext.Msg.alert('失败',"保存过程中发生错误！");
			}
		   	CodeHandler.getNotMappingCount(function(data){
				if(data!=""){
				   	var result = Ext.util.JSON.decode(data);
				   	var ds = result.ds;
				   	var gs = result.gs;
				   	Ext.getCmp("lbDs").setText("地税:共"+ds+"个未映射编码");
				   	Ext.getCmp("lbGs").setText("国税:共"+gs+"个未映射编码");
				}
			});
		}
	});
}

var dtWho=0,noMapClick=false;
var dtsm = new Ext.grid.CheckboxSelectionModel({singleSelect:true});
dtsm.handleMouseDown = Ext.emptyFn;
var dtcm = new Ext.grid.ColumnModel({
	columns: [
		dtsm,
		{
			header:'所在编码表',
			dataIndex:'table_bm',
			width: 100,
			align:'left'
		},{
			header:'编码',
			dataIndex:'bm',
			width: 65,
			align:'left'
		},{
			header: "名称",
			dataIndex: 'mc',
			width: 100,
			align:'left'
		},{
			header: "全路径",
			dataIndex: 'fpName',
			width: 180,
			align:'left',
			renderer :function(v,p,r){
				return renderFoo(v,p,r);
			}
		}
	],
	defaultSortable: false
});
var dtRecord = Ext.data.Record.create([
    {name:'table_bm',type:'string'},
    {name: 'bm', type: 'string'},
	{name: 'mc', type: 'string'},
	{name: 'fullPath', type: 'string'},
	{name: 'fpName', type: 'string'}
]);
var dtStore = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: CodeHandler.getNotMappedTaxCodes,
		paramsAsHash: false,
		paramOrder: ['who','start','limit']
  	}), 
  	reader: new Ext.data.JsonReader({
		idProperty:'bm',
		root: 'rows',
		totalProperty: 'totalCount'
	}, dtRecord)
});
dtStore.on("beforeload",function(){
	dtStore.baseParams.who=dtWho;
});
var dtGrid = new Ext.grid.GridPanel({
	title:'',
	store: dtStore,
	cm: dtcm,
	frame:false,
	stripeRows: true,
	loadMask: {msg:'正在加载数据....'},
	enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	selModel: dtsm,
	bbar: new Ext.PagingToolbar({
        pageSize: 20,
        store: dtStore,
        displayInfo: true,
        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
        emptyMsg: "没有数据",
        items: ['-']
    })
});
var noMapPath="";
var noMapWin = new Ext.Window({
	title : '未映射编码信息',
	width : 480,
	height : 320,
	autoScroll : true,
	modal: true,
	layout : 'fit',
	items : [dtGrid],
	closeAction:'hide',
	buttons : [{
		text : "确定",
		handler:function(){
			var records = dtGrid.getSelectionModel().getSelections();// 返回值为 Record 类型
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择编码!");
				return;
			}
	        var rd = records[0];
	        var tb = rd.get("table_bm");
	        CodeHandler.getTableMappingInfo(tb,dtWho,1,function(data){
				if(data!=""){
				   	var result = Ext.util.JSON.decode(data);
				   	if(result){
					   	gtb = result.gs;
				   		dtb = result.ds;
				   		ftb = result.f;
				   		gname = result.gname;
				   		dname = result.dname;
				   		fname = result.fname;
				   		ds_tg.setValue(dname);
				   		gs_tg.setValue(gname);
				   		fn_tg.setValue(fname);
				   		dsTree.root.setText(dname);
				   		gsTree.root.setText(gname);
				   		fTree.root.setText(fname);
						noMapPath=rd.get("fullPath");
						if(dtWho==0){
							noMapClick = true;
							if(Ext.getCmp('dirD').getValue()){
								mapDir=0;
				    			Ext.getCmp('saveGs2F').disable();
				    			Ext.getCmp('saveDs2F').enable();
				    			Ext.getCmp('saveF2T').disable();
			    				reloadTrees();
							}else{
								Ext.getCmp('dirD').setValue(true);
								Ext.getCmp('dirD').fireEvent('check', Ext.getCmp('dirD'));
							}
						}else if(dtWho==1){
							noMapClick = true;
							Ext.getCmp('dirG').setValue(true);
							Ext.getCmp('dirG').fireEvent('check', Ext.getCmp('dirG'));
						}
				   	}
				}
			});
			noMapWin.hide();
		}
	},{
        text : "取消",
        handler : function() {
			noMapWin.hide();
        }
    }]
});
noMapWin.on("show",function(){
	dtStore.load({params:{start:0,limit:20}});
});
Ext.onReady(function(){
	Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
    new Ext.Viewport({
    	id : 'bodyPanel',
		xtype : 'panel',
		layout : {
			type : 'hbox',
			align : 'stretch'
		},
    	items:[
    	{
        	layout:'border',
        	flex :1,
        	items:[
        	{
        		region:"north",
            	height: 80,
	    		layout: 'column',
	    		frame :true,
	    		items:[
	    		{
		    		columnWidth:0.48,
		    		layout: 'form',
		    		labelWidth :55,
		    	    labelAlign: 'left',
		    	    items:[
		    	    
			    	{ 
			    		xtype:'radio', 
			    		boxLabel:'财政=>税务',   
			    		name:'direction',  
			    		id:'dirF', 
			    		checked: true,
			    		inputValue:'9',
			    		fieldLabel: '映射方向',
			    		listeners:{check:function(r,checked){
			    			if(!checked)
				    			return;
			    			mapDir=9;
			    			Ext.getCmp('saveGs2F').disable();
			    			Ext.getCmp('saveDs2F').disable();
			    			Ext.getCmp('saveF2T').enable();
		    				reloadTrees();
			    		}}
			    	},fn_tg]
	    		},{
	    			columnWidth:0.5,
		    		layout: 'form',
		    		labelWidth :40,
		    	    labelAlign: 'left',
		    	    items:[{
		    	    	xtype:'label',
		    	    	text: ''
		    	    },{
			    		xtype:'button',
			    		id: 'saveF2T',
		    	    	text : '保存映射',
		    	    	handler: saveMappingF2T
				    }]
	    		}]
    		},{
    			region:"center",
    			layout:'fit',
    			items:[fTree]
        	}]
    	},{
    		layout:'border',
    		flex :1,
        	items:[
        	{
        		region:"north",
            	height: 80,
	    		layout: 'column',
	    		frame :true,
	    		items:[
	    		{
					columnWidth:0.48,
					layout: 'form',
					labelWidth :55,
					labelAlign: 'left',
					items:[
					{
		    	    	xtype:'label',
		    	    	id: 'lbDs',
		    	    	height:25,
		    	    	text:'地税:共0个未映射编码'
		    	    },
		    		{ 
		    			xtype:'radio',   
		    			boxLabel:'地税=>财政',   
		    			name:'direction',   
		    			inputValue:'0',
		    			id:'dirD',
		    			fieldLabel: '映射方向',
		    			listeners:{check:function(r,checked){
			    			if(!checked)
				    			return;
			    			mapDir=0;
			    			Ext.getCmp('saveGs2F').disable();
			    			Ext.getCmp('saveDs2F').enable();
			    			Ext.getCmp('saveF2T').disable();
		    				reloadTrees();
		    			}}
			    	},ds_tg]
	    		},{
	    			columnWidth:0.5,
		    		layout: 'form',
		    		labelWidth :40,
		    	    labelAlign: 'left',
		    	    items:[{
		    	    	xtype:'label',
		    	    	text: ''
		    	    },{
		    	    	xtype:'button',
		    	    	id:'btnDt_Ds',
		    	    	text : '查看详情',
		    	    	handler: function(){
	    	    			dtWho = 0;
		    	    		noMapWin.show();
		    	    	}
			    	},{
			    		xtype:'button',
			    		id: 'saveDs2F',
			    		disabled: true,
		    	    	text : '保存映射',
		    	    	handler: saveMappingT2F
				    }]
	    		}]
        	},{
    			region:"center",
    			layout:'fit',
    			items:[dsTree]
        	}]
    	},{
    		layout:'border',
    		flex :1,
        	items:[
        	{
        		region:"north",
            	height: 80,
            	layout: 'column',
	    		frame :true,
	    		items:[
	   	    	{
	   	    		columnWidth:0.48,
					layout: 'form',
					labelWidth :55,
					labelAlign: 'left',
					items:[
					{
		    	    	xtype:'label',
		    	    	id: 'lbGs',
		    	    	height:65,
		    	    	text: '共0个未映射编码'
		    	    },{ 
		    			xtype:'radio',   
		    			boxLabel:'国税=>财政',   
		    			name:'direction', 
		    			id:'dirG',
		    			inputValue:'1',
		    			fieldLabel: '映射方向',
		    			listeners:{check:function(r,checked){
			    			if(!checked)
				    			return;
			    			mapDir=1;
			    			Ext.getCmp('saveGs2F').enable();
			    			Ext.getCmp('saveDs2F').disable();
			    			Ext.getCmp('saveF2T').disable();
		    				reloadTrees();
		    			}}
			    	},gs_tg]
	   	    	},{
	   	    		columnWidth:0.5,
		    		layout: 'form',
		    		labelWidth :40,
		    	    labelAlign: 'left',
		    	    items:[{
		    	    	xtype:'label',
		    	    	text: ' '
		    	    },{
		    	    	xtype:'button',
		    	    	id:'btnDt_Gs',
		    	    	text : '查看详情',
		    	    	handler: function(){
		    	    		dtWho = 1;
		    	    		noMapWin.show();
		    	    	}
			    	},{
			    		xtype:'button',
			    		id: 'saveGs2F',
			    		disabled: true,
		    	    	text : '保存映射',
		    	    	handler: saveMappingT2F
				    }]
		   	    }]
        	},{
    			region:"center",
    			layout:'fit',
    			items:[gsTree]
        	}]
    	}]
    });

    CodeHandler.getNotMappingCount(function(data){
		if(data!=""){
		   	var result = Ext.util.JSON.decode(data);
		   	var ds = result.ds;
		   	var gs = result.gs;
		   	Ext.getCmp("lbDs").setText("地税:共"+ds+"个未映射编码");
		   	Ext.getCmp("lbGs").setText("国税:共"+gs+"个未映射编码");
		}
	});
});  
 </script>
</head>
<body>
</body>
</html>