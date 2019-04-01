package com.frame.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

/**
 * 测试用例集合
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface CaseSource {
    String name() default "";

    String type() default "csv";

    String path() default "";

    int count() default -1;
}
