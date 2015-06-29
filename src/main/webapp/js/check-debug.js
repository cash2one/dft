/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agull¨® Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.ck');

Ext.ck.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.ck.POLLING_URLS = {
}

Ext.ck.REMOTING_API = {
  url: Ext.ck.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    IncomeHandler: [
      {
        name: 'saveRuleDetails'/*(String, String, String, String, String, String, String) => String */,
        len: 7,
        formHandler: false
      },
      {
        name: 'getPRulesOfEn'/*(String, String) => java.util.List */,
        len: 2,
        formHandler: false
      },
      {
        name: 'deletePRulesByID'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'deletePRulesOfEn'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'toggleRuleQybj'/*(int, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getEnsToAddPRules'/*(int, int, String) => java.util.Map */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getPRuleEns'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      }
    ],
    CheckHandler: [
      {
        name: 'getBmPath'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'check'/*(String, String, String, int, int, int, int, int, String, String) => String */,
        len: 10,
        formHandler: false
      },
      {
        name: 'getEnChangeInfo'/*(int) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getFieldsFilter'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEnDjInfo'/*(int, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'checkChangeEn'/*(int, String, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'tryGetEns'/*(int, int, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getFields2Show'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'acceptEn'/*(String, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getCheckLogInfo'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getBmCodesTree'/*(String, String, String) => java.util.List */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getComboBms'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'importEnExcel'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'rebuildEnGrid'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEns'/*(int, int, int, String) => java.util.Map */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getBatchCheckFields'/*() => java.util.List */,
        len: 0,
        formHandler: false
      }
    ],
    EnHandler: [
      {
        name: 'importPzDetail'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'deleteVEn'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'doAutoMap'/*() => String */,
        len: 0,
        formHandler: false
      },
      {
        name: 'getPzDetail'/*(String, String) => java.util.List */,
        len: 2,
        formHandler: false
      },
      {
        name: 'mapEns'/*(int, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'checkSwdjzh'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveVirtualEn'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'savePzDetail'/*(String, String, String, String, String) => String */,
        len: 5,
        formHandler: false
      },
      {
        name: 'getNewPzDetail'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'getEnPzhBySwdjzh'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getVirtualEn'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEns'/*(int, int, String) => java.util.Map */,
        len: 3,
        formHandler: false
      },
      {
        name: 'undoMapping'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'delPz'/*(String, String) => String */,
        len: 2,
        formHandler: false
      }
    ]
  }
}

