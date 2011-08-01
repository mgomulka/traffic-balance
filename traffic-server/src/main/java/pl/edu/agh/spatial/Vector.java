package pl.edu.agh.spatial;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Vector {

	public static enum Direction {
		LEFT, RIGHT;
	}
	
	private static enum Rotation {
		LEVOROTATORY, DEXTROROTATORY;
	}
	
	private static final Rotation COORDINATE_SYSTEM_ROTATION = Rotation.LEVOROTATORY;

	private GeometryFactory geometryFactory;
	private Point begin;
	private Point end;

	public Vector(Point begin, Point end, GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
		this.begin = (Point) begin.clone();
		this.end = (Point) end.clone();
	}

	public Vector(double dx, double dy, GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
		this.begin = geometryFactory.createPoint(new Coordinate(0, 0));
		this.end = geometryFactory.createPoint(new Coordinate(dx, dy));
	}

	public Point getBegin() {
		return begin;
	}

	public Point getEnd() {
		return end;
	}

	public Vector translate(double dx, double dy) {
		this.begin.getCoordinate().x += dx;
		this.begin.getCoordinate().y += dy;
		this.end.getCoordinate().x += dx;
		this.end.getCoordinate().y += dy;

		return this;
	}

	public Vector translate(Vector other) {
		return translate(other.getX(), other.getY());
	}
	
	public Vector translatePerpendicularly(double distance, Direction direction) {
		if (distance == 0) {
			return this;
		}
		
		Vector translatingVector;
		
		if (getX() == 0) {
			translatingVector = new Vector(distance, 0, geometryFactory);
		} else if (getY() == 0) {
			translatingVector = new Vector(0, distance, geometryFactory);
		} else {
			double dy = distance / Math.sqrt((getY() * getY()) / (getX() * getX()) + 1);
			double dx = -(getY() / getX()) * dy;
			translatingVector = new Vector(dx, dy, geometryFactory);
		}
		
		if ((determinant(this, translatingVector) > 0) == ((direction == Direction.LEFT) == (COORDINATE_SYSTEM_ROTATION == Rotation.LEVOROTATORY))) {
			return translate(translatingVector);
		} else {
			return translate(translatingVector.reverse());
		}
	}
	
	private double determinant(Vector v1, Vector v2) {
		return v1.getX() * v2.getY() - v1.getY() * v2.getX();
	}

	public Vector reverse() {
		Point point = this.begin;
		this.begin = this.end;
		this.end = point;

		return this;
	}

	public double getX() {
		return end.getCoordinate().x - begin.getCoordinate().x;
	}

	public double getY() {
		return end.getCoordinate().y - begin.getCoordinate().y;
	}
}
