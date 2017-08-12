package com.github.lahahana.csv.deserialize;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.util.Utils;

/**
 * This class use {@link Reader} as source of CSV data
 * Example Code:
 * <blockquote>
 * <pre>
 * FileReader in = new FileReader("D:\\data.csv");
 * CsvDeserializer<Student> deserializer = new StreamCsvDeserializer.Builder<Student, Reader>(Student.class, in).build();
 * CsvResultSet<Student> csvResultSet = deserializer.deserialize();
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
	
	private Reader in;
	
	private int bufferSize;
	
	private int length;
	
	private boolean available = true;
	
	private CSVParserProxy csvParser;
	
	private StreamCsvDeserializer(Builder<C, I> builder) throws IOException {
		super(builder.clazz, builder.csvFormat);
		this.bufferSize = builder.bufferSize;
		this.in = builder.in;
		this.csvParser = new CSVParserProxy(in, csvFormat);
	}
	
	@Override
	protected void tryExtractCsvHeader() throws IOException, CsvException {
		Map<String, Integer> headerMap = csvParser.getHeaderMap();
		if(headerMap == null) {
			throw new CsvException("csv header not exsits");
		} 
		Set<Entry<String, Integer>> headerSequencePairs = headerMap.entrySet();
		headerSequenceMap = new HashMap<Integer, String>(headerSequencePairs.size());
		for (Entry<String, Integer> entry : headerSequencePairs) {
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
				in.close();
				return;
			}
			C  obj = null;
			try {
				obj = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Construct of " + clazz.getName() + "must be public", e);
			}
			
			for (int i = 0; i < record.size(); i++) {
				String header = headerSequenceMap.get(i);
				if(header == null) 
					continue;
				Tuple<Field, DeserializationConvertor<?>> tuple = headerFieldsMap.get(header);
				if(tuple == null) 
					continue;
				final String columnValue = record.get(i);
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
		return csvParser.tryFetchNextRecord();
	}
	
	public static final class Builder<C, I extends Reader> {
		Class<C> clazz;
		I in;
		CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
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
		
		public StreamCsvDeserializer<C, I> build() throws IOException {
			return new StreamCsvDeserializer<C, I>(this);
		}
	}

}
