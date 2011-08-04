package pl.edu.agh.bo;

import static com.google.common.collect.Lists.newArrayList;
import static pl.edu.agh.utils.DateUtils.timeDifference;
import static pl.edu.agh.utils.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.dao.SpeedInfoDao;
import pl.edu.agh.dao.WayDao;
import pl.edu.agh.exception.BusinessException;
import pl.edu.agh.logic.Path;
import pl.edu.agh.logic.PathComparatorByCost;
import pl.edu.agh.logic.PathCorrector;
import pl.edu.agh.logic.PathSplitter;
import pl.edu.agh.logic.PathUtils;
import pl.edu.agh.logic.PathWithoutLowQualityMatchingsSplitter;
import pl.edu.agh.logic.PathWithoutUTurnsSplitter;
import pl.edu.agh.logic.Road;
import pl.edu.agh.logic.RoadNetwork;
import pl.edu.agh.logic.SpeedInfoProcessor;
import pl.edu.agh.logic.UTurnPathCorrector;
import pl.edu.agh.logic.WayWithBothEndPoints;
import pl.edu.agh.logic.WayWithOneEndPoint;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.model.SpeedInfo;
import pl.edu.agh.model.Way;
import pl.edu.agh.model.WayWithSpeedInfo;
import pl.edu.agh.spatial.GeometryHelper;
import pl.edu.agh.spatial.PointComparator;
import pl.edu.agh.spatial.WGS84GeometryFactory;
import pl.edu.agh.utils.Collections;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

@Component
public class RoutingBOImpl implements RoutingBO {

	private static final double REACHING_END_OF_ROAD_FACTOR = 0.5;
	private static final int MAX_LOW_QUALITY_MATCHINGS_IN_ROW = 3;
	private static final int MAX_STORED_PATHS = 30;
	private static final double MAX_POINT_INTERVAL = 5.0;
	private static final double GPS_DEGREE_ACCURACY = 0.0004;
	private static final double GPS_METER_ACCURACY = 30.0;

	private final PathSplitter withoutUTurnsAndLowQualityMatchings = new PathWithoutLowQualityMatchingsSplitter(
			new PathWithoutUTurnsSplitter(), GPS_METER_ACCURACY);

	private final SpeedInfoProcessor speedInfoProcessor = new SpeedInfoProcessor();

	@Autowired
	private WayDao wayDao;

	@Autowired
	private SpeedInfoDao speedInfoDao;

	@Autowired
	private WGS84GeometryFactory geometryFactory;

