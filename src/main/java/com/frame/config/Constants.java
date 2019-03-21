package com.frame.config;

public class Constants {
    public static final String LOAD_CONFIG_SUCC = "配置文件加载成功，文件路径：%s";

    public class Exception {
        public static final String FILE_PATH_ERROR = "用例文件格式错误";
        public static final String ANNOTATIONS_ERROR = "不存在@CaseSource注解";
        public static final String CONFIG_FILE_ERROR = "@TestContextConfiguration未找到配置文件";
        public static final String CONFIG_FILE_TYPE_ERROR = "@TestContextConfiguration配置文件格式错误,文件路径： ";
        public static final String CONFIG_FILE_EXIST_ERROR = "配置文件不存在,文件路径： %s";

        public static final String SQL_NO_WHERE = "SQL 语句中缺少 where 语句或者 where 语句为空\n 执行SQL： %s";
        public static final String SQL_FILE_ERROR = "文件路径为空, 或表名为空";
        public static final String SQL_ERROR = "执行 SQL 失败, SQL 语句: ";
    }
}
