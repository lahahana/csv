package com.github.lahahana.csv.annotations;

import com.github.lahahana.csv.convertor.Converter;
import com.github.lahahana.csv.convertor.DefaultConverter;

import java.lang.annotation.*;

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
    
    String DEFAULT_HEADER = "";

    int DEFAULT_ORDER = Integer.MAX_VALUE;

    String DEFAULT_VALUE = "";
    

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
     * The property converter class, you should implements {@link Converter} if need customization convert
     * */
    Class<? extends Converter<?, ?>> converter() default DefaultConverter.class;


    /**
     * The default property value if property's value is null
     * */
    String defaultValue() default DEFAULT_VALUE;
    
}
