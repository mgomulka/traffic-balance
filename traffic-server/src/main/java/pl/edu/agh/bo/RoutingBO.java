package pl.edu.agh.bo;

import java.util.List;

import pl.edu.agh.exception.BusinessException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.WayWithSpeedInfo;

import com.vividsolutions.jts.geom.Point;

public interface RoutingBO {

	public enum Error {
		NO_START_ROUTE, NO_END_ROUTE, CALCULATING_ERROR;
	}

	public List<Point> calculateRoute(Point start, Point end, boolean useTrafficDataToRoute) throws BusinessException;
	public List<WayWithSpeedInfo> getTrafficData(Point point, double radius);
	public List<RoutingResult> processLocationData(LocationData locationData);
}
