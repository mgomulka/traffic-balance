package pl.edu.agh.spatial;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

public class HaversineDistanceCalculatorTest {

	private WGS84GeometryFactory geometryFactory = new WGS84GeometryFactory();
	
	@Test
	public void canCalculateDistance() {
		Point from = geometryFactory.createPoint(2.00003, 0.0);
		Point to = geometryFactory.createPoint(2.0, 0.000);
		System.out.println(HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR.distance(from, to));
		System.out.println(from.distance(to));
	}
	
}
