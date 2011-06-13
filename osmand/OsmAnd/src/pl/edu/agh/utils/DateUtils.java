package pl.edu.agh.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static Date add(Date date, int field, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, value);
		
		return calendar.getTime();
	}

}
