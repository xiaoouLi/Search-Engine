// Xiaoou, Dec,2013
// xli65@usfca.edu

package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xli65
 */
public class DayTimeHelper {

    /**
     * Returns the date and time in a long format. For example: "12:00 am on Saturday, January 01 2000".
     * 
     * This function is from https://github.com/cs212/demos/blob/master/Dynamic%20HTML/TodayServer.java
     * 
     * @author Sophie Engle
     * 
     * @return current date and time
     */
    public static String getDate() {
        String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }
}
