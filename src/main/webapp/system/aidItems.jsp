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
<title>DNFT-资助项目库</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script type="text/javascript">
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
var cNode ;//选中节点
var cAddMode = 0; 
var saveMode='add';
var currentIid;
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
			id:'delNode',
			text:'删除'
		}
		]
	});
	var tloader=new Ext.tree.TreeLoader({
		directFn: CodeHandler.getAidItemsMtTree,
        paramsAsHash: false
	});
	//根节点
	var root = new Ext.tree.AsyncTreeNode({ 
		id:'tree-root',
		text: '项目'
	});
	//树型面板treePanel
	var tree = new Ext.tree.TreePanel({   
	    region: 'west',
	    id: 'tree',   
	    width:250,
	    collapsible: true,
	    root:root, 
	    split: true,    
	    animate: false,   
	    rootVisible: true, //是否显示根节点  
	    autoScroll:true, //自动滚动条
	    title:'项目库',
	    loader: tloader,
	    tbar: [
	  	{
	        xtype:'label',
	        text: '项目名称：'
	    },{
			xtype : 'textfield',
	 		id: 'searchFor',
			allowBlank: true,
			width: 120
	  	},{
			text: '查找',
			iconCls: 'filter',
			handler : function(){
			    cNode = tree.getSelectionModel().getSelectedNode();
			    var selectedNodeID;
			    if(cNode){
			        selectedNodeID = cNode.id;
			    }
			    var searchFor = Ext.getCmp("searchFor").getValue();
			    if(searchFor=='') return;
			    var startNodeID = null;
			    if(selectedNodeID && selectedNodeID != 'tree-root'){
			    	 startNodeID = selectedNodeID ;
			    }
			    //调用服务端查找
			    CodeHandler.searchForAidItem(searchFor,startNodeID,function(data){
			    	if(data&&data!=""){
						var result = Ext.util.JSON.decode(data);
						if(result.match=='no'){ 
							Ext.Msg.alert( "查找结果","没有匹配节点！");
						}else{//展开路径，注意Path是以节点id加上/来间隔的。
							tree.expandPath('tree/tree-root/' + result.path, 'id', onExpandPathComplete);
						}
			        }
			    });
			}
		}],
	    tools:[{
			id:'refresh',//根据id的不同会出现不同的按钮
			handler:function(){
				var tree = Ext.getCmp('tree');
				tree.root.reload();//让根节点重新加载
				tree.body.mask('数据加载中……', 'x-mask-loading');//给tree的body加上蒙版
				tree.root.expand(false,false,function(){
					tree.body.unmask();//全部展开之后让蒙版消失
				});
			}
		}]   
	});
	function onExpandPathComplete(bSuccess, oLastNode) {
		if(!bSuccess) return;
	 	//focus 节点，并选中节点！
		selectNode(oLastNode);
	}
	function selectNode(node){
		if(node){
			node.ensureVisible();
	 		node.select() ;
	 		//node.ui.toggleCheck(true);
		}
	}
	 //绑定节点切换事件
    tree.on('click', function(node,e){ 
        if(!node)return;
    	cNode = node;//选中节点
    	currentIid = node.id;
    	saveMode='update';
        if(node != root){
            Ext.getCmp('save').enable();
    		proForm.getForm().findField('pname').enable();
    		proForm.getForm().findField('remark').enable();
    		proForm.getForm().findField('fileno').enable(); 
    		proForm.getForm().findField('grade').enable(); 
    		Ext.getCmp('delNode').enable();
        	loadItemForm(node.id);
		}else{
			Ext.getCmp('save').disable();
			proForm.getForm().findField('id').setValue(-10);
			proForm.getForm().findField('pid').setValue("");
			proForm.getForm().findField('pname').setValue("");
			proForm.getForm().findField('remark').setValue("");
			proForm.getForm().findField('fileno').setValue("");
			proForm.getForm().findField('grade').setValue("");
			proForm.getForm().findField('gid').setValue("");
		}
	});
	function loadItemForm(cIID){
		CodeHandler.getAidItem(""+cIID,function(data){
			var obj = data;
			if(obj){
				proForm.getForm().findField('id').setValue(cIID);
				proForm.getForm().findField('pid').setValue(obj.pid==null?"":obj.pid);
				proForm.getForm().findField('isleaf').setValue(obj.isleaf==null?"":obj.isleaf);
				proForm.getForm().findField('pname').setValue(obj.pname==null?"":obj.pname);
				proForm.getForm().findField('grade').setValue(obj.grade);
				proForm.getForm().findField('gid').setValue(obj.gid);	
				proForm.getForm().findField('fileno').setValue(obj.fileno);	
				proForm.getForm().findField('remark').setValue(obj.remark==null?"":obj.remark);	
			}else{
				proForm.getForm().findField('id').setValue(-10);
				proForm.getForm().findField('pid').setValue("");
				proForm.getForm().findField('isleaf').setValue("");
				proForm.getForm().findField('pname').setValue("");
				proForm.getForm().findField('remark').setValue("");
				proForm.getForm().findField('fileno').setValue("");	
				proForm.getForm().findField('grade').setValue("");
				proForm.getForm().findField('gid').setValue("");
            }
		});
	}
	//给tree添加右键事件
	tree.on('contextmenu',function(node,e){
		if(node == root){
			Ext.getCmp('delNode').hide();
			Ext.getCmp('addNode').show();
			Ext.getCmp('appendNode').show();
			Ext.getCmp('insertNode').hide();
		}else{
			Ext.getCmp('delNode').show();
			Ext.getCmp('addNode').show();
			Ext.getCmp('appendNode').show();
			Ext.getCmp('insertNode').show();
		}
		e.preventDefault();
		node.select();
		treeRightMenu.showAt(e.getXY());
	});
	//****开始绑定右键菜单事件*************
	Ext.getCmp('appendNode').on('click',appendNode,tree);
	Ext.getCmp('delNode').on('click',deleteNode,tree);
	Ext.getCmp('insertNode').on('click',insertNode,tree);
	function deleteNode(){
		var selectedNode = tree.getSelectionModel().getSelectedNode();//选中节点
		var cNode=selectedNode.id;
		var msg = "您真的要删除该项目吗?";
		if(!selectedNode.leaf){
			msg = "您真的要删除该项目吗?此操作会删除其下所有子节点";
		}
		Ext.Msg.confirm("确认删除",msg,
			function(btn){
				if(btn=='yes'){
					var iid = ""+selectedNode.id;
					var pid = selectedNode.attributes.pid||"";
					CodeHandler.delAidItems(iid,pid,function(data){
						var obj = Ext.util.JSON.decode(data);
						if(obj.result){
							Ext.Msg.alert('成功',"删除成功！！");
							if(selectedNode.parentNode.childNodes.length==1){
								selectedNode.parentNode.leaf = true;
							}
							var pnode=selectedNode.parentNode;
							selectedNode.remove();
							pnode.select();
							proForm.getForm().reset();				
							Ext.getCmp('save').disable();
						}else{
							Ext.Msg.alert('失败',"删除节点过程中发生错误！");
						}
					});					
				}
			}
		);
	}
	
	function appendNode(){//增加子节点
		Ext.getCmp('save').enable();
		proForm.getForm().findField('pname').enable();
		proForm.getForm().findField('remark').enable();
		proForm.getForm().findField('grade').enable();	
		proForm.getForm().findField('fileno').enable();	
		
		cNode = tree.getSelectionModel().getSelectedNode();//选中节点
		proForm.getForm().findField('id').setValue(-10);
		proForm.getForm().findField('pid').setValue(cNode.id == 'tree-root'?'':cNode.id);
		proForm.getForm().findField('isleaf').setValue(1);
		proForm.getForm().findField('pname').setValue("");
		proForm.getForm().findField('remark').setValue("");
		proForm.getForm().findField('grade').setValue("");
		proForm.getForm().findField('gid').setValue("");
		proForm.getForm().findField('fileno').setValue("");	
		saveMode='add';
       	cAddMode =0;
	}
	
	function insertNode(){//增加同级节点
		Ext.getCmp('save').enable();
		proForm.getForm().findField('pname').enable();
		proForm.getForm().findField('remark').enable();
		proForm.getForm().findField('grade').enable();	
		proForm.getForm().findField('fileno').enable();	
		
		cNode = tree.getSelectionModel().getSelectedNode();//选中节点
		Ext.getCmp('id').setValue(-10);
		Ext.getCmp('pid').setValue(cNode.parentNode.id =='tree-root'?'':cNode.parentNode.id);
		proForm.getForm().findField('isleaf').setValue(1);
		proForm.getForm().findField('pname').setValue("");
		proForm.getForm().findField('remark').setValue("");
		proForm.getForm().findField('grade').setValue("");	
		proForm.getForm().findField('gid').setValue("");
		proForm.getForm().findField('fileno').setValue("");	
		saveMode='add';
       	cAddMode =1;
	}

    //定义等级数据
	var gRecord = Ext.data.Record.create([
	       {name: 'bm', type: 'string' },
	       {name: 'mc', type: 'string' }
	]);
	var gStore = new Ext.data.Store({
	    proxy : new Ext.data.DirectProxy({
	    	directFn : CodeHandler.getGrades,
	    	paramsAsHash : false
	    }),
	    reader : new Ext.data.JsonReader({
	        idProperty : 'bm'
	    }, gRecord)
	});
	gStore.load();  
	
	var proForm = new Ext.FormPanel({
		frame:true,
		region: 'center',
	    id: 'proForm',
	    bodyStyle:'padding:5px',
	    api: {
	        submit: CodeHandler.saveAidItem
	    },
	    border:false,
	    labelWidth :80,
	    labelAlign: 'left',
	    buttonAlign: 'center',
	    layout: 'form', 
	    items: [
			new Ext.form.TextField({ 
			    fieldLabel: '项目名称',
			    width: 250,
			    name: 'pname',
			    id: 'pname'
			}),
			new Ext.form.TextField({ 
				fieldLabel: '项目编号',
				name: 'id',
				id: 'id',
				hidden: true,
				hideLabel: true
			}),
			new Ext.form.TextField({ 
				fieldLabel: '是否是底级节点',
				name: 'isleaf',
				id: 'isleaf',
				hidden: true,
				hideLabel: true
			}),
			new Ext.form.TextField({ 
				fieldLabel: '父id',
				name: 'pid',
				id: 'pid',
				hidden: true,
				hideLabel: true
			}),new Ext.form.TextField({ 
				fieldLabel: '政策依据文号',
				name: 'fileno',
				width : 250,
				id: 'fileno'
			}),new Ext.form.ComboBox({
				fieldLabel:'项目等级',
				name: 'grade',
				width : 250,
				displayField:'mc',
				valueField:'bm',
				editable: false, 
				triggerAction : 'all',
				allowBlank:true,
				value:'',
				store : gStore,
				mode: 'local',
				listeners :{
					select : function(combo, record, index){
						proForm.getForm().findField("gid").setValue(record.get('bm'));
					}
				}
			}),new Ext.form.Hidden({
				name:'gid',
				id:'gid',
				value:''
			}),new Ext.form.TextArea({ 
			    fieldLabel: '项目说明',
			    name: 'remark',
			    id: 'remark',
			    width: 250,
			    heigth: 220
			})
	    ],
	    buttons :[
	    {
	       	name: 'save',
	        id: 'save',
	        text: '保存',
	        disabled :true,
	        handler : function() {
			    this.disable();
		        if (proForm.getForm().isValid()) {
			        var id = Ext.getCmp('id').getValue();  
		        	var name = Ext.getCmp('pname').getValue();  
		        	if(name == '' || name == null){
		        		Ext.Msg.alert('系统提示','项目名称必须填写！');
		        		this.setDisabled(false);
		                return ;
		            }
		            var policy = Ext.getCmp('fileno').getValue(); 
		            if(policy==""){
		                Ext.Msg.alert('系统提示','相关政策必须填写！');
			        	this.setDisabled(false);
			            return ;
		            }        
		            proForm.getForm().submit({
		            	params: {saveMode: saveMode,id:id},
		    	        success : successfn,
		                failure : function(form,action) {
		                    Ext.getCmp('save').enable();
		                    Ext.Msg.alert('失败', "保存节点信息失败！");
		                }
		            });			                         
		        }		
	        }
	    }]
	});
	function successfn(form,action){
		if(action.result.success){
			var iid = 0;
			if(proForm.getForm().findField('id').getValue()<0){	//如果是新增
				Ext.Msg.alert('成功', "增加项目成功！");
				var uid = action.result.infos.updatedID;
				iid = uid;
				proForm.getForm().findField('id').setValue(uid);//设置新分配的id
				var newNode = new Ext.tree.TreeNode({
					id: proForm.getForm().findField('id').getValue(),
					text: proForm.getForm().findField('pname').getValue(),
					pid: proForm.getForm().findField('pid').getValue(),
					leaf: true,
				    cls: "file"
				});
				if(cAddMode==0){//如果是增加下级节点
					cNode.leaf = false;
					cNode.appendChild(newNode);
					if(newNode.parentNode!=root){
						newNode.parentNode.collapse();				
						newNode.parentNode.expand(true,false,function(){
							tree.getSelectionModel().select(newNode);
						});
					}
				}else{
					var selectedParentNode = cNode.parentNode;//选中节点父节点
					if(selectedParentNode){
						selectedParentNode.insertBefore(newNode,cNode);
					}
					newNode.select();
				}
			}else{
				Ext.Msg.alert('成功', "更新项目成功！");
				iid = proForm.getForm().findField('id').getValue();
				cNode.id = proForm.getForm().findField('id').getValue();
				cNode.text =proForm.getForm().findField('pname').getValue();
				cNode.setText(proForm.getForm().findField('pname').getValue());
			}	
		}else{
			Ext.Msg.alert('失败', "保存项目失败！");
			return;
		}
		Ext.getCmp('save').enable();
	}
	
	Ext.onReady(function(){
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';//处理ie提交中文乱码问题
		Ext.QuickTips.init();
	    new Ext.Viewport({
	    	layout: 'border',
	    	items: [tree,proForm] 
	    });
	    root.expand(false,false);
	}); 
</script>
</body>
</html>