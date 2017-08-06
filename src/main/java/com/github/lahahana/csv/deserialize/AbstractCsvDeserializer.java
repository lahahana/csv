package com.github.lahahana.csv.deserialize;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DeConvertor;
import com.github.lahahana.csv.convertor.DefaultDeConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.util.Utils;

/**
 * @author Lahahana
 * */

public abstract class AbstractCsvDeserializer<C, I> implements CsvDeserializer<C, I> {
	
	protected static final int DEFAULT_BUFFER_SIZE = 10 << 10;
	
	protected Class<C> clazz; 
	
	protected I in;
	
	protected Map<Integer, String> headerSequenceMap;

	protected Map<String, Tuple<Field, DeConvertor<?>>> headerFieldsMap;
	
	protected Object[] buffer;
	
	protected int bufferSize;

	protected int length;
	
	private boolean available = true;
	
	protected CsvResultSet<C, I> resultSet;
	
	public AbstractCsvDeserializer(Class<C> clazz, I in) {
		super();
		this.clazz = clazz;
		this.in = in;
		this.headerFieldsMap = new HashMap<String, Tuple<Field, DeConvertor<?>>>();
		this.headerSequenceMap = new HashMap<Integer, String>();
		this.bufferSize = DEFAULT_BUFFER_SIZE;
	}

	public AbstractCsvDeserializer(Class<C> clazz, I in, int bufferSize) {
		super();
		this.clazz = clazz;
		this.in = in;
		this.headerFieldsMap = new HashMap<String, Tuple<Field, DeConvertor<?>>>();
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
					Class<? extends DeConvertor<?>> deConvertorClazz = csvProperty.deConvertor();
					Tuple<Field, DeConvertor<?>> tuple = new Tuple<Field, DeConvertor<?>>(field, deConvertorClazz.newInstance());
					headerFieldsMap.put(header2, tuple);
				}else {
					Tuple<Field, DeConvertor<?>> tuple = new Tuple<Field, DeConvertor<?>>(field, DefaultDeConvertor.class.newInstance());
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
		buffer = (Object[]) Array.newInstance(this.clazz, this.bufferSize);
		try{
			while(available) {
				String row = tryExtractCsvRow();
				if (row == null) {
					//no more data
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
						Tuple<Field, DeConvertor<?>> tuple = headerFieldsMap.get(header);
						Field field = tuple.getE1();
						if(field != null) {//ignore unspecified column
							DeConvertor<?> deConvertor = tuple.getE2();
							if(deConvertor.getClass() != DefaultDeConvertor.class) {
								field.set(obj, deConvertor.convert(value));
							} else {
								Object result = Utils.transform(field.getType(), value);
								field.set(obj, result);
							}
						}
					}
				}
				if (length < bufferSize) {
					buffer[length++] = obj;
				} else {
					return;
				}
			}
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		}
	
	}
	
	protected abstract String tryExtractCsvRow() throws CsvException;
	
}
