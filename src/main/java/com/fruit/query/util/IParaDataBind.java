package com.fruit.query.util;
import com.fruit.query.data.*;
import com.fruit.query.report.*;
import javax.servlet.http.HttpServletRequest;
/**
 * 
 * @author wxh
 *2009-3-24
 *TODO 获取报表返回值的接口。如果报表参数不能直接从请求中获取（request.getParameter），
 * 也不能从会话（session）中获取，可以实现该接口，让其负责为报表参数提供值。
 */
public interface IParaDataBind {
	/**
	 * 返回特定参数的值。
	 * @param request 请求对象
	 * @param rpt 报表模板，包含报表定义信息。
	 * @param para 要取值的参数对象。
	 * @return 参数值对象。
	 */
	public ParaValue getParaValue(HttpServletRequest request,Report rpt,Parameter para);
}
