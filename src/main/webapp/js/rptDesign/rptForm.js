//报表模板的基础信息
var unitsRoot =new Ext.tree.AsyncTreeNode({    
	text : '可用单位',
	checked:false,
	expanded:false,
    draggable : false,
    uiProvider:Ext.tree.TreeCheckNodeUI,
    id : 'unitsRoot'
});    
var tloader=new Ext.tree.TreeLoader({
	dataUrl:'getTree.design?doType=getAllUnits',
	baseAttrs:{ uiProvider: Ext.tree.TreeCheckNodeUI }
}); 
tloader.on("beforeload",function(){
	 var supportUnits=rptForm.getForm().findField("supportUnits").getValue();
	 tloader.baseParams = {checkedUnits : supportUnits}
});
var unitsTree=new Ext.tree.TreePanel({    
	id: 'unitsTree',       
	root: unitsRoot, 
	checkModel: 'multiple',  
	onlyLeafCheckable :true,
	animate: true,    
	enableDD:false,    
	border:false,    
	rootVisible:true,    
	autoScroll:true,
	loader :tloader 
});
var unitsWin = new Ext.Window({
	title : '选择-金额单位',
	width : 360,
	height : 260,
	autoScroll : true,
	layout : 'fit',
	modal:true,
	closeAction:'hide',
	items : [unitsTree],
	buttons : [{
	    text : "确定",
	    handler : function() {	
	    	var bms=unitsTree.getChecked('id');
			var mcs=unitsTree.getChecked('text');  
			rptForm.getForm().findField("units_mc").setValue(mcs.join());
			rptForm.getForm().findField("supportUnits").setValue(bms.join());
			new Ext.ToolTip({
            	target:'units_mc',
            	trackMouse:false,
            	draggable:true,
            	maxWidth:200,
            	minWidth:100,
            	title:'可用单位',
            	html: mcs.join()
        	}); 
			var myData = new Array();
			for(var i=0;i<bms.length;i++){
				var rd = new Array();
				rd.push(bms[i]);
				rd.push(mcs[i]);
				myData.push(rd);
			}
			rptForm.getForm().findField("defaultUnit").setValue("");
			uStore.removeAll();
			uStore.loadData(myData);
			unitsWin.hide();
	  	}
	},{
	  	text:"取消",
	  	handler : function() {	
	  		unitsWin.hide();
	   	}
	}]
});
unitsWin.on("show",function(){
	unitsRoot.reload();
});
var unitsTrigger = new Ext.form.TriggerField({
	fieldLabel: '可用单位',
	id:'units_mc',
	name: 'units_mc',
	editable: false,
	width: 150
});
unitsTrigger.onTriggerClick=function(){
	var muval = rptForm.getForm().findField("multiUnit").getValue();
	if(muval||muval=="on"){
		unitsWin.show();
	}
};

