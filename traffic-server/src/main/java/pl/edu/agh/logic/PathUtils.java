package pl.edu.agh.logic;

import static com.google.common.collect.Sets.newHashSet;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import pl.edu.agh.utils.Collections;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
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
	
	public static ListIterator<PointMatching> iteratorForPreviousRoadMatching(ListIterator<PointMatching> current,
			List<PointMatching> matchings) {
		final Road currentRoad = matchings.get(current.previousIndex()).getRoad();

		ListIterator<PointMatching> previousRoadMatching = matchings.listIterator(current.previousIndex());
		previousRoadMatching = Collections.moveIteratorToPreviousElementMatchingPredicate(previousRoadMatching,
				new Predicate<PointMatching>() {
					@Override
					public boolean apply(PointMatching matching) {
						return !currentRoad.equals(matching.getRoad());
					}
				});
		return previousRoadMatching;
	}
	
	public static ListIterator<PointMatching> nextNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings) {
		return nextNonUTurnRoad(current, matchings, false);
	}

	private static ListIterator<PointMatching> nextNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings, boolean isCurrentUTurn) {
		if (!current.hasNext()) {
			return current;
		}

		ListIterator<PointMatching> nextRoadMatching = PathUtils.iteratorForNextRoadMatching(current, matchings);

		if (nextRoadMatching.hasNext()
				&& isUTurn(matchings.get(current.nextIndex()).getRoad(), matchings.get(nextRoadMatching.nextIndex())
						.getRoad())) {
			return nextNonUTurnRoad(nextRoadMatching, matchings, true);
		}

		return isCurrentUTurn ? nextRoadMatching : current;
	}
	
	public static ListIterator<PointMatching> previousNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings) {
		return previousNonUTurnRoad(current, matchings, false);
	}

	private static ListIterator<PointMatching> previousNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings, boolean isCurrentUTurn) {
		if (!current.hasPrevious()) {
			return current;
		}

		ListIterator<PointMatching> previousRoadMatching = PathUtils.iteratorForPreviousRoadMatching(current, matchings);

		if (previousRoadMatching.hasPrevious()
				&& isUTurn(matchings.get(current.previousIndex()).getRoad(), matchings.get(previousRoadMatching.previousIndex())
						.getRoad())) {
			return previousNonUTurnRoad(previousRoadMatching, matchings, true);
		}

		return isCurrentUTurn ? previousRoadMatching : current;
	}

	private static boolean isUTurn(Road road1, Road road2) {
		return (road1.getId() == road2.getId()) && (road1.isReversed() != road2.isReversed());
	}
	
	public static ListIterator<PointMatching> nextUTurnRoad(ListIterator<PointMatching> current, List<PointMatching> matchings) {
		if (!current.hasNext()) {
			return current;
		}

		ListIterator<PointMatching> nextRoadMatching = PathUtils.iteratorForNextRoadMatching(current, matchings);

		if (!nextRoadMatching.hasNext()) {
			return nextRoadMatching;
		}

		return (isUTurn(matchings.get(current.nextIndex()).getRoad(), matchings.get(nextRoadMatching.nextIndex())
				.getRoad())) ? current : nextUTurnRoad(nextRoadMatching, matchings);
	}
	
	public static ListIterator<PointMatching> previousUTurnRoad(ListIterator<PointMatching> current, List<PointMatching> matchings) {
		if (!current.hasPrevious()) {
			return current;
		}

		ListIterator<PointMatching> previousRoadMatching = PathUtils.iteratorForPreviousRoadMatching(current, matchings);

		if (!previousRoadMatching.hasPrevious()) {
			return previousRoadMatching;
		}

		return (isUTurn(matchings.get(current.previousIndex()).getRoad(), matchings.get(previousRoadMatching.previousIndex())
				.getRoad())) ? current : previousUTurnRoad(previousRoadMatching, matchings);
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

	public static double getDistanceBetweenPoints(List<Coordinate> points) {
		double distance = 0.0;

		if (points.isEmpty()) {
			return distance;
		}

		for (int i = 1; i < points.size(); i++) {
			distance += EARTH_DISTANCE_CALCULATOR.distance(points.get(i - 1), points.get(i));
		}

		return distance;
	}

	public static boolean pathContainsOneDirectedRoad(Path path) {
		int numberOfDirectedSegments = Collections.getNumberOfElementsWithoutAdjacentDuplicates(Lists.transform(
				path.getMatchings(), new Function<PointMatching, Road>() {

					@Override
					public Road apply(PointMatching matching) {
						return matching.getRoad();
					}
				}));
		return numberOfDirectedSegments == 1;
	}

	public static boolean pathContainsOneUndirectedRoad(Path path) {
		List<Road> segments = Lists.transform(path.getMatchings(), new Function<PointMatching, Road>() {
			@Override
			public Road apply(PointMatching pointMatching) {
				return pointMatching.getRoad();
			}
		});

		int numberOfUndirectedSegments = Collections.getNumberOfElementsWithoutAdjacentDuplicates(segments,
				new Comparator<Road>() {
					@Override
					public int compare(Road road1, Road road2) {
						return road1.getId() - road2.getId();
					}

				});
		return numberOfUndirectedSegments == 1;
	}
}
