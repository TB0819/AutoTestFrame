package com.frame.config;

import com.frame.annotations.TestContextConfiguration;
import com.frame.datafactory.AutoTestData;
import com.frame.listeners.ITestListenerHandler;
import com.frame.server.DBServer;
import com.frame.server.HttpService;
import com.frame.server.imp.DBServerImp;
import com.frame.server.imp.HttpServiceImp;
import com.frame.util.CommonUtil;
import com.frame.util.DBPoolConnection;
import org.apache.log4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

//AbstractTransactionalTestNGSpringContextTests 自动回滚
@ContextConfiguration(locations = { "classpath:spring.xml" })
@Listeners({ITestListenerHandler.class})
public abstract class AbstractTestBase extends AbstractTestNGSpringContextTests {
    private static final Logger logger = Logger.getLogger(AbstractTestBase.class);

    public static Map<String, String> baseConfig = new HashMap<String, String>();
    public static Map<String,Map<String, List<Map<String,Object>>>> currentTestReadyDBData = new HashMap<String,Map<String, List<Map<String,Object>>>>();
    protected DBServer dbServer = new DBServerImp();
    protected HttpService httpService = new HttpServiceImp();

    @BeforeSuite(alwaysRun = true)
    protected void beforeSuiteTestContext() throws Exception {
        initConfiguration(getClass());
    }

    @DataProvider(name = "autoDataProvider")
    public Iterator<Object[]> data(Method method) throws Exception {
        return AutoTestData.getTestCaseData(method);
    }

    /**
     * 加载配置文件,默认当前工程resources目录。并初始化数据库连接
     * @param clazz 当前运行的测试类
     */
    private void initConfiguration(Class clazz) throws Exception {
        if (clazz.isAnnotationPresent(TestContextConfiguration.class)){
            TestContextConfiguration testContextConfiguration = (TestContextConfiguration) clazz.getAnnotation(TestContextConfiguration.class);
            String[] paths = testContextConfiguration.locations();
            for (String path: paths) {
                if (!path.endsWith(".properties")){
                    throw new Exception(Constants.Exception.CONFIG_FILE_TYPE_ERROR + path);
                }
                File configFile;
                if (path.startsWith("classpath:")) {
                    // 默认从resources跟目录读取配置文件
                    String fileName = path.replace("classpath:","");
                    configFile = getCurrentTestProjectResources(fileName);
                }else {
                    // 从指定目录读取配置文件
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    configFile = getCurrentTestProjectResources(path);
                }
                Map<String, String> map = getConfigMap(configFile);
                baseConfig.putAll(map);
                logger.info(String.format(Constants.LOAD_CONFIG_SUCC,configFile.getAbsolutePath()));
            }
        }
        // 创建数据库连接
        if (!baseConfig.isEmpty()){
            DBPoolConnection.getInstance().createDruidPooledConnection(baseConfig);
        }
    }

    /**
     * 读取配置文件内容信息
     * @param file  配置文件
     * @return
     * @throws Exception
     */
    private Map<String, String> getConfigMap(File file) throws Exception {
        Properties properties = new Properties();
        InputStream is = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(is,"UTF-8");
        try {
            properties.load(inputStreamReader);
        }finally {
            is.close();
            inputStreamReader.close();
        }
        Map<String, String> map = new HashMap<String, String>();
        for(Map.Entry<Object, Object> element : properties.entrySet()){
            map.put((String)element.getKey(), (String)element.getValue());
        }
        return map;
    }

    private File getCurrentTestProjectResources(String fileName) throws Exception {
        String filePath = CommonUtil.getCurrentTestResourcePath((short) 1) + fileName;
        File file = new File(filePath.substring(1));
        if (!file.exists()){
            throw new Exception(String.format(Constants.Exception.CONFIG_FILE_EXIST_ERROR, file.getAbsolutePath()));
        }
        return file;
    }
}
