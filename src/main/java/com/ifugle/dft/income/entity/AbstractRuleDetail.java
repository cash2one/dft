package com.ifugle.dft.income.entity;

public abstract class AbstractRuleDetail {
	private int ruleid ;
	private String czfpbm;
	public int getRuleid() {
		return ruleid;
	}
	public void setRuleid(int ruleid) {
		this.ruleid = ruleid;
	}
	public String getCzfpbm() {
		return czfpbm;
	}
	public void setCzfpbm(String czfpbm) {
		this.czfpbm = czfpbm;
	}
}
