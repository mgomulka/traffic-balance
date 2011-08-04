package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.utils.DateUtils.timeDifference;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import pl.edu.agh.model.SpeedInfo;
import pl.edu.agh.utils.DateUtils;

public class SpeedInfoProcessor {

	public List<SpeedInfo> retrieveSpeedInfoFromPath(Path path) {
		List<SpeedInfo> infos = newArrayList();

		if (path.getPointsNumber() < 2) {
			return infos;
		}

		List<PointMatching> matchings = path.getMatchings();

		ListIterator<PointMatching> currentRoadMatching = matchings.listIterator();
		IntersectionMatching currentRoadBegin = createIntersectionMatching(currentRoadMatching, matchings);

		while (currentRoadBegin.getNextRoadMatching() != null) {
			ListIterator<PointMatching> nextRoadFirstMatching = PathUtils.iteratorForNextRoadMatching(
					currentRoadMatching, matchings);
			IntersectionMatching nextRoadBegin = createIntersectionMatching(nextRoadFirstMatching, matchings);

			double distance = calculateDistanceOfMatchedRoad(matchings, currentRoadMatching, currentRoadBegin,
					nextRoadFirstMatching, nextRoadBegin);
			double timeDifference = timeDifference(currentRoadBegin.getIntersectionTime(),
					nextRoadBegin.getIntersectionTime()).toSeconds();
			
			if (distance != 0 && timeDifference != 0) {
				infos.add(createSpeedInfo(nextRoadBegin.getPreviousRoadMatching().getRoad(), timeDifference, distance,
						nextRoadBegin.getIntersectionTime()));
			}

			currentRoadMatching = nextRoadFirstMatching;
			currentRoadBegin = nextRoadBegin;
		}

		return infos;

	}

	private IntersectionMatching createIntersectionMatching(ListIterator<PointMatching> intersectionMatching,
			List<PointMatching> matchings) {
		if (!intersectionMatching.hasNext()) {
			return new IntersectionMatching(matchings.get(intersectionMatching.previousIndex()), null);
		}

		if (!intersectionMatching.hasPrevious()) {
			return new IntersectionMatching(null, matchings.get(intersectionMatching.nextIndex()));
		}

		PointMatching lastMatchingOfPreviuosRoad = matchings.get(intersectionMatching.previousIndex());
		PointMatching firstMatchingOfNextRoad = matchings.get(intersectionMatching.nextIndex());

		return createMatchingForIntersection(lastMatchingOfPreviuosRoad, firstMatchingOfNextRoad);
	}

	private double calculateDistanceOfMatchedRoad(List<PointMatching> matchings,
			ListIterator<PointMatching> currentRoadMatching, IntersectionMatching currentRoadBegin,
			ListIterator<PointMatching> nextRoadFirstMatching, IntersectionMatching nextRoadBegin) {
		double distance = 0.0;

		distance += PathUtils.getDistanceBetweenPointMatchings(currentRoadBegin.getNextRoadMatching(),
				matchings.get(currentRoadMatching.nextIndex()));
		distance += getDistanceBetweenPointsMatchedToOneRoad(currentRoadMatching, nextRoadFirstMatching, matchings);
		distance += PathUtils.getDistanceBetweenPointMatchings(currentRoadMatching.next(),
				nextRoadBegin.getPreviousRoadMatching());
		return distance;
	}

	private SpeedInfo createSpeedInfo(Road road, double timeDifference, double distance, Date intersectionTime) {
		SpeedInfo info = new SpeedInfo();
		double speed = distance / timeDifference;

		info.setWayGid(road.getId());
		info.setTime(intersectionTime);
		if (road.isReversed()) {
			info.setReverseWaySpeed(speed);
			info.setReverseDistance(distance);
		} else {
			info.setDirectWaySpeed(speed);
			info.setDirectDistance(distance);
		}

		return info;
	}

	private IntersectionMatching createMatchingForIntersection(PointMatching lastMatchingOfPreviuosRoad,
			PointMatching firstMatchingOfNextRoad) {
		Coordinate intersectionPoint = new LineSegment(lastMatchingOfPreviuosRoad.getRoad().getEndPoint(),
				firstMatchingOfNextRoad.getRoad().getStartPoint()).midPoint();
		Date intersectionTime = calculateIntersectionTime(lastMatchingOfPreviuosRoad, firstMatchingOfNextRoad,
				intersectionPoint);

		return new IntersectionMatching(intersectionPoint, intersectionTime, lastMatchingOfPreviuosRoad.getRoad(),
				firstMatchingOfNextRoad.getRoad());
	}

	private double getDistanceBetweenPointsMatchedToOneRoad(ListIterator<PointMatching> currentRoadMatching,
			ListIterator<PointMatching> nextRoadFirstMatching, List<PointMatching> matchings) {
		double distance = 0.0;

		while (currentRoadMatching.nextIndex() + 1 != nextRoadFirstMatching.nextIndex()) {
			distance += PathUtils.getDistanceBetweenPointMatchings(matchings.get(currentRoadMatching.nextIndex()),
					matchings.get(currentRoadMatching.nextIndex() + 1));
			currentRoadMatching.next();
		}
		return distance;
	}

	private Date calculateIntersectionTime(PointMatching lastMatchingOfPreviuosRoad,
			PointMatching firstMatchingOfNextRoad, Coordinate intersectionPoint) {
		double distanceToIntersection = PathUtils.getDistanceBetweenPointMatchings(lastMatchingOfPreviuosRoad,
				new PointMatching(intersectionPoint, lastMatchingOfPreviuosRoad.getRoad(), null));
		double distanceFromIntersection = PathUtils.getDistanceBetweenPointMatchings(new PointMatching(
				intersectionPoint, firstMatchingOfNextRoad.getRoad(), null), firstMatchingOfNextRoad);

		return DateUtils.add(
				lastMatchingOfPreviuosRoad.getTime(),
				timeDifference(lastMatchingOfPreviuosRoad.getTime(), firstMatchingOfNextRoad.getTime()).multiplyBy(
						distanceToIntersection / (distanceToIntersection + distanceFromIntersection)));
	}
}
