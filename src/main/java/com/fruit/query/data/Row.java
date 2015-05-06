package com.fruit.query.data;
import java.util.*;
/**
 * 
 * @author wxh
 *2009-3-16
 *TODO 描述报表数据的每一行
 */
public class Row {
	private Map cells;
	/**
	 * 行中的各个单元格内容集合。
	 * 行中各个field按字段名――值方式索引形成的map，名-值都是String类型存放。
	 * @return 单元格构成的map。
	 */
	public Map getCells() {
		return cells;
	}

	public void setCells(Map cells) {
		this.cells = cells;
	}
}
