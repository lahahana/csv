package com.github.lahahana.csv.convertor;

/**
 * This class is for internal use only.
 * 
 * @author Lahahana
 * */

public interface Convertor<P, R> {

     R convert(P value);
    
}
