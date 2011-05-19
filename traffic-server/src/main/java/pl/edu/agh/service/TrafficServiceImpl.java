package pl.edu.agh.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Point;

import pl.edu.agh.bo.RoutingBO;
import pl.edu.agh.bo.RoutingBO.Error;
import pl.edu.agh.exception.BusinessException;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.spatial.WGS84GeometryFactory;

@Component
public class TrafficServiceImpl implements TrafficService {

	static final ImmutableMap<RoutingBO.Error, String> ROUTING_ERROR_MAPPING = new ImmutableMap.Builder<RoutingBO.Error, String>()
			.put(Error.NO_START_ROUTE, NO_START_ROUTE_ERROR).put(Error.NO_END_ROUTE, NO_END_ROUTE_ERROR)
			.put(Error.CALCULATING_ERROR, CALCULATING_ROUTE_ERROR).build();

	@Autowired
	RoutingBO routingBO;

	@Autowired
	WGS84GeometryFactory geometryFactory;

	@Override
	public RoutingResult calculateRoute(SimpleLocationInfo start, SimpleLocationInfo end) throws JSONRPCException {
		try {
			List<Point> points = routingBO.calculateRoute(
					geometryFactory.createPoint(start.getLongitude(), start.getLatitude()),
					geometryFactory.createPoint(end.getLongitude(), end.getLatitude()));
			return new RoutingResult(transformPointsToInfos(points));
		} catch (BusinessException ex) {
			throw new JSONRPCException(ROUTING_ERROR_MAPPING.get(ex.getError()), ex);
		}

	}

	private List<SimpleLocationInfo> transformPointsToInfos(List<Point> points) {
		List<SimpleLocationInfo> infos = Lists.transform(points, new Function<Point, SimpleLocationInfo>() {
			@Override
			public SimpleLocationInfo apply(Point point) {
				return new SimpleLocationInfo(point.getX(), point.getY());
			}
		});
		return infos;
	}

	@Override
	public TrafficData getTrafficData(SimpleLocationInfo location) throws JSONRPCException {
		TrafficData td = new TrafficData();
		td.setNumber(7);
		return td;
	}

}
