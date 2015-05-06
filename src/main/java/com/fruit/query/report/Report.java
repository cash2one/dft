package com.fruit.query.report;
import java.io.File;
import java.util.*;
/**
 * 
 * @author wxh
 * 2009-3-11
 * TODO 报表的描述信息
 */
public class Report extends ReportBase{
	private int totalRow;    //是否有总计
	private int totalPosition; //总计行出现的位置。0：末尾，1：头
	private String[] totalFields;//总计计算的列
	private String totalLabelIndex; //总计的标签――即“总计”两字出现在哪个列
	private List paras;
	private Title title;
	private ColumnDefine columnDef;
	private DataDefine defaultDataDef;
	private Grouping groupDef;
	private Map parasMap;
	private Map paraProcesses;
	private int remoteSort;   //是否远程排序
	private List parasForFilter; //用于筛选的字段参数。
	private Map paras4FilterMap;
	private List dataDefines;
	//2013-10-11 增加表头表尾区域
	private Head head;
	private Foot foot;
	
	private Chart chart;
	
	//2014-11-12
	private Export exportInfo;
	//保存为模板文件时的文件名
	private String saveFileName;
	
	public String getSaveFileName() {
		return saveFileName;
	}
	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}
	
	
	public Export getExportInfo() {
		return exportInfo;
	}
	public void setExportInfo(Export exportInfo) {
		this.exportInfo = exportInfo;
	}
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public Head getHead() {
		return head;
	}
	
	public void setHead(Head head) {
		this.head = head;
	}
	public Foot getFoot() {
		return foot;
	}
	public void setFoot(Foot foot) {
		this.foot = foot;
	}
	public Map getParas4FilterMap() {
		return paras4FilterMap;
	}
	public void setParas4FilterMap(Map paras4FilterMap) {
		this.paras4FilterMap = paras4FilterMap;
	}
	public List getParasForFilter() {
		return parasForFilter;
	}
	public void setParasForFilter(List parasForFilter) {
		this.parasForFilter = parasForFilter;
	}
	public int getRemoteSort() {
		return remoteSort;
	}
	public void setRemoteSort(int remoteSort) {
		this.remoteSort = remoteSort;
	}
	/**
	 * 获取报表的列定义信息。
	 * @return ColumnDefine对象。
	 * @see com.datanew.query.report.ColumnDefine
	 */
	public ColumnDefine getColumnDef() {
		return columnDef;
	}
	/**
	 * 
	 * @param columnDef
	 */
	public void setColumnDef(ColumnDefine columnDef) {
		this.columnDef = columnDef;
	}
	/**
	 * 获取报表的取数定义信息。
	 * @return DataDefine对象。
	 * @see com.datanew.query.report.DataDefine
	 */
	public DataDefine getDefaultDataDef() {
		return defaultDataDef;
	}
	/**
	 * 
	 * @param dataDef
	 */
	public void setDefaultDataDef(DataDefine defaultDataDef) {
		this.defaultDataDef = defaultDataDef;
	}
	/**
	 * 获取报表的分组功能定义信息。
	 * @return Grouping对象。
	 * @see com.datanew.query.report.Grouping
	 */
	public Grouping getGroupDef() {
		return groupDef;
	}
	/**
	 * 
	 * @param groupDef
	 */
	public void setGroupDef(Grouping groupDef) {
		this.groupDef = groupDef;
	}
	
	/**
	 * 获取报表的参数定义集合。
	 * 集合中的元素是Parameter对象。
	 * @return 参数定义集合。
	 * @see com.datanew.query.report.Parameter
	 */
	public List getParas() {
		return paras;
	}
	/**
	 * 
	 * @param paras
	 */
	public void setParas(List paras) {
		this.paras = paras;
	}
	/**
	 * 获取报表的标题。
	 * @return Title对象。
	 * @see com.datanew.query.report.Title
	 */
	public Title getTitle() {
		return title;
	}
	/**
	 * 
	 * @param title
	 */
	public void setTitle(Title title) {
		this.title = title;
	}
	/**
	 * 获取报表的参数map。
	 * 以参数名索引参数定义对象的map。
	 * @return 参数map。
	 */
	public Map getParasMap() {
		return parasMap;
	}
	/**
	 * 
	 * @param parasMap
	 */
	public void setParasMap(Map parasMap) {
		this.parasMap = parasMap;
	}
	/**
	 * 获取参与行总计的字段。
	 * 对于动态列，如果启用了总计行，非字符串类型的字段都参与行总计。
	 * @return 计算行总计的字段名。
	 */
	public String[] getTotalFields() {
		return totalFields;
	}
	/**
	 * 
	 * @param totalFields
	 */
	public void setTotalFields(String[] totalFields) {
		this.totalFields = totalFields;
	}
	/**
	 * 返回总计行的位置。
	 * 0:默认，总计行在表尾；1：总计行在内容第一行。
	 * @return 总计行的位置
	 */
	public int getTotalPosition() {
		return totalPosition;
	}
	/**
	 * 
	 * @param totalPosition
	 */
	public void setTotalPosition(int totalPosition) {
		this.totalPosition = totalPosition;
	}
	/**
	 * 是否自动插入总计行。
	 * 0：无；1：插入自动总计行。
	 * @return 是否自动插入总计行
	 */
	public int getTotalRow() {
		return totalRow;
	}
	/**
	 * 
	 * @param totalRow
	 */
	public void setTotalRow(int totalRow) {
		this.totalRow = totalRow;
	}
	/**
	 * “总计”出现的列位置。
	 * @return “总计”出现在哪列。
	 */
	public String getTotalLabelIndex() {
		return totalLabelIndex;
	}
	public void setTotalLabelIndex(String totalLabelIndex) {
		this.totalLabelIndex = totalLabelIndex;
	}
	/**
	 * 获取报表中参数加工的过程。
	 * @return 报表参数加工信息集合
	 */
	public Map getParaProcesses() {
		return paraProcesses;
	}
	/**
	 * 
	 * @param paraProcesses
	 */
	public void setParaProcesses(Map paraProcesses) {
		this.paraProcesses = paraProcesses;
	}
	public List getDataDefines() {
		return dataDefines;
	}
	public void setDataDefines(List dataDefines) {
		this.dataDefines = dataDefines;
	}
	
	public static void main(String[] args){
		File rptFile=new File("c:/测试一下文件名的文件.txt"); 
		String fileName = rptFile.getName();
		System.out.println(fileName);
		try{ 
			if(!rptFile.exists()){
				 rptFile.createNewFile();
			}
		}catch(Exception e){
			
		}
	}
}
