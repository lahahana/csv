package com.alpha.csv.serialize;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.alpha.csv.annotations.convertor.Converter;
import com.alpha.csv.base.CsvMetaNode;
import com.alpha.csv.base.CsvMetaTreeBuilder;
import com.alpha.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
import com.alpha.csv.exceptions.CsvException;
import com.alpha.csv.resolver.CsvMetaTreeResolver;
import com.alpha.csv.resolver.PropertyResolver;
import com.alpha.csv.util.Utils;

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
        Class<?> clazz = object.getClass();
        CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
        printHeader(printer, csvMetaNodes);
        printObjects(printer, csvMetaNodes, object);
        return printer.getOut().toString();
    }
    
    private static <T extends Iterable<?>> String serialize0(final T iterable, final Class<?> clazz, final CSVFormat csvFormat) throws CsvException, IOException {
        long start = System.currentTimeMillis();
        CsvMetaNode[] csvMetaNodes = resolveClass(clazz);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        start = end;
        CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
        printHeader(printer, csvMetaNodes);
        end = System.currentTimeMillis();
        System.out.println(end - start);
        start = end;
        printObjects(printer, csvMetaNodes, iterable);
        end = System.currentTimeMillis();
        System.out.println(end - start);
        start = end;
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
            printer.print(csvMetaNode.getCsvMetaInfo().getHeader());
        }
        printer.println();
    }
    
    private static <T extends Iterable<?>> void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final T iterable) throws CsvException, IOException {
        for (Object obj : iterable) {
            printObject(printer, csvMetaNodes, obj);
        }
    }
    
    private static void printObjects(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object[] objs) throws CsvException, IOException {
        for (Object obj : objs) {
            printObject(printer, csvMetaNodes, obj);
        }
    }
    
    private static void printObject(final CSVPrinter printer, final CsvMetaNode[] csvMetaNodes, final Object obj) throws CsvException, IOException {
        try{
            for (CsvMetaNode csvMetaNode : csvMetaNodes) {
                CsvMetaNode[] path = csvMetaNode.getPath();
                Object value = obj;
                for (int i = path.length - 1; i >= 0; i--) {
                    value = path[i].getCsvMetaInfo().getField().get(value);
                }
                Converter converter = csvMetaNode.getCsvMetaInfo().getConverter();
                if(converter != null) {
                    value = converter.convert(value);
                }
                printer.print(value);
            }
            printer.println();
        }catch(IllegalAccessException | IllegalArgumentException e) {
            throw new CsvException(e);
        }
    }
}
