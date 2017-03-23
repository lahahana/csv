package com.alpha.csv.annotations.convertor;

import com.alpha.csv.annotations.CsvProperty;

/**
 * This Annotation is for convert property value,
 * if property need convert, you should implement this interface.
 * And set the converter class as {@link CsvProperty.convertor} 
 * */
public interface Converter<R, P> {

     R convert(P value);
    
}
