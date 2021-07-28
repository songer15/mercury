package com.valor.mercury.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;


/**
 * 通用工具方法类
 */

public class StringTools {


    /**
     *
     * @param s  索引命名策略
     * @param dayOffset  按照当前时间的偏移量，天
     * @return 该索引命名策略下的索引名后缀 e.g. "-2020-5-server"
     */
    public static String getDateStr(GET_DATE_STR s, int dayOffset) {
        String str;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        switch (s) {
            case FIXED:
                return "";
            case WEEK:
                int week = (day / 7) + 1;
                str = "-" + year + "-" + month + "-" + week;
                break;
            case MONTH:
                str = "-" + year + "-" + month;
                break;
            case SEASON:
                int season = month / 4 + 1;
                str = "-" + year + "-" + season;
                break;
            case YEAR:
                str = "-" + year + "";
                break;
            default:
                str = "-" + year + "-" + month + "-" + day;
                break;
        }
        return str + "-server";
    }

    public static String buildErrorMessage(Object object) {
        String msg;
        if(object instanceof Throwable) {
            Throwable t = (Throwable) object;
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw, true));
            msg = sw.getBuffer().toString();
        } else if (object instanceof  Object[]) {
            Object[] objects = (Object[]) object;
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append(o.toString());
                sb.append(" ");
            }
            msg = sb.toString();
        } else {
            msg = object.toString();
        }
        return msg;
    }



    public enum GET_DATE_STR {

        FIXED(0), WEEK(1), MONTH(2), SEASON(3), YEAR(4);

        Integer value;

        private GET_DATE_STR(int value) {
            this.value = value;
        }

        public static GET_DATE_STR valueOf(Integer value) {
            switch (value) {
                case 0:
                    return FIXED;
                case 1:
                    return WEEK;
                case 2:
                    return MONTH;
                case 3:
                    return SEASON;
                case 4:
                    return YEAR;
                default:
                    return SEASON;
            }
        }
    }



}
