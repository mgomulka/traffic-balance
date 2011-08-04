package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class UTurnPathCorrector implements PathCorrector {

	private RoadNetwork roadNetwork;

	public UTurnPathCorrector(RoadNetwork roadNetwork) {
		this.roadNetwork = roadNetwork;
	}

	@Override
	public Path correct(Path path) {
		if (PathUtils.pathContainsOneUndirectedRoad(path)) {
			if ((getDistanceBetweenPathPoints(path.getMatchings()) > path.getMatchings().get(0).getRoad().getLength())
					&& !PathUtils.pathContainsOneDirectedRoad(path)) {
				return path;
			} else {
				return correctPathComposedOfOneUndirectedRoad(path);
			}
		}

		return correctPathWithManyUndirectedRoads(path);
	}

	private double getDistanceBetweenPathPoints(List<PointMatching> matchings) {
		List<Coordinate> pathPoints = Lists.transform(matchings, new Function<PointMatching, Coordinate>() {

			@Override
			public Coordinate apply(PointMatching pointMatching) {
				return pointMatching.getPoint();
			}
		});

		return PathUtils.getDistanceBetweenPoints(pathPoints);
	}

	private Path correctPathComposedOfOneUndirectedRoad(Path path) {
		Collection<Road> directedRoadSegments = roadNetwork.getRoadsById(path.getMatchings().get(0).getRoad().getId());

		Iterator<Road> segmentInterator = directedRoadSegments.iterator();

		Road firstDirectionRoad = segmentInterator.next();
		if (!segmentInterator.hasNext()) {
			return path;
		}
		Road secondDirectionRoad = segmentInterator.next();

		Coordinate roadBegin = new LineSegment(firstDirectionRoad.getStartPoint(), secondDirectionRoad.getEndPoint())
				.midPoint();

		Road correctlyDirectedRoad = (EARTH_DISTANCE_CALCULATOR.distance(roadBegin, path.getStartPoint()) < EARTH_DISTANCE_CALCULATOR
				.distance(roadBegin, path.getEndPoint())) ? firstDirectionRoad : secondDirectionRoad;

		return createPathComposedOfOneRoad(path, correctlyDirectedRoad);
	}

	private Path createPathComposedOfOneRoad(Path path, final Road road) {
		List<PointMatching> newPathMatchings = Lists.transform(path.getMatchings(),
				new Function<PointMatching, PointMatching>() {
					@Override
					public PointMatching apply(PointMatching matching) {
						return new PointMatching(matching.getPoint(), road, matching.getTime());
					}
				});

		return Path.createPath(newPathMatchings);
	}

	private Path correctPathWithManyUndirectedRoads(Path path) {
		List<PointMatching> matchings = newArrayList(path.getMatchings());
		matchings = correctUTurnAtBeginning(matchings);
		matchings = correctUTurnAtEnd(matchings);

		return Path.createPath(matchings);
	}

	private List<PointMatching> correctUTurnAtBeginning(List<PointMatching> matchings) {
		ListIterator<PointMatching> firstMatching = matchings.listIterator();
		ListIterator<PointMatching> firstNonUTurnMatching = PathUtils.nextNonUTurnRoad(firstMatching, matchings);

		if (firstMatching.nextIndex() == firstNonUTurnMatching.nextIndex()) {
			return matchings;
		}

		Road correctRoad = matchings.get(firstNonUTurnMatching.previousIndex()).getRoad();
		if (getDistanceBetweenPathPoints(matchings.subList(0, firstNonUTurnMatching.nextIndex())) > correctRoad
				.getLength()) {
			return matchings;
		}

		replaceRoadForMatchings(matchings.subList(0, firstNonUTurnMatching.nextIndex()), correctRoad);

		return matchings;
	}

	private List<PointMatching> correctUTurnAtEnd(List<PointMatching> matchings) {
		ListIterator<PointMatching> lastMatching = matchings.listIterator(matchings.size());
		ListIterator<PointMatching> lastNonUTurnMatching = PathUtils.previousNonUTurnRoad(lastMatching, matchings);

		if (lastMatching.previousIndex() == lastNonUTurnMatching.previousIndex()) {
			return matchings;
		}

		Road correctRoad = matchings.get(lastNonUTurnMatching.nextIndex()).getRoad();
		if (getDistanceBetweenPathPoints(matchings.subList(lastNonUTurnMatching.nextIndex(), matchings.size())) > correctRoad
				.getLength()) {
			return matchings;
		}

		replaceRoadForMatchings(matchings.subList(lastNonUTurnMatching.nextIndex(), matchings.size()), correctRoad);

		return matchings;
	}

	private List<PointMatching> replaceRoadForMatchings(List<PointMatching> matchings, Road road) {
		for (int i = 0; i < matchings.size(); i++) {
			PointMatching oldMatching = matchings.get(i);
			matchings.set(i, new PointMatching(oldMatching.getPoint(), road, oldMatching.getTime()));
		}

		return matchings;
	}

}
