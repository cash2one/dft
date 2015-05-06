var hfForm = new Ext.FormPanel({
	frame: true,
	labelWidth: 70,
	labelAlign: 'right',
	border: false,
	autoScroll:true,
  	layout : 'form',
	items:[{
		xtype:'fieldset',
		autoHeight: true,
        title: '标题',
        layout:'form',
		items: [{
			fieldLabel: '标题表达式',
			xtype: 'textarea',
			name: 'titleExp',
			width: 400,
			height:50
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '表头',
        layout:'form',
        items: [{
        	hideLabel: true,
			boxLabel: '启用表头区域',
			xtype: 'checkbox',
			name: 'enableHead',
			width: 120,
			checked : false,
			listeners :{
				check: function(ckbox,checked){
					if(checked){
						hfForm.getForm().findField("titleInHead").enable();
						hfForm.getForm().findField("headHeight").enable();
						hfForm.getForm().findField("ttStyle").enable();
						hfForm.getForm().findField("enableSubTitle").enable();
						var est = hfForm.getForm().findField("enableSubTitle").getValue();
						if(est=="on"||est==true){
							hfForm.getForm().findField("headLeft").enable();
							hfForm.getForm().findField("headLstyle").enable();
							hfForm.getForm().findField("headCenter").enable();
							hfForm.getForm().findField("headCstyle").enable();
							hfForm.getForm().findField("headRight").enable();
							hfForm.getForm().findField("headRstyle").enable();
						}else{
							hfForm.getForm().findField("headLeft").disable();
							hfForm.getForm().findField("headLstyle").disable();
							hfForm.getForm().findField("headCenter").disable();
							hfForm.getForm().findField("headCstyle").disable();
							hfForm.getForm().findField("headRight").disable();
							hfForm.getForm().findField("headRstyle").disable();
						}
					}else{
						hfForm.getForm().findField("titleInHead").disable();
						hfForm.getForm().findField("headHeight").disable();
						hfForm.getForm().findField("ttStyle").disable();
						hfForm.getForm().findField("enableSubTitle").disable();
						hfForm.getForm().findField("headLeft").disable();
						hfForm.getForm().findField("headLstyle").disable();
						hfForm.getForm().findField("headCenter").disable();
						hfForm.getForm().findField("headCstyle").disable();
						hfForm.getForm().findField("headRight").disable();
						hfForm.getForm().findField("headRstyle").disable(); 
					}
				}
			}
        },{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .25,
				layout : 'form',
				items : [{
					hideLabel: true,
					boxLabel: '标题在表头区域',
					xtype: 'checkbox',
					name: 'titleInHead',
					width: 120,
					disabled :true,
					checked : false
				}]
			},{
				columnWidth : .35,
				layout : 'form',
				items : [{
					fieldLabel: '区域高度',
					xtype: 'textfield',
					name: 'headHeight',
					disabled :true,
					width: 80
				}]
			},{
				columnWidth : .4,
				layout : 'form',
				items : [{
					fieldLabel: '标题样式',
					xtype: 'textfield',
					disabled :true,
					name: 'ttStyle',
					width: 100
				}]
			}]	
		},{
			hideLabel: true,
			boxLabel: '启用副标题',
			xtype: 'checkbox',
			name: 'enableSubTitle',
			width: 120,
			disabled :true,
			checked : false,
			listeners :{
				check: function(ckbox,checked){
					if(checked){
						hfForm.getForm().findField("headLeft").enable();
						hfForm.getForm().findField("headLstyle").enable();
						hfForm.getForm().findField("headCenter").enable();
						hfForm.getForm().findField("headCstyle").enable();
						hfForm.getForm().findField("headRight").enable();
						hfForm.getForm().findField("headRstyle").enable();
						
					}else{
						hfForm.getForm().findField("headLeft").disable();
						hfForm.getForm().findField("headLstyle").disable();
						hfForm.getForm().findField("headCenter").disable();
						hfForm.getForm().findField("headCstyle").disable();
						hfForm.getForm().findField("headRight").disable();
						hfForm.getForm().findField("headRstyle").disable();
					}
				}
			}
		},{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .4,
				layout : 'form',
				items : [{
					fieldLabel: '左侧样式',
					xtype: 'textfield',
					name: 'headLstyle',
					width: 80,
					disabled :true
				},{
					fieldLabel: '中部样式',
					xtype: 'textfield',
					name: 'headCstyle',
					width: 80,
					disabled :true
				},{
					fieldLabel: '右侧样式',
					xtype: 'textfield',
					name: 'headRstyle',
					width: 80,
					disabled :true
				}]
			},{
				columnWidth : .6,
				layout : 'form',
				items : [{
					fieldLabel: '左侧区域',
					name: 'headLeft',
					xtype: 'textfield',
					width: 200,
					disabled :true
				},{
					fieldLabel: '中间区域',
					name: 'headCenter',
					xtype: 'textfield',
					width: 200,
					disabled :true
				},{
					fieldLabel: '右侧区域',
					name: 'headRight',
					xtype: 'textfield',
					width: 200,
					disabled :true
				}]
			}]	
		}]
	},{
		xtype:'fieldset',
		autoHeight: true,
        title: '表尾',
        layout:'form',
        items: [{
        	hideLabel: true,
			boxLabel: '启用表尾区域',
			xtype: 'checkbox',
			name: 'enableFoot',
			width: 120,
			checked : false,
			listeners :{
				check: function(ckbox,checked){
					if(checked){
						hfForm.getForm().findField("footHeight").enable();
						hfForm.getForm().findField("footLeft").enable();
						hfForm.getForm().findField("footLstyle").enable();
						hfForm.getForm().findField("footCenter").enable();
						hfForm.getForm().findField("footCstyle").enable();
						hfForm.getForm().findField("footRight").enable();
						hfForm.getForm().findField("footRstyle").enable();
					}else{
						hfForm.getForm().findField("footHeight").disable();
						hfForm.getForm().findField("footLeft").disable();
						hfForm.getForm().findField("footLstyle").disable();
						hfForm.getForm().findField("footCenter").disable();
						hfForm.getForm().findField("footCstyle").disable();
						hfForm.getForm().findField("footRight").disable();
						hfForm.getForm().findField("footRstyle").disable();
					}
				}
			}
        },{
			fieldLabel: '区域高度',
			xtype: 'textfield',
			name: 'footHeight',
			disabled :true,
			width: 80
		},{
			xtype : 'panel',
			layout : 'column',
			items : [
			{
				columnWidth : .4,
				layout : 'form',
				items : [{
					fieldLabel: '左侧样式',
					xtype: 'textfield',
					name: 'footLstyle',
					width: 80,
					disabled :true
				},{
					fieldLabel: '中部样式',
					xtype: 'textfield',
					name: 'footCstyle',
					width: 80,
					disabled :true
				},{
					fieldLabel: '右侧样式',
					xtype: 'textfield',
					name: 'footRstyle',
					width: 80,
					disabled :true
				}]
			},{
				columnWidth : .6,
				layout : 'form',
				items : [{
					fieldLabel: '左侧区域',
					name: 'footLeft',
					xtype: 'textfield',
					width: 200,
					disabled :true
				},{
					fieldLabel: '中间区域',
					name: 'footCenter',
					xtype: 'textfield',
					width: 200,
					disabled :true
				},{
					fieldLabel: '右侧区域',
					name: 'footRight',
					xtype: 'textfield',
					width: 200,
					disabled :true
				}]
			}]	
		}]
	}]
});
function loadHfForm(){
	loadReportPart("headFoot",function(info){
		var hf = hfForm.getForm();
		hf.findField("titleExp").setValue(info.titleExp);
		if(info.head){
			hf.findField("enableHead").setValue(true);
			hf.findField("titleInHead").setValue(info.head.titleInHead);
			hf.findField("headHeight").setValue(info.head.height);
			hf.findField("ttStyle").setValue(info.head.style);
			if(info.head.subTitle){
				hf.findField("enableSubTitle").setValue(true); 
				hf.findField("headLeft").setValue(info.head.subTitle.left);
				hf.findField("headLstyle").setValue(info.head.subTitle.lStyle);
				hf.findField("headCenter").setValue(info.head.subTitle.center);
				hf.findField("headCstyle").setValue(info.head.subTitle.cStyle);
				hf.findField("headRight").setValue(info.head.subTitle.right);
				hf.findField("headRstyle").setValue(info.head.subTitle.rStyle);
			}
		}
		if(info.foot){
			hf.findField("enableFoot").setValue(true);
			hf.findField("footHeight").setValue(info.foot.height);
			hf.findField("footLeft").setValue(info.foot.left);
			hf.findField("footLstyle").setValue(info.foot.lStyle);
			hf.findField("footCenter").setValue(info.foot.center);
			hf.findField("footCstyle").setValue(info.foot.cStyle);
			hf.findField("footRight").setValue(info.foot.right);
			hf.findField("footRstyle").setValue(info.foot.rStyle);
		}
	}); 
}
function buildHeadFoot(){
	var hf = hfForm.getForm();
	var headFoot = new Object();
	if(hf.findField("enableHead").getValue()=="on"||hf.findField("enableHead").getValue()==true){
		var head = new Object();
		head.height=hf.findField("headHeight").getValue();
		head.style = hf.findField("ttStyle").getValue();
		var tih = hf.findField("titleInHead").getValue();
		head.titleInHead=(tih=="on"||tih==true)?1:0;
		if(hf.findField("enableSubTitle").getValue()=="on"||hf.findField("enableSubTitle").getValue()==true){
			var st = new Object();
			st.left = hf.findField("headLeft").getValue();
			st.right = hf.findField("headRight").getValue();
			st.center=hf.findField("headCenter").getValue();
			st.lStyle=hf.findField("headLstyle").getValue();
			st.rStyle=hf.findField("headRstyle").getValue();
			st.cStyle=hf.findField("headCstyle").getValue();
			head.subTitle=st;
		}
		headFoot.head=head;
	}
	if(hf.findField("enableFoot").getValue()=="on"||hf.findField("enableFoot").getValue()==true){
		var foot = new Object();
		foot.height=hf.findField("footHeight").getValue();
		foot.left=hf.findField("footLeft").getValue();
		foot.right=hf.findField("footRight").getValue();
		foot.center=hf.findField("footCenter").getValue();
		foot.lStyle=hf.findField("footLstyle").getValue();
		foot.rStyle=hf.findField("footRstyle").getValue();
		foot.cStyle=hf.findField("footCstyle").getValue();
		headFoot.foot = foot;
	}
	var title = new Object();
	title.titleExp=hf.findField("titleExp").getValue();
	headFoot.title = title;
	return headFoot;
}