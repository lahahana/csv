package com.alpha.csv.resolver;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.alpha.csv.annotations.CsvIgnore;
import com.alpha.csv.annotations.CsvIgnoreProperties;
import com.alpha.csv.exceptions.CsvException;
import com.alpha.csv.util.Utils;

public class PropertyResolver {

    public static Field[] filterIgnoreProperties(final Class<?> clazz, Field[] arg) throws CsvException {
        try{
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
                    ignoreAnnotatedFields[length] = f;
                    length ++;
                }
            }
            arg = Utils.filterField(arg, Arrays.copyOf(ignoreAnnotatedFields, length));
            return arg;
        }catch(Exception e) {
            throw new CsvException(e);
        }
    }
}
