package com.ifugle.dft.utils.entity;

import java.util.List;
import java.util.Map;

public class ExcelTemplate {
	private String tid;
	private String tbname;
	@SuppressWarnings("unchecked")
	private Map colmaps = null;
	@SuppressWarnings("unchecked")
	private List columns = null;
	private ExcelTable tb;
	public ExcelTable getTb() {
		return tb;
	}
	public void setTb(ExcelTable tb) {
		this.tb = tb;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getTbname() {
		return tbname;
	}
	public void setTbname(String tbname) {
		this.tbname = tbname;
	}
	
	@SuppressWarnings("unchecked")
	public Map getColmaps() {
		return colmaps;
	}
	@SuppressWarnings("unchecked")
	public void setColmaps(Map colmaps) {
		this.colmaps = colmaps;
	}
	@SuppressWarnings("unchecked")
	public List getColumns() {
		return columns;
	}
	@SuppressWarnings("unchecked")
	public void setColumns(List columns) {
		this.columns = columns;
	}
}
