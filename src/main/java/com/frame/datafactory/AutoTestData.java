package com.frame.datafactory;

import com.frame.annotations.CaseSource;
import com.frame.config.Constants;
import com.frame.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 获取自动化用例集合
 */
public class AutoTestData {

    /**
     * 获取测试用例数据
     * @param method    当前运行的测试方法
     * @return
     * @throws Exception
     */
    public static Iterator<Object[]> getTestCaseData(Method method) throws Exception{
        Map<String, Object> caseSourceMap = getCaseSourceMap(method);

        CaseSource caseSource =method.getAnnotation(CaseSource.class);
        String casePath = caseSourceMap.get("path").toString();

        /* 用例路径：
            1、未设置路径根据当初测试方法所在的包路径查找
            2、设置了路径根据设置的路径查找
        */
        if (StringUtils.isBlank(casePath)) {
            if (caseSourceMap.get("type").toString().equalsIgnoreCase("csv")) {
                casePath = CommonUtil.getCurrentTestResourcePath((short) 0) + caseSourceMap.get("name") + ".csv";
            }else if (caseSourceMap.get("type").toString().equalsIgnoreCase("excel")){
                casePath = CommonUtil.getCurrentTestResourcePath((short) 0) + caseSourceMap.get("name") + ".xls";
            }
        }else {
            casePath = CommonUtil.getCurrentTestResourcePath((short) 1) + casePath;
        }

        if (caseSourceMap.get("type").toString().equalsIgnoreCase("csv")){
            CSVTestCaseData csvTestData = new CSVTestCaseData();
            csvTestData.initAction(casePath);
            setCaseCount(caseSource,csvTestData);
            return csvTestData;
        }else if (caseSourceMap.get("type").toString().equalsIgnoreCase("excel")) {
            ExcelTestCaseData excelTestData = new ExcelTestCaseData();
            excelTestData.initAction(casePath);
            setCaseCount(caseSource,excelTestData);
            return excelTestData;
        }
        return null;
    }

    /**
     * 获取注解参数的默认值
     * @param method    当前运行的方法
     * @return
     * @throws Exception
     */
    public static Map<String, Object> getCaseSourceMap(Method method) throws Exception {
        if (method.isAnnotationPresent(CaseSource.class)) {
            CaseSource caseSource =method.getAnnotation(CaseSource.class);
            Map<String, Object> caseSourceMap = new HashMap<String, Object>();
            caseSourceMap.put("type",caseSource.type().trim().toLowerCase());
            String name = caseSource.name();
            String classFullName = method.getDeclaringClass().getName();
            String className = classFullName.split("\\.")[classFullName.split("\\.").length - 1];
            caseSourceMap.put("name", StringUtils.isBlank(name)? className: name);
            caseSourceMap.put("path",caseSource.path());
            caseSourceMap.put("count",caseSource.count());
            return caseSourceMap;
        }
        throw new Exception(Constants.ExceptionMessage.ANNOTATIONS_ERROR);
    }

    /**
     * 动态设置注解的用例数量
     * @param caseSource    用例注解类
     * @param testCaseData  用例对象类
     * @throws Exception
     */
    private static void setCaseCount(CaseSource caseSource, TestCaseData testCaseData) throws Exception {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(caseSource);
        Field field = invocationHandler.getClass().getDeclaredField("memberValues");
        field.setAccessible(true);
        Map<String, Object> memberValues = (Map<String, Object>) field.get(invocationHandler);
        memberValues.put("count", testCaseData.getCaseCount());
    }
}
