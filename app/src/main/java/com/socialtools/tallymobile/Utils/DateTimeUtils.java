package com.socialtools.tallymobile.Utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

        public static String convertToReadableDateTime(String dateTimeString) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd:MMM:yy HH:mm:ss a", Locale.getDefault());
            try {
                Date date = inputFormat.parse(dateTimeString);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

}
