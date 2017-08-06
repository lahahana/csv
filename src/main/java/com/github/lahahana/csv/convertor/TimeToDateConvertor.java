package com.github.lahahana.csv.convertor;

import java.util.Date;

public class TimeToDateConvertor implements Convertor<Long, Date>{

    public Date convert(Long millseconds) {
        Date date = new Date(millseconds.longValue());
        return date;
    }

}
