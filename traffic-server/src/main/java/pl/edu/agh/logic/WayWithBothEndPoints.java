package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import pl.edu.agh.model.Way;
import pl.edu.agh.spatial.LineSegmentList;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

public class WayWithBothEndPoints extends WayWithEndPoints {

	private Point start;
	private Point end;

	public WayWithBothEndPoints(Way way, Point start, Point end) {
		super(way, start.getFactory());

		this.start = start;
		this.end = end;
	}
	
	public boolean isValid() {
		return !way.isOneWay()
				|| EARTH_DISTANCE_CALCULATOR.distance(way.getStartPoint(), start) <= EARTH_DISTANCE_CALCULATOR.distance(
						way.getStartPoint(), end);
	}

	public List<Point> createRoute() {
		LineSegmentList lines = new LineSegmentList(way.getLineString());
		LineSegment startNearestLine = lines.nearestLine(start);
		LineSegment endNearestLine = lines.nearestLine(end);

		if (startNearestLine.equals(endNearestLine)) {
			return newArrayList(geometryFactory.createPoint(startNearestLine.closestPoint(start.getCoordinate())),
					geometryFactory.createPoint(endNearestLine.closestPoint(end.getCoordinate())));
		}

		ListIterator<LineSegment> linesIterator = lines.listIterator();

		LineSegment routeStart = skipLinesOutsideRoute(linesIterator, newHashSet(startNearestLine, endNearestLine));
		boolean correctlyDirected = routeStart.equals(startNearestLine);

		List<Point> points = newArrayList();
		addPointsInsideRoute(linesIterator, newHashSet(startNearestLine, endNearestLine), points);

		if (!correctlyDirected) {
			points = Lists.reverse(points);
		}

		points.add(0, geometryFactory.createPoint(startNearestLine.closestPoint(start.getCoordinate())));
		points.add(geometryFactory.createPoint(endNearestLine.closestPoint(end.getCoordinate())));

		return points;
	}

	private LineSegment skipLinesOutsideRoute(ListIterator<LineSegment> linesIterator, Set<LineSegment> edgeLines) {
		LineSegment segment = linesIterator.next();
		while (!edgeLines.contains(segment)) {
			segment = linesIterator.next();
		}

		linesIterator.previous();
		return linesIterator.next();
	}

	private void addPointsInsideRoute(ListIterator<LineSegment> linesIterator, Set<LineSegment> edgeLines,
			List<Point> points) {
		LineSegment segment;
		do {
			segment = linesIterator.next();
			points.add(geometryFactory.createPoint(segment.p0));
		} while (!edgeLines.contains(segment));
	}
}
