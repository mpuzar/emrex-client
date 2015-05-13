package eu.emrex.client.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.inject.Named;

@Named("tu")
public class TekstUtil {

    private static final String DATEFORMAT_STR = "dd.MM.yyyy";


    public static String convertDateToString(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat ft = new SimpleDateFormat(DATEFORMAT_STR);
        return ft.format((Date) date);
    }


    public static Date convertStringToDate(String dateStr) {
        if (dateStr == null)
            return null;
        dateStr = dateStr.trim();
        if (!dateStr.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}"))
            return null;
        String[] dmy = dateStr.split("\\.");
        int dd = (new Integer(dmy[0])).intValue();
        if ((dd < 1) || (dd > 31))
            return null;
        int mm = (new Integer(dmy[1])).intValue();
        if ((mm < 1) || (mm > 12))
            return null;
        int yyyy = (new Integer(dmy[2])).intValue();
        if ((yyyy < 1000) || (yyyy > 9999))
            return null;
        SimpleDateFormat ft = new SimpleDateFormat(DATEFORMAT_STR);
        try {
            return ft.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }


    public static String convertTimeToString(Date date) {
        if (date == null) {
            date = now();
        }
        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return ft.format((Date) date);
    }


    public static String convertTimeToStringWithSeconds(Date date) {
        if (date == null) {
            date = now();
        }
        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return ft.format((Date) date);
    }


    public static String convertTimeToISOWithSecondsFilenameSafe(Date date) {
        if (date == null) {
            date = now();
        }
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return ft.format((Date) date);
    }


    public static Date now() {
        long timeInMillis = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);

        return cal.getTime();
    }


    public static String join(Set<String> list, boolean withSpace) {
        String ret = "";
        for (String s : list) {
            if (!ret.isEmpty()) {
                ret += ",";
                if (withSpace) {
                    ret += " ";
                }
            }
            ret += s;
        }
        return ret;
    }


    public static String toCamelCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }


    public static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }


    public static String toSentenceCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    public static String sqlQuote(String verdi) {
        return verdi.replaceAll("'", "''");
    }

}
