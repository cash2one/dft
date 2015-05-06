/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agull¨® Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.treasury');

Ext.treasury.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.treasury.POLLING_URLS = {
}

Ext.treasury.REMOTING_API = {
  url: Ext.treasury.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    TreasuryHandler: [
      {
        name: 'getJks'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'impTreasury'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      }
    ]
  }
}

