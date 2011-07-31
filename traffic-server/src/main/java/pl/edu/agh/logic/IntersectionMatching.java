package pl.edu.agh.logic;

import java.util.Date;

import com.vividsolutions.jts.geom.Coordinate;

public class IntersectionMatching {

	private PointMatching previousRoadMatching;
	private PointMatching nextRoadMatching;

	public IntersectionMatching(Coordinate intersectionPoint, Date time, Road previousRoad, Road nextRoad) {
		this.previousRoadMatching = new PointMatching(intersectionPoint, previousRoad, time);
		this.nextRoadMatching = new PointMatching(intersectionPoint, nextRoad, time);
	}

	public IntersectionMatching(PointMatching previousRoadMatching, PointMatching nextRoadMatching) {
		this.previousRoadMatching = previousRoadMatching;
		this.nextRoadMatching = nextRoadMatching;
	}

	public PointMatching getPreviousRoadMatching() {
		return previousRoadMatching;
	}

	public PointMatching getNextRoadMatching() {
		return nextRoadMatching;
	}

	public Coordinate getIntersectionPoint() {
		return previousRoadMatching != null ? previousRoadMatching.getPoint() : nextRoadMatching.getPoint();
	}

	public Date getIntersectionTime() {
		return previousRoadMatching != null ? previousRoadMatching.getTime() : nextRoadMatching.getTime();
	}
}
