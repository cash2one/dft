<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.ifugle.dft.utils.*"%>
<%@ page import="com.ifugle.dft.utils.entity.*"%>
<%@ page import="com.ifugle.dft.check.entity.*"%>
<%
	//设置页面不缓存
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-cache");
	response.setDateHeader("Expires", 0);
	response.addHeader("Cache-Control", "no-cache");
	response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:01 GMT");
	Configuration cg = (Configuration)ContextUtil.getBean("config");
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
<script type="text/javascript" src="<%=request.getContextPath()%>/libs/ext-3.4.0/Ext.ux.tree.TreeCheckNodeUI.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/sys.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/check.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/TreeWindow.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/CodeTreeWindow.js"></script>
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
Ext.sys.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.sys.REMOTING_API);
Ext.ck.REMOTING_API.enableBuffer = 0;  
Ext.Direct.addProvider(Ext.ck.REMOTING_API);
var cMode = "add";
var cUserid = "";
var ssm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
ssm.handleMouseDown = Ext.emptyFn;
var cm = new Ext.grid.ColumnModel({
	columns: [
		ssm,
		{
	        header: "<div style=text-align:center>账户名</div>",
	        dataIndex: 'userid',
	        width: 100,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>别名</div>",
	        dataIndex: 'name',
	        width: 150,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>描述</div>",
	        dataIndex: 'userDesc',
	        width: 150,
	        align: 'left'    
	    },{
	        header: "<div style=text-align:center>是否管理员</div>",
	        dataIndex: 'isManager',
	        width: 70,
	        align: 'left',
	        renderer: function(v,p,r){
				if(v==0){
					return "否";
				}else{
					return "是";
				}
			}        
	    },{
	        header: "<div style=text-align:center>是否启用</div>",
	        dataIndex: 'qybj',
	        width: 70,
	        align: 'left',
	        renderer: function(v,p,r){
				if(v==0){
					return "未启用";
				}else{
					return "启用";
				}
			}        
	    }
	],
	defaultSortable: false
});
var cRecord = Ext.data.Record.create([  
	{name: 'userid', type: 'string'},
	{name: 'name', type: 'string'},
	{name: 'userDesc', type: 'string'},
	{name: 'isManager', type: 'int'},      
	{name: 'qybj', type: 'int'}
]);
var uds = new Ext.data.Store({
	proxy: new Ext.data.DirectProxy({
		directFn: MaintainHandler.getUsers,
		paramsAsHash: false,
		paramOrder: ['start','limit']
	}), 
	reader: new Ext.data.JsonReader({
		idProperty:'userid',
		root: 'rows',
		totalProperty: 'totalCount'
	}, cRecord)
});
var view = new Ext.grid.GridView();
var userGrid = new Ext.grid.GridPanel({
		title:'用户列表',
		store: uds,
		height:250,
	    cm: cm,
	    frame:false,
	    stripeRows: true,
	    loadMask: {msg:'正在加载数据....'},
	    enableColumnMove: false,
		view : view,
		selModel: ssm,
		stripeRows: true,
		tbar: [
    	{
			text: '增加用户',
			iconCls: 'addUser',
            handler : function(){
            	cMode = "add";
            	cUserid = "";
            	userWin.show();
			}
		},{
			text: '修改用户',
			iconCls: 'editUser',
            handler : function(){
				//当前选中记录
            	var records = userGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择修改的用户!");
					return;
				}
		        cUserid = records[0].get("userid");
				cMode = "modify";
				userWin.show();
			}
		},{
			text: '岗位设置',
			iconCls: 'userPost',
            handler : function(){
				//当前选中记录
            	var records = userGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择用户!");
					return;
				}
		        cUserid = records[0].get("userid");
				postWin.show();
			}
		},{
			text: '删除用户',
			iconCls: 'removeUser',
            handler : function(){
				var records = userGrid.getSelectionModel().getSelections();
		        if(!records||records.length<1){
					Ext.Msg.alert("提示","请先选择要删除的用户!");
					return;
				}
		        Ext.MessageBox.confirm('确认', '确定要删除指定用户?', function(btn){
					if(btn == 'yes') {// 选中了是按钮
						var users = new Array();
						for(var i=0;i<records.length;i++){
							var uid = records[i].get("userid");
							users.push(uid);
						}
						MaintainHandler.deleteUser(users.join(),function(data){
							var obj = Ext.decode(data);
							if(obj&&obj.result){
					           	uds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								Ext.Msg.alert("提示","选中用户已删除！");
							}else{
								Ext.Msg.alert("提示","用户删除操作时发生错误，删除失败!");
							}
						});	
					}
				});
			}
		}
		],
		bbar: new Ext.PagingToolbar({
            pageSize: <%=cg.getString("pageSize","40")%>,
	        store: uds,
	        displayInfo: true,
	        displayMsg: '当前显示 {0} - {1} ，共{2}条记录',
	        emptyMsg: "没有数据",
	        items: ['-']
        })
});
var treeWin;
function showTreeWin(table,cVal,callback){
	if(!treeWin){
		treeWin = new App.widget.CodeTreeWindow({
			directFn: CheckHandler.getBmCodesTree,
			codeTable: table,
			defaultValue: cVal,
			checkModel: 'multiple'
		});
	}
	var p = {table: table,selectedVals: cVal};
	treeWin.onSelect = callback;
	treeWin.setTreeParams( p);
	treeWin.refreshTree();
	treeWin.show();
}
function setBms(tField,hField){
	return function(value){
		userForm.getForm().findField(hField).setValue(value.id);
		userForm.getForm().findField(tField).setValue(value.text);
	}
};
var czfp_tg = new Ext.form.TriggerField({
	width: 150,
	fieldLabel: '所属乡镇',
	editable: false,
    id :'czfp',
    name: 'czfp',
    value:''
});
czfp_tg.onTriggerClick=czfpFun;
function czfpFun(e){
	var cVal = userForm.getForm().findField("czfpbms").getValue();
	showTreeWin("BM_CZFP",cVal,setBms("czfp","czfpbms"));
}
function confirmPswd(){
	var pswd = userForm.getForm().findField("pswd").getValue();
	var dupPswd = userForm.getForm().findField("dupPswd").getValue();
	if(pswd!=dupPswd){
		Ext.Msg.alert("提示","密码与确认密码不相符!");
	}
}
var userForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 80,
	api: {
        submit: MaintainHandler.saveUser
    },
	width :120,
	labelAlign:'right',
	items:[
	{
		name:'userid',
		id: "userid",
		xtype:"textfield",
		width: 150,
	    fieldLabel : '账户名',
	    boxLabel: '字母或数字',
	    maxLength: 20
	},{
		name:'name',
		id: "name",
		xtype:"textfield",
		width: 150,
	    fieldLabel : '别名',
	    maxLength: 50
	},{
		name:'userdesc',
		id: "userdesc",
		xtype:"textarea",
		width: 150,
	    height:40,
	    fieldLabel : '描述'
	},{
		name:'pswd',
		id: "pswd",
		xtype:"textfield",
		inputType:'password',
		width: 150,
	    fieldLabel : '密码',
	    maxLength: 50
	},{
		name:'dupPswd',
		id: "dupPswd",
		xtype:"textfield",
		inputType:'password',
		width: 150,
	    fieldLabel : '重复密码',
	    maxLength: 50,
	    listeners :{
	    	"blur": confirmPswd
	    }
	},{
		name:'ism',
		id: "ism",
		xtype:'combo',
		width: 150,
	    fieldLabel : '是否管理员',
	    mode : 'local', 
        triggerAction : 'all', 
        hiddenName:'ismanager',
        forceSelection:true,
        value: '0',
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
        name: "qybj",
        xtype:'combo',
        fieldLabel: '是否启用',
        width:150,
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
	},czfp_tg,
	{
		name:'czfpbms',
		id: "czfpbms",
		xtype:"hidden",
	    fieldLabel : ''
	}
	]
});	
var userWin = new Ext.Window({
	id : 'userWin',
	title : '用户信息',
	items : [userForm],
	layout : 'fit',
	width : 300,
	height : 350,
	modal : true,
	closeAction:'hide',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var pswd = userForm.getForm().findField("pswd").getValue();
			var dupPswd = userForm.getForm().findField("dupPswd").getValue();
			if(pswd!=dupPswd){
				Ext.Msg.alert("提示","密码与确认密码不相符!");
				return;
			}
			var tUserid = userForm.getForm().findField("userid").getValue();
			MaintainHandler.checkUserid(tUserid,cUserid,function(result){
				var rslt = Ext.decode(result);
				if(rslt&&!rslt.duplicate){
					userForm.getForm().submit({
				        params: {cMode: cMode,userid:tUserid},
				        success : function(form,action){
							var obj = action.result;
							if(obj&&obj.infos){
								Ext.Msg.show({title:'成功',
									msg: obj.infos.msg,
									buttons: Ext.Msg.OK,
									icon: Ext.MessageBox.INFO});
					           	uds.load({params:{start:0, limit:<%=cg.getString("pageSize","40")%>}});
								userWin.hide();
							}else{
								Ext.Msg.alert("提示","用户信息保存失败!");
							}
				        },
				        failure : function(form,action) {
				            Ext.Msg.alert('失败', "用户信息保存失败！");
				        }
				    });			
				}else{
					Ext.Msg.alert("提示","当前用户账户已存在，不能重复！");
				}
			});
		}
	},{
		text : "取消",
		handler : function() {
			userWin.hide();
		}
	}]
});	
userWin.on("show",function(){
	if(cMode == "modify"){
		var records = userGrid.getSelectionModel().getSelections();
        if(!records||records.length<1){
			Ext.Msg.alert("提示","请先选择要修改的用户!");
			return;
		}
		var rd = records[0];
		userForm.getForm().findField("userid").disable();
		MaintainHandler.getUserInfo(rd.get("userid"),function(data){
			var obj = data;
			if(obj){
				userForm.getForm().findField("userid").setValue(obj.userid);
				userForm.getForm().findField("name").setValue(obj.name);
				userForm.getForm().findField("userdesc").setValue(obj.userDesc);
				userForm.getForm().findField("pswd").setValue(obj.password);
				userForm.getForm().findField("dupPswd").setValue(obj.password);
				userForm.getForm().findField("ismanager").setValue(obj.isManager);
				userForm.getForm().findField("qybj").setValue(obj.qybj);
				var codes = obj.xzs;
				var fps="",fpbms="";
				for(var i=0;i<codes.length;i++){
					var code = codes[i];
					fpbms += code.bm;
					fps += code.mc;
					if(i<codes.length-1){
						fpbms +=",";
						fps += ",";
					}
				}
				userForm.getForm().findField("czfpbms").setValue(fpbms);
				userForm.getForm().findField("czfp").setValue(fps);
			}else{
				Ext.Msg.alert("提示","用户信息获取失败!");
			}
		});
	}else{
		userForm.getForm().findField("userid").enable();
		userForm.getForm().findField("userid").setValue("");
		userForm.getForm().findField("name").setValue("");
		userForm.getForm().findField("userdesc").setValue("");
		userForm.getForm().findField("pswd").setValue("");
		userForm.getForm().findField("dupPswd").setValue("");
		userForm.getForm().findField("ismanager").setValue("0");
		userForm.getForm().findField("qybj").setValue("1");
		userForm.getForm().findField("czfpbms").setValue("");
		userForm.getForm().findField("czfp").setValue("");
	}
});
/****************************岗位信息******************************/
pLoader = new Ext.tree.TreeLoader({
	directFn : MaintainHandler.getPostsTreeByUserid,
	// nodeParameter : 'node',
	baseAttrs : {
		uiProvider : Ext.tree.TreeCheckNodeUI
	},
	baseParams : { pid:''},
	paramOrder : ['userid']
});
pLoader.on("beforeload", function(treeLoader, node) {
	treeLoader.baseParams.pid=node.id;
	treeLoader.baseParams.userid=cUserid;
}, this);
var pTree = new Ext.tree.TreePanel({
	id : 'pTree',
	checkModel : 'multiple', // 树节点是否多选
	onlyLeafCheckable : true,// 对树所有结点都可选
	autoScroll : true,
	animate : true,
	containerScroll : true,
	rootVisible : true,
	loader :pLoader,
	root : new Ext.tree.AsyncTreeNode({
		id : 'tree-root',
		text : '全部',
		uiProvider : Ext.tree.TreeCheckNodeUI
	})
});
var postWin = new Ext.Window({
	id : 'postWin',
	title : '岗位信息',
	items : [pTree],
	layout : 'fit',
	width : 220,
	height : 300,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var posts = pTree.getChecked('id').join();
			MaintainHandler.saveUserPosts(cUserid,posts,function(data){
				var obj = Ext.decode(data);
				if(obj&&obj.result){
					Ext.Msg.alert("提示","用户岗位设定已保存！");
					postWin.hide();
				}else{
					Ext.Msg.alert("提示","用户岗位设置时发生错误！");
				}
			});
		}
	},{
		text : "关闭",
		handler : function() {
			postWin.hide();
		}
	}]
});	
postWin.on("show",function(){
	//清空岗位树选中的节点
	var nodeArray = pTree.getChecked();
	for(var i=0;i<nodeArray.length;i++){
       	 nodeArray[i].ui.toggleCheck(false);   
	}
	pTree.root.reload();
});

/*******************************整体布局*************************************/
Ext.onReady(function(){
	Ext.QuickTips.init();
	new Ext.Viewport({
		layout:'fit',
        autoScroll:true,
        items:[userGrid]
	});
	uds.load({params:{start:0,limit:<%=cg.getString("pageSize","40")%>}});
});         
 </script>
</head>
<body>
</body>
</html>