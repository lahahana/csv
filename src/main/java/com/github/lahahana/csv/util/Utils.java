package com.github.lahahana.csv.util;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

import com.github.lahahana.csv.base.CsvMetaInfo;
import com.github.lahahana.csv.base.CsvMetaNode;

public class Utils {

    public static String[] filterRepeatAndEmptyValue(String[] arg0) {
        LinkedHashSet<String> set = new LinkedHashSet<String>(arg0.length);
        for (String str : arg0) {
            if("".equals(str.trim()))
                    continue;
            set.add(str);
        }
        
        return set.toArray(new String[set.size()]);
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
        CsvMetaNode[] childs = csvMetaNode.getChilds();
        if(childs != null) {
            for (CsvMetaNode node : childs) {
               index = convertCsvMetaTreeIntoArray(node, nodes, index++);
            }
        }else {
            nodes[index++] = csvMetaNode;
        }
        return index;
    }
    
    public static int countCsvMetaTree(CsvMetaNode csvMetaNode, int count){
        CsvMetaNode[] childs = csvMetaNode.getChilds();
        if(childs != null) {
            for (CsvMetaNode node : childs) {
                countCsvMetaTree(node, count);
            }
        }else {
            count++;
        }
        return count;
    }
    
    
}
