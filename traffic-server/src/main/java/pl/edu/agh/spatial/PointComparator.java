package pl.edu.agh.spatial;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class PointComparator implements Comparator<Point> {

	public static final PointComparator COMPARATOR = new PointComparator();
	
	@Override
	public int compare(Point point1, Point point2) {
		Coordinate coord1 = point1.getCoordinate();
		Coordinate coord2 = point2.getCoordinate();

		return (coord1.x != coord2.x) ? Double.compare(coord1.x, coord2.x) : Double.compare(coord1.y, coord2.y);
	}

}
