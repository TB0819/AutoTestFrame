package com.frame.casedatafactory;

import com.frame.config.Constants;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 读取excel文件用例
 */
public class ExcelTestCaseData extends TestCaseData{
    private static final Logger logger = Logger.getLogger(ExcelTestCaseData.class);
	private Workbook book = null;
    private Sheet sheet = null;
    private String path = "";

    
    /**
	 * 数据读取初始化<br/>
     * 默认读取第一个 sheet<br/>
     */
    public void initAction(String filePath) throws Exception{
    	path = filePath;
        InputStream inputStream = null;
    	try {
            inputStream = new FileInputStream(path);
            if(getExcelType(filePath)){
            	book = new HSSFWorkbook(inputStream);   
            }else{
            	book = new XSSFWorkbook(inputStream);  
            }
            sheet =  book.getSheetAt(0);
            rowNum = sheet.getLastRowNum() + 1; 
                       
            columnNum = sheet.getRow(0).getLastCellNum();
            columnName = new String[columnNum];
            tableList = new ArrayList();
            // 先行后列
            for (int j = 0; j< rowNum; j++) {
                String[] columnArray = new String[columnNum];
                for (int i = 0; i < columnNum; i++){
                    if (j == 0) {
                        columnName[i] = sheet.getRow(0).getCell(i).getStringCellValue();
                    }
                    columnArray[i] = getCellContent(sheet.getRow(j).getCell(i));
                    if (i == columnNum-1){
                        tableList.add(columnArray);
                    }
                }
            }
            this.currentRowNo++;
        } finally {
    	    try {
                book.close();
                inputStream.close();
            }catch (Exception e){
                logger.error(e);
            }
        }
    }

    /**
     * 判断 EXCEL 版本, 老版本返回 true, 新版本返回 false<br/>
     */
    private Boolean getExcelType(String filePath) throws Exception{
    	String[] strs = filePath.split("\\.");
    	if(null == strs || strs.length < 2){
    		throw new Exception(Constants.ExceptionMessage.FILE_PATH_ERROR);
    	}
    	if(strs[1].equals("xlsx")){
    		return false;
    	}
    	return true;
    }
    
    
    /**
     * 获取单元格中的内容<br/>
     */
    private String getCellContent(Cell cell){
    	String result = "";
    	if (cell == null) {
    	    return result;
        }
        CellType cellType = cell.getCellTypeEnum();
        // 把数字当成String来读，避免出现1读成1.0的情况
        if (cellType == CellType.NUMERIC) {
            cell.setCellType(CellType.STRING);
        }
        // 判断数据的类型
        switch (cellType) {
            case NUMERIC:
                result = String.valueOf(cell.getStringCellValue());
                break;
            case STRING: // 字符串
                result = String.valueOf(cell.getStringCellValue());
                break;
            case BOOLEAN: // Boolean
                result = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA: // 公式
                result = String.valueOf(cell.getCellFormula());
                break;
            case BLANK: // 空值
                result = cell.getStringCellValue();
                break;
            case ERROR: // 故障
                result = cell.toString();
                break;
            default:
                result = cell.toString();
                break;
        }
    	return result;
    }
}
