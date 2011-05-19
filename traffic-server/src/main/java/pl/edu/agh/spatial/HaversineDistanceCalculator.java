package pl.edu.agh.spatial;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import com.vividsolutions.jts.geom.Point;

public class HaversineDistanceCalculator {

	private static final double EARTH_RADIUS = 6371.0;

	public static final HaversineDistanceCalculator EARTH_DISTANCE_CALCULATOR = new HaversineDistanceCalculator(EARTH_RADIUS);

	private double radius;

	public HaversineDistanceCalculator(double radius) {
		this.radius = radius;
	}

	public double distance(Point from, Point to) {
		double latitudeDelta = toRadians(from.getY() - to.getY());
		double longitudeDelta = toRadians(from.getX() - to.getX());

		double haversin = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) + cos(toRadians(from.getY()))
				* cos(toRadians(to.getY())) * sin(longitudeDelta / 2) * sin(longitudeDelta / 2);
		return 2 * radius * asin(sqrt(haversin));
	}
}
