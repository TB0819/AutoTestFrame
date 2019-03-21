package com.frame.server;

import java.util.List;
import java.util.Map;

public interface TestSqlData {
    public List<Map<String,Object>>  getTestSqlDataFromCsv(String filePath) throws Exception;
}
