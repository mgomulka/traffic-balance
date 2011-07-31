package pl.edu.agh.logic;

import pl.edu.agh.model.Way;

import com.vividsolutions.jts.geom.Point;

public class WayWithEndPoint extends WayWithOneEndPoint {

	public WayWithEndPoint(Way way, Point point) {
		super(way, point);
	}

	@Override
	public Integer getWayEndPoint() {
		return way.isOneWay() ? way.getSource() : getNearerVertexNumber();
	}

}
