package com.github.lahahana.csv.serialize;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
import com.github.lahahana.csv.convertor.Converter;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.resolver.CsvMetaTreeResolver;
import com.github.lahahana.csv.resolver.PropertyResolver;
import com.github.lahahana.csv.util.Utils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

/**
 * Use following method to serialize object:
 * <p>{@link #serialize(Object)}
 * <p>{@link #serialize(Object, CSVFormat)}
 * <p>{@link #serialize(Iterable, Class)}
 * <p>{@link #serialize(Iterable, Class, CSVFormat)}
 * */
public class CsvSerializer {
    
    public static String serialize(final Object object) throws CsvException, IOException {
        return serialize(object, CSVFormat.DEFAULT);
    }

    public static String serialize(final Object object, final CSVFormat csvFormat) throws CsvException, IOException {
        if(object.getClass().isArray()) {
            return serialize0((Object[])object, CSVFormat.DEFAULT);
        }else {
            return serialize0(object, CSVFormat.DEFAULT);
        }
    }
    
    public static <T extends Iterable<?>> String serialize(final T iterable, final Class<?> clazz) throws CsvException, IOException {
        return serialize(iterable, clazz, CSVFormat.DEFAULT);
    }
    
    public static <T extends Iterable<?>> String serialize(final T iterable, final Class<?> clazz, final CSVFormat csvFormat) throws CsvException, IOException {
        return serialize0(iterable, clazz, csvFormat);
    }
    
    private static String serialize0(final Object object, final CSVFormat csvFormat) throws CsvException, IOException {
        Class<?> clazz = object.getClass();
        CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
        printHeader(printer, csvMetaNodes);
        printObject(printer, csvMetaNodes, object);
        return printer.getOut().toString();
    }
    
    private static String serialize0(final Object[] object, final CSVFormat csvFormat) throws CsvException, IOException {
        Class<?> clazz = object.getClass().getComponentType();
        CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
        printHeader(printer, csvMetaNodes);
        printObjects(printer, csvMetaNodes, object);
        return printer.getOut().toString();
    }
    
    private static <T extends Iterable<?>> String serialize0(final T iterable, final Class<?> clazz, final CSVFormat csvFormat) throws CsvException, IOException {
        CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
        printHeader(printer, csvMetaNodes);
        printObjects(printer, csvMetaNodes, iterable);
        return printer.getOut().toString();
    }
    
    private static CsvMetaNode[] resolveClass(Class<?> clazz) throws CsvException {
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
    
    
    private static void printHeader(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes) throws CsvException, IOException {
        for (CsvMetaNode csvMetaNode : csvMetaNodes) {
            CsvMetaNode[] path = CsvMetaTreeResolver.resolveNodePath(csvMetaNode);
            csvMetaNode.setPath(path);
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < path.length; i++) {
                String prefix  = path[i].getCsvMetaInfo().getPrefix();
                if(!CsvProperty.DEFAULT_PREFIX.equals(prefix)) {
                    builder.append(prefix);
                    builder.append("_");
                }
            }
            String header = builder.append(csvMetaNode.getCsvMetaInfo().getHeader()).toString();
            printer.print(header);
        }
        printer.println();
    }
    
    private static <T extends Iterable<?>> void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final T iterable) throws CsvException, IOException {
        for (Object obj : iterable) {
            printObject(printer, csvMetaNodes, obj);
        }
    }
    
    private static void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object[] objects) throws CsvException, IOException {
        for (Object obj : objects) {
            printObject(printer, csvMetaNodes, obj);
        }
    }
    
    private static void printObject(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object obj) throws CsvException, IOException {
        try{
            if(obj == null) {
                for(int i = 0; i < csvMetaNodes.length; i++)
                    printer.print(csvMetaNodes[i].getCsvMetaInfo().getDefaultValue());
                return;
            }
            for (CsvMetaNode csvMetaNode : csvMetaNodes) {
                CsvMetaNode[] path = csvMetaNode.getPath();
                Object value = obj;
                if(value == null) {
                    printer.print(csvMetaNode.getCsvMetaInfo().getDefaultValue());
                    continue;
                }
                for (int i = path.length - 1; i >= 0; i--) {
                    if(value == null) {
                        value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
                        break;
                    }
                    Field f = path[i].getCsvMetaInfo().getField();
                    if(i == 0) {
                        if(f.getType().isArray()) {
                            Object[] objects = (Object[])(f.get(value));
                            if(objects == null) {
                                value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
                                break;
                            }
                            StringBuilder builder = new StringBuilder();
                            for (int j = 0; j < objects.length; j++) {
                                builder.append(objects[j]);
                                if(j != objects.length - 1) {
                                    builder.append(",");
                                }
                            }
                            value =builder.toString();
                            break;
                        }else if(f.getType().isAssignableFrom(Collection.class)) {
                            Collection objects = (Collection)(f.get(value));
                            if(objects == null) {
                                value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
                                break;
                            }
                            StringBuilder builder = new StringBuilder();
                            Iterator iterator = objects.iterator();
                            for(;iterator.hasNext();) {
                                Object e = iterator.next();
                                builder.append(e);
                                if(iterator.hasNext()) {
                                    builder.append(",");
                                }
                            }
                            value = builder.toString();
                            break;
                        }
                    }

                    value = f.get(value);
                    if(value == null) {
                        value = csvMetaNode.getCsvMetaInfo().getDefaultValue();
                        break;
                    }
                }
                Converter converter = csvMetaNode.getCsvMetaInfo().getConverter();
                if(converter != null) {
                    value = converter.convert(value);
                }
                printer.print(value);
            }
            printer.println();
        }catch(IllegalAccessException e) {
            throw new CsvException(e);
        }catch(IllegalArgumentException e) {
            throw new CsvException(e);
        }
    }
}
