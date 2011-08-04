package pl.edu.agh.spatial;

import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Ordering;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.UnmodifiableListIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class LineSegmentList implements Iterable<LineSegment> {

	private ImmutableList<LineSegment> lines;
	private double length;

	public LineSegmentList(LineString lineString) {
		ImmutableList.Builder<LineSegment> listBuilder = new Builder<LineSegment>();

		Coordinate[] coords = lineString.getCoordinates();
		for (int i = 1; i < coords.length; i++) {
			listBuilder.add(new LineSegment(coords[i - 1], coords[i]));
		}

		lines = listBuilder.build();
		calculateLength();
	}

	public LineSegmentList(List<LineString> segments) {
		ImmutableList.Builder<LineSegment> listBuilder = new Builder<LineSegment>();

		for (LineString segment : segments) {
			listBuilder.add(new LineSegment(segment.getStartPoint().getCoordinate(), segment.getEndPoint()
					.getCoordinate()));
		}

		lines = listBuilder.build();
		calculateLength();
	}
	
	private void calculateLength() {
		length = 0.0;
		
		for (LineSegment segment : lines) {
			length += EARTH_DISTANCE_CALCULATOR.distance(segment.p0, segment.p1); 
		}
	}

	@Override
	public UnmodifiableIterator<LineSegment> iterator() {
		return lines.iterator();
	}

	public UnmodifiableListIterator<LineSegment> listIterator() {
		return lines.listIterator();
	}
	
	public LineSegment nearestLine(final Coordinate point) {
		Ordering<LineSegment> byDistanceToLocation = new Ordering<LineSegment>() {

			@Override
			public int compare(LineSegment left, LineSegment right) {
				return Double
						.compare(left.distance(point), right.distance(point));
			}
		};

		return byDistanceToLocation.min(lines);
	}
	
	public LineSegment nearestLine(final Point point) {
		return nearestLine(point.getCoordinate());
	}
	
	public Coordinate nearestPoint(Coordinate point) {
		return nearestLine(point).closestPoint(point);
	}
	
	public Coordinate nearestPoint(Point point) {
		return nearestPoint(point.getCoordinate());
	}
	
	public double getLength() {
		return length;
	}
	
	public Coordinate getStartPoint() {
		return lines.get(0).p0;
	}
	
	public Coordinate getEndPoint() {
		return lines.get(lines.size() - 1).p1;
	}
}
