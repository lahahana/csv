package com.github.lahahana.csv.deserialize;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.util.Utils;

/**
 * @author Lahahana
 * */

public abstract class AbstractCsvDeserializer<C, I> implements CsvDeserializer<C, I> {
	
	private static final int DEFAULT_BUFFER_SIZE = 10 << 10;
	
	private Map<Integer, String> headerSequenceMap;
	
	private Map<String, Tuple<Field, DeserializationConvertor<?>>> headerFieldsMap;
	
	private Class<C> clazz; 
	
	I in;
	
	private int bufferSize;
	
	Object[] buffer;
	
	int length;
	
	private boolean available = true;
	
	public AbstractCsvDeserializer(Class<C> clazz, I in) {
		super();
		this.clazz = clazz;
		this.in = in;
		this.headerFieldsMap = new HashMap<String, Tuple<Field, DeserializationConvertor<?>>>();
		this.headerSequenceMap = new HashMap<Integer, String>();
		this.bufferSize = DEFAULT_BUFFER_SIZE;
	}

	public AbstractCsvDeserializer(Class<C> clazz, I in, int bufferSize) {
		super();
		this.clazz = clazz;
		this.in = in;
		this.headerFieldsMap = new HashMap<String, Tuple<Field, DeserializationConvertor<?>>>();
		this.headerSequenceMap = new HashMap<Integer, String>();
		if(bufferSize < 0) {
			throw new IllegalArgumentException("buffer size must larger than zero.");
		}
		this.bufferSize = bufferSize;
	}

	public CsvResultSet<C, I> deserialize() throws CsvException {
		resolveClass();
		tryExtractCsvHeader();
		return new CsvResultSet<C, I>(this);
	}

	protected void resolveClass() throws CsvException {
		Field[] declaredFields = clazz.getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			final Field field = declaredFields[i];
			field.setAccessible(true);
			boolean csvPropertyAnnotated = field.isAnnotationPresent(CsvProperty.class);
			try{
				if(csvPropertyAnnotated) {
					CsvProperty csvProperty = field.getAnnotation(CsvProperty.class);
					String header = csvProperty.header();
					String header2 = header.equals(CsvProperty.DEFAULT_HEADER) ? field.getName() : header;
					Class<? extends DeserializationConvertor<?>> deConvertorClazz = csvProperty.deserializationConvertor();
					Tuple<Field, DeserializationConvertor<?>> tuple = new Tuple<Field, DeserializationConvertor<?>>(field, deConvertorClazz.newInstance());
					headerFieldsMap.put(header2, tuple);
				}else {
					Tuple<Field, DeserializationConvertor<?>> tuple = new Tuple<Field, DeserializationConvertor<?>>(field, DefaultDeserializationConvertor.class.newInstance());
					headerFieldsMap.put(field.getName(), tuple);
				}
			} catch(IllegalAccessException e) {
				throw new CsvException(e);
			} catch(InstantiationException e) {
				throw new CsvException(e);
			}
		}
	}
	
	protected void tryExtractCsvHeader() throws CsvException {
		String row = tryExtractCsvRow();
		String[] headers = row.split(",");
		for (int i = 0; i < headers.length; i++) {
			headerSequenceMap.put(i, headers[i]);
		}
	
	}
	
	protected void tryExtractCsvBody() throws CsvException {
		//reset length to zero for next iteration
		length = 0;
		buffer =  new Object[bufferSize];
		
		while(available) {
			if (length == bufferSize) {
				return;
			} 
			String row = tryExtractCsvRow();
			if (row == null) {
				//no more data
				available = false;
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
			
			String[] records = row.split(",");
			for (int i = 0; i < records.length; i++) {
				String value = records[i];
				String header = headerSequenceMap.get(i);
				if(header != null) {//ignore out of size unknown column
					Tuple<Field, DeserializationConvertor<?>> tuple = headerFieldsMap.get(header);
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
			}
			buffer[length++] = obj;
		}
	
	}
	
	protected abstract String tryExtractCsvRow() throws CsvException;
	
}
