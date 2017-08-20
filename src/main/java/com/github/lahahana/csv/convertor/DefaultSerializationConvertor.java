package com.github.lahahana.csv.convertor;

import com.github.lahahana.csv.annotations.CsvProperty;

/**
 * This annotation is for convert property value,
 * if property need convert, you should implement this interface.
 * And set the converter class into {@link CsvProperty#serializationConvertor()}
 * */

public final class DefaultSerializationConvertor implements SerializationConvertor<Object> {
    
    public DefaultSerializationConvertor() {}

    public String convert(Object value) {
        return String.valueOf(value);
    }

}
