package com.ifugle.dft.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ifugle.dft.datapro.dao.DataProcessDao;
import com.ifugle.dft.datapro.handler.DataHandler;
import com.ifugle.dft.system.dao.MaintainDao;
import com.ifugle.dft.system.entity.SimpleBean;
import com.ifugle.dft.system.entity.User;
import com.ifugle.dft.utils.ContextUtil;
import com.ifugle.dft.utils.ExportController;
import com.ifugle.dft.utils.JsonHelper;
import com.softwarementors.extjs.djn.servlet.ssm.WebContext;
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager;

public class BaseServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse respond)
    throws ServletException, IOException {
		doPost(request,respond);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String action = request.getParameter("doType");
		String destination = "/index.jsp";
		RequestDispatcher dispatcher;
		String msg = null;	
		MaintainDao mtdao=(MaintainDao)ContextUtil.getBean("sysDao");
		try {
			if("login".equals(action)){
				String userid=request.getParameter("userID");
				String pswd=request.getParameter("pswd");
				User user= mtdao.authenticUser(userid, pswd);
				if(user!=null){
		    		//存放到cookie中
		    		Cookie userCookie = new Cookie("ifugle_dft_user", user.getUserid());
			   	    userCookie.setMaxAge(60*60*24*30);
			   	    response.addCookie(userCookie);
			   	    List xzs = null;
			   	    //不是管理员，则检查对应的乡镇和模块，是管理员，则不设置乡镇信息，默认有所有模块操作权限
			    	if(user.getIsManager()!=1){
			    		//获取用户对应的乡镇
			    		xzs = mtdao.getUser_xzs(user);
			    		user.setXzs(xzs);
			    	}
			   	    request.getSession().setAttribute("user", user);
			   	    request.getSession().setAttribute("userid", userid);
			   	    String sid = request.getSession().getId();
			   	    request.getSession().setAttribute("SESSIONID_DNFT5", sid);
					destination = "/main.jsp";
				}
			}else if("getModules".equals(action)){
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				String nodes = "";
				String pid = request.getParameter("pid");
				if(pid==null){
					pid = "";
				}
	    		List allModules = mtdao.getModules(pid);
	    		List userModules = null;
	    		User user = (User)request.getSession().getAttribute("user");
	    		if(user!=null){
		    		if(user.getIsManager()==1){
		    			userModules = allModules;
		    		}else{
		    			userModules = mtdao.getAccessableModules(user.getUserid(),pid);
		    		}
		    		/*if(userModules!=null){
		    			for(int i=0;i<userModules.size();i++){
		    				SimpleBean sb = (SimpleBean)userModules.get(i);
		    				if(sb.getIsLeaf()==1){
		    					sb.setNodeIcon("images/bfile.png");
		    				}
		    			}
		    		}*/
		    		request.getSession().setAttribute("userModules", userModules);
		    		nodes = JsonHelper.getJsonHelper().getTreeNodesOfJson(userModules, 0, "");
	    		}
	    		out.print(nodes);
				out.close();
				return;
			}else if("export".equals(action)){
				WebContextManager.initializeWebContextForCurrentThread(this, request, response);
				WebContext context =WebContextManager.get();
				ExportController ec = new ExportController();
				ec.doExport(request, response);
				return;
			}else if("exportExtendTemplate".equals(action)){
				WebContextManager.initializeWebContextForCurrentThread(this, request, response);
				WebContext context =WebContextManager.get();
				DataHandler dh = new DataHandler();
				dh.exportTemplate(request, response);
			}
		}catch(Exception e) {
			destination = "/index.jsp";
			msg = e.getMessage();
			request.setAttribute("failedInfo", msg);
		}
		dispatcher = getServletContext().getRequestDispatcher(destination);
		dispatcher.forward(request, response);
	}
}
