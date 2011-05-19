package pl.edu.agh.bo;

import org.springframework.stereotype.Component;

import pl.edu.agh.model.WayWithBothEndPoints;
import pl.edu.agh.model.WayWithOneEndPoint;
import pl.edu.agh.model.entity.Way;

import com.vividsolutions.jts.geom.Point;

@Component
public class WayWithEndpointsFactory {

	public WayWithOneEndPoint create(Way way, Point point) {
		return new WayWithOneEndPoint(way, point);
	}
	
	public WayWithBothEndPoints create(Way way, Point start, Point end) {
		return new WayWithBothEndPoints(way, start, end);
	}
}
