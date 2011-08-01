package pl.edu.agh.model;

import android.graphics.Point;

public class Vector {
	
	public static enum Direction {
		LEFT, RIGHT;
	}
	
	private static enum Rotation {
		LEVOROTATORY, DEXTROROTATORY;
	}
	
	private static final Rotation COORDINATE_SYSTEM_ROTATION = Rotation.DEXTROROTATORY;

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
	
	public Vector translatePerpendicularly(double distance, Direction direction) {
		Vector translatingVector;
		
		if (getX() == 0) {
			translatingVector = new Vector((int)Math.round(distance), 0);
		} else if (getY() == 0) {
			translatingVector = new Vector(0, (int)Math.round(distance));
		} else {
			double dy = distance / Math.sqrt((getY() * getY()) / (getX() * getX()) + 1);
			double dx = -(getY() / getX()) * dy;
			translatingVector = new Vector((int) Math.round(dx), (int) Math.round(dy));
		}
		
		if ((determinant(this, translatingVector) > 0) == ((direction == Direction.LEFT) == (COORDINATE_SYSTEM_ROTATION == Rotation.LEVOROTATORY))) {
			return translate(translatingVector);
		} else {
			return translate(translatingVector.reverse());
		}
	}
	
	private int determinant(Vector v1, Vector v2) {
		return v1.getX() * v2.getY() - v1.getY() * v2.getX();
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
