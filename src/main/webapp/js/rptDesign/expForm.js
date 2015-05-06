 var expForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 100,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		hideLabel: true,
		boxLabel: '直接导出结果',
		xtype: 'checkbox',
		name: 'directExport',
		width: 100,
		checked : false,
		listeners :{
			check: function(ckbox,checked){
				if(checked){
					expForm.getForm().findField("expFileName").enable();
					expForm.getForm().findField("template").enable();
				}else{
					expForm.getForm().findField("expFileName").disable();
					expForm.getForm().findField("template").disable();
				}
			}
		}
	},{
		fieldLabel: '导出文件名表达式',
		xtype: 'textfield',
		name: 'expFileName',
		disabled :true,
		width: 250
	},{
		fieldLabel: '模板文件名',
		xtype: 'textfield',
		name: 'template',
		disabled :true,
		width: 250
	}]
});
 function loadExportForm(){
	loadReportPart("dirExport",function(info){
		var ef = expForm.getForm();
		ef.findField("directExport").setValue(info.directExport);
		ef.findField("expFileName").setValue(info.expFileName);
		ef.findField("template").setValue(info.template);
		if(info.directExport){
			expForm.getForm().findField("expFileName").enable();
			expForm.getForm().findField("template").enable();
		}else{
			expForm.getForm().findField("expFileName").disable();
			expForm.getForm().findField("template").disable();
		}
	}); 
}
function buildDirExport(){
	var ef = expForm.getForm();
	var upinfo = new Object();
	var isdir = ef.findField("directExport").getValue();
	if(isdir=="on"||isdir==true){
		upinfo.directExport=true;
		var exp = new Object();
		exp.expFileName=ef.findField("expFileName").getValue();
		exp.template=ef.findField("template").getValue();
		upinfo.exporter = exp;
	}else{
		upinfo.directExport=false;
	}
	return upinfo;
}