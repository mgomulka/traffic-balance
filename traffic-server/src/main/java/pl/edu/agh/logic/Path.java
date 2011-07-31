package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.List;

import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.utils.Collections;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

public class Path {

	private List<PointMatching> matchings;
	private double cost;

	private Path(Coordinate point, Road road, Date time) {
		matchings = Lists.<PointMatching> newArrayList(new PointMatching(point, road, time));
		cost = calculateCost(matchings);
	}
	
	private Path(List<PointMatching> matchings) {
		this.matchings = Lists.<PointMatching> newArrayList(matchings);
		cost = calculateCost(matchings);
	}

	private Path(Path other) {
		this.matchings = newArrayList(other.matchings);
		this.cost = other.cost;
	}

	private Road getLastRoad() {
		return matchings.get(matchings.size() - 1).getRoad();
	}

	private Coordinate getLastPoint() {
		return matchings.get(matchings.size() - 1).getPoint();
	}

	public double getCost() {
		return cost;
	}

	public double getRelativeCost() {
		return calculateCost(matchings) / matchings.size();
	}
	
	public double getRelativeCostOfLastMatchings(int matchingsNumber) {
		int number = Math.min(matchingsNumber, matchings.size());
		
		return calculateCost(matchings.subList(matchings.size() - number, matchings.size())) / number;
	}

	private double calculateCost(List<PointMatching> calculatedMatchings) {
		double result = 0;

		for (PointMatching matching : calculatedMatchings) {
			result += matching.getCost();
		}
		
		return result;
	}

	public Path copy() {
		return new Path(this);
	}

	public static Path createPath(Coordinate point, Road road, Date time) {
		return new Path(point, road, time);
	}
	
	public static Path createPath(List<PointMatching> matchings) {
		return new Path(matchings);
	}

	public Path matchPointToLastRoad(Coordinate point, Date time) {
		return matchPointToRoad(point, getLastRoad(), time);
	}

	public Path matchPointToRoad(Coordinate point, Road road, Date time) {
		PointMatching matching = new PointMatching(point, road, time);
		matchings.add(matching);
		cost += matching.getCost();

		return this;
	}

	public boolean hasPointReachedEndOfLastRoad(Coordinate point, double toleranceFactor) {
		double lastRoadLength = getLastRoad().getLength();
		double distanceBetweenPointsMatchedToLastRoad = calculateDistanceBetweenPointsMatchedToLastRoad();
		return (distanceBetweenPointsMatchedToLastRoad + getLastPoint().distance(point)) > (toleranceFactor * lastRoadLength);
	}

	private double calculateDistanceBetweenPointsMatchedToLastRoad() {
		double distance = 0.0;

		Road lastRoad = getLastRoad();
		for (int i = matchings.size() - 2; i >= 0; i--) {
			if (!matchings.get(i).getRoad().equals(lastRoad)) {
				return distance;
			}

			distance += matchings.get(i).getPoint().distance(matchings.get(i + 1).getPoint());
		}

		return distance;
	}

	public int getPointsNumber() {
		return matchings.size();
	}

	public int getUniqueSegmentsNumber() {
		return Collections.getNumberOfElementsWithoutAdjacentDuplicates(Lists.transform(matchings,
				new Function<PointMatching, Road>() {

					@Override
					public Road apply(PointMatching matching) {
						return matching.getRoad();
					}
				}));
	}

	public int getSource() {
		return matchings.get(0).getRoad().getSource();
	}

	public int getTarget() {
		return getLastRoad().getTarget();
	}

	public RoutingResult toRoutingResult() {
		List<Coordinate> coords = newArrayList();
		for (PointMatching match : matchings) {
			coords.add(match.getRoad().nearestPoint(match.getPoint()));
		}
		
		coords = Collections.removeAdjacentDuplicates(coords);
		RoutingResult routingResult = new RoutingResult(Lists.transform(coords,
				new Function<Coordinate, SimpleLocationInfo>() {
					@Override
					public SimpleLocationInfo apply(Coordinate input) {
						return new SimpleLocationInfo(input.x, input.y);
					}
				}));

		List<SimpleLocationInfo> matchedPoints = newArrayList();
		for (PointMatching matching : matchings) {
			Coordinate np = matching.getRoad().nearestPoint(matching.getPoint());
			matchedPoints.add(new SimpleLocationInfo(np.x, np.y));
		}

		routingResult.setMatchedPoints(matchedPoints);
		return routingResult;
	}

	public List<PointMatching> getMatchings() {
		return java.util.Collections.unmodifiableList(matchings);
	}

}