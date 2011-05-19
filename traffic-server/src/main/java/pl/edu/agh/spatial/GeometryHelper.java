package pl.edu.agh.spatial;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeometryHelper {

	public static List<Point> getPointsFromGeometry(Geometry geometry, GeometryFactory geometryFactory) {
		List<Point> points = newArrayList();
		
		for (Coordinate coordinate : geometry.getCoordinates()) {
			points.add(geometryFactory.createPoint(coordinate));
		}
		
		return points;
	}
	
	public static List<Point> getPointsFromGeometry(Geometry geometry) {
		return getPointsFromGeometry(geometry, geometry.getFactory());
	}
}
