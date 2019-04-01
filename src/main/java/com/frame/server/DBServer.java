package com.frame.server;

import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DBServer {
    public List<Map<String,Object>> querySql(String dbKey, String sql) throws Exception;

    public int insertSql(String dbKey, String sql) throws Exception;

    public int deleteSql(String dbKey, String sql) throws Exception;

    public int updateSql(String dbKey, String sql) throws Exception;

    public boolean insertFromFile(String dbKey, String csvPath, String tableName) throws Exception;

    public boolean deleteFromFile(String dbKey, String csvPath, String tableName,List<String> excludeColumn) throws Exception;

    public List<String[]>  getFieldMetaData(String dbKey, String tableName) throws Exception;

    public Map<String,Object>  getFieldDefaultMetaData(String dbKey, String tableName) throws Exception;

    public DruidPooledConnection getDruidConnection(String dbKey) throws SQLException;

}