var uStore = new Ext.data.SimpleStore({ 
	fields : [{name: 'bm'},{name: 'mc'}],
	data : [] 
});
var rptForm = new Ext.FormPanel({
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
			items : [
			{
				fieldLabel: '报表ID',
				xtype: 'textfield',
				readOnly: true,
				name: 'rptId',
				readOnly:true,
				width: 120
			},{
				fieldLabel: '报表名称',
				xtype: 'textfield',
				name: 'rptName',
				width: 120
			}]
		},{
			columnWidth : .5,
			layout : 'form',
			items : [
			{
				fieldLabel: '报表说明',
				xtype: 'textarea',
				name: 'description',
				width: 150,
			    height: 50
			}]
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '金额单位',
        layout:'form',
        labelWidth: 30,
		items: [{
			hideLabel: true,
			boxLabel: '可切换金额单位',
			xtype: 'checkbox',
			name: 'multiUnit',
			width: 120,
			checked : false,
			listeners :{
				check: function(ckbox,checked){
					if(checked){
						rptForm.getForm().findField("units_mc").enable();
						rptForm.getForm().findField("defaultUnit").enable();
					}else{
						rptForm.getForm().findField("units_mc").disable();
						rptForm.getForm().findField("defaultUnit").disable();
					}
				}
			}
		},{
			name: 'supportUnits',
			xtype: 'hidden'
		},{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 62,
				items : [unitsTrigger]
			},{
				columnWidth : .5,
				layout : 'form',
				labelWidth: 70,
				items : [
				{
					fieldLabel: '默认单位',
					xtype: 'combo',
					name: 'dfUnit',
					store: uStore,
					width: 150,
					hiddenName: 'defaultUnit',
					displayField : 'mc',
			        valueField : 'bm',
			        typeAhead : true,
			        mode : 'local',
			        triggerAction : 'all',
			        emptyText : '无',
			        selectOnFocus : true,
			        editable : false
				}]
			}]
		}]	
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '其他',
        layout:'form',
		items: [{
			hideLabel: true,
			boxLabel: '可隐藏0',
			xtype: 'checkbox',
			name: 'zeroCanHide',
			width: 100,
			checked : false
		}]
	}]
});
function loadRptForm(){
	Ext.Ajax.request({
		url : 'getData.design?doType=loadRptForm',
		params : {rptId: cRid},
		success : function(form, action){
			var obj = Ext.decode(form.responseText);
			if(obj.success){
				var rpt = obj.rpt;
				rptForm.getForm().findField("rptId").setValue(cRid);
				rptForm.getForm().findField("rptName").setValue(rpt.name);
				rptForm.getForm().findField("description").setValue(rpt.description);
				rptForm.getForm().findField("multiUnit").setValue(rpt.multiUnit==1);
				rptForm.getForm().findField("supportUnits").setValue(rpt.supportUnits);
				rptForm.getForm().findField("units_mc").setValue(rpt.supportUnitsName);
				var myData = new Array();
				var bms = rpt.supportUnits?rpt.supportUnits.split(","):"";
				var mcs = rpt.supportUnitsName?rpt.supportUnitsName.split(","):"";
				for(var i=0;i<bms.length;i++){
					var rd = new Array();
					rd.push(bms[i]);
					rd.push(mcs[i]);
					myData.push(rd);
				}
				uStore.removeAll();
				uStore.loadData(myData);
				rptForm.getForm().findField("defaultUnit").setValue(rpt.defaultUnit);
				if(rpt.multiUnit==1){
					rptForm.getForm().findField("units_mc").enable();
					rptForm.getForm().findField("defaultUnit").enable();
				}else{
					rptForm.getForm().findField("units_mc").disable();
					rptForm.getForm().findField("defaultUnit").disable();
				}
				chartForm.getForm().findField("hasChart").setValue(rpt.hasChart==1);
				expForm.getForm().findField("directExport").setValue(rpt.directExport==1);
				rptForm.getForm().findField("zeroCanHide").setValue(rpt.zeroCanHide==1);
				new Ext.ToolTip({
	            	target:'units_mc',
	            	trackMouse:false,
	            	draggable:true,
	            	maxWidth:200,
	            	minWidth:100,
	            	title:'可用单位',
	            	html: rpt.supportUnitsName
	        	}); 
			}else{
				var info = obj.errorInfo;
			    Ext.Msg.alert('失败', "加载报表过程中发生错误。"+info);
			}
		},
		failure : function(form,action) {
			var obj = Ext.decode(form.responseText);
			var info = obj.errorInfo;
		    Ext.Msg.alert('失败', "加载报表过程中发生错误。"+info);
		}
	});	
}
function updateRptBase(){
	var name = rptForm.getForm().findField("rptName").getValue();  
	var description = rptForm.getForm().findField("description").getValue();  
	if(name == '' || name == null){
		Ext.Msg.alert('系统提示','报表名称必须填写！');
        return ;
    }
	rptForm.getForm().doAction('submit', {
        url : 'getData.design?doType=updateRptBase',
        method : 'POST',
        params : {saveType: 1},
        success : function(form, action){
        	ds.getById(cRid).set("name",name);
        	ds.getById(cRid).set("description",description);
        	ds.commitChanges();
        	Ext.Msg.alert('成功', "保存成功！");
		},
        failure : function(form,action) {
    		Ext.Msg.alert('失败', "保存过程中发生错误。");
        }
    });	
}