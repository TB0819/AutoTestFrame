package com.frame.server.imp;

import au.com.bytecode.opencsv.CSVReader;
import com.frame.server.TestSqlData;
import com.frame.util.CommonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DB文件转sql语句类
 */
public class TestSqlDataImp implements TestSqlData {
    /**
     * 获取DB文件(CSV)的值,文件默认在工程的resources目录下
     * @param filePath      csv文件路径
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String,Object>>  getTestSqlDataFromCsv(String filePath) throws Exception {
        String path = CommonUtil.getCurrentTestResourcePath((short)1) + filePath;
        File csv = new File(path);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(csv), "utf-8");
        CSVReader csvReader = new CSVReader(isr);
        List<String[]> tableList = csvReader.readAll();
        List<Map<String,Object>> tableMaps = new ArrayList<Map<String,Object>>();
        for (String[] strings: tableList.subList(1,tableList.size())){
            Map<String,Object> map = new HashMap<String,Object>();
            for (int i=0; i<strings.length; i++) {
                map.put(tableList.get(0)[i],strings[i]);
            }
            tableMaps.add(map);
        }
        csvReader.close();
        return tableMaps;
    }
}
