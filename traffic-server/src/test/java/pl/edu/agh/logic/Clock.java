package pl.edu.agh.logic;

import java.util.Calendar;
import java.util.Date;

public class Clock {

	private Calendar time = Calendar.getInstance();

	public Clock() {
	}
	
	public Clock(Date initialTime) {
		time.setTime(initialTime);
	}

	public Date nextSecond() {
		time.add(Calendar.SECOND, 1);
		return time.getTime();
	}

}
