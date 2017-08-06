package com.github.lahahana.csv.deserialize;

import java.util.Arrays;
import java.util.List;

import com.github.lahahana.csv.exceptions.CsvException;

/**
 * @author Lahahana
 * */

public class CsvResultSet<C, I> {
	
	private AbstractCsvDeserializer<C, I> abstractCsvDeserializer;
	
	CsvResultSet(AbstractCsvDeserializer<C, I> abstractCsvDeserializer) {
		this.abstractCsvDeserializer = abstractCsvDeserializer;
	}

	/**
	 * Invoking this method as follows:
     * <blockquote>
     * <pre>
	 * while((students = csvResultSet.next()) != null) {
	 *	 ...
	 * }
     * </pre>
     * </blockquote>
	 * @return target deserialized object array, or null if there is not data for deserialization
	 */
	@SuppressWarnings("unchecked")
	public List<C> next() throws CsvException {
		abstractCsvDeserializer.tryExtractCsvBody();
		List<C> bufferList = Arrays.asList((C[])abstractCsvDeserializer.buffer).subList(0, abstractCsvDeserializer.length);
		return (abstractCsvDeserializer.length == 0 ? null : bufferList);
	}
}