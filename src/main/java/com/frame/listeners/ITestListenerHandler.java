package com.frame.listeners;

import com.frame.annotations.CaseSource;
import com.frame.annotations.TestReadyData;
import com.frame.config.AbstractTestBase;
import com.frame.config.Constants;
import com.frame.util.CommonUtil;
import org.testng.*;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ITestListenerHandler implements ITestListener, IInvokedMethodListener {
    private Map<Long, String> currentMethodMap = new HashMap<>();

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult testResult) {

    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        Method method = iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        Class currentTestClass = iTestResult.getTestClass().getRealClass();
        boolean flag = false;
        Test testAnnotation = method.getAnnotation(Test.class);
        TestReadyData testReadyData = method.getAnnotation(TestReadyData.class);
        try {
            if (method.isAnnotationPresent(CaseSource.class)) {
                CaseSource caseSource = method.getAnnotation(CaseSource.class);
                int caseCount = caseSource.count();
                int currentCount = iTestResult.getMethod().getCurrentInvocationCount();
                int testInvocationCount = testAnnotation.invocationCount();
                if (currentCount == (caseCount * testInvocationCount) || caseCount == -1){
                    AbstractTestBase.clearTestReadyDbData(currentTestClass.getCanonicalName(), testReadyData);
                    flag = true;
                    CommonUtil.executeReadyTestDbData(Constants.METHOD_ANNOTATION,Constants.DEL_DATA,currentTestClass,method);
                }
            }else {
                if (method.isAnnotationPresent(TestReadyData.class)){
                    flag = true;
                    CommonUtil.executeReadyTestDbData(Constants.METHOD_ANNOTATION,Constants.DEL_DATA,currentTestClass,method);
                }
            }
        }catch (Exception e){
            Assert.fail("准备数据清理失败",e);
        }finally {
            if (flag && AbstractTestBase.DbReadyData.get(currentTestClass.getCanonicalName()) != null && !AbstractTestBase.DbReadyData.get(currentTestClass.getCanonicalName()).isEmpty()){
                AbstractTestBase.clearTestReadyDbData(currentTestClass.getCanonicalName(), testReadyData);
            }
        }
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        Class currentTestClass = iTestResult.getTestClass().getRealClass();
        Method currentTestMethod = iTestResult.getMethod().getConstructorOrMethod().getMethod();
        String currentTestMethodPath = iTestResult.getMethod().getQualifiedName();
        long threadId = Thread.currentThread().getId();
        try {
            //并发测试根据线程区分 DataProvider模式只执行一次
            if (currentTestMethodPath.equals(currentMethodMap.get(threadId))){
                return;
            }
            currentMethodMap.put(threadId,currentTestMethodPath);
            //测试方法上注解执行插入准备数据
            if (currentTestMethod != null) {
                CommonUtil.executeReadyTestDbData(Constants.METHOD_ANNOTATION, Constants.INSERT_DATA, currentTestClass, currentTestMethod);
            }
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
}
