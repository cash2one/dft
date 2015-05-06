package com.ifugle.dft.query.entity;

import org.apache.commons.lang.StringUtils;

public class QueryPlan {
	private long id;
	private String name;
	private String rptid;
	private String remark;
	private String savetime;
	private String qpcontent;
	private String userid;
	private int isdefault;
	
	public int getIsdefault() {
		return isdefault;
	}
	public void setIsdefault(int isdefault) {
		this.isdefault = isdefault;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRptid() {
		return rptid;
	}
	public void setRptid(String rptid) {
		this.rptid = rptid;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSavetime() {
		return savetime;
	}
	public void setSavetime(String savetime) {
		this.savetime = savetime;
	}
	public String getQpcontent() {
		return qpcontent;
	}
	public void setQpcontent(String qpcontent) {
		if (qpcontent != null && qpcontent.length() >= 1001
				&& qpcontent.length() <= 2000) {
			this.qpcontent = StringUtils.rightPad(qpcontent, 2010);
		} else {
			this.qpcontent = qpcontent;
		}
	}
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
}
