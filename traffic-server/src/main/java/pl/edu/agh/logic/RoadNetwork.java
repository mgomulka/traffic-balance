package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static pl.edu.agh.spatial.HaversineDistanceCalculator.EARTH_DISTANCE_CALCULATOR;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import pl.edu.agh.model.Way;
import pl.edu.agh.spatial.GeometryHelper;
import pl.edu.agh.spatial.Vector;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class RoadNetwork {

	private static final double ROAD_SHIFT = 0.00003;

	private GeometryFactory geometryFactory;
	private List<Road> roads;
	private Multimap<Integer, Road> roadsBySource;
	private Multimap<Integer, Road> roadsById;

	public RoadNetwork(List<Way> ways, GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
		this.roadsBySource = ArrayListMultimap.create();
		this.roadsById = ArrayListMultimap.create();
		this.roads = newArrayListWithCapacity(ways.size());

		for (Way way : ways) {
			registerRoad(way);
		}
	}

	private void registerRoad(Way way) {
		if (way.isOneWay()) {
			registerOneWayRoad(way.getLineString(), way.getGid(), way.getSource(), way.getTarget());
		} else {
			registerTwoWayRoad(way.getLineString(), way.getGid(), way.getSource(), way.getTarget());
		}

	}

	private void registerOneWayRoad(LineString lineString, int id, int source, int target) {
		registerRoad(lineString, id, source, target, false, 0.0);

	}

	private void registerTwoWayRoad(LineString lineString, int id, int source, int target) {
		registerRoad(lineString, id, source, target, false, ROAD_SHIFT);
		registerRoad((LineString) lineString.reverse(), id, target, source, true, ROAD_SHIFT);
	}

	private void registerRoad(LineString lineString, int id, int source, int target, boolean reversed,
			double translation) {
		List<LineString> segments = newArrayListWithCapacity(lineString.getNumPoints() - 1);

		for (int i = 0; i < lineString.getNumPoints() - 1; i++) {
			segments.add(createRoadSegment(lineString.getPointN(i), lineString.getPointN(i + 1), translation));
		}

		Road registeredRoad = new Road(segments, id, source, target, reversed);
		roads.add(registeredRoad);
		roadsBySource.put(registeredRoad.getSource(), registeredRoad);
		roadsById.put(registeredRoad.getId(), registeredRoad);
	}

	private LineString createRoadSegment(Point start, Point end, double distance) {
		Vector segment = new Vector(start, end, geometryFactory).translatePerpendicularly(distance,
				Vector.Direction.RIGHT);
		return geometryFactory.createLineString(new Coordinate[] { segment.getBegin().getCoordinate(),
				segment.getEnd().getCoordinate() });
	}

	public List<Path> getNearestPathsForPoint(Point point, double degreeRadius, double meterRadius) {
		List<Path> paths = newArrayList();

		final Envelope pointNeighbourhood = GeometryHelper.getExpandedEnvelope(point, degreeRadius);

		Iterable<Road> roadsInsideNeighbourhood = Iterables.filter(roads, new Predicate<Road>() {
			@Override
			public boolean apply(Road road) {
				return GeometryHelper.getLineSegmentListEnvelope(road).intersects(pointNeighbourhood);
			}
		});

		for (Road road : roadsInsideNeighbourhood) {
			if (EARTH_DISTANCE_CALCULATOR.distance(point.getCoordinate(), road.nearestPoint(point)) < meterRadius) {
				paths.add(Path.createPath(point.getCoordinate(), road, (Date) point.getUserData()));
			}
		}

		return paths;
	}

	public Collection<Road> getRoadsBySource(int source) {
		return roadsBySource.get(source);
	}
	
	public Collection<Road> getRoadsById(int id) {
		return roadsById.get(id);
	}
}
