package com.github.lahahana.csv.convertor;

import com.github.lahahana.csv.annotations.CsvProperty;

/**
 * This Annotation is for convert property value,
 * if property need convert, you should implement this interface.
 * And set the converter class into {@link CsvProperty#converter()}
 * 
 * @author Lahahana
 * */

public interface Convertor<P, R> {

     R convert(P value);
    
}
