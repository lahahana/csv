package com.alpha.csv.resolver;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import com.alpha.csv.annotations.CsvProperty;
import com.alpha.csv.base.CsvMetaInfo;
import com.alpha.csv.base.CsvMetaNode;
import com.alpha.csv.base.CsvMetaTreeBuilder.CsvMetaTree;
import com.alpha.csv.convertor.Converter;
import com.alpha.csv.convertor.DefaultConverter;
import com.alpha.csv.exceptions.CsvException;
import com.alpha.csv.util.Utils;

public class CsvMetaTreeResolver {

    public static void scanCsvMetaTree(CsvMetaTree csvMetaTree) throws CsvException {
        scanCsvMetaTree0(csvMetaTree.getRoot());
    }
    
    private static void scanCsvMetaTree0(CsvMetaNode csvMetaNode) throws CsvException{
        if(csvMetaNode.getChilds() == null) {
            Class<?> clazz = csvMetaNode.getCsvMetaInfo().getField().getType();
            if(!checkIsPrimitiveClass(clazz)) {
                Field[] fields = clazz.getDeclaredFields();
                if(fields.length == 0) {
                    return;
                }   
                fields =  PropertyResolver.filterIgnoreProperties(clazz, fields);
                csvMetaNode.setChilds(Utils.convertFieldsToCsvMetaNodes(csvMetaNode, fields));
                scanCsvMetaTree0(csvMetaNode);
            }
        }else {
            for (CsvMetaNode node : csvMetaNode.getChilds()) {
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
    
    private static void resolveCsvMetaTree0(CsvMetaNode csvMetaNode) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {
        if(csvMetaNode.getChilds() != null) {
            CsvMetaNode[] childs = csvMetaNode.getChilds();
            for (CsvMetaNode node : childs) {
                CsvMetaInfo csvMetaInfo = node.getCsvMetaInfo();
                Field f = node.getCsvMetaInfo().getField();
                f.setAccessible(true);
                if(f.isAnnotationPresent(CsvProperty.class)) {
                    CsvProperty anno = f.getAnnotation(CsvProperty.class);
                    String header = anno.defaultValue().equals(anno.header()) ? f.getName() : anno.header();
                    csvMetaInfo.setHeader(header);
                    int order = anno.order();
                    csvMetaInfo.setOrder(order);
                }
                resolveCsvMetaTree0(node);
            }
        }else {
            CsvMetaInfo csvMetaInfo = csvMetaNode.getCsvMetaInfo();
            Field f = csvMetaInfo.getField();
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
        }
    }
    
    public static void sortCsvMetaTree(CsvMetaTree csvMetaTree) {
        sortCsvMetaTree0(csvMetaTree.getRoot());
    }
    
    private static void sortCsvMetaTree0(CsvMetaNode csvMetaNode) {
        CsvMetaNode[] csvMetaNodes = csvMetaNode.getChilds();
        if(csvMetaNodes != null) {
            Collections.sort(Arrays.asList(csvMetaNodes));
            for (CsvMetaNode csvMetaNode2 : csvMetaNodes) {
                sortCsvMetaTree0(csvMetaNode2);
            }
        }
    }
    
    public static CsvMetaNode[] resolveNodePath(CsvMetaNode csvMetaNode) {
        return resolveNodePath0(csvMetaNode, new CsvMetaNode[csvMetaNode.getDepth()], 0);
    }
    
    private static CsvMetaNode[] resolveNodePath0(CsvMetaNode csvMetaNode, CsvMetaNode[] path, int index) {
        CsvMetaNode parentNode = csvMetaNode.getParent();
        if (parentNode != null) {
            path[index ++] = csvMetaNode;
            resolveNodePath0(parentNode, path, index);
        }
        return path;
    }
    
    
    private static boolean checkIsPrimitiveClass(Class<?> clazz) {
        try{
            if((clazz.isPrimitive() || clazz == String.class || clazz.getField("TYPE").get(null).getClass().isPrimitive())){
                return true;
            }else {
                return false;
            }
        }catch(Exception e) {
            return false;
        }
    }
}
