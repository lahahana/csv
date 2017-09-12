package com.github.lahahana.csv.deserialize;

import java.io.Closeable;
import java.io.IOException;

import com.github.lahahana.csv.exceptions.CsvException;

/**
 * Class which is the interface of all kinds of CsvDeserializer.
 * Instance of class implement this interface can not be reused.
 * 
 * @author Lahahana
 * */

public interface CsvDeserializer<C> extends Closeable {

	CsvResultSet<C> deserialize() throws CsvException, IOException;
	
}
