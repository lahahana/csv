package com.github.lahahana.csv.deserialize;

import java.io.IOException;
import java.util.List;

import com.github.lahahana.csv.exceptions.CsvException;

/**
 * @author Lahahana
 * */

public class CsvResultSet<C> {
	
	private AbstractCsvDeserializer<C> absttactCsvDeserializer;
	
	CsvResultSet(AbstractCsvDeserializer<C> absttactCsvDeserializer) {
		this.absttactCsvDeserializer = absttactCsvDeserializer;
	}

	/**
	 * Invoking this method as follows:
     * <blockquote>
     * <pre>
	 * while((result = csvResultSet.next()) != null) {
	 *	 ...
	 * }
     * </pre>
     * </blockquote>
     * 
     * @exception IOException if error occur when io
     * @exception CsvException if any other exceptions occur
	 * @return target deserialized object list, or null if there is not data for deserialization
	 */
	public List<C> next() throws CsvException, IOException {
		absttactCsvDeserializer.tryExtractCsvBody();
		List<C> bufferList = absttactCsvDeserializer.buffer.subList(0, absttactCsvDeserializer.buffer.size());
		return (bufferList.size() == 0 ? null : bufferList);
	}
}