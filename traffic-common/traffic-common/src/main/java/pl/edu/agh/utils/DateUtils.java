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
	
	public static Date add(Date date, TimeDifference timeDifference) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MILLISECOND, (int) timeDifference.getDifference());
		
		return calendar.getTime();
	}
	
	public static TimeDifference timeDifference(Date earlier, Date later) {
		return new TimeDifference(later.getTime() - earlier.getTime());
	}

}
