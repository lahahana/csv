package com.github.lahahana.csv.resolver;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import com.github.lahahana.csv.annotations.CsvProperty;
import com.github.lahahana.csv.base.CsvMetaInfo;
import com.github.lahahana.csv.base.CsvMetaNode;
import com.github.lahahana.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
import com.github.lahahana.csv.convertor.DefaultSerializationConvertor;
import com.github.lahahana.csv.convertor.SerializationConvertor;
import com.github.lahahana.csv.exceptions.CsvException;
import com.github.lahahana.csv.util.Utils;

/**
 * @author Lahahana
 * */

public class CsvMetaTreeResolver {

    public static void scanCsvMetaTree(CsvMetaTree csvMetaTree) throws CsvException {
        scanCsvMetaTree0(csvMetaTree.getRoot());
    }
    
    private static <T> void scanCsvMetaTree0(CsvMetaNode<T> csvMetaNode) throws CsvException {
        if(csvMetaNode.getChilds() == null) {
            Class<?> clazz = csvMetaNode.getCsvMetaInfo().getField().getType();
            if(!Utils.isPrimitiveOrWrapper(clazz)) {
                Field[] fields = clazz.getDeclaredFields();
                if(fields.length == 0) {
                    return;
                }   
                fields =  PropertyResolver.filterIgnoreProperties(clazz, fields);
                csvMetaNode.setChilds(Utils.convertFieldsToCsvMetaNodes(csvMetaNode, fields));
                scanCsvMetaTree0(csvMetaNode);
            }
        }else {
            for (CsvMetaNode<?> node : csvMetaNode.getChilds()) {
                scanCsvMetaTree0(node);
            }
        }
    }
    
    public static void resolveCsvMetaTree(CsvMetaTree csvMetaTree) throws CsvException {
        try{
            resolveCsvMetaTree0(csvMetaTree.getRoot());
        }catch (Exception e) {
            throw new CsvException(e);
        }
    }
    
    private static <T> void resolveCsvMetaTree0(CsvMetaNode<T> csvMetaNode) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {
        if(csvMetaNode.getChilds() != null) {
            CsvMetaNode<?>[] nodes = csvMetaNode.getChilds();
            for (CsvMetaNode<?> node : nodes) {
                CsvMetaInfo<?> csvMetaInfo = node.getCsvMetaInfo();
                resolveCsvProperty(csvMetaInfo);
                resolveCsvMetaTree0(node);
            }
        }else {
            CsvMetaInfo<T> csvMetaInfo = csvMetaNode.getCsvMetaInfo();
            resolveCsvProperty(csvMetaInfo);
        }
    }
    
    public static void sortCsvMetaTree(CsvMetaTree csvMetaTree) {
        sortCsvMetaTree0(csvMetaTree.getRoot());
    }
    
    private static void sortCsvMetaTree0(CsvMetaNode<?> csvMetaNode) {
        CsvMetaNode<?>[] csvMetaNodes = csvMetaNode.getChilds();
        if(csvMetaNodes != null) {
            Collections.sort(Arrays.asList(csvMetaNodes));
            for (CsvMetaNode<?> csvMetaNode2 : csvMetaNodes) {
                sortCsvMetaTree0(csvMetaNode2);
            }
        }
    }
    
    public static CsvMetaNode<?>[] resolveNodePath(CsvMetaNode<?> csvMetaNode) {
        return resolveNodePath0(csvMetaNode, new CsvMetaNode[csvMetaNode.getDepth()], 0);
    }

    @SuppressWarnings("unchecked")
	private static <T> void resolveCsvProperty(CsvMetaInfo<T> csvMetaInfo) throws IllegalAccessException, InstantiationException {
        Field f = csvMetaInfo.getField();
        f.setAccessible(true);
        if (f.isAnnotationPresent(CsvProperty.class)) {
            CsvProperty csvProperty = f.getAnnotation(CsvProperty.class);
            String header = CsvProperty.DEFAULT_HEADER.equals(csvProperty.header()) ? f.getName() : csvProperty.header();
            csvMetaInfo.setHeader(header);
            csvMetaInfo.setDefaultValue(csvProperty.defaultValue());
            csvMetaInfo.setOrder(csvProperty.order());
            csvMetaInfo.setPrefix(csvProperty.prefix());
            Class<? extends SerializationConvertor<?>> converterClazz = csvProperty.serializationConvertor();
            if (converterClazz == DefaultSerializationConvertor.class) {
                //DO_NOTHING
            } else {
                csvMetaInfo.setConverter((SerializationConvertor<T>) converterClazz.newInstance());
            }
        }
    }

    private static CsvMetaNode<?>[] resolveNodePath0(CsvMetaNode<?> csvMetaNode, CsvMetaNode<?>[] path, int index) {
        CsvMetaNode<?> parentNode = csvMetaNode.getParent();
        if (parentNode != null) {
            path[index ++] = csvMetaNode;
            resolveNodePath0(parentNode, path, index);
        }
        return path;
    }
    
}
