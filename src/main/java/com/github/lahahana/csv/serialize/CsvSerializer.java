package com.github.lahahana.csv.serialize;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.CsvMetaInfo;
import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
import com.github.lahahana.csv.convertor.DefaultSerializationConvertor;
import com.github.lahahana.csv.convertor.SerializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.resolver.CsvMetaTreeResolver;
import com.github.lahahana.csv.resolver.PropertyResolver;
import com.github.lahahana.csv.util.Utils;

/**
 * Use following method to serialize object:
 * <p>
 * {@link #serialize(Object)}
 * <p>
 * {@link #serialize(Object[])}
 * <p>
 * {@link #serialize(Iterable, Class)}
 * <p>
 * In order to avoid resource leak risk, please call {@link #close()} after serialization.
 * 
 * @author Lahahana
 */
public class CsvSerializer implements Closeable {
	
	private static final String DELIMITER = ",";
	
	private static final String HEADER_DELIMITER = "_";
	
	private int flushThreshold;
	
	private Class<?> clazz;
	
	Appendable out;
	
	CSVPrinter csvPrinter;
	
	CsvSerializer(Builder builder) throws IOException {
		this.out = builder.out;
		this.flushThreshold = builder.flushThreshold;
		this.csvPrinter = new CSVPrinter(this.out, builder.csvFormat);
	}

	public <T> void serialize(final T object) throws CsvException, IOException {
		serialize0(object);
		csvPrinter.flush();
	}

	public <T> void serialize(final T[] array) throws CsvException, IOException {
		serialize0(array);
		csvPrinter.flush();
	}

	public <T> void serialize(final Iterable<T> iterable, final Class<T> clazz) throws CsvException, IOException {
		serialize0(iterable, clazz);
		csvPrinter.flush();
	}

	protected <T> void serialize0(final T object) throws CsvException, IOException {
		this.clazz = object.getClass();
		CsvMetaNode<?>[] csvMetaNodes = resolveClass(clazz);
		printHeader(csvMetaNodes);
		printObject(csvMetaNodes, object);
	}

	protected <T> void serialize0(final T[] object) throws CsvException, IOException {
		this.clazz = object.getClass().getComponentType();
		CsvMetaNode<?>[] csvMetaNodes = resolveClass(clazz);
		printHeader(csvMetaNodes);
		printObjects(csvMetaNodes, object);
	}

	protected <T> void serialize0(final Iterable<T> iterable, final Class<T> clazz) throws CsvException, IOException {
		this.clazz = clazz;
		CsvMetaNode<?>[] csvMetaNodes = resolveClass(clazz);
		printHeader(csvMetaNodes);
		printObjects(csvMetaNodes, iterable);
	}

	protected <T> CsvMetaNode<?>[] resolveClass(Class<T> clazz) throws CsvException {
		Field[] fields = clazz.getDeclaredFields();
		fields = PropertyResolver.filterIgnoreProperties(clazz, fields);
		CsvMetaTree csvMetaTree = CsvMetaTreeBuilder.buildCsvMetaTree(fields);
		CsvMetaTreeResolver.scanCsvMetaTree(csvMetaTree);
		CsvMetaTreeResolver.resolveCsvMetaTree(csvMetaTree);
		CsvMetaTreeResolver.sortCsvMetaTree(csvMetaTree);
		CsvMetaNode<?>[] csvMetaNodes = new CsvMetaNode[csvMetaTree.getLeafNodeCount()];
		Utils.convertCsvMetaTreeIntoArray(csvMetaTree.getRoot(), csvMetaNodes, 0);
		return csvMetaNodes;
	}

	protected void printHeader(final CsvMetaNode<?>[] csvMetaNodes) throws CsvException, IOException {
		for (CsvMetaNode<?> csvMetaNode : csvMetaNodes) {
			CsvMetaNode<?>[] path = CsvMetaTreeResolver.resolveNodePath(csvMetaNode);
			csvMetaNode.setPath(path);
			StringBuilder builder = new StringBuilder();
			for (int i = path.length - 1; i > 0; i--) {
				String prefix = path[i].getCsvMetaInfo().getPrefix();
				String header = path[i].getCsvMetaInfo().getPrefix();
				if (!CsvProperty.DEFAULT_PREFIX.equals(prefix)) {
					builder.append(prefix +  HEADER_DELIMITER);
				} else if(!CsvProperty.DEFAULT_HEADER.equals(header)) {
					builder.append(header + HEADER_DELIMITER);
				}
			}
			String header = builder.append(csvMetaNode.getCsvMetaInfo().getHeader()).toString();
			csvPrinter.print(header);
		}
		csvPrinter.println();
	}

