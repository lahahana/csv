package com.github.lahahana.csv.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DefaultSerializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.convertor.SerializationConvertor;
import com.github.lahahana.csv.serialize.CsvSerializer;

/**
 * This Annotation is used to do customization when serialize property.
 * <p>Customization:
 *                <p>custom CSV header, use {@link #header()}
 *                <p>custom serialization order, use {@link #order()}
 *                <p>custom default value, use {@link #defaultValue()}
 *                <p>custom property serialization convert, use {@link #serializationConvertor()}
 *                <p>custom property serialization convert, use {@link #deserializationConvertor()}
 *                <p>custom prefix, use {@link #prefix()}
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
     * otherwise, {@link CsvSerializer} will use {@link java.lang.reflect.Field}'s name as default header
     * */
    String header() default DEFAULT_HEADER;
    
    /**
     * The serialization order of  property, {@link #order()} more smaller, the priority more higher
     * */
    int order() default DEFAULT_ORDER;
    
    
    /**
     * The property converter class when serialization, you should implements {@link SerializationConvertor} if need customization convert
     * */
    Class<? extends SerializationConvertor<?>> serializationConvertor() default DefaultSerializationConvertor.class;
    
    /**
     * The property converter class when deserialization , you should implements {@link DeserializationConvertor} if need customization convert
     * */
    Class<? extends DeserializationConvertor<?>> deserializationConvertor() default DefaultDeserializationConvertor.class;

    /**
     * The default property value if property's value is null
     * */
    String defaultValue() default DEFAULT_VALUE;

    /**
     * Only have effect when property is not a primitive type, primitive wrapper or String
     * */
    String prefix() default DEFAULT_PREFIX;
    
}
