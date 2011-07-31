package pl.edu.agh.logic;

import pl.edu.agh.model.Way;

import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class WayWithEndPoints {

	protected Way way;
	protected GeometryFactory geometryFactory;

	protected WayWithEndPoints(Way way, GeometryFactory geometryFactory) {
		this.way = way;
		this.geometryFactory = geometryFactory;
	}
	
	public Way getWay() {
		return way;
	}

}
