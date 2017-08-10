package com.github.lahahana.csv.base;

import java.lang.reflect.Field;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.convertor.SerializationConvertor;

/**
 * {@link CsvMetaInfo} contains all the information of an {@link Field} when do csv serialization
 * 
 * @author Lahahana
 * */

public class CsvMetaInfo<T> implements Comparable<CsvMetaInfo<T>>{
	
	private static final String DEFAULT_PREFIX = CsvProperty.DEFAULT_PREFIX;
	
	private static final int DEFAULT_ORDER = CsvProperty.DEFAULT_ORDER;

    private String header;
    
    private Field field;
    
    private int order;
    
    private SerializationConvertor<T> converter;

    private String defaultValue;

    private String prefix;
    
    public CsvMetaInfo(Field field) {
        super();
        this.header = field.getName();
        this.field = field;
        this.order = DEFAULT_ORDER;
        this.prefix = DEFAULT_PREFIX;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    
    public SerializationConvertor<T> getConverter() {
        return converter;
    }

    public void setConverter(SerializationConvertor<T> converter) {
        this.converter = converter;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int compareTo(CsvMetaInfo<T> o) {
        return order -  o.getOrder();
    }
    
}
