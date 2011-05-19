package pl.edu.agh.bo;

import java.util.List;

import pl.edu.agh.exception.BusinessException;

import com.vividsolutions.jts.geom.Point;

public interface RoutingBO {

	public enum Error {
		NO_START_ROUTE, NO_END_ROUTE, CALCULATING_ERROR;
	}

	public List<Point> calculateRoute(Point start, Point end) throws BusinessException;
}
