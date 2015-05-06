package com.ifugle.dft.income.entity;

import com.ifugle.dft.check.entity.Enterprise;

public class EnHasRules extends Enterprise{
	private int hasrule;
	private int inuse;
	private String rule;
	public int getHasrule() {
		return hasrule;
	}
	public void setHasrule(int hasrule) {
		this.hasrule = hasrule;
	}
	public int getInuse() {
		return inuse;
	}
	public void setInuse(int inuse) {
		this.inuse = inuse;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
}
