package com.github.lahahana.csv.deserialize;

import com.github.lahahana.csv.exceptions.CsvException;

/**
 * Class which is the interface of all kinds of Stream-Style CsvDeserializer.
 * Instance of class implement this interface can not be reused.
 * 
 * @author Lahahana
 * */

public interface CsvDeserializer<C, I> {

	CsvResultSet<C, I> deserialize() throws CsvException;
	
}
