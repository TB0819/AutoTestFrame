package com.frame.config;

import com.frame.annotations.ReadyData;
import com.frame.annotations.TestReadyData;
import com.frame.annotations.TestContextConfiguration;
import com.frame.casedatafactory.AutoTestData;
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
import org.testng.annotations.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

@ContextConfiguration(locations = { "classpath:spring.xml" })
@Listeners({ITestListenerHandler.class})
public abstract class AbstractTestBase extends AbstractTestNGSpringContextTests {
    private static final Logger logger = Logger.getLogger(AbstractTestBase.class);

    public static Map<String, String> baseConfig = new HashMap<String, String>();
    public static Map<String,Map<String,Map<String, List<Map<String,Object>>>>> DbReadyData = new HashMap<>();
    protected DBServer dbServer = new DBServerImp();
    protected HttpService httpService = new HttpServiceImp();


    /**
     * 启动测试套件之前初始化配置文件
     * @throws Exception
     */
    @BeforeSuite(alwaysRun = true)
    protected void beforeSuiteTestContext() throws Exception {
        initConfiguration(getClass());
    }

    @DataProvider(name = "autoDataProvider")
    public Iterator<Object[]> data(Method method) throws Exception {
        return AutoTestData.getTestCaseData(method);
    }

    @BeforeClass(alwaysRun = true)
    protected synchronized void beforeTestClass() throws Exception {
        CommonUtil.executeReadyTestDbData(Constants.CLASS_ANNOTATION,Constants.INSERT_DATA,getClass(),null);
    }

    @AfterClass(alwaysRun = true)
    protected synchronized void afterTestClass() throws Exception {
        CommonUtil.executeReadyTestDbData(Constants.CLASS_ANNOTATION, Constants.DEL_DATA, getClass(), null);
    }

    /**
     * 获取当前线程下的测试准备DB数据
     * @return
     */
    public synchronized static Map<String,Map<String, List<Map<String,Object>>>> getCurrentTestReadyDBData(Class testClass){
        return DbReadyData.get(testClass.getCanonicalName());
    }

    public synchronized static void clearTestReadyDbData(String testClassName, TestReadyData testReadyData){
        if (testReadyData == null ){
            DbReadyData.get(testClassName).clear();
        } else{
            ReadyData[] readyData = testReadyData.datas();
            for (ReadyData dbData: readyData){
                String dbKey = dbData.dbName();
                String tableName = dbData.tableName();
                DbReadyData.get(testClassName).get(dbKey).get(tableName).clear();
            }
        }
    }

    public synchronized static void addTestReadyDbData(String dbKey,Map<String, List<Map<String,Object>>> tableData, String testClassName, short anntType){
        //  测试方法注解数据，相同数据库时则putAll表数据，否则put数据库
        if (!DbReadyData.isEmpty() && DbReadyData.get(testClassName) != null){
            if(DbReadyData.get(testClassName).containsKey(dbKey)){
                Iterator iterator = tableData.keySet().iterator();
                while (iterator.hasNext()) {
                    String tableName = iterator.next().toString();
                    if (DbReadyData.get(testClassName).get(dbKey).containsKey(tableName)) {
                        DbReadyData.get(testClassName).get(dbKey).get(tableName).addAll(tableData.get(tableName));
                    } else {
                        DbReadyData.get(testClassName).get(dbKey).put(tableName,tableData.get(tableName));
                    }
                }
            }else {
                DbReadyData.get(testClassName).put(dbKey,tableData);
            }
        }else {
            Map<String,Map<String, List<Map<String,Object>>>> currentTestReadyDBData = new HashMap<String,Map<String, List<Map<String,Object>>>>();
            currentTestReadyDBData.put(dbKey,tableData);
            DbReadyData.put(testClassName,currentTestReadyDBData);
        }
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
                    throw new Exception(Constants.ExceptionMessage.CONFIG_FILE_TYPE_ERROR + path);
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
            throw new Exception(String.format(Constants.ExceptionMessage.CONFIG_FILE_EXIST_ERROR, file.getAbsolutePath()));
        }
        return file;
    }
}
