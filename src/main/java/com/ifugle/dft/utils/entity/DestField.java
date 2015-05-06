package com.ifugle.dft.utils.entity;
/**
 * 导入的目标表的字段信息
 * @author wxh
 *2009-5-19
 */
public class DestField {
	private int excelcol;
	private String tbname;
	private String colname;
	private String coldesc;
	private int coltype;
	private int rptkey;
	private int isrindex;
	private int showorder;
	public int getExcelcol() {
		return excelcol;
	}
	public void setExcelcol(int excelcol) {
		this.excelcol = excelcol;
	}
	public String getTbname() {
		return tbname;
	}
	public void setTbname(String tbname) {
		this.tbname = tbname;
	}
	public String getColname() {
		return colname;
	}
	public void setColname(String colname) {
		this.colname = colname;
	}
	public String getColdesc() {
		return coldesc;
	}
	public void setColdesc(String coldesc) {
		this.coldesc = coldesc;
	}
	public int getColtype() {
		return coltype;
	}
	public void setColtype(int coltype) {
		this.coltype = coltype;
	}
	public int getRptkey() {
		return rptkey;
	}
	public void setRptkey(int rptkey) {
		this.rptkey = rptkey;
	}
	public int getIsrindex() {
		return isrindex;
	}
	public void setIsrindex(int isrindex) {
		this.isrindex = isrindex;
	}
	public int getShoworder() {
		return showorder;
	}
	public void setShoworder(int showorder) {
		this.showorder = showorder;
	}
	
}
