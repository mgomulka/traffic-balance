package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.Date;
import java.util.List;

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
		return (distanceBetweenPointsMatchedToLastRoad + EARTH_DISTANCE_CALCULATOR.distance(getLastPoint(), point)) > (toleranceFactor * lastRoadLength);
	}

	private double calculateDistanceBetweenPointsMatchedToLastRoad() {
		double distance = 0.0;

		Road lastRoad = getLastRoad();
		for (int i = matchings.size() - 2; i >= 0; i--) {
			if (!matchings.get(i).getRoad().equals(lastRoad)) {
				return distance;
			}

			distance += EARTH_DISTANCE_CALCULATOR
					.distance(matchings.get(i).getPoint(), matchings.get(i + 1).getPoint());
		}

		return distance;
	}

	public int getPointsNumber() {
		return matchings.size();
	}

	public int getSource() {
		return matchings.get(0).getRoad().getSource();
	}

	public int getTarget() {
		return getLastRoad().getTarget();
	}

	public List<PointMatching> getMatchings() {
		return java.util.Collections.unmodifiableList(matchings);
	}

	public Coordinate getStartPoint() {
		return matchings.get(0).getPoint();
	}

	public Coordinate getEndPoint() {
		return getLastPoint();
	}

}
