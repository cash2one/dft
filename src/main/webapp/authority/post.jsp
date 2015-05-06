<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<title>DNFT-岗位维护</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript">
var checkModuleid='';
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);

var cPostid =-1;
var postloader = new Ext.tree.TreeLoader({
	directFn : MaintainHandler.getPostsTree,
	baseAttrs : {
		uiProvider : Ext.tree.TreeCheckNodeUI
	},
	baseParams : { pid:''}
});
	
postloader.on("beforeload", function(treeLoader, node) {
	treeLoader.baseParams.pid=node.id;
}, this); 
	
//岗位树
var post_root_value=new Ext.tree.AsyncTreeNode({    
	text : '岗位',
	draggable : false,
	uiProvider:Ext.tree.TreeNodeUI,
	id : 'tree-root'
});
	
var post_tree=new Ext.tree.TreePanel({    
	id: 'post_root_value',       
	root: post_root_value, 
	checkModel: 'single',   //树节点是否多选    
	onlyLeafCheckable: false,//对树所有结点都可选  
	height:368,
	autoScroll :true,
	animate:false,  
	enableDD:false,
	border: false,    
	rootVisible:true,    
	loader : postloader
});  
var treePanel = new Ext.Panel({
	frame:false,
	height:395,
	tbar: [
	{
		text: '增加岗位',
		iconCls: 'addPost',
		handler : function(){
			Ext.getCmp("postEnmodulesWin").setDisabled(true);
			cPostid = -1;
		    addWin.show();
		}
	},{
		text: '修改岗位',
		iconCls: 'modifyPost',
		handler : function(){
			var records = post_tree.getChecked();
			if(records.length == 0){
				Ext.Msg.alert("提示","请指定要修改的岗位!");
				return;
			}	
			cPostid = records[0].id;
			Ext.getCmp("postEnmodulesWin").setDisabled(true);	
			updateWin.show();
		}
	},{
		text: '删除岗位',
		iconCls: 'removePost',
		handler :function(){
			var records = post_tree.getChecked();
			if(records.length == 0){
				Ext.Msg.alert("提示","请先选择要删除的岗位!");
				return;
			}
			var postIds = '';
			for(var i=0;i<records.length;i++){
				postIds += records[i].id+',';
			}	
			if(records){
				Ext.MessageBox.confirm('确认删除', '你真的要删除所选岗位?',function(btn){
					if(btn == 'yes') {
						MaintainHandler.deletePost(postIds,function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
								var nodeArray = post_tree.getChecked();
								for(var i=0;i<nodeArray.length;i++){
									nodeArray[i].remove();
								}
								checkModuleid = '';
								module_root.reload();
								Ext.Msg.alert('系统提示','岗位信息删除成功！');
							}else{
								Ext.Msg.alert('系统提示','岗位信息删除失败！');
							}
						});	
					}
				});
			}
		}		 
	}],
	items:[post_tree]
});

