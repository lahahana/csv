package com.github.lahahana.csv.base;

import java.lang.reflect.Field;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.convertor.SerializationConvertor;

/**
 * {@link CsvMetaInfo} contains all the information of an {@link Field} when do csv serialization
 * 
 * @author Lahahana
 * */

public class CsvMetaInfo implements Comparable<CsvMetaInfo>{

    private String header;
    
    private Field field;
    
    private int order;
    
    private SerializationConvertor<?> converter;

    private String defaultValue;

    private String prefix;
    
    public CsvMetaInfo(Field field) {
        super();
        this.header = field.getName();
        this.field = field;
        this.order = CsvProperty.DEFAULT_ORDER;
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
    
    public SerializationConvertor<?> getConverter() {
        return converter;
    }

    public void setConverter(SerializationConvertor<?> converter) {
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

    public int compareTo(CsvMetaInfo o) {
        return order -  o.getOrder();
    }
    
}
