package com.valor.mercury.task.flink.util;

import java.util.Calendar;

/**
 * @author Gavin
 * 2020/3/25 13:18
 */
public class DateStr {

    public enum GET_DATE_STR {

        DAY(0), WEEK(1), MONTH(2), SEASON(3), YEAR(4);

        Integer value;

        private GET_DATE_STR(int value) {
            this.value = value;
        }

        public static GET_DATE_STR valueOf(Integer value) {
            switch (value) {
                case 0:
                    return DAY;
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

    public static String getDateStr(GET_DATE_STR s) {
        String str;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH) + 1;
        int day = calendar.get(calendar.DATE);
        switch (s) {
            case DAY:
                str = "-" + year + "-" + month + "-" + day;
                break;
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
                str = "-" + year;
                break;
            default:
                str = "-" + year + "-" + month + "-" + day;
                break;
        }
        return str;
    }
}
