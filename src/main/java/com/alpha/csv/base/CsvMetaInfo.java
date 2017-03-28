package com.alpha.csv.base;

import java.lang.reflect.Field;

import com.alpha.csv.annotations.CsvProperty;
import com.alpha.csv.convertor.Converter;

public class CsvMetaInfo implements Comparable<CsvMetaInfo>{

    private String header;
    
    private Field field;
    
    private int order;
    
    private Converter<?, ?> converter;
    
    public CsvMetaInfo(Field field) {
        super();
        this.header = field.getName();
        this.field = field;
        this.order = CsvProperty.DEFAULT_ORDER;
    }

    public CsvMetaInfo(String value, Field field, int order) {
        super();
        this.header = value;
        this.field = field;
        this.order = order;
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

    public int compareTo(CsvMetaInfo o) {
        return order -  o.getOrder();
    }
    
}
