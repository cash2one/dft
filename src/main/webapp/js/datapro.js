/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agulló Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.datapro');

Ext.datapro.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.datapro.POLLING_URLS = {
}

Ext.datapro.REMOTING_API = {
  url: Ext.datapro.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    DataHandler: [
      {
        name: 'saveFormalApps'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'queryContributeOfEn'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'deleteTb'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'saveFormalAids'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'matchEnOfTmpAid'/*(int, String, int, String) => String */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getFormalAppData'/*(int, int, int) => java.util.Map */,
        len: 3,
        formHandler: false
      },
      {
        name: 'impData'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'getEnHistoryData'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'CheckTemplateDownload'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getAppTemplate'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'CheckTableName'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'checkApplyOfIid'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getImportedAppData'/*(int, String, int, int, int) => java.util.Map */,
        len: 5,
        formHandler: false
      },
      {
        name: 'deleteFormalAids'/*(int, int, String, String) => String */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getTbList'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'getImportedAidData'/*(int, String, String, int, int) => java.util.Map */,
        len: 5,
        formHandler: false
      },
      {
        name: 'addExtendTables'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEns'/*(int, int, String, String) => java.util.Map */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getFormalAidData'/*(int, String, int, int) => java.util.Map */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getSameAppCount'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getTbs'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      },
      {
        name: 'checkDoneAid'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveExtendTables'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'saveTempAids'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'deleteImportedApps'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveTempApps'/*(int, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'matchEn'/*(int, int, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'deleteFormalApps'/*(int, int, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'deleteImportedAids'/*(int, String, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getTbCols'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      }
    ]
  }
}

