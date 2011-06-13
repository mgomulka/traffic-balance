package pl.edu.agh.bo;

import org.springframework.stereotype.Component;

import pl.edu.agh.model.WayWithBothEndPoints;
import pl.edu.agh.model.WayWithEndPoint;
import pl.edu.agh.model.WayWithOneEndPoint;
import pl.edu.agh.model.WayWithStartPoint;
import pl.edu.agh.model.entity.Way;

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
