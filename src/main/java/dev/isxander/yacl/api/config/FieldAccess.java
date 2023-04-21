package dev.isxander.yacl.api.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface FieldAccess {
    Object get();

    void set(Object value);

    Type type();

    ParameterizedType genericType();

    String name();
}
