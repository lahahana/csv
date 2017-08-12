package com.github.lahahana.csv.convertor;

import java.util.Date;

public class MilliSecondsToDateFormatConvertor implements SerializationConvertor<Long>{

    public String convert(Long milliseconds) {
        Date date = new Date(milliseconds.longValue());
        return date.toString();
    }

}
