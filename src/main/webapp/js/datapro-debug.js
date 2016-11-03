/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agull¨® Soliveres
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
        name: 'CheckTemplateDownload'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'CheckTableName'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getTbs'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getTbList'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'saveExtendTables'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'deleteTb'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'addExtendTables'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getTbCols'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getEns'/*(int, int, String, String) => java.util.Map */,
        len: 4,
        formHandler: false
      },
      {
        name: 'impData'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      }
    ]
  }
}

