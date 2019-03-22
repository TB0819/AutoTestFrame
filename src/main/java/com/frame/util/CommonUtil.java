package com.frame.util;

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
}
