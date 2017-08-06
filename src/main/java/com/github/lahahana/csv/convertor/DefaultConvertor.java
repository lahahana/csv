package com.github.lahahana.csv.convertor;

/**
 * This class is for internal use only.
 * */

public final class DefaultConvertor implements Convertor<Object, String> {
    
    public DefaultConvertor() {}

    public String convert(Object value) {
        return String.valueOf(value);
    }

}
