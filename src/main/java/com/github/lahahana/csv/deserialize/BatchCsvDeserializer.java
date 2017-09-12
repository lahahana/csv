package com.github.lahahana.csv.deserialize;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.util.Utils;

/**
 * Deserialize data one-time. For big scale data, use {@link StreamCsvDeserializer}
 * 
 * @author Lahahana
 * */

public class BatchCsvDeserializer<C, I> extends AbstractCsvDeserializer<C> {
	
	private BatchCsvDeserializer(Builder<C, I> builder) throws IOException {
		super(builder.clazz, builder.csvFormat);
		initiateCsvParser(builder.in);
	}

	private void initiateCsvParser(I in) throws IOException {
		if(in == null) {
			throw new IllegalArgumentException("csv data source can not be null");
		}
		if(in instanceof String)
			csvParser = CSVParser.parse((String)in, csvFormat);
		else if(in instanceof File)
			csvParser = CSVParser.parse((File)in, Charset.defaultCharset(), csvFormat);
		else if(in instanceof Reader)
			csvParser = new CSVParser((Reader)in, csvFormat);
		else if(in instanceof URL)
			csvParser = CSVParser.parse((URL)in, Charset.defaultCharset(), csvFormat);
	}
	
	@Override
	protected void tryExtractCsvHeader() throws CsvException {
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
	
	@Override
	protected void tryExtractCsvBody() throws IOException {
		List<CSVRecord> records = tryExtractCsvRows();
		if (records == null || records.size() == 0) {
			if(buffer != null) {
				buffer.clear();
			}
			return;
		}
		buffer = new ArrayList<C>(records.size());
		for (int i = 0; i < records.size(); i++) {
			final CSVRecord record = records.get(i);
			C  obj = null;
			try {
				obj = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Construct of " + clazz.getName() + "must be public", e);
			}
			for (int j = 0; j < record.size(); j++) {
				String header = headerSequenceMap.get(j);
				if(header == null) 
					continue;
				Tuple<Field, DeserializationConvertor<?>> tuple = headerFieldsMap.get(header);
				if(tuple == null) 
					continue;
				String value = record.get(j);
				Field field = tuple.getE1();
				if(field != null) {//ignore unspecified column
					DeserializationConvertor<?> convertor = tuple.getE2();
					try {
						if(convertor.getClass() != DefaultDeserializationConvertor.class) {
							field.set(obj, convertor.convert(value));
						} else {
							Object result = Utils.transform(field.getType(), value);
							field.set(obj, result);
						}
					} catch(IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
			buffer.add(obj);
		}
	}
	
	protected List<CSVRecord> tryExtractCsvRows() throws IOException {
		return csvParser.getRecords();
	}
	
	@SuppressWarnings("unchecked")
	public static final class Builder<C, I> {
		Class<C> clazz;
		I in;
		CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
		
		public Builder(Class<C> clazz, File in) throws IOException {
			this.clazz = clazz;
			this.in = (I) in;
		}
		
		public Builder(Class<C> clazz, Reader in) throws IOException {
			this.clazz = clazz;
			this.in = (I) in;
		}
		
		public Builder(Class<C> clazz, String in) throws IOException {
			this.clazz = clazz;
			this.in = (I) in;
		}
		
		public Builder(Class<C> clazz, URL in) {
			this.clazz = clazz;
			this.in = (I) in;
		}
		
		public Builder<C, I> csvFormat(CSVFormat csvFormat) {
			this.csvFormat = csvFormat.withFirstRecordAsHeader();
			return this;
		}
		
		public BatchCsvDeserializer<C, I> build() throws IOException {
			return new BatchCsvDeserializer<C, I>(this);
		}
	}
}
