package com.github.lahahana.csv.deserialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.util.Utils;

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

public class StreamCsvDeserializer<C, I extends Reader> extends AbstractCsvDeserializer<C> {
	
	private BufferedReader in;
	
	private int bufferSize;
	
	private int length;
	
	private boolean available = true;
	
	private StreamCsvDeserializer(Builder<C, I> builder) {
		super(builder.clazz, builder.csvFormat);
		this.bufferSize = builder.bufferSize;
		this.in = builder.in instanceof BufferedReader ? in : new BufferedReader(builder.in);
	}
	
	@Override
	protected void tryExtractCsvHeader() throws IOException {
		String row = ((BufferedReader)in).readLine();
		CSVParser csvParser = CSVParser.parse(row, csvFormat.withFirstRecordAsHeader());
		Map<String, Integer>headersSequences =  csvParser.getHeaderMap();
		for (Entry<String, Integer> entry : headersSequences.entrySet()) {
			headerSequenceMap.put(entry.getValue(), entry.getKey());
		}
	}
	
	protected void tryExtractCsvBody() throws IOException {
		//reset length to zero for next iteration
		length = 0;
		buffer =  new ArrayList<C>(bufferSize);
		
		while(available) {
			if (length == bufferSize) {
				return;
			} 
			CSVRecord record = tryExtractCsvRow();
			if (record == null) {
				available = false;
				return;
			}
			C  obj = null;
			for (int i = 0; i < record.size(); i++) {
				final String columnValue = record.get(i);
				try {
					obj = clazz.newInstance();
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Construct of " + clazz.getName() + "must be public", e);
				}
				String header = headerSequenceMap.get(i);
				if(header == null) 
					continue;
				Tuple<Field, DeserializationConvertor<?>> tuple = headerFieldsMap.get(header);
				if(tuple == null) 
					continue;
				Field field = tuple.getE1();
				if(field != null) {//ignore unspecified column
					DeserializationConvertor<?> convertor = tuple.getE2();
					try {
						if(convertor.getClass() != DefaultDeserializationConvertor.class) {
							field.set(obj, convertor.convert(columnValue));
						} else {
							Object result = Utils.transform(field.getType(), columnValue);
							field.set(obj, result);
						}
					} catch(IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
			buffer.add(obj);
			length++;
		}
	}
	
	
	
	protected CSVRecord tryExtractCsvRow() throws IOException {
		String row = ((BufferedReader)in).readLine();
		if(row == null) {
			return null;
		}
		CSVParser csvParser = CSVParser.parse(row, csvFormat);
		return csvParser.getRecords().get(0);
	}
	
	public static final class Builder<C, I extends Reader> {
		Class<C> clazz;
		I in;
		CSVFormat csvFormat = CSVFormat.DEFAULT;
		int bufferSize = 1 << 10;
		
		public Builder(Class<C> clazz, I in) {
			this.clazz = clazz;
			this.in = in;
		}
		
		public Builder<C, I> bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}
		
		public Builder<C, I> csvFormat(CSVFormat csvFormat) {
			this.csvFormat = csvFormat;
			return this;
		}
		
		public StreamCsvDeserializer<C, I> build() {
			return new StreamCsvDeserializer<C, I>(this);
		}
	}

}
