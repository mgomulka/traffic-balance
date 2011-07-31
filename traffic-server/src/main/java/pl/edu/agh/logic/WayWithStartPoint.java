package pl.edu.agh.logic;

import pl.edu.agh.model.Way;

import com.vividsolutions.jts.geom.Point;

public class WayWithStartPoint extends WayWithOneEndPoint {

	public WayWithStartPoint(Way way, Point point) {
		super(way, point);
	}
	
	public Integer getWayEndPoint() {
		return way.isOneWay() ? way.getTarget() : getNearerVertexNumber();
	}

}
