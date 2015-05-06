package com.fruit.query.util;
import java.util.Map;
import com.fruit.query.data.OptionItem;
import com.fruit.query.report.Parameter;
import com.fruit.query.report.Report;

public interface IParaDefaultOption {
	public OptionItem getParaDefaultOption(Report rpt,Parameter para,Map paraVals)throws RptServiceException;
}