//新增岗位
var addForm = new Ext.FormPanel({
	id: 'addForm',
	frame: true,
	labelAlign: 'right',
	bodyStyle:'padding:5px',
	width: 300,
	layout: 'form', 
	items: [
		new Ext.form.TextField({ 
		    fieldLabel: '岗位名称',
		    name: 'aPostName',
		    readOnly: false, 
		    id: 'aPostName'
		}),new Ext.form.TextField({ 
		    fieldLabel: '岗位描述',
		    name: 'aRemark',
		    readOnly: false,
		    id: 'aRemark'})
	]
});
//新增窗口
var addWin = new Ext.Window({
	title : '新增岗位',
	width : 300,
	height : 150,
	layout : 'fit',
	items : [addForm],
	closeAction:'hide',
	buttons : [{
	    text : "确定",
		handler:function(){
	        var postname =Ext.get('aPostName').getValue().trim();
	        var remark = Ext.get('aRemark').getValue().trim();
	        if(postname == ''){
	        	Ext.Msg.alert('系统提示','岗位名称不能为空！');
			     return ;
	        }
	        MaintainHandler.addPost(postname,remark,function(data){
	        	var obj = Ext.decode(data);
				if(obj&&obj.result){
					Ext.Msg.alert('成功',"岗位添加成功！");
			        addWin.hide(); 
			        Ext.getCmp("postEnmodulesWin").setDisabled(false); 
			        post_tree.root.reload()
				    addWin.hide();
				}else{
					Ext.Msg.alert('失败', '新增岗位信息失败');
				}
	        }); 
		}
	},{
	    text : "取消",
	    handler : function(){
	        addForm.getForm().reset();
	        addWin.hide();	
	        Ext.getCmp("postEnmodulesWin").setDisabled(false);
	    }
	}]
});
//窗口显示时清空内容
addWin.on("show",function(){
	Ext.getCmp("addForm").getForm().findField("aPostName").setValue(""); 
	Ext.getCmp("addForm").getForm().findField("aRemark").setValue(""); 
});
var updateForm = new Ext.FormPanel({
	id: 'updateForm',
	frame: true,
	labelAlign: 'right',
	bodyStyle:'padding:5px',
	width: 300,
	layout: 'form', 
	items: [
		new Ext.form.TextField({ 
		    fieldLabel: '岗位名称',
		    name: 'PostName',
		    readOnly: false,
		    id: 'PostName'
		})
		, new Ext.form.TextField({ 
		    fieldLabel: '岗位描述',
		    name: 'Remark',
		    readOnly: false,
		    id: 'Remark'})
		,new Ext.form.TextField({ 
			fieldLabel: '岗位Id',
			name: 'PostID',
			hidden: true,
			hideLabel: true,
			id: 'PostID'})
	]
});
//修改窗口
var updateWin = new Ext.Window({
	title : '修改岗位',
	width : 300,
	height : 150,
	layout : 'fit',
	items : [updateForm],
	closeAction:'hide',
	buttons : [{
	    text : "确定",
		handler:function(){
	        var postname =Ext.get('PostName').getValue().trim();
	        var remark = Ext.get('Remark').getValue().trim();
	        if(postname == ''){
	        	Ext.Msg.alert('系统提示','岗位名称不能为空！');
			    return ;
	        }
	        MaintainHandler.updatePost(cPostid,postname,remark,function(data){
	        	var obj = Ext.decode(data);
				if(obj&&obj.result){
					Ext.Msg.alert('成功', "岗位已修改！");
	                updateWin.hide();
	                post_tree.getChecked()[0].setText(postname);
	                Ext.getCmp("postEnmodulesWin").setDisabled(false);
				}else{
					Ext.Msg.alert('失败', '岗位信息更新失败');
				}
	        }); 
		}
	},{
	    text : "取消",
	    handler : function(){
	        updateForm.getForm().reset();
	        updateWin.hide();	
	        Ext.getCmp("postEnmodulesWin").setDisabled(false);
	    }
	}]
});

updateWin.on("show",function(){
	MaintainHandler.getModuleInfo(post_tree.getChecked()[0].id,function(data){
        var obj = Ext.decode(data);
		if(obj){
			Ext.getCmp("updateForm").getForm().findField("PostName").setValue(obj.POSTNAME); 
			Ext.getCmp("updateForm").getForm().findField("Remark").setValue(obj.REMARK); 
			Ext.getCmp("updateForm").getForm().findField("PostID").setValue(post_tree.getChecked()[0].id); 
		}
	});
});
//注册check事件    	   
post_tree.on("checkchange",function(node,checked){
	check(node,checked);
}); 

//注册click事件
post_tree.on("click",function(node,e){
	node.ui.toggleCheck(true);
	check(node,true);
});
function check(node,checked){
	if(checked == true){
		//得到岗位对应的功能
		MaintainHandler.getModulesOfPost(node.id,function(data){
	        var obj = Ext.decode(data);
			if(obj&&obj.result){
				checkModuleid = obj.modules;
				module_root.reload();
			}else{
				Ext.Msg.alert('失败', '获取岗位的权限信息发生错误！');
			}
	    }); 
	}
}
var moduleloader=new Ext.tree.TreeLoader({
	directFn : MaintainHandler.getModuleTree,
	baseAttrs : {
		uiProvider : Ext.tree.TreeCheckNodeUI
	},
	baseParams : { pid:''}
});
	
