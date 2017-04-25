package com.github.lahahana.csv.base;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.convertor.Converter;

import java.lang.reflect.Field;

public class CsvMetaInfo implements Comparable<CsvMetaInfo>{

    private String header;
    
    private Field field;
    
    private int order;
    
    private Converter<?, ?> converter;

    private String defaultValue;
    
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
    
    public Converter<?, ?> getConverter() {
        return converter;
    }

    public void setConverter(Converter<?, ?> converter) {
        this.converter = converter;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int compareTo(CsvMetaInfo o) {
        return order -  o.getOrder();
    }
    
}
