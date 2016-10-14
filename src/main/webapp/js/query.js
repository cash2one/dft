/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agull¨® Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.query');

Ext.query.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.query.POLLING_URLS = {
}

Ext.query.REMOTING_API = {
  url: Ext.query.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    QueryHandler: [
      {
        name: 'getOptionItemsOfTree'/*(String, String, String, String) => java.util.List */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getUnits'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'deleteQueryPlan'/*(int) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getOptionItems'/*(String, String, String) => java.util.List */,
        len: 3,
        formHandler: false
      },
      {
        name: 'saveQueryPlan'/*(int, boolean, String, String, String, int, String) => String */,
        len: 7,
        formHandler: false
      },
      {
        name: 'getQueryPlans'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'queryGeneralDataDynamic'/*(String, int, int, String) => com.ifugle.dft.query.entity.StoreResult */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getFieldsOfComplexFilter'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      }
    ],
    PortalHandler: [
      {
        name: 'getOptionItemsOfTree'/*(String, String, String, String) => java.util.List */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getUnits'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'checkPortalid'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getOptionItems'/*(String, String, String) => java.util.List */,
        len: 3,
        formHandler: false
      },
      {
        name: 'savePortal'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'deletePortalDesign'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getPortalDesign'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'queryGeneralDataDynamic'/*(String, int, int, String) => com.ifugle.dft.query.entity.StoreResult */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getPortals'/*() => java.util.List */,
        len: 0,
        formHandler: false
      }
    ]
  }
}

