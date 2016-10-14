/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agull¨® Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.utils');

Ext.utils.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.utils.POLLING_URLS = {
}

Ext.utils.REMOTING_API = {
  url: Ext.utils.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    EnCollectionHandler: [
      {
        name: 'checkCollectionNode'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'toggleQybj'/*(String, String, int) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getEnCollection'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getImportedEns'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveEnOrder'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveEnCollection'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'checkCode'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'removeEn'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'deleteEnCollection'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'addExcelMatchEns'/*(String, String, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'importEnExcel'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'getEnCollectionById'/*(String) => com.ifugle.dft.check.entity.EnCollection */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getCollectionEns'/*(int, int, String, String, String) => java.util.Map */,
        len: 5,
        formHandler: false
      },
      {
        name: 'addEn'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'delImportedEns'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEnterprisesToAdd'/*(int, int, String, String, String) => java.util.Map */,
        len: 5,
        formHandler: false
      }
    ]
  }
}

