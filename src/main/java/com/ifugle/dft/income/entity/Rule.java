package com.ifugle.dft.income.entity;

import java.util.List;

public class Rule {
	private int ruleid;
	private int ruletype;
	private int qybj;
	private String detailtb;
	private String objaffect;
	private int begindate;
	private int enddate;
	private List details;
	private String description;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getRuleid() {
		return ruleid;
	}
	public List getDetails() {
		return details;
	}
	public void setDetails(List details) {
		this.details = details;
	}
	public void setRuleid(int ruleid) {
		this.ruleid = ruleid;
	}
	public int getRuletype() {
		return ruletype;
	}
	public void setRuletype(int ruletype) {
		this.ruletype = ruletype;
	}
	public int getQybj() {
		return qybj;
	}
	public void setQybj(int qybj) {
		this.qybj = qybj;
	}
	public String getDetailtb() {
		return detailtb;
	}
	public void setDetailtb(String detailtb) {
		this.detailtb = detailtb;
	}
	public String getObjaffect() {
		return objaffect;
	}
	public void setObjaffect(String objaffect) {
		this.objaffect = objaffect;
	}
	public int getBegindate() {
		return begindate;
	}
	public void setBegindate(int begindate) {
		this.begindate = begindate;
	}
	public int getEnddate() {
		return enddate;
	}
	public void setEnddate(int enddate) {
		this.enddate = enddate;
	}
}
