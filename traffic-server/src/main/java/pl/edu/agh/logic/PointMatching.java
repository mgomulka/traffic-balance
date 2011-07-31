package pl.edu.agh.logic;

import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.Date;

import com.vividsolutions.jts.geom.Coordinate;

public class PointMatching {
	private Coordinate point;
	private Road road;
	private Date time;

	public Coordinate getPoint() {
		return point;
	}
	
	public Road getRoad() {
		return road;
	}
	
	public Date getTime() {
		return time;
	}

	public PointMatching(Coordinate point, Road road, Date time) {
		this.point = point;
		this.road = road;
		this.time = time;
	}
	
	public double getCost() {
		return EARTH_DISTANCE_CALCULATOR.distance(point, road.nearestPoint(point));
	}
	
}
