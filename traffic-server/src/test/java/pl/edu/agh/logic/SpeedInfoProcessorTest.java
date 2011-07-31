package pl.edu.agh.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import pl.edu.agh.model.SpeedInfo;
import pl.edu.agh.spatial.WGS84GeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class SpeedInfoProcessorTest {

	private static final double TOLERANCE = 1.0;
	private WGS84GeometryFactory geometryFactory = new WGS84GeometryFactory();
	private SpeedInfoProcessor speedInfoProcessor = new SpeedInfoProcessor();

	@Test
	public void correctlyRetrievesSpeedInfoForPathWith2Segments() {
		LineString l1 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-7, 0.3),
				new Coordinate(-5, 0.3) });
		LineString l2 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-5, 0.3),
				new Coordinate(0, 0.3) });
		Road road1 = new Road(Arrays.asList(l1, l2), 1, 1, 2, false);

		LineString l3 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(0, -0.3),
				new Coordinate(5, -0.3) });
		Road road2 = new Road(Arrays.asList(l3), 2, 2, 3, false);

		Clock clock = new Clock();

		Path path = Path.createPath(new Coordinate(-6, 0), road1, clock.nextSecond())
				.matchPointToLastRoad(new Coordinate(-4, 0), clock.nextSecond())
				.matchPointToLastRoad(new Coordinate(-2, 0), clock.nextSecond())
				.matchPointToRoad(new Coordinate(3, 0), road2, clock.nextSecond());

		List<SpeedInfo> infos = speedInfoProcessor.retrieveSpeedInfoFromPath(path);

		assertEquals(infos.size(), 2);
		assertEquals(277983.5, infos.get(0).getDirectWaySpeed(), TOLERANCE);
		assertNull(infos.get(0).getReverseWaySpeed());
		assertEquals(555967.0, infos.get(1).getDirectWaySpeed(), TOLERANCE);
		assertNull(infos.get(1).getReverseWaySpeed());
	}

	@Test
	public void correctlyRetrievesSpeedInfoForPathWith3Segments() {
		LineString l1 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-7, 0.3),
				new Coordinate(-5, 0.3) });
		LineString l2 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-5, 0.3),
				new Coordinate(0, 0.3) });
		Road road1 = new Road(Arrays.asList(l1, l2), 1, 1, 2, false);

		LineString l3 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(0, -0.3),
				new Coordinate(5, -0.3) });
		Road road2 = new Road(Arrays.asList(l3), 2, 2, 3, true);

		LineString l4 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(5, 0.3),
				new Coordinate(7, 0.3) });
		Road road3 = new Road(Arrays.asList(l4), 3, 3, 4, false);

		Clock clock = new Clock();

		Path path = Path.createPath(new Coordinate(-6, 0), road1, clock.nextSecond())
				.matchPointToLastRoad(new Coordinate(-4, 0), clock.nextSecond())
				.matchPointToLastRoad(new Coordinate(-5, 0), clock.nextSecond())
				.matchPointToLastRoad(new Coordinate(-2, 0), clock.nextSecond())
				.matchPointToRoad(new Coordinate(3, 0), road2, clock.nextSecond())
				.matchPointToRoad(new Coordinate(6, 0), road3, clock.nextSecond());

		List<SpeedInfo> infos = speedInfoProcessor.retrieveSpeedInfoFromPath(path);

		assertEquals(infos.size(), 3);
		assertEquals(261631.5, infos.get(0).getDirectWaySpeed(), TOLERANCE);
		assertNull(infos.get(0).getReverseWaySpeed());
		assertNull(infos.get(1).getDirectWaySpeed());
		assertEquals(438805.8, infos.get(1).getReverseWaySpeed(), TOLERANCE);
		assertEquals(333914.1, infos.get(2).getDirectWaySpeed(), TOLERANCE);
		assertNull(infos.get(2).getReverseWaySpeed());
	}

	@Test
	public void forPathWithOneMatchingsReuturnsEmptyList() {
		LineString l1 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-7, 0.3),
				new Coordinate(-5, 0.3) });
		LineString l2 = geometryFactory.createLineString(new Coordinate[] { new Coordinate(-5, 0.3),
				new Coordinate(0, 0.3) });
		Road road1 = new Road(Arrays.asList(l1, l2), 1, 1, 2, false);

		Clock clock = new Clock();

		Path path = Path.createPath(new Coordinate(-6, 0), road1, clock.nextSecond());

		List<SpeedInfo> infos = speedInfoProcessor.retrieveSpeedInfoFromPath(path);

		Assert.assertTrue(infos.isEmpty());
	}

}
