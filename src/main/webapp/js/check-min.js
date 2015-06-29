Ext.namespace("Ext.ck");
Ext.ck.PROVIDER_BASE_URL=window.location.protocol+"//"+window.location.host+"/"+(window.location.pathname.split("/").length>2?window.location.pathname.split("/")[1]+"/":"")+"djn/directprovider";
Ext.ck.POLLING_URLS={};
Ext.ck.REMOTING_API={url:Ext.ck.PROVIDER_BASE_URL,type:"remoting",actions:{IncomeHandler:[{name:"saveRuleDetails",len:7,formHandler:false},{name:"getPRulesOfEn",len:2,formHandler:false},{name:"deletePRulesByID",len:1,formHandler:false},{name:"deletePRulesOfEn",len:2,formHandler:false},{name:"toggleRuleQybj",len:2,formHandler:false},{name:"getEnsToAddPRules",len:3,formHandler:false},{name:"getPRuleEns",len:2,formHandler:false}],CheckHandler:[{name:"getBmPath",len:2,formHandler:false},{name:"check",len:10,formHandler:false},{name:"getEnChangeInfo",len:1,formHandler:false},{name:"getFieldsFilter",len:1,formHandler:false},{name:"getEnDjInfo",len:2,formHandler:false},{name:"checkChangeEn",len:3,formHandler:false},{name:"tryGetEns",len:3,formHandler:false},{name:"getFields2Show",len:0,formHandler:false},{name:"acceptEn",len:2,formHandler:false},{name:"getCheckLogInfo",len:1,formHandler:false},{name:"getBmCodesTree",len:3,formHandler:false},{name:"getComboBms",len:1,formHandler:false},{name:"importEnExcel",len:1,formHandler:true},{name:"rebuildEnGrid",len:1,formHandler:false},{name:"getEns",len:4,formHandler:false},{name:"getBatchCheckFields",len:0,formHandler:false}],EnHandler:[{name:"importPzDetail",len:1,formHandler:true},{name:"deleteVEn",len:1,formHandler:false},{name:"doAutoMap",len:0,formHandler:false},{name:"getPzDetail",len:2,formHandler:false},{name:"mapEns",len:2,formHandler:false},{name:"checkSwdjzh",len:2,formHandler:false},{name:"saveVirtualEn",len:1,formHandler:true},{name:"savePzDetail",len:5,formHandler:false},{name:"getNewPzDetail",len:0,formHandler:false},{name:"getEnPzhBySwdjzh",len:1,formHandler:false},{name:"getVirtualEn",len:1,formHandler:false},{name:"getEns",len:3,formHandler:false},{name:"undoMapping",len:1,formHandler:false},{name:"delPz",len:2,formHandler:false}]}};