package com.valor.mercury.manager.tool;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

public class DateUtils {

    public static SimpleDateFormat sdf = new SimpleDateFormat("dd-HH");
    public static SimpleDateFormat localSDF = new SimpleDateFormat("yyyy/MM/ddHH");

    public static String getDateStr(Date date) {
        return sdf.format(date);
    }


    public static Date parseStringToDate(String format, String parameterValue) throws Exception{
        localSDF.applyPattern(format);
        return localSDF.parse(parameterValue);
    }

    public static String parseDateToString(String format, Date date) throws Exception{
        localSDF.applyPattern(format);
        return localSDF.format(date);
    }
}
