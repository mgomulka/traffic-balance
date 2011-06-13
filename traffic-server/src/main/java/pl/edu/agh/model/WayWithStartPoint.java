package pl.edu.agh.model;

import pl.edu.agh.model.entity.Way;

import com.vividsolutions.jts.geom.Point;

public class WayWithStartPoint extends WayWithOneEndPoint {

	public WayWithStartPoint(Way way, Point point) {
		super(way, point);
	}
	
	public Integer getWayEndPoint() {
		return way.isOneWay() ? way.getTarget() : getNearerVertexNumber();
	}

}