	protected <T> void printObjects(final CsvMetaNode<?>[] csvMetaNodes, final Iterable<T> iterable) throws CsvException, IOException {
		int count = 0;
		for (Object obj : iterable) {
			if(++count % flushThreshold == 0) {
				csvPrinter.flush();
			}
			printObject(csvMetaNodes, obj);
		}
	}

	protected <T> void printObjects(final CsvMetaNode<?>[] csvMetaNodes, final T[] objects) throws CsvException, IOException {
		for (int i = 0; i < objects.length; i++) {
			if(i % flushThreshold == 0) {
				csvPrinter.flush();
			}
			printObject(csvMetaNodes, objects[i]);
		}
	}

	protected <T> void printObject(final CsvMetaNode<?>[] csvMetaNodes, final T obj) throws CsvException, IOException {
		try {
			if (obj == null) {
				return;
			}
			for (int i = 0; i < csvMetaNodes.length; i++) {
				printField(csvMetaNodes[i], obj);
			}
			csvPrinter.println();
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T, C> void printField(final CsvMetaNode<C> csvMetaNode,final T obj) throws CsvException, IOException, IllegalArgumentException, IllegalAccessException {
		final CsvMetaNode<?>[] path = csvMetaNode.getPath();
		final CsvMetaInfo<C> csvMetaInfo = csvMetaNode.getCsvMetaInfo();
		final SerializationConvertor<C> convertor = csvMetaInfo.getConvertor();
		final String defaultValue = csvMetaInfo.getDefaultValue();
		final boolean needConvert = convertor != null && !(convertor instanceof DefaultSerializationConvertor);
		Object obj2 = obj;
		for (int j = path.length - 1; j >= 0; j--) {
			final Field f = path[j].getCsvMetaInfo().getField();
			obj2 = f.get(obj2);
			if (obj2 == null) {
				obj2 = defaultValue;
				break;
			} 
			if (j == 0) { //only process array/collection at first layer
				if (f.getType().isArray()) {
					C[] objects = (C[]) obj2;
					if (objects.length == 0) {
						obj2 = defaultValue;
						break;
					}
					
					StringBuilder builder = new StringBuilder();
					final int lastIndex =  objects.length - 1;
					for (int k = 0; k < objects.length; k++) {
						final C object = objects[k];
						if (needConvert) {
							builder.append(convertor.convert(object));
						} else {
							builder.append(object);
						}
						if (k != lastIndex) {
							builder.append(DELIMITER);
						}
					}
					obj2 = builder.toString();
					break;
				} else if (Collection.class.isAssignableFrom(f.getType())) {
					Collection<C> objects = (Collection<C>)obj2;
					if (objects.size() == 0) {
						obj2 = defaultValue;
						break;
					}
					StringBuilder builder = new StringBuilder();
					Iterator<C> iterator = objects.iterator();
					for (; iterator.hasNext(); ) {
						C object = iterator.next();
						if (needConvert) {
							builder.append(convertor.convert(object));
						} else {
							builder.append(object);
						}
						if (iterator.hasNext()) {
							builder.append(DELIMITER);
						}
					}
					obj2 = builder.toString();
					break;
				}
			}
			if (needConvert) {
				obj2 = convertor.convert((C) obj2);
				break;
			}
		}
		csvPrinter.print(obj2);
	}
	

	@Override
	public void close() throws IOException {
		csvPrinter.close();
		csvPrinter = null;
		out = null;
	}
	
	public static final class Builder {
		Appendable out;
		CSVFormat csvFormat = CSVFormat.DEFAULT;
		int flushThreshold = 1000;
		
		public Builder(Appendable out) {
			super();
			assert out != null;
			this.out = out;
		}
		
		public Builder csvFormat(CSVFormat csvFormat) {
			this.csvFormat = csvFormat;
			return this;
		}
		
		public Builder flushThreshold(int flushThreshold) {
			this.flushThreshold = flushThreshold;
			return this;
		}
		
		public CsvSerializer build() throws IOException {
			return new CsvSerializer(this);
		}
	}

}
