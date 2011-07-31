package pl.edu.agh.bo;

import org.springframework.stereotype.Component;

import pl.edu.agh.logic.WayWithBothEndPoints;
import pl.edu.agh.logic.WayWithEndPoint;
import pl.edu.agh.logic.WayWithOneEndPoint;
import pl.edu.agh.logic.WayWithStartPoint;
import pl.edu.agh.model.Way;

import com.vividsolutions.jts.geom.Point;

@Component
public class WayWithEndpointsFactory {

	public WayWithOneEndPoint createWayWithStartPoint(Way way, Point point) {
		return new WayWithStartPoint(way, point);
	}
	
	public WayWithOneEndPoint createWayWithEndPoint(Way way, Point point) {
		return new WayWithEndPoint(way, point);
	}
	
	public WayWithBothEndPoints create(Way way, Point start, Point end) {
		return new WayWithBothEndPoints(way, start, end);
	}
}
