package pl.edu.agh.bo;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.utils.Preconditions.checkNotNull;

import java.util.List;

import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.dao.WayDao;
import pl.edu.agh.exception.BusinessException;
import pl.edu.agh.model.WayWithBothEndPoints;
import pl.edu.agh.model.WayWithOneEndPoint;
import pl.edu.agh.model.entity.Way;
import pl.edu.agh.model.entity.WayWithSpeedInfo;
import pl.edu.agh.spatial.GeometryHelper;
import pl.edu.agh.spatial.PointComparator;
import pl.edu.agh.spatial.WGS84GeometryFactory;
import pl.edu.agh.utils.Collections;

import com.vividsolutions.jts.geom.Point;

@Component
public class RoutingBOImpl implements RoutingBO {

	@Autowired
	private WayDao wayDao;

	@Autowired
	private WGS84GeometryFactory geometryFactory;

	//@Autowired
	//private TrafficGenerator trafficGenerator;

	@Autowired
	private WayWithEndpointsFactory wayWithEndpointsFactory;

	@Transactional
	@Override
	public List<Point> calculateRoute(Point start, Point end, boolean useTrafficDataToRoute) throws BusinessException {
		Way startWay = checkNotNull(wayDao.findNearestWay(start), Error.NO_START_ROUTE);
		Way endWay = checkNotNull(wayDao.findNearestWay(end), Error.NO_END_ROUTE);

		List<Point> route;
		if (startWay.equals(endWay) && wayWithEndpointsFactory.create(startWay, start, end).isValid()) {
			route = calculateRouteWithOneWay(wayWithEndpointsFactory.create(startWay, start, end));
		} else {
			route = calculateRouteWithManyWays(wayWithEndpointsFactory.createWayWithStartPoint(startWay, start),
					wayWithEndpointsFactory.createWayWithEndPoint(endWay, end), useTrafficDataToRoute);
		}

		return Collections.removeAdjacentDuplicates(completeRoute(start, end, route), PointComparator.COMPARATOR);
	}

	private List<Point> calculateRouteWithOneWay(WayWithBothEndPoints wayWithBothEndPoints) {
		return wayWithBothEndPoints.createRoute();
	}

	@SuppressWarnings("unchecked")
	private List<Point> calculateRouteWithManyWays(WayWithOneEndPoint startWayWithPoint,
			WayWithOneEndPoint endWayWithPoint, boolean useTrafficDataToRoute) {
		List<Way> route;
		try {
			route = wayDao.findRoute(startWayWithPoint.getWayEndPoint(),
					endWayWithPoint.getWayEndPoint(), useTrafficDataToRoute);
			//trafficGenerator.generate(route);

		} catch (GenericJDBCException ex) {
			throw new BusinessException(Error.CALCULATING_ERROR);
		}

		Integer startIndex = removeWayFromRouteIfExists(startWayWithPoint, route);
		Integer endIndex = removeWayFromRouteIfExists(endWayWithPoint, route);

		List<Point> routeInterior = normalizeRoute(startIndex, endIndex, route);
		List<Point> startFragment = startWayWithPoint.createRouteFromPointToIndex(startIndex);
		List<Point> endFragment = endWayWithPoint.createRouteFromIndexToPoint(endIndex);

		return Collections.concatLists(startFragment, routeInterior, endFragment);
	}

	private Integer removeWayFromRouteIfExists(WayWithOneEndPoint wayWithPoint, List<Way> route) {
		if (route.contains(wayWithPoint.getWay())) {
			route.remove(wayWithPoint.getWay());
			return wayWithPoint.getWayEndPointOppositeTo(wayWithPoint.getWayEndPoint());
		}

		return wayWithPoint.getWayEndPoint();
	}

	private List<Point> normalizeRoute(Integer startVertexNumber, Integer endVertexNumber, List<Way> route) {
		Integer currentSource = startVertexNumber;

		List<Point> points = newArrayList();

		for (Way way : route) {
			if (way.getSource().equals(currentSource)) {
				points.addAll(GeometryHelper.getPointsFromGeometry(way.getLineString(), geometryFactory));
				currentSource = way.getTarget();
			} else {
				points.addAll(GeometryHelper.getPointsFromGeometry(way.getLineString().reverse(), geometryFactory));
				currentSource = way.getSource();
			}
		}

		return points;
	}

	private List<Point> completeRoute(Point start, Point end, List<Point> route) {
		route.add(0, start);
		route.add(end);

		return route;
	}

	@Transactional
	@Override
	public List<WayWithSpeedInfo> getTrafficData(Point point, double radius) {
		return wayDao.getTrafficData(point, radius);
	}
}
