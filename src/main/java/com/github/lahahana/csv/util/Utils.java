package com.github.lahahana.csv.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.github.lahahana.csv.base.CsvMetaInfo;
import com.github.lahahana.csv.base.CsvMetaNode;

public class Utils {
	
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>(9);
    
    private static final Map<Class<?>, CsvPrimitiveEnum> primitiveWrapperEnumMap = new HashMap<Class<?>, CsvPrimitiveEnum>(17);
    
    private enum CsvPrimitiveEnum {
    	BOOLEAN(Boolean.class), BYTE(Byte.class), CHARACTER(Character.class), SHORT(Short.class), 
    	INTEGER(Integer.class), FLOAT(Float.class), DOUBLE(Double.class), LONG(Long.class), STRING(String.class);
    	
		CsvPrimitiveEnum(Class<?> clazz) {}
    }
    
    static {
         primitiveWrapperMap.put(Boolean.class, Boolean.TYPE);
         primitiveWrapperMap.put(Byte.class, Byte.TYPE);
         primitiveWrapperMap.put(Character.class, Character.TYPE);
         primitiveWrapperMap.put(Short.class, Short.TYPE);
         primitiveWrapperMap.put(Integer.class, Integer.TYPE);
         primitiveWrapperMap.put(Float.class, Float.TYPE);
         primitiveWrapperMap.put(Long.class, Long.TYPE);
         primitiveWrapperMap.put(Double.class, Double.TYPE);
         primitiveWrapperMap.put(String.class, String.class);
         
         primitiveWrapperEnumMap.put(Boolean.TYPE, CsvPrimitiveEnum.BOOLEAN);
         primitiveWrapperEnumMap.put(Byte.TYPE, CsvPrimitiveEnum.BYTE);
         primitiveWrapperEnumMap.put(Character.TYPE, CsvPrimitiveEnum.CHARACTER);
         primitiveWrapperEnumMap.put(Short.TYPE, CsvPrimitiveEnum.SHORT);
         primitiveWrapperEnumMap.put(Integer.TYPE, CsvPrimitiveEnum.INTEGER);
         primitiveWrapperEnumMap.put(Float.TYPE, CsvPrimitiveEnum.FLOAT);
         primitiveWrapperEnumMap.put(Long.TYPE, CsvPrimitiveEnum.LONG);
         primitiveWrapperEnumMap.put(Double.TYPE, CsvPrimitiveEnum.DOUBLE);
         
         primitiveWrapperEnumMap.put(Boolean.class, CsvPrimitiveEnum.BOOLEAN);
         primitiveWrapperEnumMap.put(Byte.class, CsvPrimitiveEnum.BYTE);
         primitiveWrapperEnumMap.put(Character.class, CsvPrimitiveEnum.CHARACTER);
         primitiveWrapperEnumMap.put(Short.class, CsvPrimitiveEnum.SHORT);
         primitiveWrapperEnumMap.put(Integer.class, CsvPrimitiveEnum.INTEGER);
         primitiveWrapperEnumMap.put(Float.class, CsvPrimitiveEnum.FLOAT);
         primitiveWrapperEnumMap.put(Long.class, CsvPrimitiveEnum.LONG);
         primitiveWrapperEnumMap.put(Double.class, CsvPrimitiveEnum.DOUBLE);
         primitiveWrapperEnumMap.put(String.class, CsvPrimitiveEnum.STRING);
    }
    
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
    	return clazz.isPrimitive() || primitiveWrapperMap.containsKey(clazz);
    }
    
    public static Object transform(Class<?> clazz, String value) throws IllegalArgumentException {
    	CsvPrimitiveEnum csvPrimitiveEnum = primitiveWrapperEnumMap.get(clazz);
    	if(csvPrimitiveEnum == null) {
    		throw new IllegalArgumentException("value:" + value + "," + clazz.getCanonicalName() + " not supported, please implement custom deserialization convertor for this field");
    	}
    	switch (csvPrimitiveEnum) {
			case BOOLEAN: return Boolean.parseBoolean(value);
			case BYTE: return Byte.parseByte(value);
			case CHARACTER: return value.charAt(0);
			case SHORT: return Short.parseShort(value);
			case INTEGER: return Integer.parseInt(value);
			case FLOAT: return Float.parseFloat(value);
			case LONG: return Long.parseLong(value);
			case DOUBLE: return Double.parseDouble(value);
			case STRING: return value;
			default: return value;
			}
    }

    public static String[] filterRepeatAndEmptyValue(String[] arg0) {
        String[] array = new String[arg0.length];
        int count = 0;
        for (String str : arg0) {
            if("".equals(str.trim()))
                    continue;
            array[count++] = str;
        }
        String[] array2 = new String[count];
        System.arraycopy(array, 0, array2, 0, count);
        return array2;
    }
    
    public static Field[] filterField(Field[] arg0, Field[] arg1) {
        Field[] result = new Field[arg0.length - arg1.length];
        for (int i = 0, index = 0; i < arg0.length; i++) {
                Field f = arg0[i];
                int j = 0;
                label:for (; j < arg1.length; j++) {
                        Field f2 = arg1[j];
                        if(f.getName().equals(f2.getName())) {
                            break label;
                        }
                      }
                if(j == arg1.length){
                    result[index ++] = f;
                }
            }
        return result;
    }
    
    public static <T> CsvMetaNode<?>[] convertFieldsToCsvMetaNodes(CsvMetaNode<T> csvMetaNode, Field[] fields) {
        CsvMetaNode<?>[] csvMetaNodes = new CsvMetaNode<?>[fields.length];
        int childDepth = csvMetaNode.getDepth() + 1;
        for (int i = 0; i < fields.length; i++) {
            CsvMetaInfo<Object> csvMetaInfo = new CsvMetaInfo<Object>(fields[i]);
            csvMetaNodes[i] = new CsvMetaNode<Object>(csvMetaInfo, csvMetaNode, null, childDepth);
        }
        return csvMetaNodes;
    }
    
    public static int convertCsvMetaTreeIntoArray(CsvMetaNode<?> csvMetaNode, CsvMetaNode<?>[] nodes, int index) {
        CsvMetaNode<?>[] csvMetaNodes = csvMetaNode.getChilds();
        if(csvMetaNodes != null) {
            for (CsvMetaNode<?> node : csvMetaNodes) {
               index = convertCsvMetaTreeIntoArray(node, nodes, index++);
            }
        }else {
            nodes[index++] = csvMetaNode;
        }
        return index;
    }

}
