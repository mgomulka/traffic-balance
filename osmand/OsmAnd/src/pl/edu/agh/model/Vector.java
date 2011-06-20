package pl.edu.agh.model;

import android.graphics.Point;

public class Vector {

	private Point begin;
	private Point end;

	public Vector(Point begin, Point end) {
		this.begin = new Point(begin);
		this.end = new Point(end);
	}

	public Vector(int dx, int dy) {
		this.begin = new Point(0, 0);
		this.end = new Point(dx, dy);
	}

	public Point getBegin() {
		return begin;
	}

	public Point getEnd() {
		return end;
	}

	public Vector translate(int dx, int dy) {
		this.begin.offset(dx, dy);
		this.end.offset(dx, dy);

		return this;
	}

	public Vector translate(Vector other) {
		return translate(other.getX(), other.getY());
	}

	public Vector reverse() {
		Point point = this.begin;
		this.begin = this.end;
		this.end = point;

		return this;
	}

	public int getX() {
		return end.x - begin.x;
	}

	public int getY() {
		return end.y - begin.y;
	}

}
