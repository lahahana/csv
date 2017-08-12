package com.github.lahahana.csv.deserialize;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

final class CSVParserProxy extends CSVParser {

	public CSVParserProxy(Reader reader, CSVFormat format) throws IOException {
		super(reader, format);
	}

	public CSVParserProxy(Reader reader, CSVFormat format, long characterOffset, long recordNumber) throws IOException {
		super(reader, format, characterOffset, recordNumber);
	}
	
	protected CSVRecord tryFetchNextRecord() throws IOException {
		return super.nextRecord();
	}

}