	// @Autowired
	// private TrafficGenerator trafficGenerator;

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
			route = wayDao.findRoute(startWayWithPoint.getWayEndPoint(), endWayWithPoint.getWayEndPoint(),
					useTrafficDataToRoute);
			// trafficGenerator.generate(route);

		} catch (GenericJDBCException ex) {
			throw new BusinessException(Error.CALCULATING_ERROR);
		}

		Integer startIndex = removeWayFromRouteIfExists(startWayWithPoint, route);
		Integer endIndex = removeWayFromRouteIfExists(endWayWithPoint, route);

		List<Point> routeInterior = normalizeRoute(startIndex, route);
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

	private List<Point> normalizeRoute(Integer startVertexNumber, List<Way> route) {
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

	@Transactional
	@Override
	public void processLocationData(LocationData locationData) {
		List<Point> points = retrievePointsFromLocationData(locationData);

		RoadNetwork roadNetwork = createRoadNetworkForPoints(points);
		PathCorrector pathCorrector = new UTurnPathCorrector(roadNetwork);
		ListIterator<Point> currentPointIterator = points.listIterator();

		while (currentPointIterator.hasNext()) {
			Path bestPath = calculateBestPath(currentPointIterator, points, roadNetwork);
			if (bestPath == null) {
				continue;
			}

			List<Path> splittedBestPath = withoutUTurnsAndLowQualityMatchings.split(pathCorrector.correct(bestPath));
			retrieveAndSaveSpeedInfoFromPaths(splittedBestPath);
		}
	}

	private void retrieveAndSaveSpeedInfoFromPaths(List<Path> paths) {
		for (Path path : paths) {
			List<SpeedInfo> infos = speedInfoProcessor.retrieveSpeedInfoFromPath(path);
			for (SpeedInfo speedInfo : infos) {
				speedInfoDao.save(speedInfo);
			}
		}
	}

	private Path calculateBestPath(ListIterator<Point> currentPointIterator, List<Point> points, RoadNetwork roadNetwork) {
		List<Path> currentBestPaths = filterPathsWithLowCost(createInitialPaths(currentPointIterator, roadNetwork));
		Path bestPath = null;

		while (currentPointIterator.hasNext()
				&& !pathIsBroken(points.get(currentPointIterator.previousIndex()),
						points.get(currentPointIterator.previousIndex())) && !currentBestPaths.isEmpty()) {
			List<Path> nextStepBestPaths = createPathsForNextPoint(currentPointIterator.next(), currentBestPaths,
					roadNetwork);
			bestPath = java.util.Collections.min(nextStepBestPaths, PathComparatorByCost.COMPARATOR);
			currentBestPaths = filterPathsWithLowCost(nextStepBestPaths);
		}

		return bestPath;
	}

	private List<Path> createPathsForNextPoint(Point point, List<Path> currentPaths, RoadNetwork roadNetwork) {
		List<Path> nextStepBestPaths = newArrayList();
		Date pointTime = (Date) point.getUserData();

		for (Path currentPath : currentPaths) {
			nextStepBestPaths.add(currentPath.copy().matchPointToLastRoad(point.getCoordinate(), pointTime));

			if (PathUtils.pathContainsOneDirectedRoad(currentPath)
					|| currentPath.hasPointReachedEndOfLastRoad(point.getCoordinate(), REACHING_END_OF_ROAD_FACTOR)) {
				Collection<Road> forwardStar = roadNetwork.getRoadsBySource(currentPath.getTarget());
				for (Road nextRoad : forwardStar) {
					nextStepBestPaths.add(currentPath.copy().matchPointToRoad(point.getCoordinate(), nextRoad,
							pointTime));
				}
			}
		}
		return nextStepBestPaths;
	}

	private List<Path> filterPathsWithLowCost(List<Path> paths) {
		List<Path> filtered = newArrayList(Iterables.filter(paths, new Predicate<Path>() {

			@Override
			public boolean apply(Path path) {
				return path.getRelativeCostOfLastMatchings(MAX_LOW_QUALITY_MATCHINGS_IN_ROW) < GPS_METER_ACCURACY;
			}
		}));
		java.util.Collections.sort(filtered, PathComparatorByCost.COMPARATOR);
		return filtered.subList(0, Math.min(MAX_STORED_PATHS, filtered.size()));
	}

	private boolean pathIsBroken(Point previousPoint, Point currentPoint) {
		Date previousPointTime = (Date) previousPoint.getUserData();
		Date currentPointTime = (Date) currentPoint.getUserData();

		return timeDifference(previousPointTime, currentPointTime).toSeconds() > MAX_POINT_INTERVAL;
	}

	private List<Path> createInitialPaths(Iterator<Point> pointIterator, RoadNetwork roadNetwork) {
		while (pointIterator.hasNext()) {
			List<Path> paths = roadNetwork.getNearestPathsForPoint(pointIterator.next(), GPS_DEGREE_ACCURACY,
					GPS_METER_ACCURACY);
			if (!paths.isEmpty()) {
				return paths;
			}
		}

		return newArrayList();
	}

	private RoadNetwork createRoadNetworkForPoints(List<Point> points) {
		Geometry boundingBox = getBoundingBoxForPoints(points);
		List<Way> ways = wayDao.getWaysInsideBox(boundingBox);

		RoadNetwork roadNetwork = new RoadNetwork(ways, geometryFactory);
		return roadNetwork;
	}

	private List<Point> retrievePointsFromLocationData(LocationData locationData) {
		return newArrayList(Lists.transform(locationData.getLocationInfos(), new Function<LocationInfo, Point>() {
			@Override
			public Point apply(LocationInfo info) {
				Point point = geometryFactory.createPoint(info.getLongitude(), info.getLatitude());
				point.setUserData(info.getTime());
				return point;
			}
		}));
	}

	private Geometry getBoundingBoxForPoints(List<Point> points) {
		MultiPoint multipoint = geometryFactory.createMultiPoint(points.toArray(new Point[0]));
		return GeometryHelper.getExpandedEnvelopeAsGeometry(multipoint, GPS_DEGREE_ACCURACY);
	}
}