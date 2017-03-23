package com.alpha.csv.serialize;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.alpha.csv.annotations.CsvIgnore;
import com.alpha.csv.annotations.CsvIgnoreProperties;
import com.alpha.csv.annotations.CsvProperty;
import com.alpha.csv.annotations.convertor.Converter;
import com.alpha.csv.annotations.convertor.DefaultConverter;
import com.alpha.csv.base.CsvMetaInfo;
import com.alpha.csv.exceptions.CsvException;
import com.alpha.csv.util.Utils;

public class CsvSerializer {
    
    public static String serialize(final Object object) throws CsvException {
        return serialize(object, CSVFormat.DEFAULT);
    }

    public static String serialize(final Object object, final CSVFormat csvFormat) throws CsvException {
        if(object.getClass().isArray()) {
            return serialize0((Object[])object, CSVFormat.DEFAULT);
        }else {
            return serialize0(object, CSVFormat.DEFAULT);
        }
    }
    
    public static <T extends Iterable<?>> String serialize(final T iterable, Class<?> clazz) throws CsvException {
        return serialize(iterable, clazz, CSVFormat.DEFAULT);
    }
    
    public static <T extends Iterable<?>> String serialize(final T iterable, Class<?> clazz, final CSVFormat csvFormat) throws CsvException {
            return serialize0(iterable, clazz, csvFormat);
    }
    
    private static String serialize0(final Object object, final CSVFormat csvFormat) throws CsvException {
        final Class<?> clazz = object.getClass();
        CSVPrinter printer;
        try{
            printer = new CSVPrinter(new StringBuilder(), csvFormat);
            Field[] fields = clazz.getDeclaredFields();
            fields = filterIgnoreProperties(clazz, fields);
            CsvMetaInfo[] csvMetaInfos = retrieveCsvMetaInfo(clazz, fields);
            csvMetaInfos = sortCsvMetaInfos(csvMetaInfos);
            printHeader(printer, csvMetaInfos);
            printObject(printer, csvMetaInfos, object);
        }catch (Exception e) {
            throw new CsvException(e);
        }
        return printer.getOut().toString();
    }
    
    private static String serialize0(final Object[] object, final CSVFormat csvFormat) throws CsvException {
        try{
            final Class<?> clazz = object.getClass();
            CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
            Field[] fields = clazz.getDeclaredFields();
            fields = filterIgnoreProperties(clazz, fields);
            CsvMetaInfo[] csvMetaInfos = retrieveCsvMetaInfo(clazz, fields);
            csvMetaInfos = sortCsvMetaInfos(csvMetaInfos);
            printHeader(printer, csvMetaInfos);
            printObjects(printer, csvMetaInfos, object);
            return printer.getOut().toString();
        }catch (Exception e) {
            throw new CsvException(e);
        }
    }
    
    private static <T extends Iterable<?>> String serialize0(final T iterable, final Class<?> clazz, final CSVFormat csvFormat) throws CsvException {
        try{
            CSVPrinter printer = new CSVPrinter(new StringBuilder(), csvFormat);
            Field[] fields = clazz.getDeclaredFields();
            fields = filterIgnoreProperties(clazz, fields);
            CsvMetaInfo[] csvMetaInfos = retrieveCsvMetaInfo(clazz, fields);
            csvMetaInfos = sortCsvMetaInfos(csvMetaInfos);
            printHeader(printer, csvMetaInfos);
            printObjects(printer, csvMetaInfos, iterable);
            return printer.getOut().toString();
        }catch (Exception e) {
            throw new CsvException(e);
        }
    }
    
    
    private static void printHeader(final CSVPrinter printer, final CsvMetaInfo[] csvMetaInfos) throws IOException {
        for (CsvMetaInfo csvMetaInfo : csvMetaInfos) {
            printer.print(csvMetaInfo.getHeader());
        }
        printer.println();
    }
    
    private static <T extends Iterable<?>> void printObjects(final CSVPrinter printer, final CsvMetaInfo[] csvMetaInfos, final T collection) throws  ReflectiveOperationException, IOException {
        for (Object obj : collection) {
            printObject(printer, csvMetaInfos, obj);
        }
    }
    
    private static void printObjects(final CSVPrinter printer, final CsvMetaInfo[] csvMetaInfos, final Object[] objs) throws ReflectiveOperationException, IOException {
        for (Object obj : objs) {
            printObject(printer, csvMetaInfos, obj);
        }
    }
    
    private static void printObject(final CSVPrinter printer, final CsvMetaInfo[] csvMetaInfos, final Object obj) throws ReflectiveOperationException, IOException {
        for (CsvMetaInfo csvMetaInfo : csvMetaInfos) {
            Object value = csvMetaInfo.getField().get(obj);
            Converter converter = csvMetaInfo.getConverter();
            if(converter != null) {
                value = converter.convert(value);
            }
            printer.print(value);
        }
        printer.println();
    }
    
    private static Field[] filterIgnoreProperties(final Class<?> clazz, Field[] arg) throws NoSuchFieldException, SecurityException{
        if(clazz.isAnnotationPresent(CsvIgnoreProperties.class)) {
            CsvIgnoreProperties anno =  clazz.getAnnotation(CsvIgnoreProperties.class);
            String[] ignoreProperties = anno.value();
            ignoreProperties = Utils.filterRepeatAndEmptyValue(ignoreProperties);
            Field[] ignoreFields = new Field[ignoreProperties.length];
            for (int i = 0; i < ignoreProperties.length; i++) {
                ignoreFields[i] = clazz.getDeclaredField(ignoreProperties[i]);
            }
            arg = Utils.filterField(arg, ignoreFields);
        }
        
        Field[] ignoreAnnotatedFields = new Field[arg.length];
        int length = 0;
        for (int i = 0; i < arg.length; i++) {
            Field f = arg[i];
            if(f.isAnnotationPresent(CsvIgnore.class)) {
                length ++;
                ignoreAnnotatedFields[i] = f;
            }
        }
        
        arg = Utils.filterField(arg, Arrays.copyOf(ignoreAnnotatedFields, length));
        return arg;
    }

    private static CsvMetaInfo[] retrieveCsvMetaInfo(final Class<?> clazz, final Field[] arg) throws ReflectiveOperationException {
        final CsvMetaInfo[] csvMetaInfos = new CsvMetaInfo[arg.length];
        for (int i = 0; i < arg.length; i++) {
            final Field f = arg[i];
            f.setAccessible(true);
            CsvMetaInfo csvMetaInfo = new CsvMetaInfo(f);
            if(f.isAnnotationPresent(CsvProperty.class)) {
                CsvProperty anno = f.getAnnotation(CsvProperty.class);
                String header = anno.defaultValue().equals(anno.header()) ? f.getName() : anno.header();
                csvMetaInfo.setHeader(header);
                int order = anno.order();
                csvMetaInfo.setOrder(order);
                Class<? extends Converter> converterClazz = anno.converter();
                if(converterClazz == DefaultConverter.class) {
                    //DO_NOTHING
                }
                else {
                    csvMetaInfo.setConverter(converterClazz.newInstance());
                }
            }
            csvMetaInfos[i] = csvMetaInfo;
        }
        return csvMetaInfos;
    }

    //need optimization
    private static CsvMetaInfo[] sortCsvMetaInfos(CsvMetaInfo[] csvMetaInfos) {
        List<CsvMetaInfo> list = Arrays.asList(csvMetaInfos);
        Collections.sort(list);
        return list.toArray(new CsvMetaInfo[list.size()]);
    }

}
