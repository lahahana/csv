package com.alpha.csv.util;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

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
}
