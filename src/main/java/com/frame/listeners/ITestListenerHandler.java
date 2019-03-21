package com.frame.listeners;

import com.frame.annotations.CaseSource;
import com.frame.annotations.ReadyData;
import com.frame.annotations.ReadyTestData;
import com.frame.config.AbstractTestBase;
import com.frame.server.DBServer;
import com.frame.server.imp.DBServerImp;
import org.apache.commons.lang3.StringUtils;
import org.testng.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ITestListenerHandler implements ITestListener, IInvokedMethodListener {
    private String currentMethodPath = "";

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult testResult) {

    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        Method method = iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        boolean flag = false;
        try {
            if (method.isAnnotationPresent(CaseSource.class)) {
                CaseSource caseSource = method.getAnnotation(CaseSource.class);
                int caseCount = caseSource.count();
                int currentCount = iTestResult.getMethod().getCurrentInvocationCount();
                if (currentCount == caseCount || caseCount == -1){
                    AbstractTestBase.currentTestReadyDBData.clear();
                    flag = true;
                    deleteDbData(method);
                }
            }
        }catch (Exception e){
            Assert.fail("准备数据清理失败",e);
        }finally {
            if (flag && !AbstractTestBase.currentTestReadyDBData.isEmpty()){
                AbstractTestBase.currentTestReadyDBData.clear();
            }
        }
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        Method method = iTestResult.getMethod().getConstructorOrMethod().getMethod();
        String methodPath = iTestResult.getMethod().getQualifiedName();
        try {
            //  DataProvider模式只执行一次
            if (currentMethodPath.equals(methodPath)){
                return;
            }
            currentMethodPath = methodPath;
            insertDbData(method);
        } catch (Exception e) {
            Assert.fail("数据准备初始化失败",e);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {

    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {

    }

    /**
     * 测试执行前插入准备的DB数据
     * @param method
     */
    private void insertDbData(Method method) throws Exception {
        if (method.isAnnotationPresent(ReadyTestData.class)){
            ReadyData[] readyData = method.getAnnotation(ReadyTestData.class).datas();
            String temp = "";
            for (ReadyData dbData: readyData){
                String dbKey = dbData.dbName();
                String tableName = dbData.tableName();
                String filePath = dbData.path();
                checkDbData(method, filePath);
                DBServer dbServer = new DBServerImp();
                dbServer.insertFromFile(dbKey,filePath,tableName);
                Map<String, List<Map<String,Object>>> currentTableData = ((DBServerImp) dbServer).getCurrentTableDataList();
                //  相同数据库时则putAll表数据，否则put数据库
                if (temp.equals(dbKey)) {
                    AbstractTestBase.currentTestReadyDBData.get(dbKey).putAll(currentTableData);
                } else {
                    AbstractTestBase.currentTestReadyDBData.put(dbKey,currentTableData);
                    temp = dbKey;
                }
            }
        }
    }

    /**
     * 测试执行后清理准备的DB数据
     * @param method
     */
    private void deleteDbData(Method method) throws Exception {
        if (method.isAnnotationPresent(ReadyTestData.class)){
            ReadyData[] readyData = method.getAnnotation(ReadyTestData.class).datas();
            for (ReadyData dbData: readyData){
                String dbKey = dbData.dbName();
                String tableName = dbData.tableName();
                String filePath = dbData.path();
                DBServer dbServer = new DBServerImp();
                dbServer.deleteFromFile(dbKey,filePath,tableName,null);
            }
        }
    }

    private void checkDbData(Method method, String filePath){
        if (StringUtils.isBlank(filePath)){
            Assert.fail(method.getName() + " -测试准备数据文件路径为空!");
        }
    }
}
