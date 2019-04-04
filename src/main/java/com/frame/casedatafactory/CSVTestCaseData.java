package com.frame.casedatafactory;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 读取CSV文件用例
 */
public class CSVTestCaseData extends TestCaseData{
	private static final Logger logger = Logger.getLogger(CSVTestCaseData.class);
	private CSVReader csvReader;
	private static final String UTF_8 = "utf-8";
	
	/**
	 * 数据读取初始化<br/>
	 */
	public void initAction(String filePath) throws Exception{
		try {
			File csv = new File(filePath);
			InputStreamReader isr = new InputStreamReader(new FileInputStream(csv), UTF_8);
			csvReader = new CSVReader(isr);

			tableList = csvReader.readAll();
			columnName = (String[]) tableList.get(0);

			this.rowNum = tableList.size();
			this.columnNum = columnName.length;
			this.currentRowNo++;
		}finally {
			try {
				csvReader.close();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
