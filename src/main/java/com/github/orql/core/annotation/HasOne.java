package com.github.orql.core.annotation;

import com.github.orql.core.Cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOne {

    String refKey() default "";

    boolean required() default true;

    Cascade onDelete() default Cascade.Restrict;

    Cascade onUpdate() default Cascade.Restrict;
}
