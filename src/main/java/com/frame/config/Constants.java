package com.frame.config;

public class Constants {
    public static final String LOAD_CONFIG_SUCC = "配置文件加载成功，文件路径：%s";

    /*  注解类型，0：测试类；1：测试方法 */
    public static final short CLASS_ANNOTATION = (short) 0;
    public static final short METHOD_ANNOTATION = (short) 1;

    /*  操作类型，0：新增数据；1：删除数据  */
    public static final short INSERT_DATA = (short) 0;
    public static final short DEL_DATA = (short) 1;

    public class ExceptionMessage {
        public static final String FILE_PATH_ERROR = "用例文件格式错误";
        public static final String ANNOTATIONS_ERROR = "不存在@CaseSource注解";
        public static final String CONFIG_FILE_ERROR = "@TestContextConfiguration未找到配置文件";
        public static final String CONFIG_FILE_TYPE_ERROR = "@TestContextConfiguration配置文件格式错误,文件路径： ";
        public static final String CONFIG_FILE_EXIST_ERROR = "配置文件不存在,文件路径： %s";

        public static final String SQL_NO_WHERE = "SQL 语句中缺少 where 语句或者 where 语句为空。 执行SQL： %s";
        public static final String SQL_FILE_ERROR = "文件路径为空, 或表名为空";
        public static final String SQL_ERROR = "执行 SQL 失败, SQL 语句: ";
    }
}
