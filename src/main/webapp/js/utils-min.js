Ext.namespace("Ext.utils");
Ext.utils.PROVIDER_BASE_URL=window.location.protocol+"//"+window.location.host+"/"+(window.location.pathname.split("/").length>2?window.location.pathname.split("/")[1]+"/":"")+"djn/directprovider";
Ext.utils.POLLING_URLS={};
Ext.utils.REMOTING_API={url:Ext.utils.PROVIDER_BASE_URL,type:"remoting",actions:{EnCollectionHandler:[{name:"checkCollectionNode",len:1,formHandler:false},{name:"toggleQybj",len:3,formHandler:false},{name:"getEnCollection",len:1,formHandler:false},{name:"getImportedEns",len:2,formHandler:false},{name:"saveEnOrder",len:2,formHandler:false},{name:"saveEnCollection",len:1,formHandler:true},{name:"checkCode",len:2,formHandler:false},{name:"removeEn",len:2,formHandler:false},{name:"deleteEnCollection",len:1,formHandler:false},{name:"addExcelMatchEns",len:3,formHandler:false},{name:"importEnExcel",len:1,formHandler:true},{name:"getEnCollectionById",len:1,formHandler:false},{name:"getCollectionEns",len:5,formHandler:false},{name:"addEn",len:2,formHandler:false},{name:"delImportedEns",len:1,formHandler:false},{name:"getEnterprisesToAdd",len:5,formHandler:false}]}};