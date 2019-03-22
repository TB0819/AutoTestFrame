package com.frame.server.imp;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.frame.config.Constants;
import com.frame.server.DBServer;
import com.frame.util.DBPoolConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.*;

/**
 * 数据库操作类
 */
public class DBServerImp implements DBServer {
    private static final Logger logger = Logger.getLogger(DBServerImp.class);
    private DBPoolConnection dbPoolConnection = DBPoolConnection.getInstance();
    private Map<String,List<Map<String,Object>>> currentTableDataList;

    /**
     * 查询记录
     * @param dbKey 数据库key
     * @param sql   sql语句
     * @return  返回查询结果，没有记录则返回空List
     */
    @Override
    public List<Map<String, Object>> querySql(String dbKey, String sql) throws Exception {
        DruidPooledConnection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dbPoolConnection.getConnection(dbKey);
            ps = connection.prepareStatement(sql);
            ResultSet result = ps.executeQuery();
            logger.info("执行SQL：" + sql);
            return convertResultSet(result);
        }catch (SQLException e){
            logger.error("unexpect SQLException:", e);
        }finally {
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 新增记录
     * @param dbKey 数据库key
     * @param sql   sql语句
     * @return
     */
    @Override
    public int insertSql(String dbKey, String sql) throws Exception {
        return executeSql(dbKey, sql, false);
    }

    /**
     * 删除记录
     * @param dbKey 数据库key
     * @param sql   sql语句
     * @return
     */
    @Override
    public int deleteSql(String dbKey, String sql) throws Exception {
        return executeSql(dbKey, sql, true);
    }

    /**
     * 更新记录
     * @param dbKey 数据库key
     * @param sql   sql语句
     * @return
     */
    @Override
    public int updateSql(String dbKey, String sql) throws Exception {
        return executeSql(dbKey, sql, true);
    }

    /**
     * 根据文件插入数据
     * @param dbKey         数据库key
     * @param csvPath       csv文件路径
     * @param tableName     表名
     * @return
     * @throws Exception
     */
    @Override
    public boolean insertFromFile(String dbKey, String csvPath, String tableName) throws Exception {
        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(tableName)) {
            logger.error(Constants.ExceptionMessage.SQL_FILE_ERROR);
            return false;
        }
        List<String> sqlList = getInsertSqlList(dbKey,csvPath,tableName);

        for (String insertSQL : sqlList) {
            Integer result = insertSql(dbKey,insertSQL);
            if(result < 0){
                throw new Exception(Constants.ExceptionMessage.SQL_ERROR + insertSQL);
            }
        }
        return true;
    }

