package com.ifugle.dft.treasury.entity;

public class TreasuryImpMap {
	private String skgkdm_jk;
	private int mtype;
	private String fnode;
	private String colname;
	private int rptkeyflag;
	private int dtype;  //目标库表字段类型。0：字符串，1：整数，2：小数
	public int getDtype() {
		return dtype;
	}
	public void setDtype(int dtype) {
		this.dtype = dtype;
	}
	public String getSkgkdm_jk() {
		return skgkdm_jk;
	}
	public void setSkgkdm_jk(String skgkdmJk) {
		skgkdm_jk = skgkdmJk;
	}
	public int getMtype() {
		return mtype;
	}
	public void setMtype(int mtype) {
		this.mtype = mtype;
	}
	public String getFnode() {
		return fnode;
	}
	public void setFnode(String fnode) {
		this.fnode = fnode;
	}
	public String getColname() {
		return colname;
	}
	public void setColname(String colname) {
		this.colname = colname;
	}
	public int getRptkeyflag() {
		return rptkeyflag;
	}
	public void setRptkeyflag(int rptkeyflag) {
		this.rptkeyflag = rptkeyflag;
	}
	
}
