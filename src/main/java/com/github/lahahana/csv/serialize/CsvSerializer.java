package com.github.lahahana.csv.serialize;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
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
 * {@link #serialize(Object, CSVFormat)}
 * <p>
 * {@link #serialize(Object[], Class)}
 * <p>
 * {@link #serialize(Object[], Class, CSVFormat)}
 * <p>
 * {@link #serialize(Iterable, Class)}
 * <p>
 * {@link #serialize(Iterable, Class, CSVFormat)}
 * 
 * @author Lahahana
 */
public class CsvSerializer {
	
	private static final String ARRAY_DELIMITER = ",";
	
	private Class<?> clazz;
	
	private CSVFormat csvFormat;
	
	Appendable out;
	
	CSVPrinter csvPrinter;
	
	CsvSerializer(Builder builder) throws IOException {
		this.out = builder.out;
		this.csvFormat = builder.csvFormat;
		this.csvPrinter = new CSVPrinter(this.out, this.csvFormat);
	}

	public <T> void serialize(final T object) throws CsvException, IOException {
		serialize0(object);
	}

	public <T> void serialize(final T[] array) throws CsvException, IOException {
		serialize0(array);
	}

	public <T> void serialize(final Iterable<T> iterable, final Class<T> clazz) throws CsvException, IOException {
		serialize0(iterable, clazz);
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
				if (!CsvProperty.DEFAULT_PREFIX.equals(prefix)) {
					builder.append(prefix);
					builder.append("_");
				}
			}
			String header = builder.append(csvMetaNode.getCsvMetaInfo().getHeader()).toString();
			csvPrinter.print(header);
		}
		csvPrinter.println();
	}

	protected <T> void printObjects(final CsvMetaNode<?>[] csvMetaNodes, final Iterable<T> iterable) throws CsvException, IOException {
		for (Object obj : iterable) {
			printObject(csvMetaNodes, obj);
		}
	}

	protected <T> void printObjects(final CsvMetaNode<?>[] csvMetaNodes, final T[] objects) throws CsvException, IOException {
		for (int i = 0; i < objects.length; i++) {
			printObject(csvMetaNodes, objects[i]);
		}
	}

	protected <T> void printObject(final CsvMetaNode<?>[] csvMetaNodes, final T obj) throws CsvException, IOException {
		try {
			if (obj == null) {
				for (int i = 0; i < csvMetaNodes.length; i++)
					csvPrinter.print(csvMetaNodes[i].getCsvMetaInfo().getDefaultValue());
				return;
			}
			for (int i = 0; i < csvMetaNodes.length; i++) {
				printField(csvMetaNodes[i], obj);
			}
			csvPrinter.println();
		} catch (IllegalArgumentException e) {
			throw new CsvException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T, C> void printField(final CsvMetaNode<C> csvMetaNode, final T obj) throws CsvException, IOException, IllegalArgumentException, IllegalAccessException {
		CsvMetaNode<?>[] path = csvMetaNode.getPath();
		if(obj == null) {
			return;
		}
		Object obj2 = obj;
		for (int j = path.length - 1; j >= 0; j--) {
			Field f = path[j].getCsvMetaInfo().getField();
			if (j == 0) {
				if (f.getType().isArray()) {
					Object[] objects = (Object[]) (f.get(obj2));
					if (objects == null) {
						obj2 = csvMetaNode.getCsvMetaInfo().getDefaultValue();
						break;
					}

					StringBuilder builder = new StringBuilder();
					final int lastIndex =  objects.length - 1;
					for (int k = 0; k < objects.length; k++) {
						builder.append(objects[k]);
						if (k != lastIndex) {
							builder.append(ARRAY_DELIMITER);
						}
					}
					obj2 = builder.toString();
					break;
				} else if (f.getType().isAssignableFrom(Collection.class)) {
					Collection<?> objects = (Collection<?>)f.get(obj2);
					if (objects == null) {
						obj2 = csvMetaNode.getCsvMetaInfo().getDefaultValue();
						break;
					}
					StringBuilder builder = new StringBuilder();
					Iterator<?> iterator = objects.iterator();
					for (; iterator.hasNext();) {
						Object e = iterator.next();
						builder.append(e);
						if (iterator.hasNext()) {
							builder.append(ARRAY_DELIMITER);
						}
					}
					obj2 = builder.toString();
					break;
				}
			}

			obj2 = f.get(obj2);
			if (obj2 == null) {
				obj2 = csvMetaNode.getCsvMetaInfo().getDefaultValue();
				csvPrinter.print(obj2);
				return;
			}
		}
		SerializationConvertor converter = csvMetaNode.getCsvMetaInfo().getConverter();
		if (converter != null) {
			obj2 = converter.convert(obj2);
		}
		csvPrinter.print(obj2);
	}
	
	public static final class Builder {
		Appendable out;
		CSVFormat csvFormat = CSVFormat.DEFAULT;
		
		public Builder(Appendable out) {
			super();
			this.out = out;
		}
		
		public Builder csvFormat(CSVFormat csvFormat) {
			this.csvFormat = csvFormat;
			return this;
		}
		
		public CsvSerializer build() throws IOException {
			return new CsvSerializer(this);
		}
	}
}
