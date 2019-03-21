package com.frame.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DBPoolConnection {
    private static DBPoolConnection dbPoolConnection = null;
    private static Map<String,DruidDataSource> druidDataSourceMap = new LinkedHashMap<String,DruidDataSource>();

    /**
     * 数据库连接池单例
     * @return
     */
    public static synchronized DBPoolConnection getInstance(){
        if (null == dbPoolConnection){
            dbPoolConnection = new DBPoolConnection();
        }
        return dbPoolConnection;
    }

    /**
     * 返回druid数据库连接
     * @param dbKey     数据库键值
     * @return
     * @throws SQLException
     */
    public DruidPooledConnection getConnection(String dbKey) throws SQLException {
        return druidDataSourceMap.get(dbKey).getConnection();
     }

    /**
     * 创建数据库连接
     * @param configMap 配置信息
     */
     public void createDruidPooledConnection(Map<String, String> configMap){
        for (Map.Entry entry: configMap.entrySet()) {
            String value = entry.getValue().toString().toLowerCase();
            /* mysql连接 */
            if (value.startsWith("jdbc:mysql:")){
                createMysqlConnection(String.valueOf(entry.getKey()), value);
            }
            /* Oracle连接 */
        }
     }

     private void createMysqlConnection(String key, String value){
         DruidDataSource dataSource = new DruidDataSource();
         String[] dbInfo = value.split(",");
         //设置连接参数
         dataSource.setUrl(dbInfo[0].trim());
         dataSource.setDriverClassName("com.mysql.jdbc.Driver");
         dataSource.setUsername(dbInfo[1].trim());
         dataSource.setPassword(dbInfo[2].trim());
         //配置初始化大小、最小、最大
         dataSource.setInitialSize(5);
         dataSource.setMinIdle(1);
         dataSource.setMaxActive(20);
         //连接泄漏监测
         dataSource.setRemoveAbandoned(true);
         dataSource.setRemoveAbandonedTimeout(30);
         //配置获取连接等待超时的时间
         dataSource.setMaxWait(20000);
         //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
         dataSource.setTimeBetweenEvictionRunsMillis(20000);
         //防止过期
         dataSource.setValidationQuery("SELECT 'x'");
         dataSource.setTestWhileIdle(true);
         dataSource.setTestOnBorrow(true);
         druidDataSourceMap.put(key,dataSource);
     }
}
