package com.alpha.csv.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alpha.csv.convertor.Converter;
import com.alpha.csv.convertor.DefaultConverter;

/**
 * This Annotation is used to do customization when serialize property.
 * <p>Customization:
 *                <p>custom CSV header, use {@link #header()}
 *                <p>custom serialization order, use {@link #order()}
 *                <p>custom property convert, use {@link #converter()}
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CsvProperty {
    
    static final String DEFAULT_HEADER = "";
    
    static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    static final String DEFAULT_VALUE = "";
    

    /**
     * The CSV header of this properties, 
     * otherwise, {@link CSVSerializer} will use {@link Field} name as default header 
     * */
    String header() default DEFAULT_HEADER;
    
    /**
     * The serialization order of  property, {@link #order()} more smaller, the priority more higher
     * */
    int order() default DEFAULT_ORDER;
    
    
    /**
     * The property converter class, you should implements {@link Converter} if need customization convert
     * */
    Class<? extends Converter<?, ?>> converter() default DefaultConverter.class;
    
    
    String defaultValue() default DEFAULT_VALUE;
    
}
