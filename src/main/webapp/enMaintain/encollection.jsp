<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.system.entity.User"%>

<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
	User user = (User)session.getAttribute("user");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT">
<META HTTP-EQUIV="expires" CONTENT="0">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
<title>DNFT</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/libs/ext-3.4.0/resources/css/ext-all.css" />
<link href="<%=request.getContextPath()%>/css/dfCommon.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/src/locale/ext-lang-zh_CN.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/utils.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/GridExporter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/ExportEditorGridPanel.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/dfCommon.js"></script>
<script type="text/javascript">
/*
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
Ext.BLANK_IMAGE_URL = '../libs/ext-3.4.0/resources/images/default/s.gif';
Ext.utils.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.utils.REMOTING_API);
Ext.ck.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.ck.REMOTING_API);
	var cNode ;
	var cAddMode;
	var cCreator;
	var cIsManager = <%=user==null?0:user.getIsManager()%>;
	var cCode = "";
	var saveMode='add';
	var cUser = '<%=user==null?"":user.getUserid()%>';
	var treeRightMenu = new Ext.menu.Menu({
		id: 'theContextMenu',
		items:[
		{
			id:'addNode',
			text:'新建',
			menu:[
				{
					id:'insertNode',
					text:'目录'
				},{
					id:'appendNode',
					text:'集合'
				}
			]
		},'-',
		{
			id:'delNode',
			text:'删除'
		}
		]
	});
	var enloader=new Ext.tree.TreeLoader({
		directFn: EnCollectionHandler.getEnCollection,
        paramsAsHash: false
	});
	//企业集合树
	var en_root_value=new Ext.tree.AsyncTreeNode({    
		text : '库',
		draggable : false,
		expanded:true, 
		uiProvider:Ext.tree.TreeNodeUI,
		id : 'en_root_value'
	});
	//树型
	var en_tree_node=new Ext.tree.TreePanel({    
		region: 'west',
		id: 'en_tree_node',      
		root: en_root_value,   
		width:200,
		collapsible: true,
		split: true,    
		animate: false,   
		rootVisible: true, 
		autoScroll:true, //自动滚动条
		title:'企业集合',
		header:true,
		loader : enloader
	}); 

	//注册点击事件	
	en_tree_node.on("click",function(node,e){
		cNode = node;
		cCode = node.id;
		cCreator = node.attributes.creator;
		saveMode='update';
		if(node.id != 'en_root_value'){
			if(node.leaf == true){
				Ext.getCmp('addEn').setDisabled(false);
				Ext.getCmp('impExcel').setDisabled(false);
				Ext.getCmp('removeEn').setDisabled(false);
				Ext.getCmp('saveEn').setDisabled(false);
				Ext.getCmp('expExcel').setDisabled(false);
			}else{
				Ext.getCmp('addEn').setDisabled(true);
				Ext.getCmp('impExcel').setDisabled(true);
				Ext.getCmp('removeEn').setDisabled(true);
				Ext.getCmp('saveEn').setDisabled(true);
				Ext.getCmp('expExcel').setDisabled(true);
			}
			EnCollectionHandler.getEnCollectionById(""+node.id,function(data){
				var obj = data;
				if(obj){
					enForm.getForm().findField('id').setValue(obj.id);
					enForm.getForm().findField('name').setValue(obj.name);
					enForm.getForm().findField('remark').setValue(obj.remark);
					enForm.getForm().findField('pid').setValue(obj.pid);
					enForm.getForm().findField('isprivate').setValue(obj.isprivate);
					enForm.getForm().findField('isleaf').setValue(obj.isleaf);
					enForm.getForm().findField('creator').setValue(obj.creator);
					if(cCreator!=cUser&&!cIsManager){
						enForm.getForm().findField('isprivate').setDisabled(true);
						enForm.getForm().findField('name').setDisabled(true);
						enForm.getForm().findField('remark').setDisabled(true);
						enForm.getForm().findField('creator').hide();
						Ext.getCmp('creatorLabel').hide();
						Ext.getCmp('addEn').setDisabled(true);
						Ext.getCmp('impExcel').setDisabled(true);
						Ext.getCmp('removeEn').setDisabled(true);
						Ext.getCmp('saveEn').setDisabled(true);
						Ext.getCmp('expExcel').setDisabled(true);
					    Ext.getCmp('Save').disable();
					}else{
						enForm.getForm().findField('isprivate').setDisabled(false);
						enForm.getForm().findField('name').setDisabled(false);
						enForm.getForm().findField('remark').setDisabled(false);
						if(cIsManager){
							enForm.getForm().findField('creator').show();
							Ext.getCmp('creatorLabel').show();
						}
						if(obj.isleaf==1){
							Ext.getCmp('addEn').setDisabled(false);
							Ext.getCmp('impExcel').setDisabled(false);
							Ext.getCmp('removeEn').setDisabled(false);
							Ext.getCmp('saveEn').setDisabled(false);
							Ext.getCmp('expExcel').setDisabled(false);
						}
					    Ext.getCmp('Save').enable();
					}
				}else{
					enForm.getForm().findField('id').setValue('');
					enForm.getForm().findField('name').setValue('');
					enForm.getForm().findField('remark').setValue('');
					enForm.getForm().findField('pid').setValue('');
					enForm.getForm().findField('isprivate').setValue(0);
					enForm.getForm().findField('isleaf').setValue('');
					enForm.getForm().findField('creator').setValue('');
					cCreator = "";
				}
			});
			en_grid_ds.baseParams.enId=node.id;
			en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}else{
			Ext.getCmp('Save').setDisabled(true);
			enForm.getForm().findField('id').setValue('');
			enForm.getForm().findField('name').setValue('');
			enForm.getForm().findField('remark').setValue('');
			enForm.getForm().findField('isprivate').setValue(0);
			enForm.getForm().findField('creator').setValue('');
			Ext.getCmp('addEn').setDisabled(true);
			Ext.getCmp('impExcel').setDisabled(true);
			Ext.getCmp('removeEn').setDisabled(true);
			Ext.getCmp('saveEn').setDisabled(true);
			Ext.getCmp('expExcel').setDisabled(true);
			Ext.getCmp('pid').setValue("");
			Ext.getCmp('isleaf').setValue("");
			en_grid_ds.baseParams.enId='0';
			en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		}
	});

	//给tree添加事件
	en_tree_node.on('contextmenu',function(node,e){
		if(node == en_root_value){
			Ext.getCmp('delNode').hide();
			Ext.getCmp('addNode').show();
			Ext.getCmp('appendNode').show();
			Ext.getCmp('insertNode').show();
		}else if(node.leaf == true){
			if(cIsManager==1||node.attributes.creator==cUser){
				Ext.getCmp('delNode').show();
			}else{
				Ext.getCmp('delNode').hide();
			}
			Ext.getCmp('addNode').hide();
			Ext.getCmp('appendNode').hide();
			Ext.getCmp('insertNode').hide();
		}else{
			//2014-01-26 非底级节点，只能在无下级节点时删除。
			//管理员能删所有空目录，非管理员只能删自己创建的空目录
			/*if(cIsManager==1){
				Ext.getCmp('delNode').show();
			}else{
				Ext.getCmp('delNode').hide();
			}*/
			EnCollectionHandler.checkCollectionNode(node.id,function(data){
				var obj = Ext.util.JSON.decode(data);
				if(obj){
					var hasChild = obj.hasChild;
					if(hasChild){
						Ext.getCmp('delNode').hide();
					}else{
						if(cIsManager==1||node.attributes.creator==cUser){
							Ext.getCmp('delNode').show();
						}else{
							Ext.getCmp('delNode').hide();
						}
					}
					if(cIsManager==1||node.attributes.creator==cUser||!obj.isPrivate){						Ext.getCmp('addNode').show();
						Ext.getCmp('appendNode').show();
						Ext.getCmp('insertNode').show();
					}else{
						Ext.getCmp('addNode').hide();
						Ext.getCmp('appendNode').hide();
						Ext.getCmp('insertNode').hide();
					}
				}
			});
		}
		e.preventDefault();
		node.select();
		treeRightMenu.showAt(e.getXY());
	});
	Ext.getCmp('appendNode').on('click',appendNode,en_tree_node);
	Ext.getCmp('delNode').on('click',deleteNode,en_tree_node);
	Ext.getCmp('insertNode').on('click',insertNode,en_tree_node);

	function appendNode(){
		Ext.getCmp('Save').enable();
		cCode = "";
		saveMode='add';
		cNode = en_tree_node.getSelectionModel().getSelectedNode();
		Ext.getCmp('Save').enable();
		enForm.getForm().findField('isprivate').setDisabled(false);
		enForm.getForm().findField('name').setDisabled(false);
		enForm.getForm().findField('remark').setDisabled(false);
		enForm.getForm().findField('name').setValue("");
		enForm.getForm().findField('remark').setValue("");
		enForm.getForm().findField('creator').setValue("");
		enForm.getForm().findField('creator').hide();
		Ext.getCmp('creatorLabel').hide();
		enForm.getForm().findField('id').setValue('');
		enForm.getForm().findField('pid').setValue(cNode.id=='en_root_value'?"":cNode.id);
		enForm.getForm().findField('isleaf').setValue(1);
        cAddMode =0;
        en_grid_ds.baseParams.enId='0';
		en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
	}

	function insertNode(){
		cNode = en_tree_node.getSelectionModel().getSelectedNode();
		cCode = "";
		saveMode='add';
		Ext.getCmp('addEn').setDisabled(true);
		Ext.getCmp('impExcel').setDisabled(true);
		Ext.getCmp('removeEn').setDisabled(true);
		Ext.getCmp('saveEn').setDisabled(true);
		Ext.getCmp('expExcel').setDisabled(true);
		Ext.getCmp('Save').enable();
		enForm.getForm().findField('isprivate').setDisabled(false);
		enForm.getForm().findField('name').setDisabled(false);
		enForm.getForm().findField('remark').setDisabled(false);
		enForm.getForm().findField('name').setValue("");
		enForm.getForm().findField('remark').setValue("");
		enForm.getForm().findField('creator').setValue("");
		enForm.getForm().findField('creator').hide();
		Ext.getCmp('creatorLabel').hide();
		enForm.getForm().findField('id').setValue('');
		enForm.getForm().findField('pid').setValue(cNode.id=='en_root_value'?"":cNode.id);
		enForm.getForm().findField('isleaf').setValue(0);
    	cAddMode =1;
    	en_grid_ds.baseParams.enId='0';
		en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
	}
	function deleteNode(){
		var msg = "您确定要删除该企业集合吗?";
		var selectedNode = en_tree_node.getSelectionModel().getSelectedNode();
		cNode=selectedNode;
		if(!selectedNode.leaf){
			msg = "您真的要删除该集合吗?此操作会删除其下所有子集合！";
		}
		Ext.Msg.confirm("确认删除",msg,
			function(btn){
				if(btn=='yes'){
					EnCollectionHandler.deleteEnCollection(selectedNode.id,function(data){
						var obj = Ext.util.JSON.decode(data);
						if(obj&&obj.result){
							Ext.Msg.alert('成功',"企业集合删除成功！");
							en_grid_ds.baseParams.enId=selectedNode.id;
							en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});						
							var pnode = selectedNode.parentNode;
							selectedNode.remove();
							if(pnode.childNodes.length<1){
								pnode.getUI().getIconEl().src='<%=request.getContextPath()%>/js/ext/resources/images/default/tree/folder.gif';
							}else{
								pnode.getUI().getIconEl().src='<%=request.getContextPath()%>/js/ext/resources/images/default/tree/folder-open.gif';
							}
							Ext.getCmp('name').setValue("");
							Ext.getCmp('remark').setValue("");
							Ext.getCmp('Save').setDisabled(true); 
							Ext.getCmp('addEn').setDisabled(true);
							Ext.getCmp('impExcel').setDisabled(true);
							Ext.getCmp('removeEn').setDisabled(true);
							Ext.getCmp('saveEn').setDisabled(true);
							Ext.getCmp('expExcel').setDisabled(true);
						}else{
							Ext.Msg.alert('失败',"企业集合删除失败！");
						}
					});	
				}
			}
		);
	}
	function checkCode(tcode,fnCallback){
		EnCollectionHandler.checkCode(tcode.trim(),cCode,function(result){
			var rslt = Ext.decode(result);
			if(rslt.exist == 1){
				Ext.Msg.show({
					title:'失败',
					msg: "虚拟机构代码【"+tcode+"】已存在！",
					buttons: Ext.Msg.OK,
					fn: function(){
						Ext.getCmp('id').focus(true);
					},
					icon: Ext.MessageBox.WARNING
				});
			}else{
				if(fnCallback){
					fnCallback();
				}
			}
		});
	}	
	//上面的form
	var enForm = new Ext.FormPanel({
		frame: true,
		border: false,
		buttonAlign :'center',
	    id: 'enForm',
	    api: {
	        submit: EnCollectionHandler.saveEnCollection
	    },
	    labelAlign: 'right',
	    layout : 'absolute', 
	    items: [
		{ 
			xtype: 'hidden',
			fieldLabel: '虚拟机构代码',
			width:150,
			name: 'id',
			allowBlank:false,
			maxLength:50,
			listeners: {'change':function(fld){
				var tmpId=fld.getValue()==null?"":fld.getValue();
				checkCode(tmpId,null);	
			}}
		},{
            x: 5,
            y: 10,
            xtype:'label',
            text: '名称:'
    	},{
			x: 40,
	        y: 5,
			xtype: 'textfield',
			width: 140,
			allowBlank:false,
			name: 'name',
			id: 'name'
		},{ 
			x: 225,
	        y: 10,
			xtype: 'checkbox',
			hideLabel : true,
			boxLabel : '私有集合',
			width: 70,
			allowBlank:false,
			name: 'isprivate'
		},{ 
			xtype: 'hidden',
			fieldLabel: '父id',
			name: 'pid',
			id: 'pid'
		},{
			x: 325,
	        y: 10,
            xtype:'label',
            text: '描述:'
    	},{ 
			x: 365,
	        y: 5,
			xtype: 'textfield',
			name: 'remark',
			id: 'remark',
			width: 270
		},{
            x: 675,
            y: 10,
            id: 'creatorLabel',
            xtype:'label',
            hidden: true,
            text: '创建者:'
    	},{ 
			x: 720,
	        y: 9,
			xtype: 'textfield',
			width: 90,
			hidden: true,
			style: 'background:none;border:0px;',
			readOnly:true,
			name: 'creator',
			id: 'creator'
		},{ 
			xtype: 'hidden',
			fieldLabel: '是否是底级节点',
			name: 'isleaf',
			id: 'isleaf'
		}],
	    buttons:[{
    		name: 'Save',
          	id: 'Save',
          	text: '保存',
          	disabled:true,
          	handler : function() {
		        var code = enForm.getForm().findField('id').getValue();
	      		var name = enForm.getForm().findField('name').getValue();
	      		if(name==null||name.trim()==""){
          			Ext.Msg.alert('错误', "企业集合名称不能为空！");
          			return;
          		}
		    	if(enForm.getForm().isValid()) {
		    		var jhid = enForm.getForm().findField('id').getValue();
			    	enForm.getForm().submit({
			    		params: {saveMode: saveMode,id:jhid},
		    	        success : function(form,action){
				    		if(action.result.success){
				    		    if(saveMode=='add'){	//如果是新增
					    			Ext.Msg.alert('成功', "添加企业集合信息成功！");
					    			var newNode;
					    			enForm.getForm().findField('id').setValue(action.result.infos.newID);
					    			if(cAddMode==0){
					    				newNode = new Ext.tree.TreeNode({
						    				id: action.result.infos.newID,
						    				text: enForm.getForm().findField('name').getValue(),
						    				creator: cUser,
						    				leaf:true
						    			});
					    				Ext.getCmp('addEn').setDisabled(false);
					    				Ext.getCmp('impExcel').setDisabled(false);
					    				Ext.getCmp('removeEn').setDisabled(false);
					    				Ext.getCmp('saveEn').setDisabled(false);
					    				Ext.getCmp('expExcel').setDisabled(false);
					    			}else{		
					    				newNode = new Ext.tree.TreeNode({
						    				id: action.result.infos.newID,
						    				text: enForm.getForm().findField('name').getValue(),
						    				leaf:false,
						    				creator: cUser,
						    				icon:'<%=request.getContextPath()%>/libs/ext-3.4.0/resources/images/default/tree/folder.gif'
						    			});
					    				Ext.getCmp('addEn').setDisabled(true);
					    				Ext.getCmp('impExcel').setDisabled(true);
					    				Ext.getCmp('removeEn').setDisabled(true);		
					    				Ext.getCmp('saveEn').setDisabled(true);	
					    				Ext.getCmp('expExcel').setDisabled(true);
					    			}
					    			cNode.getUI().getIconEl().src='<%=request.getContextPath()%>/libs/ext-3.4.0/resources/images/default/tree/folder-open.gif';
					    			cNode.appendChild(newNode);
				    				newNode.parentNode.collapse();				
				    				newNode.parentNode.expand(true,false,function(){
				    					en_tree_node.getSelectionModel().select(newNode);
				    				});//将上级树形展开
				    				en_tree_node.render();
					    		}else{
					    			Ext.Msg.alert('成功', "企业集合更新成功！");
					    			cNode = en_tree_node.getSelectionModel().getSelectedNode();
					    			cNode.setText(enForm.getForm().findField('name').getValue());
					    		}
					    	}else{
					    		Ext.Msg.alert('失败', "企业集合操作失败！");
						    }
		    	        },
		    	        failure : function(form,action) {
		    	            Ext.Msg.alert('失败', "操作企业集合信息失败！");
		    	        }
		    	    });			 
		    	}
          	}
	    }]
	});
	function setQybj(swdjzh,cQybj){
		if(cCreator!=cUser&&!cIsManager){
			return;
		}
		var ecid = enForm.getForm().findField('id').getValue();
		EnCollectionHandler.toggleQybj(ecid,swdjzh,cQybj,function(data){
			var obj = Ext.util.JSON.decode(data);
			if(obj&&obj.result){
				var rd = en_grid_ds.getById(swdjzh);
				rd.set("qybj",cQybj==0?1:0);
			}
		});
	}
	//下面的grid
	var checkmodel1 = new Ext.grid.CheckboxSelectionModel();
	checkmodel1.handleMouseDown = Ext.emptyFn;
	var en_cm = new Ext.grid.ColumnModel({
		columns: [
		checkmodel1,
	    {
	       header: "税号",
	       dataIndex: 'swdjzh',
	       width: 150,
	       align:'left'      
	    },{
	       header: "企业名称",
	       dataIndex: 'mc',
	       width: 210,
	       align: 'left',
	       renderer : renderFoo   
	    },{
	        header: "法人",
	        dataIndex: 'fddbr',
	        width: 80,
	        align: 'left',
	        renderer : renderFoo 
	     },{
	        header: "地址",
	        dataIndex: 'dz',
	        width: 250,
	        align: 'left',
	        renderer : renderFoo       
	    },{
	    	header: "排序",
		    id:'showorder',
		    dataIndex: 'showorder',
		    width: 60,
		    align: 'right',
		    editor: new Ext.form.NumberField({selectOnFocus:true,maxLength:4,decimalPrecision:0}),
		    renderer : renderFoo
		},{
		    header:'启用标记',
		    width: 80,
		    align:'left',
		    dataIndex:'qybj',
		    renderer: function(v,p,r){
		    	var str = v==1?"禁用":"启用";
			    return "<input type='button' "+((cCreator!=cUser&&!cIsManager)?"disabled":"")+" value='"+str+"' onclick='setQybj(\""+r.get("swdjzh")+"\","+v+")' style='height:20px;font-size:12px' >" ;    
		    }
		}],
		defaultSortable: false
	});
	var en_Record = Ext.data.Record.create([                             
		{name: 'swdjzh', type: 'string'},
		{name: 'mc', type: 'string'},
		{name: 'fddbr', type: 'string'},
		{name: 'dz', type: 'string'},
		{name: 'showorder',type:'int'},
		{name: 'qybj',type:'qybj'}
	]);
	var en_grid_ds = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: EnCollectionHandler.getCollectionEns,
			paramOrder: ['start','limit','enId'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty:'swdjzh',
			root: 'rows',
			totalProperty: 'totalCount'
		}, en_Record)
	});
	var view = new Ext.grid.GridView({
		getRowClass : function(r, rowIndex){
			if(r.get("qybj")==0){
				return "changed";
			}
		}
	});
	var en_grid = new App.ux.ExportEditorGridPanel({
		title:'企业列表',
		store: en_grid_ds,
	    cm: en_cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		clicksToEdit:1,
	    selModel: checkmodel1,
	    tbar: [{
			text: '增加',
			iconCls: 'add',
			id:'addEn',
			handler : function(){
	    		addWin.show();
			}
		},{
			text:"导入",
			iconCls: "impExcel",
			id: 'impExcel',
			handler: function(){
				excelWin.show();
			}
		},{
		    text: '删除',
		    iconCls: 'remove',
		    id:'removeEn',
			handler :function(){
		        var records = en_grid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除的企业!");
					return;
				}		
		        //弹出对话框警告
				if(records){
					Ext.MessageBox.confirm('确认删除', '你真的要删除所选企业吗?', function(btn){
						if(btn == 'yes') {// 选中了是按钮
						    var e_Ids = '';
						    for(var i=0;i<records.length;i++){
						        e_Ids += records[i].get("swdjzh");
						        if(i<records.length-1){
						            e_Ids += ",";
						        }
						    }
						    var c_Id = enForm.getForm().findField('id').getValue();
						    EnCollectionHandler.removeEn(c_Id,e_Ids,function(data){
								var obj = Ext.decode(data);
								if(obj&&obj.result){
									en_grid_ds.baseParams.enId=c_Id;
									en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								}else{
									Ext.Msg.alert('系统提示','企业集合删除失败！');
								}
							});	
						}
					});
				}
		    }
		},{
			text: '保存顺序',
		    iconCls: 'save',
		    id:'saveEn',
			handler :function(){
				var crds = en_grid_ds.getModifiedRecords();
				var rows = new Array();
				for(var i=0;i<crds.length;i++){
					var obj = new Object();
					obj.swdjzh=crds[i].get("swdjzh");
					obj.showorder = crds[i].get("showorder");
					rows.push(obj);
				}
				var ecid = enForm.getForm().findField('id').getValue();
				EnCollectionHandler.saveEnOrder(ecid,Ext.encode(rows),function(data){
					var obj = Ext.util.JSON.decode(data);
					if(obj&&obj.result){
						en_grid_ds.commitChanges();
						Ext.Msg.show({title:'保存结果',
		   					msg: "已保存！",
		   					buttons: Ext.Msg.OK,
		   					icon: Ext.MessageBox.INFO});
					}
				});
			}
		}],
	    bbar: new Ext.PagingToolbar({
	        pageSize: <%=cg.getString("pageSize","40")%>,
		    store: en_grid_ds,
		    displayInfo: true,
		    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
		    emptyMsg: "没有数据",
		    items: ['-']
        })
	});
	en_grid.on("beforeedit",function(e){
		var editField = e.field;
		if(editField == "showorder"){
			if(cCreator!=cUser&&!cIsManager){//非创建者非管理员，不可编辑
				e.cancel = true; 
			}
		}
	});
	/**excel导入信息******/
	function required(para){
		var obj=document.getElementById(para);
		if(obj.getAttribute("value")&&obj.getAttribute("value")!=""){
			return "success";
		}else{
			return "不能为空！";
		}
	}
	var radiogroup= new Ext.form.RadioGroup({   
	    fieldLabel : '匹配模式',
		width:100,  
	    items : [{   
	        boxLabel : '税号',   
	        inputValue : "sh",      
	        name : "d_type",
	        id:"d_sh"
	    },{   
	        boxLabel : '名称',   
	        name : "d_type",   
	        checked: true,
	        inputValue : "mc" ,
	        id:"d_mc" 
	    }]   
	}); 
	var excelForm = new Ext.FormPanel({    
		id: 'excelForm',
		frame:true,
		labelWidth:60,
		api: {
	        submit: EnCollectionHandler.importEnExcel
	    },
	    bodyStyle:'padding-top:5px',
	    layout : 'form',
	    labelAlign: 'right',
	    fileUpload : true,
		items:[
		{
			layout:'column',
			frame: true,
			items:[
			{
				columnWidth:.5,
				layout: 'form',
				items:[
				{ 
				    fieldLabel: 'Excel文件',
				    inputType:'file',
				    width:120,
				    height:20,
				    xtype: 'textfield',
				    name: 'filepath',
				    id: 'filepath'
				},{ 
			    	fieldLabel:'匹配列',
				    id : 'matchCol',
				    name:'matchCol',
				    xtype:'numberfield',
				    width:120,
				    value:1,
				    allowDecimals:false,
				    allowBlank:false
				}]
			},{
				columnWidth:.5,
				layout: 'form',
				items:[
					radiogroup,
				{ 
				    fieldLabel:'起始行',
					id : 'beginRow',
					name:'beginRow',
					xtype:'numberfield',
					width:120,
					value:2,
					allowDecimals:false,
					allowBlank:false
				}
				]
			}]
		}],
		buttons:[{
	    	text : "导入",
		    handler:function(){
				var fpath=document.getElementById('filepath').getAttribute("value");
				if(fpath==""){
		  			Ext.Msg.alert("提示","请选择要导入的文件!");
		  			return;
		  		}
		  		var x=document.getElementById('filepath').value;
		  		if(x.substr(x.lastIndexOf(".")).toUpperCase()!='.XLS'&&x.substr(x.lastIndexOf(".")).toUpperCase()!="XLSX"){
		  			Ext.Msg.alert("提示","请选择Excel文件导入！");
		  			return;
				}  
		  		if (excelForm.getForm().isValid()) {  
					Ext.Msg.wait("正在导入...");
					excelForm.getForm().submit({
			       		timeout: 10*60*1000,
			       		success: function(form, action) {
			       			Ext.Msg.hide();
			       			var obj = action.result;
							if(obj&&obj.infos){
					           	Ext.Msg.show({title:'导入结果',
				   					msg: obj.infos.msg,
				   					buttons: Ext.Msg.OK,
				   					icon: Ext.MessageBox.INFO});
			   					//加载数据
					           	excel_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							}
			       		},
			       		failure: function(form,action){
							var obj = action.result;
							if(obj&&obj.errors){
								Ext.Msg.alert("警告",obj.errors.msg);
							}
						},
						exceptionHandler : function(msg){
							Ext.Msg.hide();
							Ext.Msg.alert('提示',msg);
							return ; 
						}
			       	});
				}
		    }
	    }]
	});
	var exssm = new Ext.grid.CheckboxSelectionModel(); 
	exssm.handleMouseDown = Ext.emptyFn;
	var exc_cm = new Ext.grid.ColumnModel({
		columns: [
			exssm,
		    {
			    id:'adjust',
			    header:'手工匹配',
			    width: 120,
			    align:'left',
			    dataIndex:'ismatch',
			    renderer: function(v,p,r){
				    if(v==0){//未匹配
					    var pXh = r.get("showorder");
				    	return "<input type='button' value='手工匹配' onclick='matchFn("+pXh+")' style='height:20px;font-size:12px' >" ;    
				    }else{
				    	return '';
				    }
			    }
			},{
		       id:'swdjzh',
		       header: "税号",
		       dataIndex: 'swdjzh',
		       width: 120,
		       align:'left'
		    },
		    {
		       id:'mc',
		       header: "企业名称",
		       dataIndex: 'mc',
		       width: 180,
		       align: 'left'
		    },
		    {
		        header: "法人代表",
		        id:'fddbr',
		        dataIndex: 'fddbr',
		        width: 100,
		        align: 'left'
		    },{
			    header: "地址",
			    id:'dz',
			    dataIndex: 'dz',
			    width: 180,
			    align: 'left'
			}
		],
		defaultSortable: false
	});
	var excel_Record = Ext.data.Record.create([                             
	   {name: 'swdjzh', type: 'string'},
	   {name: 'ismatch', type: 'int'},
	   {name: 'jhid', type: 'string'},
	   {name: "showorder",type:'int'},
	   {name: 'mc', type: 'string'},
	   {name: 'dz',type:'string'},
	   {name:'fddbr',type:'string'}
	]);
	var excel_grid_ds = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: EnCollectionHandler.getImportedEns,
			paramOrder: ['start','limit'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			root: 'rows',
			idProperty : 'showorder',
			totalProperty: 'totalCount'
		}, excel_Record)
	});
	var exview = new Ext.grid.GridView({
		getRowClass : function(r, rowIndex){
			if(r.get("jhid")!=null&&r.get("jhid")!=""){
				return "changed";
			}
		}
	});
	var excel_grid = new Ext.grid.GridPanel({
		title:'导入企业信息<font color=red>*红色背景的行表示已属于某集合</font>',
		store: excel_grid_ds,
		cm: exc_cm,
		frame:false,
		stripeRows: true,
		loadMask: {msg:'正在加载数据....'},
		enableColumnMove: false,
		view : exview,
		selModel: exssm,
		tbar:[
		{
			text:"删除",
			iconCls:"remove",
			id:"delete",
			handler: function(){
				var records = excel_grid.getSelectionModel().getSelections();// 返回值为 Record 类型
		    	if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除的记录!");
					return;
				}		
		    	var xhs = new Array();
				if(records){
					Ext.MessageBox.confirm('确认删除', '确定删除？', function(btn){
		    	    	if(btn == 'yes') {// 选中了是按钮
		    	    		for(var rc=0;rc<records.length;rc++){
		    	    			xhs.push(records[rc].get("showorder"));						    	    	
		    	    			excel_grid_ds.remove(records[rc]);
						    }
		    	    		//删除临时表中的相应记录，以用户+xh的方式判别记录
			    	    	EnCollectionHandler.delImportedEns(xhs.join(),function(data){
								var obj = Ext.decode(data);
								Ext.Msg.hide();
								if(obj&&obj.result){
				    	 	        Ext.Msg.alert('信息','指定的导入数据已删除！');
								}else{
					    	 	    Ext.Msg.alert('信息','未能删除指定的数据！');
								}
								//excel_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							});
						}
					});
				}
			}
		},{
			text : "添加为下属企业",
			iconCls: 'save',
	 		id:'saveBatch',
			handler:function(){	
				//循环组织提交数据
				var xhs=new Array();
				var shs = new Array();
				var block = false;
				excel_grid_ds.each(function(rs){ 		
					var row=new Object();
				    var fields=rs.data;
				    if(fields["ismatch"]!=1){
				    	Ext.Msg.alert('系统提示','添加本批次下属企业前，请先删除未匹配记录或手工匹配之！');
				    	block = true;
			   	    }
				    xhs.push(fields["showorder"]);
				    shs.push(fields["swdjzh"]);
				});  
				if(!block){
					Ext.MessageBox.confirm('确认添加', '本页所列的导入企业将添加为当前集团的下属企业，是否继续？', function(btn){
						if(btn == 'yes') {
							Ext.Msg.wait("正在保存...");
							var cid = enForm.getForm().findField('id').getValue();
					    	var strShs = shs?shs.join():"" ;
					    	var strXhs = xhs?xhs.join():"" ;
							EnCollectionHandler.addExcelMatchEns(cid,strShs,strXhs,function(data){
								var obj = Ext.decode(data);
								Ext.Msg.hide();
								if(obj&&obj.result){
									Ext.Msg.hide();
				    	 	        Ext.Msg.alert('信息','保存数据成功！');
								}else{
									Ext.Msg.hide();
				    	 	        Ext.Msg.alert('信息','保存数据失败！');
								}
								var c_Id = enForm.getForm().findField('id').getValue();
								//一页保存完成后，刷新，重载匹配信息
								excel_grid_ds.load({params:{ start:0, limit:<%=cg.getString("pageSize","40")%>}});
								en_grid_ds.baseParams.enId=c_Id;
								en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
							});
						}	 
					})     
				}
		    }
		}
		],
		bbar: new Ext.PagingToolbar({
	        pageSize: <%=cg.getString("pageSize","40")%>,
		    store: excel_grid_ds,
		    displayInfo: true,
		    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
		    emptyMsg: "没有数据",
		    items: ['-']
        })
	});
	var expanel = new Ext.Panel({
	    border:false,
	    frame:false,
	    id:'panel',
	    layout:'border',
	    items:[{
	    	id:'north',
	    	layout:'fit',
	        region:'north',	
	        height:125,
	        frame:false,
		    border:false,
			items: excelForm
		},{	
			region:'center',	
	        layout:'fit',
	        frame:false,
		    border:false,
			items: excel_grid
		}]
	});
	var excelWin = new Ext.Window({
	    title : 'Excel文件导入',
	    width : 600,
	    height : 450,
	    layout : 'fit',
	    items : [expanel],
	    closeAction:'hide',
	    buttons : [
	    {
	    	text : "关闭",
		    handler:function(){
	    		excelWin.hide();
	    		//var c_Id = enForm.getForm().findField('id').getValue();
	    		//en_grid_ds.baseParams.enId=c_Id;
				//en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		    }
	    }]
	});
	excelWin.on("show",function(){
		excelForm.getForm().getEl().dom.reset(); 
		excelForm.getForm().findField("matchCol").setValue(1); 
		excelForm.getForm().findField("beginRow").setValue(2);
		Ext.getCmp("d_mc").setValue(true);
		Ext.getCmp("d_sh").setValue(false);
		excel_grid_ds.removeAll();
	});
	var matchRecord;
	function matchFn(pXh){
		matchRecord = excel_grid_ds.getById(pXh);
		enterpriseWin.show();
	} 
	/************enterpriseWin手工匹配企业查询窗体*********************/
	var egssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
	egssm.handleMouseDown = Ext.emptyFn;
	var enterprisecm=new Ext.grid.ColumnModel({
		columns:[
		egssm,
		{
			id:'sh',
			header: '税号',
			width: 120, 
			dataIndex: 'swdjzh',
			sortable: false,
			align: 'left',
			hideable :true,
			css:"color:black;font-size:12px;"
		},{
			id:'eName',
			header: '企业名称',
			width: 220, 
			dataIndex: 'mc',
			sortable: false,
			align: 'left',
			hideable :true,
			css:"color:black;font-size:12px;"
		},{
			id:'fddbr',
			header: '法人代表',
			width: 100, 
			dataIndex: 'fddbr',
			sortable: false,
			align: 'left',
			hideable :true,
			css:"color:black;font-size:12px;"
		},{
			id:'address',
			header: '地址',
			width: 250, 
			dataIndex: 'dz',
			sortable: false,
			align: 'left',
			hideable :true,
			css:"color:black;font-size:12px;"
		}],
		defaultSortable: false
	});
	var enterpriseRecord = Ext.data.Record.create([
		{name: 'swdjzh', type: 'string'},
		{name: 'mc', type: 'string'},
		{name: 'czfpbm', type: 'string'},
		{name: 'fddbr', type: 'string'},
		{name: 'dz', type: 'string'}
	]);
	var enterpriseStore = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: CheckHandler.getEns,
			paramOrder: ['enType','start','limit','conditions'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty:'swdjzh',
			root: 'rows',
			totalProperty: 'totalCount'
		}, enterpriseRecord)
	});
	var conditions ={};
	enterpriseStore.on("beforeload",function(){
		enterpriseStore.baseParams.enType=9;
		enterpriseStore.baseParams.conditions=Ext.encode(conditions);
	});
	var enterpriseGrid=new Ext.grid.GridPanel({
		store: enterpriseStore,
		cm: enterprisecm,
		collapsible: false,
		animCollapse: false,
		loadMask: {msg:'正在加载数据....'},
		view: new Ext.grid.GridView({}),
		enableColumnMove: false,
		stripeRows: true,
		selModel: egssm,
		tbar: [{
			xtype:'label',   
			text:'企业名称：'
		},{
			xtype:'textfield',
			id:'para_qymc',
			width: 180,
			enableKeyEvent:true,
			name:'para_qymc',
			hideLabel:true
			,listeners:{   
				specialkey:function(field,e){   
					if (e.getKey()==Ext.EventObject.ENTER){  
						doConditions();
					}   
				}   
			}   
		}, 
		new Ext.Toolbar.Separator(),
		{
		    text: '搜索',
		    iconCls: 'filter',
		    handler : function(){
				doConditions();
		    }
		}],
		bbar: new Ext.PagingToolbar({
		    pageSize: <%=cg.getString("pageSize", "20")%>,
		    store: enterpriseStore,
		    displayInfo: true,
		    displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
		    emptyMsg: "没有数据",
		    items: ['-']
		})
	});
	function doConditions(){
		var qymc = Ext.getCmp('para_qymc').getValue();
		conditions = conditions||{};
		var fldNames = new Array();
		var fldValues = new Array();
		var relations = new Array();
		var connections = new Array();
		if(qymc&&!qymc==""){
			fldNames.push("mc");
			fldValues.push(qymc);
			relations.push("like");
			connections.push("empty");
		}
		conditions.fldNames=fldNames.join();
		conditions.fldValues=fldValues.join();
		conditions.relations=relations.join();
		conditions.connections=connections.join();
		enterpriseStore.load({params:{start:0, limit:<%=cg.getString("pageSize", "40")%>}});
	}
	/*enterpriseGrid.on("rowdblclick",function(eGrid,rIndex,e){
		var rc=enterpriseStore.getAt(rIndex);
		matchRecord.set("swdjzh",rc.get("swdjzh"));
		matchRecord.set("mc",rc.get("mc"));
		matchRecord.set("fddbr",rc.get("fddbr"));
		matchRecord.set("dz",rc.get("dz"));
		enterpriseWin.hide();//双击关闭窗体，自动填写企业信息到界面	
	});*/
	var enterpriseWin = new Ext.Window({
	    title : '企业列表',
	    width : 450,
	    height : 400,
	    layout : 'fit',
		autoScroll : true,
		modal:true,
	    items : [enterpriseGrid],
	    closeAction:'hide',
	    buttons : [{
	    	text : "确定",
		    handler:function(){
            	var records = enterpriseGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请选择匹配的企业!");
					return;
				}
				var rc= records[0];
				matchRecord.set("swdjzh",rc.get("swdjzh"));
				matchRecord.set("mc",rc.get("mc"));
				matchRecord.set("fddbr",rc.get("fddbr"));
				matchRecord.set("dz",rc.get("dz"));
				enterpriseWin.hide();//双击关闭窗体，自动填写企业信息到界面	
		    }
	    },{
	    	text : "关闭",
		    handler:function(){
	    		enterpriseWin.hide();
		    }
	    }]
	});
	enterpriseWin.on("show",function(){
		conditions={};
		Ext.getCmp('para_qymc').setValue("");
	    enterpriseStore.load({params:{start:0, limit:<%=cg.getString("pageSize", "20")%>}});
	});
	/*******************整体panel****************************/
	var enPanel = new Ext.Panel({
		region: 'center',
		frame: false,
		layout:'border',
		border: false,
		items:[{	
	        id:'north',
	        region:'north', 
	        layout:'fit',
	        height:100,
	        frame:false,
		    border:false,
			items: enForm
        },{  
			region:'center',  
			layout:'fit',  
			frame:false,
		    border:false,  
			autoScroll:true, //自动滚动条
			items: en_grid  
		}]
	});
	//“添加企业”弹出框内容
	var checkmodel2 = new Ext.grid.CheckboxSelectionModel();
	checkmodel2.handleMouseDown = Ext.emptyFn;

	var add_cm = new Ext.grid.ColumnModel({
		columns: [
		checkmodel2,
		{
		    header: "税号",
		    dataIndex: 'swdjzh',
		    width: 180,
		    align:'left'      
		},{
		    header: "企业名称",
		    dataIndex: 'mc',
		    width: 250,
		    align: 'left'    
		},{
		    header: "法人",
		    dataIndex: 'fddbr',
		    width: 90,
		    align: 'left'    
		}],
		defaultSortable: false
	});
	var add_Record = Ext.data.Record.create([  
		{name: 'swdjzh', type: 'string'},
		{name: 'mc', type: 'string'},
		{name: 'fddbr', type: 'string'},
		{name: 'dz', type: 'string'},  
		{name: 'jhid', type:'string'}
	]);
	var add_grid_ds = new Ext.data.Store({
		proxy: new Ext.data.DirectProxy({
			directFn: EnCollectionHandler.getEnterprisesToAdd,
			paramOrder: ['start','limit','pField','pValue','cID'],
			paramsAsHash: false
		}), 
		reader: new Ext.data.JsonReader({
			idProperty:'swdjzh',
			root: 'rows',
			totalProperty: 'totalCount'
		}, add_Record)
	});
	var view = new Ext.grid.GridView({
		getRowClass : function(record, rowIndex){
			if(record.get('jhid') != ""){
				return 'changed' ;
			}
		}
	});
	var add_grid = new Ext.grid.GridPanel({
		title:'企业列表（红色背景的企业已经是本集合的成员）',
		store: add_grid_ds,
		cm: add_cm,
		frame:false,
		stripeRows: true,
		loadMask: {msg:'正在加载数据....'},
		enableColumnMove: false,
		view : view,
		selModel: checkmodel2,
		tbar: [{
			text: '添加到企业集合',
			iconCls: 'add',
			handler : function(){
				var records = add_grid.getSelectionModel().getSelections();// 返回值为 Record 类型
			    if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要添加到企业集合的企业!");
					return;
				}
				var c_Id = enForm.getForm().findField('id').getValue();
				var arrEid = new Array();
			    var dupEid = new Array();
			    for(var i=0;i<records.length;i++){
				    var ecid = records[i].get("jhid");
				    if(ecid!=""){//已经存在于当前集合
				        dupEid.push(records[i].get("swdjzh"));
				    }else{
				        arrEid.push( records[i].get("swdjzh"));
				    }
			    }
			    if(dupEid.length>0){
			      	Ext.Msg.show({
					    title:'提示',
					    msg:'选中行中有'+dupEid.length+'个企业已属于当前集合，不会被重复加入当前集合！', 
					    buttons: Ext.Msg.OK,
					    fn: function(){
			      		   	addToCollection();
			      		}
			      	});
			    }else{
			      	addToCollection();
			    }
			    function addToCollection(){
			    	EnCollectionHandler.addEn(c_Id,arrEid.join(),function(data){
						var obj = Ext.decode(data);
						Ext.Msg.hide();
						if(obj&&obj.result){
							Ext.Msg.alert('系统提示','成功添加企业到企业集合！');
							addWin.hide();
							en_grid_ds.baseParams.enId=c_Id;
							en_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});																			
						}else{
							Ext.Msg.alert('系统提示','添加企业到企业集合失败！');
						}
					});
			    }  	
			}
		}],
		bbar: new Ext.PagingToolbar({
		    pageSize: <%=cg.getString("pageSize","40")%>,
			store: add_grid_ds,
			displayInfo: true,
			displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
			emptyMsg: "没有数据",
			items: ['-']
	    })
	});
	var addPanel = new Ext.Panel({
		frame:false,
		layout:'fit',
		autoScroll:true, //自动滚动条
		items:[add_grid],
		tbar: [{
			xtype:'radio',   
			boxLabel:'按税号',   
			name:'ftype',   
			id:'sh',   
			hideLabel:true,
			listeners:{check:function(){
			    Ext.getCmp('paras').setValue("");
			}}
		},{ 
			xtype:'radio',   
			boxLabel:'按名称&nbsp;&nbsp;',   
			name:'ftype',   
			id:'mc',  
			checked:true, 
			hideLabel:true 
		},{
			xtype:'textfield',
			id:'paras',
			width:150,
			enableKeyEvent:true,
			name:'paras',
			hideLabel:true
			,listeners:{   
				specialkey:function(field,e){   
					if (e.getKey()==Ext.EventObject.ENTER){  
						var field ='mc';
						if(Ext.getCmp('sh').checked){
							field='swdjzh';
						}else{
							field='mc';
						}
						var value = Ext.getCmp('paras').getValue();
						value=(value==null)?"":value.trim();
						var c_Id = enForm.getForm().findField('id').getValue();
						add_grid_ds.baseParams.pField=field;
						add_grid_ds.baseParams.pValue=value;
						add_grid_ds.baseParams.cID=c_Id;
						add_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
					}   
				}   
			}   
		},new Ext.Toolbar.Separator(),
		{
		    text: '搜索',
		    iconCls: 'filter',
		    handler : function(){
				var field ='mc';
				if(Ext.getCmp('sh').checked){
					field='swdjzh';
				}else{
					field='mc';
				}
				var value = Ext.getCmp('paras').getValue();
				value=(value==null)?"":value.trim();
				var c_Id = enForm.getForm().findField('id').getValue();
				add_grid_ds.baseParams.pField=field;
				add_grid_ds.baseParams.pValue=value;
				add_grid_ds.baseParams.cID=c_Id;
				add_grid_ds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
		    }
		}]
	});
	var addWin = new Ext.Window({
		title : '添加企业',
		width : 700,
		height : 450,
		layout : 'fit',
		items : [addPanel],
		closeAction:'hide',
		buttons : [{
		    text : "关闭",
			handler:function(){
		        addWin.hide();
			}
		}]
	});
	addWin.on("show",function(){
		checkmodel2.handleMouseDown = Ext.emptyFn;
		var c_Id = enForm.getForm().findField('id').getValue();
		Ext.getCmp('paras').setValue("");
		Ext.getCmp('mc').setValue(true);
		Ext.getCmp('sh').setValue(false);
		checkmodel2.clearSelections();
		add_grid_ds.baseParams.cID=c_Id;
		add_grid_ds.baseParams.pField="";
		add_grid_ds.baseParams.pValue="";
		add_grid_ds.reload({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
	});
	Ext.onReady(function(){
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	    new Ext.Viewport({
	    	layout: 'border',
	    	frame: false,
	    	items: [en_tree_node,enPanel]
	    });
		en_tree_node.expandAll();
	    Ext.getCmp('addEn').setDisabled(true);
	    Ext.getCmp('impExcel').setDisabled(true);
	    Ext.getCmp('removeEn').setDisabled(true);
	    Ext.getCmp('saveEn').setDisabled(true);
	    Ext.getCmp('expExcel').setDisabled(true);
	}); 
</script>
</head>
<body>
</body>
</html>