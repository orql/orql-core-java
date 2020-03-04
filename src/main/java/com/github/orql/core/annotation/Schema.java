package com.github.orql.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schema {

    /**
     * schema name
     * @return
     */
    String value() default "";

    String name() default "";

    /**
     * schema table
     * @return
     */
    String table() default "";

}
