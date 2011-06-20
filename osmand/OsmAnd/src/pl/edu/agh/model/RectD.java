package pl.edu.agh.model;

import android.graphics.RectF;

public class RectD {

	private double left;
	private double top;
	private double right;
	private double bottom;

	public RectD(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public RectD(RectF other) {
		this(other.left, other.top, other.right, other.bottom);
	}

	public double getLeft() {
		return left;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public boolean contains(RectD other) {
		return this.left <= other.left && this.top >= other.top && this.right >= other.right
				&& this.bottom <= other.bottom;
	}

	public boolean contains(SimpleLocationInfo point) {
		return this.left <= point.getLongitude() && this.right >= point.getLongitude()
				&& this.top >= point.getLatitude() && this.bottom <= point.getLatitude();
	}

	public RectD expandByDistance(double distance) {
		left -= distance;
		right += distance;
		top += distance;
		bottom -= distance;
		
		return this;
	}
	
	public RectD expandByMultiplier(double multiplier) {
		double centerX = (left + right) / 2;
		double centerY = (bottom + top) / 2;
		
		left = centerX + (left - centerX) * multiplier;
		right = centerX + (right - centerX) * multiplier;
		top = centerY + (top - centerY) * multiplier;
		bottom = centerY + (bottom - centerY) * multiplier;
		
		return this;
	}

}
