package pl.edu.agh.model;

import pl.edu.agh.model.entity.Way;
import pl.edu.agh.spatial.LineSegmentList;

import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

public abstract class WayWithEndPoints {

	protected Way way;
	protected GeometryFactory geometryFactory;

	protected WayWithEndPoints(Way way, GeometryFactory geometryFactory) {
		this.way = way;
		this.geometryFactory = geometryFactory;
	}
	
	public Way getWay() {
		return way;
	}
	
	protected LineSegment findNearestLine(final Point location, LineSegmentList lines) {
		Ordering<LineSegment> byDistanceToLocation = new Ordering<LineSegment>() {

			@Override
			public int compare(LineSegment left, LineSegment right) {
				return Double
						.compare(left.distance(location.getCoordinate()), right.distance(location.getCoordinate()));
			}
		};

		return byDistanceToLocation.min(lines);
	}

}
