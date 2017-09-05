package com.github.lahahana.csv.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to suppress 
 * serialization/deserialization of specific properties.
 *
 * @author Lahahana
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvIgnoreProperties {

    /**
     * Names of properties to ignore.
     */
    String[] value() default {};

}
