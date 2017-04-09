package com.github.lahahana.csv.convertor;

import com.github.lahahana.csv.annotations.CsvProperty;

/**
 * This Annotation is for convert property value,
 * if property need convert, you should implement this interface.
 * And set the converter class into {@link CsvProperty#converter()}
 * */
public interface Converter<R, P> {

     R convert(P value);
    
}
