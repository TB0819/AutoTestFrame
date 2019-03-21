package com.frame.annotations;

import java.lang.annotation.*;

/**
 * 单个DB表的测试准备
 * description： 说明
 * dbName：      数据库标识
 * tableName     表名
 * path          DB文件路径
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ReadyData {
    String description() default "";

    String dbName() default "";

    String tableName() default "";

    String path() default "";
}
