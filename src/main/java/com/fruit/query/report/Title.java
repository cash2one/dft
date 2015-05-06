package com.fruit.query.report;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 报表的标题
 */
public class Title {
	private String titleExp;
	/**
	 * 获取报表标题的表达式。
	 * 
	 * @return 设计文件中标题字符串。
	 */
	public String getTitleExp() {
		return titleExp;
	}
	/**
	 * 
	 * @param titleExp
	 */
	public void setTitleExp(String titleExp) {
		this.titleExp = titleExp;
	}
}
