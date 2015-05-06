/************************paramsGrid.js************************/
var params_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var params_cm = new Ext.grid.ColumnModel({
	columns: [
	params_sm,
	{
	    header: "参数名",
	    dataIndex: 'name',
	    width: 70,
	    align:'left',
	    renderer: renderFoo
	},{
		header: "描述名",
	    dataIndex: 'desc',
	    width: 90,
	    align:'left',
	    renderer: renderFoo
	},{
	    header: "数据类型",
	    dataIndex: 'dataType',
	    width: 70,
	    align:'left',
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "整数";
	    	}else if(v==2){
		    	return "小数";
	    	}else {
		    	return "字符串";
	    	}
		}
	},{
	    header: "隐藏",
	    dataIndex: 'isHidden',
	    width: 60,
	    align:'left',
	    renderer: function (v,p,r){
			if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
	    header: "交互方式",
	    dataIndex: 'renderType',
	    width: 70,
	    align:'left',
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "下拉框";
	    	}else if(v==2){
		    	return "树";
	    	}else if(v==3){
		    	return "日期";
	    	}else {
		    	return "文本框";
	    	}
		}
	},{
	    header: "宽度",
	    dataIndex: 'width',
	    width: 70,
	    align:'left'
	},{
		header: "详情",
	    width: 100,
	    align:'center',
 		css:"color:black;font-size:12px;",
 		renderer: function(v,p,r){
 			return "<input type='button' value='详情' onclick='showParaDetails(\""+r.get("name")+"\")' style='width:50px;height:20px;font-size:12px'>" ;    
 		}
	}],
	defaultSortable: false
});
var param_Record = Ext.data.Record.create([    
	{name: 'name', type: 'string'},
	{name: 'desc', type: 'string'},
	{name: 'isHidden', type: 'int'},
	{name: 'renderType', type: 'int'},
	{name: 'dataType', type: 'int'},
	{name: 'width', type: 'int'},
	{name: 'isFilter', type: 'int'}
]);
var paramProxy=new Ext.data.HttpProxy({url:'getData.design?doType=getAllParams'});  
var params_ds = new Ext.data.Store({
	proxy: paramProxy, 
    reader: new Ext.data.JsonReader({
    	 id:'name'
	},param_Record)
});
var paramsGrid = new Ext.grid.GridPanel({
	title:'参数',
	store: params_ds,
    cm: params_cm,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: params_sm,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加',
		iconCls: 'add',
        handler : function(){
			var spf = singleParamForm.getForm();
			//初始化窗体form
			spf.findField("name").setValue("");
			spf.findField("name").enable();
			spf.findField("dataType").setValue("0");
			spf.findField("desc").setValue("");
			spf.findField("width").setValue("100");
			spf.findField("isHidden").setValue("0");
			spf.findField("bindMode").setValue("0");
			spf.findField("bindMode").disable();
			spf.findField("bindTo").setValue("");
			spf.findField("bindTo").disable();
			spf.findField("dateFormat").setValue("");
			spf.findField("showMode").setValue("2");
			spf.findField("validates").setValue("");
			spf.findField("isMulti").setValue("0");
			spf.findField("leafOnly").setValue("1");
			spf.findField("autoAll").setValue("0");
			spf.findField("sourceType").setValue("0");
			spf.findField("paraOpts").setValue("");
			spf.findField("paraSql").setValue("");
			spf.findField("paraPro").setValue("");
			spf.findField("defaultValBindMode").setValue("0");
			spf.findField("defaultValueBindTo").setValue("");
			spf.findField("affect").setValue("");
			spf.findField("affectCallBack").setValue("");
			spf.findField("affectedByParas").setValue("");
			spf.findField("isFilter").setValue("0");
			spf.findField("filterFld").setValue("");
			spf.findField("filterFld").disable();
			spf.findField("valueOprator").setValue("equ");
			spf.findField("valueOprator").disable();
			singleParamWin.addMode="add";
			singleParamWin.show();
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = paramsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的参数定义!");
				return;
			}
			if(records){
				Ext.MessageBox.confirm('确认删除', '确定删除选中参数？', function(btn){
	    	    	if(btn == 'yes') {
	    	    		var cn=records[0].get("name");
	    				Ext.Ajax.request({
	    					url : 'getData.design?doType=deleteParamDefine',
	    					params : {rptId:cRid,pname: cn},
	    					success : function(response, options) {
	    						var obj = Ext.util.JSON.decode(response.responseText);
	    						if(obj){
	    							for(var rc=0;rc<records.length;rc++){						    	    	
	    								params_ds.getModifiedRecords().remove(records[rc]);
	    								params_ds.remove(records[rc]);
	    							}
	    							Ext.Msg.alert("提示","指定的参数定义信息已删除!");
	    						}
	    					},
	    					failure : function(response,option) {
	    						Ext.Msg.alert("失败","删除参数定义信息时发生错误！");		
	    		            }
	    		        });
					}
				});
	        }
		}
	},{
		text: '修改',
		iconCls: 'edit',
        handler : function(){
			var records = paramsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择参数!");
				return;
			}
	        var cn = records[0].get("name");
	        showParaDetails(cn);
		}
	}]
});
function showParaDetails(pname){
	Ext.Ajax.request({
		url : 'getData.design?doType=getParamDefine',
		params : {rptId:cRid,pname: pname},
		success : function(response, options) {
			var info = Ext.util.JSON.decode(response.responseText);
			if(info.success){
				var obj = info.parameter;
			    var spf = singleParamForm.getForm();
			    //初始化窗体
			    spf.findField("name").setValue(obj.name);
			    spf.findField("name").disable();
				spf.findField("dataType").setValue(obj.dataType);
				spf.findField("desc").setValue(obj.desc);
				spf.findField("width").setValue(obj.width);
				spf.findField("isHidden").setValue(obj.isHidden);
				spf.findField("bindMode").setValue(obj.bindMode);
				spf.findField("bindTo").setValue(obj.bindTo);
				if(obj.isHidden==1){
					spf.findField("bindMode").enable();
					spf.findField("bindTo").enable();
					spf.findField("renderType").disable();
					spf.findField("dateFormat").disable();
					spf.findField("showMode").disable();
					spf.findField("validates").disable();
					spf.findField("isMulti").disable();
					spf.findField("leafOnly").disable();
					spf.findField("autoAll").disable();
					spf.findField("sourceType").disable();
					spf.findField("defaultValBindMode").disable();
					spf.findField("defaultValueBindTo").disable();
					spf.findField("affect").disable();
					spf.findField("affectCallBack").disable();
					spf.findField("affectedByParas").disable();
					spf.findField("isFilter").disable();
					spf.findField("filterFld").disable();
					spf.findField("valueOprator").disable();
				}else{
					spf.findField("bindMode").disable();
					spf.findField("bindTo").disable();
					spf.findField("renderType").enable();
					spf.findField("dateFormat").enable();
					spf.findField("showMode").enable();
					spf.findField("validates").enable();
					spf.findField("isMulti").enable();
					spf.findField("leafOnly").enable();
					spf.findField("autoAll").enable();
					spf.findField("sourceType").enable();
					spf.findField("defaultValBindMode").enable();
					spf.findField("defaultValueBindTo").enable();
					spf.findField("affect").enable();
					spf.findField("affectCallBack").enable();
					spf.findField("affectedByParas").enable();
					spf.findField("isFilter").enable();
					if(obj.isFilter==1){
						spf.findField("filterFld").enable();
						spf.findField("valueOprator").enable();
					}else{
						spf.findField("filterFld").disable();
						spf.findField("valueOprator").disable();
					}
				}
				spf.findField("renderType").setValue(obj.renderType);
				spf.findField("dateFormat").setValue(obj.dateFormat);
				spf.findField("showMode").setValue(obj.showMode);
				spf.findField("validates").setValue(obj.validates);
				spf.findField("isMulti").setValue(obj.isMulti);
				spf.findField("leafOnly").setValue(obj.leafOnly);
				spf.findField("autoAll").setValue(obj.autoAll);
				spf.findField("sourceType").setValue(obj.sourceType);
				spf.findField("paraOpts").setValue(Ext.encode(obj.paraOpts));
				spf.findField("paraSql").setValue(obj.paraSql);
				spf.findField("paraPro").setValue(Ext.encode(obj.paraPro));
				spf.findField("defaultValBindMode").setValue(obj.defaultValBindMode);
				if(obj.defaultValBindMode==0){
					spf.findField("defaultValueBindTo").setValue(obj.defaultValue);
				}else if(obj.defaultValBindMode==9){
					spf.findField("defaultValueBindTo").setValue(obj.defaultRule);
				}else {
					spf.findField("defaultValueBindTo").setValue(obj.defaultValueBindTo);
				}
				spf.findField("affect").setValue(obj.affect);
				spf.findField("affectCallBack").setValue(obj.affectCallBack);
				spf.findField("affectedByParas").setValue(obj.affectedByParas);
				spf.findField("isFilter").setValue(obj.isFilter);
				spf.findField("filterFld").setValue(obj.filterFld);
				spf.findField("valueOprator").setValue(obj.valueOprator);
			    singleParamWin.addMode="modify";
			    singleParamWin.show();
			}else{
				Ext.Msg.alert("失败","加载参数定义信息失败！"+info.errorInfo);		
			}
		},
		failure : function(response,option) {
			var info = Ext.util.JSON.decode(response.responseText);
			Ext.Msg.alert("失败","加载参数定义信息失败！"+info.errorInfo);		
        }
    });
}
//参数编辑窗体
var cb_dataType=new Ext.form.ComboBox({
	fieldLabel:'数据类型',
	name: 'cb_dataType',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'dataType',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '字符串'], 
			[1, '整数'],
			[2, '小数']
		] 
	}),
	mode: 'local'
});
var cb_bindMode = new Ext.form.ComboBox({
	fieldLabel:'绑定类型',
	name: 'cb_bindMode',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'bindMode',
	disabled :true,
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '固定值'], 
			[1, 'request变量'],
			[2, 'session变量']
		] 
	}),
	mode: 'local'
});
var cb_renderType =  new Ext.form.ComboBox({
	fieldLabel:'控件类型',
	name: 'cb_renderType',
	width : 120,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'renderType',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '文本框'], 
			[1, '下拉框'],
			[2, '选项树'],
			[3, '日期']
		] 
	}),
	mode: 'local'
});
var cb_showMode= new Ext.form.ComboBox({
	fieldLabel:'显示位置(列表)',
	name: 'cb_showMode',
	width : 100,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'showMode',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[1, '弹出框'],
			[2, '工具栏']
		] 
	}),
	mode: 'local'
});
var cb_dtSource = new Ext.form.ComboBox({
	fieldLabel:'选项来源',
	name: 'cb_dtSource',
	width : 100,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'sourceType',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '静态数据'],
			[1, 'SQL取数'],
			[2, '过程取数']
		] 
	}),
	mode: 'local'
});
var cb_dfBindMode = new Ext.form.ComboBox({
	fieldLabel:'绑定类型',
	name: 'cb_dfBindMode',
	width : 100,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'defaultValBindMode',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '固定值'], 
			[1, 'request变量'],
			[2, 'session变量'],
			[9, '规则']
		] 
	}),
	mode: 'local'
});
var cb_operator = new Ext.form.ComboBox({
	fieldLabel:'运算符',
	name: 'cb_operator',
	width : 110,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	hiddenName:'valueOprator',
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [
			['equ', '等于'], 
			['gt', '大于'],
			['lt', '小于'], 
			['gt_e', '大于等于'], 
			['lt_e', '小于等于'], 
			['not_e', '不等于'], 
			['like', '匹配'], 
			['in', '包含']
		] 
	}),
	mode: 'local'
});

