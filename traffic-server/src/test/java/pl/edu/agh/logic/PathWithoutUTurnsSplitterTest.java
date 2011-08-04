package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import pl.edu.agh.logic.Path;
import pl.edu.agh.logic.PathSplitter;
import pl.edu.agh.logic.PathWithoutUTurnsSplitter;
import pl.edu.agh.logic.Road;
import pl.edu.agh.spatial.WGS84GeometryFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class PathWithoutUTurnsSplitterTest {

	private PathSplitter transformer = new PathWithoutUTurnsSplitter();
	private WGS84GeometryFactory geometryFactory = new WGS84GeometryFactory();

	@Test
	public void forUTurnsOnlyReturnsEmptyList() {
		List<LineString> lineStrings = newArrayList(geometryFactory.createLineString(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(1, 1) }));
		Road road1 = new Road(lineStrings, 1, 1, 2, false);
		Road road2 = new Road(lineStrings, 1, 2, 1, true);

		Coordinate coordinate = new Coordinate(0, 0);

		Path path = Path.createPath(coordinate, road1, null).matchPointToLastRoad(coordinate, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road2, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road1, null)
				.matchPointToLastRoad(coordinate, null);

		List<Path> transformedPaths = transformer.split(path);

		assertTrue(transformedPaths.isEmpty());
	}

	@Test
	public void forPathsWithoutUTurnsReturnsOnePath() {
		List<LineString> lineStrings = newArrayList(geometryFactory.createLineString(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(1, 1) }));
		Road road1 = new Road(lineStrings, 1, 1, 2, false);
		Road road2 = new Road(lineStrings, 2, 2, 3, false);
		Road road3 = new Road(lineStrings, 3, 3, 1, false);

		Coordinate coordinate = new Coordinate(0, 0);

		Path path = Path.createPath(coordinate, road1, null).matchPointToLastRoad(coordinate, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road2, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road3, null)
				.matchPointToLastRoad(coordinate, null);

		List<Path> transformedPaths = transformer.split(path);

		assertEquals(transformedPaths.size(), 1);
		assertEquals(transformedPaths.get(0).getMatchings().size(), 7);
	}

	@Test
	public void properlyRemoveUTurnFromBeginningOfPath() {
		List<LineString> lineStrings = newArrayList(geometryFactory.createLineString(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(1, 1) }));
		Road road1 = new Road(lineStrings, 1, 1, 2, false);
		Road road2 = new Road(lineStrings, 1, 2, 1, true);
		Road road3 = new Road(lineStrings, 3, 1, 3, false);

		Coordinate coordinate = new Coordinate(0, 0);

		Path path = Path.createPath(coordinate, road1, null).matchPointToLastRoad(coordinate, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road2, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road3, null)
				.matchPointToLastRoad(coordinate, null);

		List<Path> transformedPaths = transformer.split(path);

		assertEquals(transformedPaths.size(), 1);
		assertEquals(transformedPaths.get(0).getMatchings().size(), 2);
	}

	@Test
	public void properlyRemoveUTurnFromEndOfPath() {
		List<LineString> lineStrings = newArrayList(geometryFactory.createLineString(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(1, 1) }));
		Road road1 = new Road(lineStrings, 1, 1, 2, false);
		Road road2 = new Road(lineStrings, 2, 2, 3, false);
		Road road3 = new Road(lineStrings, 2, 3, 2, true);

		Coordinate coordinate = new Coordinate(0, 0);

		Path path = Path.createPath(coordinate, road1, null).matchPointToLastRoad(coordinate, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road2, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road3, null)
				.matchPointToLastRoad(coordinate, null);

		List<Path> transformedPaths = transformer.split(path);

		assertEquals(transformedPaths.size(), 1);
		assertEquals(transformedPaths.get(0).getMatchings().size(), 3);
	}

	@Test
	public void properlyRemoveUTurnFromMiddleOfPath() {
		List<LineString> lineStrings = newArrayList(geometryFactory.createLineString(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(1, 1) }));
		Road road1 = new Road(lineStrings, 1, 1, 2, false);
		Road road2 = new Road(lineStrings, 2, 2, 3, false);
		Road road3 = new Road(lineStrings, 2, 3, 2, true);
		Road road4 = new Road(lineStrings, 4, 2, 4, false);

		Coordinate coordinate = new Coordinate(0, 0);

		Path path = Path.createPath(coordinate, road1, null).matchPointToLastRoad(coordinate, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road2, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road3, null)
				.matchPointToLastRoad(coordinate, null).matchPointToRoad(coordinate, road4, null);

		List<Path> transformedPaths = transformer.split(path);

		assertEquals(transformedPaths.size(), 2);
		assertEquals(transformedPaths.get(0).getMatchings().size(), 3);
		assertEquals(transformedPaths.get(1).getMatchings().size(), 1);
	}
}
