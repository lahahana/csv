package com.alpha.csv.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to either suppress 
 * serialization/deserialization of properties.
 * 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvIgnoreProperties {

    /**
     * Names of properties to ignore.
     */
    String[] value() default {};
    
    /**
     * If true, it will throw exception when deserialize unknown properties;
     * If false, unknown properties will never be deserialized without any exceptions.
     * */
    public boolean ignoreUnknown() default false;
}