//单个参数定义窗体（含form）
var singleParamForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 70,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
	layout : 'form',
	items:[{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .5,
			layout : 'form',
			items : [{
				fieldLabel: '参数名',
				xtype: 'textfield',
				name: 'name',
				width: 120
			},cb_dataType]
		},{
			columnWidth : .5,
			layout : 'form',
			items : [{
				fieldLabel: '描述名',
				xtype: 'textfield',
				name: 'desc',
				width: 110
			},{
				fieldLabel: '宽度',
				xtype: 'numberfield',
				name: 'width',
				width: 110
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '隐藏参数(无交互)',
        layout:'form',
        items: [{
    		hideLabel: true,
    		boxLabel: '隐藏',
    		xtype: 'checkbox',
    		name: 'isHidden',
    		width: 100,
    		checked : false,
    		listeners :{
    			check: function(ckbox,checked){
    				var spf = singleParamForm.getForm();
    				if(checked){
    					cb_bindMode.enable();
    					spf.findField("bindTo").enable();
    					spf.findField("renderType").disable();
    					spf.findField("dateFormat").disable();
    					spf.findField("showMode").disable();
    					spf.findField("validates").disable();
    					spf.findField("isMulti").disable();
    					spf.findField("leafOnly").disable();
    					spf.findField("autoAll").disable();
    					spf.findField("sourceType").disable();
    					spf.findField("defaultValBindMode").disable();
    					spf.findField("defaultValueBindTo").disable();
    					spf.findField("affect").disable();
    					spf.findField("affectCallBack").disable();
    					spf.findField("affectedByParas").disable();
    					spf.findField("isFilter").disable();
    					spf.findField("filterFld").disable();
    					spf.findField("valueOprator").disable();
    				}else{
    					cb_bindMode.disable();
    					spf.findField("bindTo").disable();
						spf.findField("renderType").enable();
						spf.findField("dateFormat").enable();
						spf.findField("showMode").enable();
						spf.findField("validates").enable();
						spf.findField("isMulti").enable();
						spf.findField("leafOnly").enable();
						spf.findField("autoAll").enable();
						spf.findField("sourceType").enable();
						spf.findField("defaultValBindMode").enable();
						spf.findField("defaultValueBindTo").enable();
						spf.findField("affect").enable();
						spf.findField("affectCallBack").enable();
						spf.findField("affectedByParas").enable();
						spf.findField("isFilter").enable();
						var isflt = spf.findField("isFilter").getValue();
						if(isflt=="on"||isflt=="1"||isflt){
							spf.findField("filterFld").enable();
							spf.findField("valueOprator").enable();
						}else{
							spf.findField("filterFld").disable();
							spf.findField("valueOprator").disable();
						}
    				}
        		}
        	}
    	},{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 61,
				items : [cb_bindMode]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 70,
				items : [{
					fieldLabel: '绑定到',
					xtype: 'textfield',
					name: 'bindTo',
					disabled :true,
					width: 110
				}]
			}]
        }]
	},{
		xtype : 'panel',
		layout : 'column',
		items : [
		{
			columnWidth : .45,
			layout : 'form',
			labelWidth: 70,
			items : [
			cb_renderType
			,{
				fieldLabel: '日期格式',
				xtype: 'textfield',
				name: 'dateFormat',
				value:'Y-m-d',
				width: 120
			}]
		},{
			columnWidth : .55,
			layout : 'form',
			labelWidth: 105,
			items : [
			cb_showMode
			,{
				fieldLabel: '验证函数',
				xtype: 'textfield',
				name: 'validates',
				width: 100
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '选项',
        layout:'form',
        items: [{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 100,
				items : [{
		    		hideLabel: true,
		    		boxLabel: '可多选',
		    		xtype: 'checkbox',
		    		name: 'isMulti',
		    		width: 100,
		    		checked : false
		    	}]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 100,
				items : [{
					hideLabel: true,
		    		boxLabel: '仅叶子可选',
		    		xtype: 'checkbox',
		    		name: 'leafOnly',
		    		width: 100,
		    		checked : false
				}]
			}]
        },{
    		hideLabel: true,
    		boxLabel: '自动添加"全部"节点',
    		xtype: 'checkbox',
    		name: 'autoAll',
    		width: 150,
    		checked : false
	    },{
			xtype : 'panel',
			layout : 'column',
			items : [{
				columnWidth : .5,
				layout : 'form',
				items : [
				cb_dtSource
				,{
					xtype: 'hidden',
					name: 'paraOpts' 
				},{
					xtype: 'hidden',
					name: 'paraSql' 
				},{
					xtype: 'hidden',
					name: 'paraPro' 
				}]
			},{
				columnWidth : .5,
				layout : 'form',
				items : [{
					name : 'btn_paraDt',
					id:'btn_paraDt',
	                xtype: 'button',
	                text: '详情...',
	                handler: function(){
	                	var sv = singleParamForm.getForm().findField("sourceType").getValue();
						if(sv==0){
							var paraOpts = singleParamForm.getForm().findField("paraOpts").getValue();
							var opts = Ext.decode(paraOpts);
							if(opts){
								paraOps_ds.removeAll();
								paraOps_ds.loadData(opts);
							}
							paraOpsWin.show();
						}else if(sv==1){
							var sql = singleParamForm.getForm().findField("paraSql").getValue();
							sqlWindow.setSql(sql);
							sqlWindow.onHideSetValue=function(v){
								singleParamForm.getForm().findField("paraSql").setValue(v);
							}
							sqlWindow.show();
						}else if(sv==2){
							procedureWindow.setProInfo(singleParamForm.getForm().findField("paraPro").getValue());
							procedureWindow.onHideSetValue=function(v){
								singleParamForm.getForm().findField("paraPro").setValue(v);
							}
							procedureWindow.show();
						}
					}
				}]
			}]
	    }]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '默认值',
        layout:'form',
        items: [{
        	xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth:70,
				items : [cb_dfBindMode]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 70,
				items : [{
					fieldLabel: '绑定到',
					xtype: 'textfield',
					name: 'defaultValueBindTo',
					width: 110
				}]
			}]
        }]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '联动',
        layout:'form',
        labelWidth: 100,
        items: [{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .45,
				layout : 'form',
				labelWidth: 70,
				items : [{
					fieldLabel: '影响参数',
					xtype: 'textfield',
					name: 'affect',
					width: 100
				}]
			},{
				columnWidth : .55,
				layout : 'form',
				labelWidth: 90,
				items : [{
					fieldLabel: '值变化回调函数',
					xtype: 'textfield',
					name: 'affectCallBack',
					width: 110
				}]
			}]
        },{
        	fieldLabel: '受哪些参数影响',
			xtype: 'textfield',
			name: 'affectedByParas',
			width: 287
        }]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '筛选',
        layout:'form',
        items: [{
    		hideLabel: true,
    		boxLabel: '用于筛选',
    		xtype: 'checkbox',
    		name: 'isFilter',
    		width: 100,
    		checked : false,
    		listeners :{
    			check: function(ckbox,checked){
    				var spf = singleParamForm.getForm();
    				if(checked){
						spf.findField("filterFld").enable();
						spf.findField("valueOprator").enable();
					}else{
						spf.findField("filterFld").disable();
						spf.findField("valueOprator").disable();
					}
        		}
        	}
    	},{
			xtype : 'panel',
			layout : 'column',
			labelWidth: 70,
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				items : [{
					fieldLabel: '绑定字段',
					xtype: 'textfield',
					name: 'filterFld',
					width: 100
		    	}]
			},{
				columnWidth : .5,
				layout : 'form',
				items : [cb_operator]
			}]
        }]
	}]
});
var singleParamWin = new Ext.Window({
	id : 'singleParamWin',
	title : '参数定义',
	items : [singleParamForm],
	layout : 'fit',
	width : 480,
	height : 360,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			var spf = singleParamForm.getForm();
			var st = singleParamWin.addMode;
			var pname = spf.findField("name").getValue();
			singleParamForm.getForm().doAction('submit', {
		        url : 'getData.design?doType=updateParam',
		        params : {rptId:cRid,pname:pname,saveType: st},
		        method : 'POST',
		        success : function(form, action){
		            if(st=="add"){
		            	var ishidden = spf.findField("isHidden").getValue();
		            	var isfilter = spf.findField("isFilter").getValue();
						var prd = new param_Record({
							name: pname,
							desc: spf.findField("desc").getValue(),
							isHidden: (ishidden=="on"||ishidden)?1:0,
							renderType: spf.findField("renderType").getValue(),
							dataType: spf.findField("dataType").getValue(),
							width: spf.findField("width").getValue(),
							isFilter : (isfilter=="on"||isfilter)?1:0
					    });
						paramsGrid.stopEditing();
						params_ds.insert(params_ds.getCount(), prd);
		            }else{
						var records = paramsGrid.getSelectionModel().getSelections();
						if (records&&records.length>0) {
							var record = records[0];
							record.set("name",spf.findField("name").getValue());
							record.set("desc",spf.findField("desc").getValue());
							record.set("isHidden",spf.findField("isHidden").getValue());
							record.set("renderType",spf.findField("renderType").getValue());
							record.set("dataType",spf.findField("dataType").getValue());
							record.set("width",spf.findField("width").getValue());
						}
		            }
		            singleParamWin.hide();
		            Ext.Msg.alert('成功', (st=="add"?"增加":"修改")+"参数成功！");
				},
		        failure : function(form,action) {
					singleParamWin.hide();
		            var info = action.result.errorInfo;
	            	Ext.Msg.alert('失败', (st=="add"?"增加":"修改")+"参数过程中发生错误。"+info);
		        }
		    });	
		}
	},{
		text : "关闭",
		handler : function() {
			singleParamWin.hide();
		}
	}]
});
//静态选项窗体（含可编辑grid）
var editor_isleaf=new Ext.form.ComboBox({
	fieldLabel:'是否底级',
	name: 'editor_isleaf',
	width : 70,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[1, '是'],
			[0, '否']
		] 
	}),
	mode: 'local'
});
var editor_isDefault=new Ext.form.ComboBox({
	fieldLabel:'是否默认',
	name: 'editor_isDefault',
	width : 70,
	displayField:'text',
	valueField:'id',
	editable: false, 
	triggerAction : 'all',
	allowBlank:false,
	store : new Ext.data.SimpleStore({ 
		fields : ["id", "text"], 
		data : [ 
			[0, '否'],
			[1, '是']
		] 
	}),
	mode: 'local'
});
var paraOp_sm = new Ext.grid.CheckboxSelectionModel({singleSelect: true});
var paraOp_cm = new Ext.grid.ColumnModel({
	columns: [
	paraOp_sm,
	{
	    header: "选项值",
	    dataIndex: 'bm',
	    width: 80,
	    align:'left',
	    editor : new Ext.form.TextField({
			selectOnFocus : true,
			maxLength : 80
		}),
	    renderer: renderFoo
	},{
		header: "描述名",
	    dataIndex: 'name',
	    width: 100,
	    align:'left',
	    editor : new Ext.form.TextField({
			selectOnFocus : true,
			maxLength : 100
		}),
	    renderer: renderFoo
	},{
		header: "底级节点",
	    dataIndex: 'isleaf',
	    width: 70,
	    align:'left',
	    editor : editor_isleaf,
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	},{
		header: "默认值",
	    dataIndex: 'isDefault',
	    width: 70,
	    align:'left',
	    editor : editor_isDefault,
	    renderer: function(v,p,r){
	    	if(v==1){
		    	return "是";
	    	}else{
		    	return "否";
	    	}
		}
	}],
	defaultSortable: false
});
var paraOp_Record = Ext.data.Record.create([    
	{name: 'bm', type: 'string'},
	{name: 'name', type: 'string'},
	{name: 'isleaf', type: 'int'},
	{name: 'pid', type: 'string'},
	{name: 'isDefault', type: 'int'}
]);
var paraOps_ds = new Ext.data.Store({
    reader: new Ext.data.JsonReader({
    	 id:'bm'
	},paraOp_Record)
});
var paraOpsGrid = new Ext.grid.EditorGridPanel({
	title:'',
	store: paraOps_ds,
    cm: paraOp_cm,
    frame:true,
    stripeRows: true,
    loadMask: {msg:'正在加载数据....'},
    sm: paraOp_sm,
    clicksToEdit : 1,
    enableColumnMove: false,
	view : new Ext.grid.GridView(),
	stripeRows: true,
	tbar: [
	{
		text: '增加',
		iconCls: 'add',
        handler : function(){
			var rd_op = new dts_Record({
				bm: '',
				name:'',
				isleaf: 1,
				pid:'',
				isDefault: 0
	        });
			paraOpsGrid.stopEditing();
			paraOps_ds.insert(paraOps_ds.getCount(), rd_op);
		}
	},{
		text: '删除',
		iconCls: 'remove',
        handler : function(){
			var records = paraOpsGrid.getSelectionModel().getSelections();
	        if(!records||records.length<1){
				Ext.Msg.alert("提示","请先选择要删除的选项!");
				return;
			}	
			if(records){
				for(var rc=0;rc<records.length;rc++){						    	    	
					paraOps_ds.getModifiedRecords().remove(records[rc]);
					paraOps_ds.remove(records[rc]);
				}
	        }
		}
	}]
});
var paraOpsWin=new Ext.Window({
	id : 'paraOpsWin',
	title : '参数待选项',
	items : [paraOpsGrid],
	layout : 'fit',
	width : 400,
	height : 300,
	autoScroll: true,
	modal : true,
	closeAction:'hide',
	buttonAlign : 'center',
	buttons : [
	{
		text : "确定",
		handler : function(){
			//编辑grid中的内容，encode成字符串写回form
			var opts = new Array();
			for(var i=0;i<paraOps_ds.getCount();i++){
				var rd = paraOps_ds.getAt(i);
				var opt = new Object();
				opt.bm=rd.get("bm");
				opt.name = rd.get("name");
				opt.pid = rd.get("pid");
				opt.isleaf = rd.get("isleaf");
				opt.isDefault = rd.get("isDefault");
				opts.push(opt);
			}
			var strOpts = Ext.encode(opts);
			singleParamForm.getForm().findField("paraOpts").setValue(strOpts);
			paraOpsWin.hide();
		}
	},{
		text : "关闭",
		handler : function() {
			paraOpsWin.hide();
		}
	}]
});
function loadParamForm(){
	params_ds.baseParams.rptId=cRid;
	params_ds.load();
}
function buildParameters(){
	var upinfo = new Object();
	return upinfo;
}
/********************************************************/