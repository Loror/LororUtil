package com.loror.lororUtil.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";

    String defaultValue() default "";

    Class<? extends Encryption> encryption() default Encryption.class;

    boolean notNull() default false;
}