package com.github.lahahana.csv.deserialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import com.github.lahahana.csv.exceptions.CsvException;


/**
 * This class use {@link Reader} as source of CSV data
 * Example Code:</br>
 * <blockquote>
 * <pre>
 * FileReader in = new FileReader("D:\\data.csv");
 * CsvDeserializer<Student, Reader> deserializer = new ReaderCsvDeserializer<Student>(Student.class, in);
 * CsvResultSet<Student, Reader> csvResultSet = deserializer.deserialize();
 * List<Student> students = null;
 * while((students = csvResultSet.next()) != null) {
 *		....
 * }
 * </pre>
 * </blockquote>
 * 
 * @author Lahahana
 * */

public class ReaderCsvDeserializer<C> extends AbstractCsvDeserializer<C, Reader> {
	
	public ReaderCsvDeserializer(Class<C> clazz, Reader in) {
		super(clazz, in instanceof BufferedReader ? in : new BufferedReader(in));

	}
	
	public ReaderCsvDeserializer(Class<C> clazz, int bufferSize, Reader in) {
		super(clazz, in instanceof BufferedReader ? in : new BufferedReader(in), bufferSize);
	}

	@Override
	protected String tryExtractCsvRow() throws CsvException {
		try {
			return ((BufferedReader)in).readLine();
		} catch (IOException e) {
			throw new CsvException(e);
		}
	}
}
