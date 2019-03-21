package com.frame.util;

public class CommonUtil {
    /**
     * 获取当前程序运行的目录
     * @param type  0：测试用例目录，1：配置文件目录
     * @return
     */
    public static String getCurrentTestResourcePath(short type) {
        String resourcePath = ClassLoader.getSystemResource("").getPath();
        switch (type){
            case 0:
                resourcePath = resourcePath.replaceAll("target/test-classes","src/test/resources/testcase");
                break;
            case 1:
                resourcePath = resourcePath.replaceAll("target/test-classes","src/test/resources");
                break;
        }
        return resourcePath;
    }
}
