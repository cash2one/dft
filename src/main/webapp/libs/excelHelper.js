function ExportExcel(gridPanel,config) {
    if(gridPanel) {
        var tmpStore = gridPanel.getStore();
        var tmpExportContent = '';

		//以下处理分页grid数据导出的问题，从服务器中获取所有数据，需要考虑性能
        var tmpParam = Ext.ux.clone(tmpStore.lastOptions);//此处克隆了原网格数据源的参数信息
        if (tmpParam && tmpParam.params) {
        	if(tmpParam.params[tmpStore.paramNames.start]){
        		tmpParam.params.start=0;
        	}
        	if(tmpParam.params[tmpStore.paramNames.limit]){
        		tmpParam.params.limit=2000;
        	}
        	//delete (tmpParam.params[tmpStore.paramNames.start]);//删除分页参数
            //delete (tmpParam.params[tmpStore.paramNames.limit]);
        }
        var tmpAllStore = new Ext.data.Store({//重新定义一个数据源
            proxy: tmpStore.proxy,
            reader:tmpStore.reader
        });
        tmpAllStore.on('load', function(store) {
            config.store = store;
            tmpExportContent = gridPanel.getExcelXml(false, config);//此方法用到了一中的扩展
            if (Ext.isIE8 || Ext.isIE || Ext.isSafari || Ext.isSafari2 || Ext.isSafari3) {//在这几种浏览器中才需要，IE8测试不能直接下载了
                /*if (!Ext.fly('frmDummy')) {
                    var frm = document.createElement('form');
                    frm.id = 'frmDummy';
                    frm.name = id;
                    frm.className = 'x-hidden';
                    document.body.appendChild(frm);
                }
                Ext.Ajax.request({
                    url: 'ExportServicePage.jsp?ExportFile=dddd',//将生成的xml发送到服务器端
                    method: 'POST',
                    form: Ext.fly('frmDummy'),
                    callback: function(o, s, r) {
                        //alert(r.responseText);
                    },
                    isUpload: true,
                    params: {econtent: tmpExportContent }
                });*/
            	var fd=Ext.get('frmDummy');
                if (!fd) {
                    fd=Ext.DomHelper.append(Ext.getBody(),{tag:'form',method:'post',id:'frmDummy',action:'ExportServicePage.jsp', target:'_blank',name:'frmDummy',cls:'x-hidden',cn:[
                        {tag:'input',name:'exportContent',id:'exportContent',type:'hidden'}
                    ]},true);
                }
                fd.child('#exportContent').set({value: tmpExportContent});
                fd.dom.submit();
            } else {
                document.location = 'data:application/vnd.ms-excel;base64,' + Base64.encode(tmpExportContent);
            }
        });
        tmpAllStore.load(tmpParam);//获取所有数据
    }
};
Ext.ux.clone = function(obj) {
    if (obj == null || typeof (obj) != 'object')
        return obj;
    if (Ext.isDate(obj))
        return obj.clone();
    var cloneArray = function(arr) {
        var len = arr.length;
        var out = [];
        if (len >0) {
            for (var i = 0; i < len; ++i)
                out[i] = Ext.ux.clone(arr[i]);
        }
        return out;
    };
    var c = new obj.constructor();
    for (var prop in obj) {
        var p = obj[prop];
        if (Ext.isArray(p))
            c[prop] = cloneArray(p);
        else if (typeof p == 'object')
            c[prop] = Ext.ux.clone(p);
        else
            c[prop] = p;
    }
    return c;
};