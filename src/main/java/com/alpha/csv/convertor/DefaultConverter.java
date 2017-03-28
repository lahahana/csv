package com.alpha.csv.convertor;

/**
 * This class is for internal use only.
 * */
public final class DefaultConverter implements Converter<Object, Object> {
    
    public DefaultConverter() {}

    public Object convert(Object value) {
        return value;
    }

}
