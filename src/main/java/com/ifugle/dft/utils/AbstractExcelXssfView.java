package com.ifugle.dft.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ifugle.dft.utils.entity.Column;

public class AbstractExcelXssfView {
	protected Cell getCell(Sheet sheet, int rowNum, int col,short rowHeight) {
		Row sheetRow = sheet.getRow(rowNum);
		if (sheetRow == null){
			sheetRow = sheet.createRow(rowNum);
			sheetRow.setHeight(rowHeight);
		}
		Cell cell = sheetRow.getCell((short) col);
		if (cell == null){
			cell = sheetRow.createCell((short) col);
		}
		return cell;
	}

	protected void setText(Cell cell, String text) {
		cell.setCellType(Cell.CELL_TYPE_STRING);
		cell.setCellValue(text);
	}
	//按单元格的渲染设置，进一步加工样式
	public CellStyle cellStyleFormat(Column col,Workbook wb,boolean isContent,Style style,String unit){
		CellStyle cs=null;
		if(col.getDataType()==0){
			if(isContent){
				if("right".equals(col.getAlign())){
					cs = style.getStrContStyle_R(wb);
				}else if("center".equals(col.getAlign())){
					cs=style.getStrContStyle_C(wb);
				}else{
					cs=style.getStrContStyle(wb);
				}
			}else{
				cs=style.getStrSecStyle(wb);
			}
		}else{
			String render=col.getRenderer();
			if(!StringUtils.isEmpty(render)){
				if(isContent){
			    	if("rMoney".equals(render)||"rWan2Decimals".equals(render)){
			    		if("center".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_L(wb);
			    		}else{
			    			cs=style.getMoneyContStyle(wb);
			    		}
			    	}else if("renderInt".equals(render)||"rWanInt".equals(render)){
			    		if("center".equals(col.getAlign())){
			    			cs=style.getIntContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getIntContStyle_L(wb);
			    		}else{
			    			cs=style.getIntContStyle(wb);
			    		}
			    	}else{
			    		if("center".equals(col.getAlign())){
			    			cs=style.getNumContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getNumContStyle_L(wb);
			    		}else{
			    			cs=style.getNumContStyle(wb);
			    		}
			    	}
				}else{
					if("rMoney".equals(render)||"rWan2Decimals".equals(render)){
			    		cs=style.getMoneySecStyle(wb);
			    	}else if("renderInt".equals(render)||"rWanInt".equals(render)){
			    		cs=style.getIntSecStyle(wb);
			    	}else{
			    		cs=style.getNumSecStyle(wb);
			    	}
				}
			}else if(col.getIsMultiUnit()==1){
				if("yuan2Decimals".equals(unit)||"wan2Decimals".equals(unit)){
					if(isContent){
						if("center".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getMoneyContStyle_L(wb);
			    		}else{
			    			cs=style.getMoneyContStyle(wb);
			    		}
					}else{
						cs=style.getMoneySecStyle(wb);
					}
				}else if("yuanInt".equals(unit)||"wanInt".equals(unit)){
					if(isContent){
						if("center".equals(col.getAlign())){
			    			cs=style.getIntContStyle_C(wb);
			    		}else if("left".equals(col.getAlign())){
			    			cs=style.getIntContStyle_L(wb);
			    		}else{
			    			cs=style.getIntContStyle(wb);
			    		}
					}else{
						cs=style.getIntSecStyle(wb);
					}
				}
			}else{
				if(isContent){
					if("center".equals(col.getAlign())){
		    			cs=style.getNumContStyle_C(wb);
		    		}else if("left".equals(col.getAlign())){
		    			cs=style.getNumContStyle_L(wb);
		    		}else{
		    			cs=style.getNumContStyle(wb);
		    		}
				}else{
					cs=style.getNumSecStyle(wb);
				}
			}
		}
		return cs;
	}
}
