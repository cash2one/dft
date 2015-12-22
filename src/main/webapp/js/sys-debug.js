/**********************************************************************
 * 
 * Code generated automatically by DirectJNgine
 * Copyright (c) 2009, Pedro Agulló Soliveres
 * 
 * DO NOT MODIFY MANUALLY!!
 * 
 **********************************************************************/

Ext.namespace( 'Ext.sys');

Ext.sys.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '')  + 'djn/directprovider';

Ext.sys.POLLING_URLS = {
}

Ext.sys.REMOTING_API = {
  url: Ext.sys.PROVIDER_BASE_URL,
  type: 'remoting',
  actions: {
    MaintainHandler: [
      {
        name: 'getModuleExpandPathes'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getAlias'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'changePswd'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'setUserPosts'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'checkUserid'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'setPostModules'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'updatePost'/*(String, String, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'deletePost'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'saveUser'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'getUserInfo'/*(String) => com.ifugle.dft.system.entity.User */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getReportsToAudit'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      },
      {
        name: 'publishReports'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'saveUserPosts'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'undoPublishReports'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getModulesOfPost'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getPostsTreeByUserid'/*(String, String) => java.util.List */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getModuleTree'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getPostsTree'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'deleteUser'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getModuleInfo'/*(String) => String */,
        len: 1,
        formHandler: false
      },
      {
        name: 'addPost'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getUsers'/*(int, int) => java.util.Map */,
        len: 2,
        formHandler: false
      }
    ],
    CodeHandler: [
      {
        name: 'saveCodeTableMapping'/*(String, String, int) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'delAidItems'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getAidItem'/*(String) => com.ifugle.dft.system.entity.AidItem */,
        len: 1,
        formHandler: false
      },
      {
        name: 'deleteCodeTable'/*(String, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'deleteCode'/*(String, String, String, int, String) => String */,
        len: 5,
        formHandler: false
      },
      {
        name: 'getCodeTables'/*(int) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getGrades'/*() => java.util.List */,
        len: 0,
        formHandler: false
      },
      {
        name: 'getAidItemsMtTree'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getMappingPath'/*(int, String, String) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'saveMappingT2F'/*(int, String, String, String, String) => String */,
        len: 5,
        formHandler: false
      },
      {
        name: 'getFCodesTree'/*(String, String, String, int) => java.util.List */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getCode'/*(String, int, String) => com.ifugle.dft.system.entity.Code */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getNotMappedTaxCodes'/*(int, int, int) => java.util.Map */,
        len: 3,
        formHandler: false
      },
      {
        name: 'saveMappingF2T'/*(String, String, String, String, String) => String */,
        len: 5,
        formHandler: false
      },
      {
        name: 'getAidItems'/*(String) => java.util.List */,
        len: 1,
        formHandler: false
      },
      {
        name: 'getMappedFTable'/*(String, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'moveCode'/*(int, String, String, String, String) => String */,
        len: 5,
        formHandler: false
      },
      {
        name: 'searchForAidItem'/*(String, String) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'getTableMappingInfo'/*(String, int, int) => String */,
        len: 3,
        formHandler: false
      },
      {
        name: 'getNotMappingCount'/*() => String */,
        len: 0,
        formHandler: false
      },
      {
        name: 'deleteCodeTableMapping'/*(String, int) => String */,
        len: 2,
        formHandler: false
      },
      {
        name: 'saveAidItem'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'saveCodeTable'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'saveCode'/*() => com.ifugle.dft.utils.entity.SubmitResult -- FORM HANDLER */,
        len: 1,
        formHandler: true
      },
      {
        name: 'searchForCode'/*(String, int, String, String) => String */,
        len: 4,
        formHandler: false
      },
      {
        name: 'getTCodesTree'/*(String, String, int, String, int) => java.util.List */,
        len: 5,
        formHandler: false
      }
    ]
  }
}

