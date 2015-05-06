package com.fruit.query.data;
/**
 * 
 * @author wxh
 *2009-5-4
 *TODO 报表模板的文件结构
 */
public class ReportFile {
	private String path;
	private int isFile;
	private String pPath;
	/**
	 * 是否是文件
	 * @return 文件还是文件夹，1：文件；0：文件夹
	 */
	public int getIsFile() {
		return isFile;
	}
	public void setIsFile(int isFile) {
		this.isFile = isFile;
	}
	/**
	 * 文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * 文件所属的文件夹，直接在报表的repository目录下的，该属性为""
	 * @return 文件所属的文件夹
	 */
	public String getPPath() {
		return pPath;
	}
	public void setPPath(String path) {
		pPath = path;
	}
}
