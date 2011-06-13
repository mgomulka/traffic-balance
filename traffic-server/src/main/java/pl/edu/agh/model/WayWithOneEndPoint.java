package pl.edu.agh.model;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.List;

import pl.edu.agh.model.entity.Way;
import pl.edu.agh.spatial.LineSegmentList;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class WayWithOneEndPoint extends WayWithEndPoints {

	protected Point point;

	public WayWithOneEndPoint(Way way, Point point) {
		super(way, point.getFactory());
		this.point = point;
	}

	protected Integer getNearerVertexNumber() {
		return EARTH_DISTANCE_CALCULATOR.distance(way.getStartPoint(), point) < EARTH_DISTANCE_CALCULATOR.distance(
				way.getEndPoint(), point) ? way.getSource() : way.getTarget();
	}
	
	public abstract Integer getWayEndPoint();
	
	public Integer getWayEndPointOppositeTo(Integer endPoint) {
		if (endPoint.equals(way.getSource())) {
			return way.getTarget();
		} else if (endPoint.equals(way.getTarget())) {
			return way.getSource();
		} else {
			throw new IllegalArgumentException("Passed endPoint is neither start nor end of way");
		}
	}

	public List<Point> createRouteFromIndexToPoint(Integer index) {
		if (way.getSource().equals(index)) {
			return createWayFromSource();
		} else if (way.getTarget().equals(index)) {
			return createWayFromTarget();
		} else
			throw new IllegalArgumentException("Passed index is neither start nor end of way");
	}

	public List<Point> createRouteFromPointToIndex(Integer index) {
		return Lists.reverse(createRouteFromIndexToPoint(index));
	}

	private List<Point> createWayFromTarget() {
		LineSegmentList lines = new LineSegmentList((LineString) way.getLineString().reverse());
		LineSegment nearestLine = findNearestLine(point, lines);
		return createWayFromPoint(lines, nearestLine);
	}

	private List<Point> createWayFromSource() {
		LineSegmentList lines = new LineSegmentList(way.getLineString());
		LineSegment nearestLine = findNearestLine(point, lines);
		return createWayFromPoint(lines, nearestLine);
	}

	private List<Point> createWayFromPoint(LineSegmentList lines, LineSegment nearestLine) {
		List<Point> route = newArrayList();
		for (LineSegment line : lines) {
			route.add(geometryFactory.createPoint(line.p0));
			if (line.equals(nearestLine)) {
				break;
			}
		}
		route.add(geometryFactory.createPoint(nearestLine.closestPoint(point.getCoordinate())));
		return route;
	}

}