    /**
     * 根据文件删除数据
     * @param dbKey         数据库key
     * @param csvPath       csv文件路径
     * @param tableName     表名
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteFromFile(String dbKey, String csvPath, String tableName,List<String> excludeColumn) throws Exception {
        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(tableName)) {
            logger.error(Constants.ExceptionMessage.SQL_FILE_ERROR);
            return false;
        }
        List<Map<String,Object>> testSqlList = new TestSqlDataImp().getTestSqlDataFromCsv(csvPath);
        for(Map<String,Object> testSqlMap: testSqlList){
            StringBuffer sb = new StringBuffer();
            sb.append("delete from " + tableName + " where ");
            for (Map.Entry<String, Object> entry: testSqlMap.entrySet()){
                String field = entry.getKey();
                if (excludeColumn != null && excludeColumn.contains(field)){
                    continue;
                } else if (entry.getValue().toString().indexOf("()") > 0){
                    continue;
                }
                sb.append(field + "='" + entry.getValue() +"' and ");
            }
            sb.delete(sb.lastIndexOf("and"),sb.length());
            deleteSql(dbKey,sb.toString());
        }
        return true;
    }

    /**
     * 获取数据库表字段名称和默认值
     * @param dbKey         数据库key
     * @param tableName     表名
     * @return
     * @throws Exception
     */
    @Override
    public List<String[]> getFieldMetaData(String dbKey, String tableName) throws Exception {
        DruidPooledConnection connection = null;
        try {
            List<String[]> list = new ArrayList<String[]>();
            connection = dbPoolConnection.getConnection(dbKey);
            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet rsColimns = dbMetaData.getColumns(null, "%", tableName, "%");
            while (rsColimns.next()) {
                String fieldName = rsColimns.getString("COLUMN_NAME");
                String fieldDef = rsColimns.getString("COLUMN_DEF");
                fieldDef = fieldDef==null ? "null":fieldDef;
                String[] strArray = {fieldName,fieldDef};
                list.add(strArray);
            }
            return list;
        }catch (SQLException e){
            logger.error("unexpect SQLException:", e);
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }

    /**
     * 获取数据库表字段名称和默认值
     * @param dbKey             数据库key
     * @param tableName         表名
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getFieldDefaultMetaData(String dbKey, String tableName) throws Exception {
        DruidPooledConnection connection = null;
        try {
            Map<String,Object> defaultData = new HashMap<>();
            connection = dbPoolConnection.getConnection(dbKey);
            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet rsColimns = dbMetaData.getColumns(null, "%", tableName, "%");
            while (rsColimns.next()) {
                String fieldName = rsColimns.getString("COLUMN_NAME");
                String fieldDefault = rsColimns.getString("COLUMN_DEF");
                defaultData.put(fieldName,fieldDefault);
            }
            return defaultData;
        }catch (SQLException e){
            logger.error("unexpect SQLException:", e);
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }

    /**
     * delete 语句以及 update 语句需要检查 where 字段, 避免对全表进行操作<br/>
     * @return
     */
    private boolean checkSQL(String sqlStr) throws Exception {
        String[] strArray = sqlStr.toLowerCase().split("where");

        if(strArray.length < 2 || strArray[1].trim().isEmpty()){
            logger.error(String.format(Constants.ExceptionMessage.SQL_NO_WHERE,sqlStr));
            throw new Exception(String.format(Constants.ExceptionMessage.SQL_NO_WHERE,sqlStr));
        }
        return  true;
    }

    /**
     * 获取插入的sql语句
     * @param dbKey         数据库key
     * @param csvPath       文件路径
     * @param tableName     表名
     * @return
     * @throws Exception
     */
    public List<String> getInsertSqlList(String dbKey, String csvPath, String tableName) throws Exception {
        List<String> insertSqlList = new ArrayList<String>();
        List<String[]> fieldMetaData = getFieldMetaData(dbKey, tableName);
        List<Map<String,Object>> testSqlList = new TestSqlDataImp().getTestSqlDataFromCsv(csvPath);
        testSqlList.forEach(testSqlMap ->{
            String sql = getInsertSql(fieldMetaData,testSqlMap,tableName);
            insertSqlList.add(sql);
        });
        currentTableDataList = new HashMap<>();
        currentTableDataList.put(tableName,testSqlList);
        return insertSqlList;
    }

    private String getInsertSql(List<String[]> fieldMetaData,Map<String,Object> sqlMap, String tableName){
        StringBuffer sqlSB = new StringBuffer();
        StringBuffer nameSB = new StringBuffer();
        StringBuffer valueSB = new StringBuffer();
        for (String[] fieldMeta: fieldMetaData){
            String field = fieldMeta[0];
            nameSB.append(field + ", ");
            if (sqlMap.containsKey(field)){
                String temp = sqlMap.get(field).toString().trim();
                if (temp.trim().isEmpty()){
                    temp = "'" + temp + "'";
                }else if (!temp.contains("()")) {
                    if(!temp.startsWith("'"))
                        temp = "'" + temp;
                    if(!temp.endsWith("'"))
                        temp = temp + "'";
                }
                valueSB.append(temp + ", ");    //支持函数
            }else {
                //  默认值为null时设置空
                if ("null".equalsIgnoreCase(fieldMeta[1])){
                    valueSB.append("'', ");
                }else {
                    valueSB.append("'" + fieldMeta[1] + "', ");
                }
            }
        }
        nameSB.delete(nameSB.lastIndexOf(","),nameSB.length());
        valueSB.delete(valueSB.lastIndexOf(","),valueSB.length());
        sqlSB.append("INSERT  INTO " + tableName + " (");
        sqlSB.append(nameSB);
        sqlSB.append(") VALUES (");
        sqlSB.append(valueSB);
        sqlSB.append(")");
        return sqlSB.toString();
    }

    /**
     * sql查询结果转换List
     * @param resultSet     sql查询结果
     * @return
     * @throws Exception
     */
    private List<Map<String, Object>> convertResultSet(ResultSet resultSet) throws Exception{
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (resultSet != null && resultSet.last()) {
            resultSet.beforeFirst();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    map.put(resultSetMetaData.getColumnLabel(i), resultSet.getString(i));
                }
                list.add(map);
            }
            resultSet.beforeFirst();
            resultSet.close();
        }
        return list;
    }

    /**
     * 执行sql
     * @param dbKey     数据库key
     * @param sql       sql语句
     * @param checkSql  是否check sql语句
     * @return
     * @throws Exception
     */
    private int executeSql(String dbKey, String sql, boolean checkSql) throws Exception {
        if (checkSql) {
            checkSQL(sql);
        }
        DruidPooledConnection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dbPoolConnection.getConnection(dbKey);
            ps = connection.prepareStatement(sql);
            logger.info("执行SQL：" + sql);
            Integer result = ps.executeUpdate();
            return result;
        }catch (SQLException e){
            logger.error("unexpect SQLException:", e);
        }finally {
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return 0;
    }

    /**
     * 获取当前插入表的数据
     * @return
     */
    public Map<String,List<Map<String,Object>>> getCurrentTableDataList(){
        return this.currentTableDataList;
    }
}
