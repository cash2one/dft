package com.ifugle.dft.query.entity;

import java.io.Serializable;
import java.util.List;
/**
 * DirectStore 请求返回的结构。
 * 
 */

public class StoreResult implements Serializable{
    private List records;
    private Long total;
    private Object metaData;
    private Boolean success;
    private String title ;
    private String subTitleLeft;
    private String subTitleCenter;
    private String subTitleRight;
    private String footLeft;
    private String footCenter;
    private String footRight;
    
	public String getTitle() {
		return title;
	}

	public String getSubTitleLeft() {
		return subTitleLeft;
	}

	public void setSubTitleLeft(String subTitleLeft) {
		this.subTitleLeft = subTitleLeft;
	}

	public String getSubTitleCenter() {
		return subTitleCenter;
	}

	public void setSubTitleCenter(String subTitleCenter) {
		this.subTitleCenter = subTitleCenter;
	}

	public String getSubTitleRight() {
		return subTitleRight;
	}

	public void setSubTitleRight(String subTitleRight) {
		this.subTitleRight = subTitleRight;
	}

	public String getFootLeft() {
		return footLeft;
	}

	public void setFootLeft(String footLeft) {
		this.footLeft = footLeft;
	}

	public String getFootCenter() {
		return footCenter;
	}

	public void setFootCenter(String footCenter) {
		this.footCenter = footCenter;
	}

	public String getFootRight() {
		return footRight;
	}

	public void setFootRight(String footRight) {
		this.footRight = footRight;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public StoreResult() {
    }

    public StoreResult(final List records) {
        this(records, null, null, true);
    }

    public StoreResult(final List records, final Long total) {
        this(records, total, null, true);
    }

    public StoreResult(final List records, final Long total, final Object metaData) {
        this(records, total, metaData, true);
    }

    public StoreResult(final List records, final Long total, final Object metaData, final Boolean success) {
        this.records = records;
        this.total = total;
        this.metaData = metaData;
        this.success = success;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRecords() {
        return records;
    }

    public void setRecords(final List records) {
        this.records = records;
    }

    public Object getMetaData() {
        return metaData;
    }

    public void setMetaData(Object metaData) {
        this.metaData = metaData;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "StoreResult [records=" + records + ", metaData=" + metaData + ", success=" + success + ", total="
                + total + "]";
    }

}
