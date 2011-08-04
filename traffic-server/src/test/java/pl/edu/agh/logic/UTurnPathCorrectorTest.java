package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pl.edu.agh.model.Way;
import pl.edu.agh.spatial.WGS84GeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class UTurnPathCorrectorTest {

	private PathCorrector pathCorrector;
	private RoadNetwork roadNetwork;
	private GeometryFactory geometryFactory = new WGS84GeometryFactory();

	@Before
	public void preparePathCorrector() {

		LineString line1 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(0, 0),
				new Coordinate(1, 0), new Coordinate(2, 0) });
		LineString line2 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(2, 0),
				new Coordinate(3, 0), new Coordinate(4, 0) });
		LineString line3 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(4, 0),
				new Coordinate(5, 0), new Coordinate(6, 0) });

		List<Way> ways = newArrayList();
		ways.add(createWay(1, 1, 2, line1, false));
		ways.add(createWay(2, 2, 3, line2, true));
		ways.add(createWay(3, 3, 4, line3, false));

		roadNetwork = new RoadNetwork(ways, geometryFactory);
		pathCorrector = new UTurnPathCorrector(roadNetwork);
	}

	private Way createWay(int gid, int source, int target, LineString line, boolean isOneWay) {
		Way way1 = new Way();
		way1.setGid(gid);
		way1.setSource(source);
		way1.setTarget(target);
		way1.setGeometry(geometryFactory.createMultiLineString(new LineString[] { line }));
		way1.setLength(2.0);
		way1.setReverseCost(isOneWay ? 1.0 : 2.0);
		return way1;
	}

	@Test
	public void forPathWithTwoMatchingToCorrectlyDirectedRoadReturnsTheSamePath() {
		Road[] roads = roadNetwork.getRoadsById(1).toArray(new Road[0]);
		Road directRoad = roads[0].isReversed() ? roads[1] : roads[0];

		Path path = Path.createPath(new Coordinate(0.5, 0), directRoad, new Date()).matchPointToLastRoad(
				new Coordinate(1.5, 0), new Date());

		Path corrected = pathCorrector.correct(path);

		assertTrue(PathUtils.pathContainsOneDirectedRoad(corrected));
		assertEquals(corrected.getMatchings().get(0).getRoad(), directRoad);
	}

	@Test
	public void forPathWithTwoMatchingToWrongDirectedRoadReturnsThePathWithReversedRoad() {
		Road[] roads = roadNetwork.getRoadsById(1).toArray(new Road[0]);
		Road directRoad = roads[0].isReversed() ? roads[1] : roads[0];
		Road reversedRoad = roads[0].isReversed() ? roads[0] : roads[1];

		Path path = Path.createPath(new Coordinate(1.5, 0), directRoad, new Date()).matchPointToLastRoad(
				new Coordinate(0.5, 0), new Date());

		Path corrected = pathCorrector.correct(path);

		assertTrue(PathUtils.pathContainsOneDirectedRoad(corrected));
		assertEquals(corrected.getMatchings().get(0).getRoad(), reversedRoad);
	}
	
	@Test
	public void forPathWithTwoMatchingToOneWayRoadReturnsTheSamePath() {
		Road[] roads = roadNetwork.getRoadsById(2).toArray(new Road[0]);
		Road directRoad = roads[0].isReversed() ? roads[1] : roads[0];

		Path path = Path.createPath(new Coordinate(1.5, 0), directRoad, new Date()).matchPointToLastRoad(
				new Coordinate(0.5, 0), new Date());

		Path corrected = pathCorrector.correct(path);

		assertEquals(path, corrected);
	}

	@Test
	public void forPathWithUTurnReturnsThePathWithOneRoad() {
		Road[] roads = roadNetwork.getRoadsById(1).toArray(new Road[0]);
		Road directRoad = roads[0].isReversed() ? roads[1] : roads[0];
		Road reversedRoad = roads[0].isReversed() ? roads[0] : roads[1];

		Path path = Path.createPath(new Coordinate(1.5, 0), directRoad, new Date()).matchPointToRoad(
				new Coordinate(0.5, 0), reversedRoad, new Date());

		Path corrected = pathCorrector.correct(path);

		assertTrue(PathUtils.pathContainsOneDirectedRoad(corrected));
		assertEquals(corrected.getMatchings().get(0).getRoad(), reversedRoad);
	}

	@Test
	public void forPathWithUTurnAndDistanceBetweenPointsGreaterThanPathLengthReturnsTheSamePath() {
		Road[] roads = roadNetwork.getRoadsById(1).toArray(new Road[0]);
		Road directRoad = roads[0].isReversed() ? roads[1] : roads[0];
		Road reversedRoad = roads[0].isReversed() ? roads[0] : roads[1];

		Path path = Path.createPath(new Coordinate(1.6, 0), directRoad, new Date())
				.matchPointToRoad(new Coordinate(0.4, 0), reversedRoad, new Date())
				.matchPointToRoad(new Coordinate(1.6, 0), directRoad, new Date());

		Path corrected = pathCorrector.correct(path);

		assertEquals(path, corrected);
	}
	
	@Test
	public void forPathWithoutUTurnsReturnsTheSamePath() {
		Path path = Path.createPath(new Coordinate(1, 0), getRoad(1, false), new Date())
				.matchPointToLastRoad(new Coordinate(1.5, 0), new Date())
				.matchPointToRoad(new Coordinate(3, 0), getRoad(2, false), new Date())
				.matchPointToRoad(new Coordinate(5.0, 0), getRoad(3, false), new Date())
				.matchPointToLastRoad(new Coordinate(5.5, 0), new Date());
		
		Path corrected = pathCorrector.correct(path);
		
		assertEquals(path.getMatchings().size(), corrected.getMatchings().size());
		for (int i = 0 ; i < path.getMatchings().size(); i++) {
			assertEquals(path.getMatchings().get(i).getRoad(), corrected.getMatchings().get(i).getRoad());
		}
	}
	
	@Test
	public void forPathWithUTurnsAtBeginningAndEndCorrectsThem() {
		Path path = Path.createPath(new Coordinate(1, 0), getRoad(1, true), new Date())
				.matchPointToRoad(new Coordinate(1.5, 0), getRoad(1, false), new Date())
				.matchPointToRoad(new Coordinate(3, 0), getRoad(2, false), new Date())
				.matchPointToRoad(new Coordinate(5.0, 0), getRoad(3, false), new Date())
				.matchPointToRoad(new Coordinate(5.5, 0), getRoad(3, true), new Date());
		
		Path corrected = pathCorrector.correct(path);
		
		assertEquals(path.getMatchings().size(), corrected.getMatchings().size());
		assertEquals(corrected.getMatchings().get(0).getRoad(), corrected.getMatchings().get(1).getRoad());
		assertEquals(corrected.getMatchings().get(3).getRoad(), corrected.getMatchings().get(4).getRoad());
		for (int i = 1; i < 4; i++) {
			assertEquals(path.getMatchings().get(i).getRoad(), corrected.getMatchings().get(i).getRoad());
		}
	}
	
	@Test
	public void forPathWithLongUTurnsAtBeginningAndEndDoesNotCorrectThem() {
		Path path = Path.createPath(new Coordinate(0, 0), getRoad(1, true), new Date())
				.matchPointToRoad(new Coordinate(1.5, 0), getRoad(1, false), new Date())
				.matchPointToLastRoad(new Coordinate(0, 0), new Date())
				.matchPointToLastRoad(new Coordinate(1.5, 0), new Date())
				.matchPointToRoad(new Coordinate(3, 0), getRoad(2, false), new Date())
				.matchPointToRoad(new Coordinate(4.0, 0), getRoad(3, false), new Date())
				.matchPointToRoad(new Coordinate(5.5, 0), getRoad(3, true), new Date())
				.matchPointToLastRoad(new Coordinate(4.0, 0), new Date())
				.matchPointToLastRoad(new Coordinate(5.5, 0), new Date());
		
		Path corrected = pathCorrector.correct(path);
		
		assertEquals(path.getMatchings().size(), corrected.getMatchings().size());
		for (int i = 0 ; i < path.getMatchings().size(); i++) {
			assertEquals(path.getMatchings().get(i).getRoad(), corrected.getMatchings().get(i).getRoad());
		}
	}

	private Road getRoad(int id, boolean reversed) {
		Road[] roads = roadNetwork.getRoadsById(id).toArray(new Road[0]);

		if (roads.length == 1) {
			return (roads[0].isReversed() == reversed) ? roads[0] : null;
		}
		
		return (roads[0].isReversed() == reversed) ? roads[0] : roads[1];
	}

}
