package com.frame.annotations;

import java.lang.annotation.*;

/**
 * 加载DB多数据源、Http请求基础Url等配置文件
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestContextConfiguration {
    String[] locations() default {};
}
