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
import com.github.lahahana.csv.convertor.Convertor;
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
 * {@link #serialize(Iterable, Class)}
 * <p>
 * {@link #serialize(Iterable, Class, CSVFormat)}
 * 
 * @author Lahahana
 */
public class CsvSerializer {
	
	private Class<?> clazz;
	
	private CSVFormat csvFormat;
	
	public CsvSerializer() {
		this.csvFormat = CSVFormat.DEFAULT;
	}
	
	public CsvSerializer(CSVFormat csvFormat) {
		super();
		this.csvFormat = csvFormat;
	}

	public <T> void serialize(final T object, final Appendable out) throws CsvException, IOException {
		serialize0(object, out);
	}

	public <T> void serialize(final T[] array, final Appendable out) throws CsvException, IOException {
		serialize0(array, out);
	}

	public <T> void serialize(final Iterable<T> iterable, Class<T> clazz, final Appendable out) throws CsvException, IOException {
		serialize0(iterable, clazz, out);
	}

	private <T> void serialize0(final T object, final Appendable out) throws CsvException, IOException {
		this.clazz = object.getClass();
		CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
		CSVPrinter printer = new CSVPrinter(out, csvFormat);
		printHeader(printer, csvMetaNodes);
		printObject(printer, csvMetaNodes, object);
	}

	private <T> void serialize0(final T[] object, Appendable out) throws CsvException, IOException {
		this.clazz = object.getClass().getComponentType();
		CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
		CSVPrinter printer = new CSVPrinter(out, csvFormat);
		printHeader(printer, csvMetaNodes);
		printObjects(printer, csvMetaNodes, object);
	}

	private <T> void serialize0(final Iterable<T> iterable, Class<T> clazz, Appendable out) throws CsvException, IOException {
		this.clazz = clazz;
		CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
		CSVPrinter printer = new CSVPrinter(out, csvFormat);
		printHeader(printer, csvMetaNodes);
		printObjects(printer, csvMetaNodes, iterable);
	}

	private CsvMetaNode[] resolveClass(Class<?> clazz) throws CsvException {
		Field[] fields = clazz.getDeclaredFields();
		fields = PropertyResolver.filterIgnoreProperties(clazz, fields);
		CsvMetaTree csvMetaTree = CsvMetaTreeBuilder.buildCsvMetaTree(fields);
		CsvMetaTreeResolver.scanCsvMetaTree(csvMetaTree);
		CsvMetaTreeResolver.resolveCsvMetaTree(csvMetaTree);
		CsvMetaTreeResolver.sortCsvMetaTree(csvMetaTree);
		CsvMetaNode[] csvMetaNodes = new CsvMetaNode[csvMetaTree.getLeafNodeCount()];
		Utils.convertCsvMetaTreeIntoArray(csvMetaTree.getRoot(), csvMetaNodes, 0);
		return csvMetaNodes;
	}

	private void printHeader(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes) throws CsvException, IOException {
		for (CsvMetaNode csvMetaNode : csvMetaNodes) {
			CsvMetaNode[] path = CsvMetaTreeResolver.resolveNodePath(csvMetaNode);
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
			printer.print(header);
		}
		printer.println();
	}

	private <T> void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Iterable<T> iterable) throws CsvException, IOException {
		for (Object obj : iterable) {
			printObject(printer, csvMetaNodes, obj);
		}
	}

	private void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object[] objects) throws CsvException, IOException {
		for (int i = 0; i < objects.length; i++) {
			printObject(printer, csvMetaNodes, objects[i]);
		}
	}

	private void printObject(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object obj) throws CsvException, IOException {
		try {
			if (obj == null) {
				for (int i = 0; i < csvMetaNodes.length; i++)
					printer.print(csvMetaNodes[i].getCsvMetaInfo().getDefaultValue());
				return;
			}
			for (int i = 0; i < csvMetaNodes.length; i++) {
				CsvMetaNode csvMetaNode = csvMetaNodes[i];
				CsvMetaNode[] path = csvMetaNode.getPath();
				Object value = obj;
				if (value == null) {
					printer.print(csvMetaNode.getCsvMetaInfo().getDefaultValue());
					continue;
				}
				for (int j = path.length - 1; j >= 0; j--) {
					if (value == null) {
						value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
						break;
					}
					Field f = path[j].getCsvMetaInfo().getField();
					if (j == 0) {
						if (f.getType().isArray()) {
							Object[] objects = (Object[]) (f.get(value));
							if (objects == null) {
								value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
								break;
							}

							StringBuilder builder = new StringBuilder();
							for (int k = 0; k < objects.length; k++) {
								builder.append(objects[k]);
								if (k != objects.length - 1) {
									builder.append(",");
								}
							}
							value = builder.toString();
							break;
						} else if (f.getType().isAssignableFrom(Collection.class)) {
							Collection objects = (Collection) (f.get(value));
							if (objects == null) {
								value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
								break;
							}
							StringBuilder builder = new StringBuilder();
							Iterator iterator = objects.iterator();
							for (; iterator.hasNext();) {
								Object e = iterator.next();
								builder.append(e);
								if (iterator.hasNext()) {
									builder.append(",");
								}
							}
							value = builder.toString();
							break;
						}
					}

					value = f.get(value);
					if (value == null) {
						value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
						break;
					}
				}
				Convertor converter = csvMetaNode.getCsvMetaInfo().getConverter();
				if (converter != null) {
					value = converter.convert(value);
				}
				printer.print(value);
			}
			printer.println();
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		} catch (IllegalArgumentException e) {
			throw new CsvException(e);
		}
	}
}
