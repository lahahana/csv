package com.github.lahahana.csv.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.lahahana.csv.convertor.Convertor;
import com.github.lahahana.csv.convertor.DeConvertor;
import com.github.lahahana.csv.convertor.DefaultConvertor;
import com.github.lahahana.csv.convertor.DefaultDeConvertor;

/**
 * This Annotation is used to do customization when serialize property.
 * <p>Customization:
 *                <p>custom CSV header, use {@link #header()}
 *                <p>custom serialization order, use {@link #order()}
 *                <p>custom property convert, use {@link #converter()}
 * 
 * @author Lahahana
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CsvProperty {
    
    String DEFAULT_HEADER = "";

    int DEFAULT_ORDER = Integer.MAX_VALUE;

    String DEFAULT_VALUE = "";

    String DEFAULT_PREFIX = "";
    

    /**
     * The CSV header of this properties, 
     * otherwise, {@link com.github.lahahana.csv.serialize.CsvSerializer} will use {@link java.lang.reflect.Field} name as default header
     * */
    String header() default DEFAULT_HEADER;
    
    /**
     * The serialization order of  property, {@link #order()} more smaller, the priority more higher
     * */
    int order() default DEFAULT_ORDER;
    
    
    /**
     * The property converter class when serialization, you should implements {@link Convertor} if need customization convert
     * */
    Class<? extends Convertor<?, String>> converter() default DefaultConvertor.class;
    
    /**
     * The property converter class when deserialization , you should implements {@link Convertor} if need customization convert
     * */
    Class<? extends DeConvertor<?>> deConvertor() default DefaultDeConvertor.class;

    /**
     * The default property value if property's value is null
     * */
    String defaultValue() default DEFAULT_VALUE;

    /**
     * Only have effect when property is not a primitive type, primitive wrapper or String
     * */
    String prefix() default DEFAULT_PREFIX;
    
}
