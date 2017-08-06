package com.github.lahahana.csv.deserialize;

import com.github.lahahana.csv.exceptions.CsvException;

/**
 * This class use String as source of CSV data. 
 * {@link StringCsvDeserializer} is used for small csv file, It may cause {@link OutOfMemoryError} when it used for large csv file.
 * @author Lahahana
 * */

public class StringCsvDeserializer<T> extends AbstractCsvDeserializer<T, String> {
	
	private static final String UNIX_DELIMITER = "\n";

	private String[] rows;
	
	private int cursor;
	
	public StringCsvDeserializer(Class<T> clazz, String in) {
		super(clazz, in);
	}

	public StringCsvDeserializer(Class<T> clazz, int bufferSize, String in) {
		super(clazz, in, bufferSize);
	}

	@Override
	protected String tryExtractCsvRow() throws CsvException {
		if (cursor == 0) {
			rows = in.split(UNIX_DELIMITER);
			return rows[cursor++];
		} else if(cursor < rows.length){
			return rows[cursor++];
		} else {
			//indicate no more data to extract
			return null;
		}
	}

}
