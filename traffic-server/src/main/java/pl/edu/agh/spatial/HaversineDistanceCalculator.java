package pl.edu.agh.spatial;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class HaversineDistanceCalculator {

	private static final double EARTH_RADIUS = 6371000;

	public static final HaversineDistanceCalculator EARTH_DISTANCE_CALCULATOR = new HaversineDistanceCalculator(EARTH_RADIUS);

	private double radius;

	public HaversineDistanceCalculator(double radius) {
		this.radius = radius;
	}

	public double distance(Coordinate from, Coordinate to) {
		double latitudeDelta = toRadians(from.y - to.y);
		double longitudeDelta = toRadians(from.x - to.x);

		double haversin = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) + cos(toRadians(from.y))
				* cos(toRadians(to.y)) * sin(longitudeDelta / 2) * sin(longitudeDelta / 2);
		return 2 * radius * asin(sqrt(haversin));
	}
	
	public double distance(Point from, Point to) {
		return distance(from.getCoordinate(), to.getCoordinate());
	}
}