moduleloader.on("beforeload", function(treeLoader, node) {
	treeLoader.baseParams.pid=node.id;
}, this); 
//菜单树
var module_root=new Ext.tree.AsyncTreeNode({    
	text : '菜单',
	draggable : false,
	expanded:true,
	uiProvider:Ext.tree.TreeNodeUI,
	id : 'module_root'
});

function onExpandPathComplete(bSuccess, oLastNode) {
	if(!bSuccess) return;
	selectNode(oLastNode);
}
function selectNode(node){
	if(node){
		node.ensureVisible();
	 	node.select() ;
	 	if(node.leaf){
	 		node.ui.toggleCheck(true);
		}
	}
}
moduleloader.on("load",function(treeLoader, node){
	if(!checkModuleid){
		return;
	}
	MaintainHandler.getModuleExpandPathes(checkModuleid,function(data){
		var result = Ext.util.JSON.decode(data);
		if (result&&result.pathes) {
			for(var i=0;i<result.pathes.length;i++){
				var path = result.pathes[i];
				module_tree_node.expandPath('module_tree_node/module_root/'+ path, 'id', onExpandPathComplete);
			}
		}
    });
}); 
var module_tree_node=new Ext.tree.TreePanel({    
	frame:false,  
	autoScroll :true,
	height:395,
	id: 'module_tree',       
	root: module_root, 
	checkModel: 'cascade',   //树节点是否多选    级联
	onlyLeafCheckable: false,//对树所有结点都可选  
	animate:false,  
	enableDD:false,    
	//border:false,    
	rootVisible:true,    
	loader : moduleloader
}); 

//节点的开合
module_tree_node.on('checkchange',function(node,checked){ 
	checkModuleid='';
	if(checked){ 
	    node.expand(); 
	} 
});
module_tree_node.on('click',function(node){ 
	checkModuleid='';
	if(!node.isLeaf()){ 
	    node.toggle(); 
	} 
});
	
var post_EnModulePanel = new Ext.Panel({
    id: 'post_EnModule',
    region:'center',
    layout : 'column',
	border : false,
	//height:400,
    items: [{
        columnWidth: 0.5,
        layout:'form',  
		title:'岗位选项',
		border : true,
		frame: false,
		items:[treePanel]
    },{
        columnWidth: 0.5,
        style: {"margin-left": "10px"},
        margins:'0 0 0 0',
        layout : 'form',
		border : true,
		frame: false,
        title:'菜单选项',
    	items:[module_tree_node]
    }]
});
Ext.onReady(function(){
	Ext.QuickTips.init();	
	var postEnmodulesWin = new Ext.FormPanel({
		id : 'postEnmodulesWin',
		layout :'fit',
		height:490,
		layout:'border',
		border : false,
		items : [post_EnModulePanel,
		{	
			region:'south',
			frame:false,
			height:30,
			html:'<font size="2" face="Verdana">&nbsp说明&nbsp&nbsp：请先选择岗位,然后选择对应的菜单功能【选中的菜单为该岗位有权限访问的菜单】</font>'
		}],
		applyTo:'fill',
		buttonAlign : 'center',
		buttons : [{
			text : '保存',
			handler : function() {
				var records = post_tree.getChecked();
				if(records.length == 0){
					Ext.Msg.alert("系统提示","请选中要匹配功能的岗位!");
					return;
				}	
				var nodeArray = module_tree_node.getChecked();
				if(nodeArray.length == 0){
					Ext.Msg.alert('系统提示','请为岗位匹配相应功能！');
				    return ;
				}
				var moduleIds = '';
				for(var i=0;i<nodeArray.length;i++){
				    moduleIds += nodeArray[i].id+',';
				}
				MaintainHandler.setPostModules(records[0].id,moduleIds,function(data){
					var obj = Ext.decode(data);
					if(obj&&obj.result){
						Ext.Msg.alert('系统提示','设置岗位对应功能信息成功！');
					}else{
						Ext.Msg.alert('系统提示','设置岗位对应功能信息失败！');
					}
			    });
			}
		}]
	});
	post_tree.expandAll(); 
});
</script>
</head>
<body>
<div id="fill"></div>
</body>
</html>