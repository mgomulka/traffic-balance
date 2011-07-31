package pl.edu.agh.logic;

import static com.google.common.collect.Sets.newHashSet;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import pl.edu.agh.utils.Collections;

import com.google.common.base.Predicate;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class PathUtils {

	public static ListIterator<PointMatching> iteratorForNextRoadMatching(ListIterator<PointMatching> current,
			List<PointMatching> matchings) {
		final Road currentRoad = matchings.get(current.nextIndex()).getRoad();

		ListIterator<PointMatching> nextRoadMatching = matchings.listIterator(current.nextIndex());
		nextRoadMatching = Collections.moveIteratorToNextElementMatchingPredicate(nextRoadMatching,
				new Predicate<PointMatching>() {
					@Override
					public boolean apply(PointMatching matching) {
						return !currentRoad.equals(matching.getRoad());
					}
				});
		return nextRoadMatching;
	}

	public static final double getDistanceBetweenPointMatchings(PointMatching matching1, PointMatching matching2) {
		if (!matching1.getRoad().equals(matching2.getRoad())) {
			throw new IllegalArgumentException("Cannot calculate distance bewteen matchings to different roads!");
		}

		Road road = matching1.getRoad();
		Coordinate point1 = matching1.getPoint();
		Coordinate point2 = matching2.getPoint();

		LineSegment matching1NearestLine = road.nearestLine(point1);
		LineSegment matching2NearestLine = road.nearestLine(point2);

		if (matching1NearestLine.equals(matching2NearestLine)) {
			return EARTH_DISTANCE_CALCULATOR.distance(road.nearestPoint(point1), road.nearestPoint(point2));
		}

		double distance = 0.0;
		ListIterator<LineSegment> segmentsIterator = road.listIterator();
		HashSet<LineSegment> edgeSegments = newHashSet(matching1NearestLine, matching2NearestLine);

		LineSegment startSegment = skipLinesOutsideRoute(segmentsIterator, edgeSegments);
		distance += (startSegment.equals(matching1NearestLine)) ? EARTH_DISTANCE_CALCULATOR.distance(
				road.nearestPoint(point1), startSegment.p1) : EARTH_DISTANCE_CALCULATOR.distance(
				road.nearestPoint(point2), startSegment.p1);

		LineSegment endSegment;
		while (true) {
			LineSegment nextSegment = segmentsIterator.next();

			if (!edgeSegments.contains(nextSegment)) {
				distance += EARTH_DISTANCE_CALCULATOR.distance(nextSegment.p0, nextSegment.p1);
			} else {
				endSegment = nextSegment;
				break;
			}
		}

		distance += (endSegment.equals(matching1NearestLine)) ? EARTH_DISTANCE_CALCULATOR.distance(
				road.nearestPoint(point1), endSegment.p0) : EARTH_DISTANCE_CALCULATOR.distance(
				road.nearestPoint(point2), endSegment.p0);

		return distance;
	}

	private static LineSegment skipLinesOutsideRoute(ListIterator<LineSegment> linesIterator, Set<LineSegment> edgeLines) {
		LineSegment segment = linesIterator.next();
		while (!edgeLines.contains(segment)) {
			segment = linesIterator.next();
		}

		linesIterator.previous();
		return linesIterator.next();
	}
}
