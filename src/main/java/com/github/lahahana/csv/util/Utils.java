package com.github.lahahana.csv.util;

import com.github.lahahana.csv.base.CsvMetaInfo;
import com.github.lahahana.csv.base.CsvMetaNode;

import java.lang.reflect.Field;

public class Utils {

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
    
    public static CsvMetaNode[] convertFieldsToCsvMetaNodes(CsvMetaNode parent, Field[] fields) {
        CsvMetaNode[] csvMetaNodes = new CsvMetaNode[fields.length];
        int childDepth = parent.getDepth() + 1;
        for (int i = 0; i < fields.length; i++) {
            CsvMetaInfo csvMetaInfo = new CsvMetaInfo(fields[i]);
            csvMetaNodes[i] = new CsvMetaNode(csvMetaInfo, parent, null);
            csvMetaNodes[i].setDepth(childDepth);
        }
        return csvMetaNodes;
    }
    
    public static int convertCsvMetaTreeIntoArray(CsvMetaNode csvMetaNode, CsvMetaNode[] nodes, int index) {
        CsvMetaNode[] csvMetaNodes = csvMetaNode.getChilds();
        if(csvMetaNodes != null) {
            for (CsvMetaNode node : csvMetaNodes) {
               index = convertCsvMetaTreeIntoArray(node, nodes, index++);
            }
        }else {
            nodes[index++] = csvMetaNode;
        }
        return index;
    }

}
