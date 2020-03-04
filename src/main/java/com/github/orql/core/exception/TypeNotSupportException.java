package com.github.orql.core.exception;

import java.lang.reflect.Field;

public class TypeNotSupportException extends RuntimeException {

    public TypeNotSupportException(Field field) {
        super(field.getDeclaringClass() + " not support type " + field.getType().getName());
    }

}
