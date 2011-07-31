package pl.edu.agh.utils;

public class TimeDifference {

	private static final double MILLIS_IN_SECOND = 1000.0;
	private static final double MILLIS_IN_MINUTE = 60000.0;
	private static final double MILLIS_IN_HOUR = 3600000.0;
	
	private long difference;

	TimeDifference(long difference) {
		this.difference = difference;
	}
	
	public long getDifference() {
		return difference;
	}

	public double toMillis() {
		return difference;
	}

	public double toSeconds() {
		return difference / MILLIS_IN_SECOND;
	}

	public double toMinutes() {
		return difference / MILLIS_IN_MINUTE;
	}

	public double toHours() {
		return difference / MILLIS_IN_HOUR;
	}
	
	public TimeDifference multiplyBy(double multiplier) {
		return new TimeDifference(Math.round(difference * multiplier));
	}

}
