package com.alpha.csv.annotations.convertor;

import java.util.Date;

public class TimeToDateConvertor implements Converter<Date, Long>{

    public Date convert(Long millseconds) {
        Date date = new Date(millseconds.longValue());
        return date;
    }

}
