package com.fruit.query.report;

import com.fruit.query.data.*;
/**
 * 
 * @author wxh
 *2009-3-11
 *TODO 数据集定义
 */
public class DataDefine {
	private int sourceType;//取数方式
	private int canPaging;  //是否能分页
	private String name;
	private int defaultPageSize=20; //默认的每页记录数
	private int maxSize=200;     //分页时有用，每页最多显示多少条记录。
	private int pagingMode;    //分页方式。0：服务端分页，1：客户端分页
	private int autoSubTotal;
	private String recordID;
	private String parentID;
	private String sql;   //取数sql，如果取数方式是1时使用。
	private ProcedureBean procedure;  //取数用存储过程
	//如果报表是静态数据，则包含从文件读取的静态数据。该属性基本不用。
	//从文本中读取报表数据的解析器也暂时不实现。
	
	private String implClass;//报表取数由自定义类实现
	private DataSet staticDataSet;
	/**
	 * 报表数据是否分页。
	 * @return 0：不分页；1：分页显示
	 */
	public int getCanPaging() {
		return canPaging;
	}
	/**
	 * 
	 * @param canPaging
	 */
	public void setCanPaging(int canPaging) {
		this.canPaging = canPaging;
	}
	/**
	 * 报表分页显示时，提供默认的每页记录数。
	 * @return 每页记录数。
	 */
	public int getDefaultPageSize() {
		return defaultPageSize;
	}
	/**
	 * 
	 * @param defaultPageSize
	 */
	public void setDefaultPageSize(int defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}
	/**
	 * 分页的方式。<br>
	 * 0：默认模式，取数前计算好取数范围；<br>
	 * 1：取数后，对记录集加工，形成新的记录集，再获取指定范围的子集。<br>
	 * 分页方式1一般用于需要引擎加工记录集时的分页，比如插入分组小计行的情况，这时记录数往往会发生变化。<br>
	 * 采用方式1能正确统计总页数，翻页时的记录范围也能保证正确。<br>
	 * <b>启用引擎分组时，报表分页模式强制为1(paginMode=1)。</b>
	 * 
	 * @return 分页方式
	 */
	public int getPagingMode() {
		return pagingMode;
	}
	/**
	 * 
	 * @param pagingMode
	 */
	public void setPagingMode(int pagingMode) {
		this.pagingMode = pagingMode;
	}
	/**
	 * 采用存储过程取数(sourceType=2)时，使用的存储过程的定义信息。
	 * @return 存储过程定义信息对象。
	 * @see com.datanew.query.data.ProcedureBean
	 */
	public ProcedureBean getProcedure() {
		return procedure;
	}
	/**
	 * 
	 * @param procedure
	 */
	public void setProcedure(ProcedureBean procedure) {
		this.procedure = procedure;
	}
	/**
	 * 报表数据的取数方式。
	 * 0：静态；1：sql语句取数；2：存储过程取数；3：自定义类实现取数
	 * @return 报表取数方式
	 */
	public int getSourceType() {
		return sourceType;
	}
	/**
	 * 
	 * @param sourceType
	 */
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	/**
	 * 采用sql取数(sourceType=1)时的sql语句。<br>
	 * sql中能引用参数，引用方式为{参数名}，如A.CZFPBM={czfpbm}，大括号中的czfpbm为参数名。
	 * @return sql语句
	 */
	public String getSql() {
		return sql;
	}
	/**
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	/**
	 * 获取报表静态数据集合。
	 * 如果报表数据是静态的，则在报表定义文件读取时就能获取报表数据。<br>
	 * 由于很少有此种情况，目前暂时未具体实现，仅保留此方法，便于今后需要时扩展。<br>
	 * 报表非静态取数模式下，根据每次请求构造数据，数据也不在DataDefine对象中传递。和ColumnDefine类似。
	 * @return 报表的静态数据集。
	 * @see ColumnDefine#getColumns()
	 */
	public DataSet getStaticDataSet() {
		return staticDataSet;
	}
	public void setStaticDataSet(DataSet staticDataSet) {
		this.staticDataSet = staticDataSet;
	}
	/**
	 * 采用自定义类取数(sourceType=3)时，这个自定义类的全路径。<br>
	 * 这个类应实现com.datanew.query.util.IDataService接口
	 * @return 类的全路径
	 * @see com.datanew.query.util.IDataService
	 */
	public String getImplClass() {
		return implClass;
	}
	/**
	 * 
	 * @param implClass
	 */
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}
	/**
	 * 分页时，每页最多显示多少行。
	 * @return 每页最多显示多少行。
	 */
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	/**
	 * 获取是否自动逐层行小计的配置
	 * @return 是否自动逐层进行行小计
	 */
	public int getAutoSubTotal() {
		return autoSubTotal;
	}
	/**
	 * 
	 * @param aotuSubTotal
	 */
	public void setAutoSubTotal(int autoSubTotal) {
		this.autoSubTotal = autoSubTotal;
	}
	/**
	 * 自动行小计时，哪个字段内容是指向父ID的。
	 * @return 存放父ID的字段
	 */
	public String getParentID() {
		return parentID;
	}
	/**
	 * 
	 * @param parentID
	 */
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}
	/**
	 * 获取记录的标识字段
	 * @return 记录的标识字段
	 */
	public String getRecordID() {
		return recordID;
	}
	/**
	 * 
	 * @param recordID
	 */
	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
