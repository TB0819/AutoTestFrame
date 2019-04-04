package com.frame.util;

import com.frame.annotations.ReadyData;
import com.frame.annotations.TestReadyData;
import com.frame.config.AbstractTestBase;
import com.frame.config.Constants;
import com.frame.server.DBServer;
import com.frame.server.imp.DBServerImp;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class CommonUtil {
    /**
     * 获取当前程序运行的目录
     * @param type  0：测试用例目录，1：配置文件目录
     * @return
     */
    public static String getCurrentTestResourcePath(short type) throws Exception {
        String resourcePath = ClassLoader.getSystemResource("").getPath();
        switch (type){
            case 0:
                resourcePath = resourcePath.replaceAll("target/test-classes","src/test/resources/testcase");
                break;
            case 1:
                resourcePath = resourcePath.replaceAll("target/test-classes","src/test/resources");
                break;
            default:
                throw new Exception("目录类型错误!");
        }
        return resourcePath;
    }

    /**
     * 操作准备数据
     * @param annotationType    注解类型, 0：测试类；1：测试方法
     * @param optType           操作类型, 0：新增数据；1：删除数据
     * @param clazz             测试类
     * @param method            测试方法
     */
    public static void executeReadyTestDbData(short annotationType, short optType ,Class clazz, Method method) throws Exception {
        TestReadyData testReadyData = null;
        ReadyData[] readyDataArray = null;
        // 获取注解数据
        if (Constants.CLASS_ANNOTATION == annotationType){
            if (clazz.isAnnotationPresent(TestReadyData.class)){
                testReadyData = (TestReadyData) clazz.getAnnotation(TestReadyData.class);
                readyDataArray = testReadyData.datas();
            }
        } else if (Constants.METHOD_ANNOTATION == annotationType){
            if (method.isAnnotationPresent(TestReadyData.class)){
                testReadyData = method.getAnnotation(TestReadyData.class);
                readyDataArray = testReadyData.datas();
            }
        } else {
            throw new Exception("注解类型错误!");
        }

        if (testReadyData == null) {
            return;
        }
        DBServer dbServer = new DBServerImp();
        Map<String, List<Map<String,Object>>> currentTableData = null;
        //操作测试准备数据
        String dbKey, tableName, filePath;
        for (ReadyData readyData: readyDataArray){
            dbKey = readyData.dbName();
            tableName = readyData.tableName();
            filePath = readyData.path();
            if (Constants.INSERT_DATA == optType) {
                dbServer.insertFromFile(dbKey,filePath,tableName);
                currentTableData = ((DBServerImp) dbServer).getCurrentTableDataList();
                AbstractTestBase.addTestReadyDbData(dbKey,currentTableData,clazz.getCanonicalName(),annotationType);
            } else if (Constants.DEL_DATA == optType) {
                dbServer.deleteFromFile(dbKey,filePath,tableName,null);
                if (Constants.CLASS_ANNOTATION == annotationType){
                    AbstractTestBase.clearTestReadyDbData(clazz.getCanonicalName(),null);
                } else {
                    AbstractTestBase.clearTestReadyDbData(clazz.getCanonicalName(), testReadyData);
                }
            } else {
                throw new Exception("DB操作类型错误!");
            }
        }
    }
}
