package com.github.lahahana.csv.deserialize;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.Tuple;
import com.github.lahahana.csv.convertor.DefaultDeserializationConvertor;
import com.github.lahahana.csv.convertor.DeserializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.resolver.PropertyResolver;

/**
 * @author Lahahana
 * */

public abstract class AbstractCsvDeserializer<C> implements CsvDeserializer<C> {
	
	Map<Integer, String> headerSequenceMap;
	
	Map<String, Tuple<Field, DeserializationConvertor<?>>> headerFieldsMap;
	
	Class<C> clazz; 
	
	CSVFormat csvFormat;
	
	List<C> buffer = new ArrayList<C>(0);
	
	public AbstractCsvDeserializer(Class<C> clazz, CSVFormat csvFormat) {
		this.clazz = clazz;
		this.csvFormat = csvFormat;
		this.headerFieldsMap = new HashMap<String, Tuple<Field, DeserializationConvertor<?>>>();
		this.headerSequenceMap = new HashMap<Integer, String>();
	}
	
	public CsvResultSet<C> deserialize() throws CsvException, IOException {
		resolveClass();
		tryExtractCsvHeader();
		return new CsvResultSet<C>(this);
	}

	protected void resolveClass() throws CsvException {
		Field[] declaredFields = clazz.getDeclaredFields();
		Field[] csvPropertyField = PropertyResolver.filterIgnoreProperties(clazz, declaredFields);
		for (int i = 0; i < csvPropertyField.length; i++) {
			final Field field = csvPropertyField[i];
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
	
	protected abstract void  tryExtractCsvHeader() throws IOException;
	
	protected abstract void tryExtractCsvBody() throws CsvException, IOException;
	
}
