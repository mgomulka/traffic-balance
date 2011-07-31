package pl.edu.agh.spatial;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

public class GeometryHelper {

	public static List<Point> getPointsFromGeometry(Geometry geometry, GeometryFactory geometryFactory) {
		List<Point> points = newArrayList();

		for (Coordinate coordinate : geometry.getCoordinates()) {
			points.add(geometryFactory.createPoint(coordinate));
		}

		return points;
	}

	public static List<Point> getPointsFromGeometry(Geometry geometry) {
		return getPointsFromGeometry(geometry, geometry.getFactory());
	}

	public static Geometry getExpandedEnvelopeAsGeometry(Geometry geometry, double expansion) {
		Envelope envelope = getExpandedEnvelope(geometry, expansion);
		return geometry.getFactory().toGeometry(envelope);
	}

	public static Envelope getExpandedEnvelope(Geometry geometry, double expansion) {
		Envelope envelope = geometry.getEnvelopeInternal();
		envelope.expandBy(expansion);
		return envelope;
	}
	
	public static Envelope getLineSegmentListEnvelope(LineSegmentList lineSegmentList) {
		Envelope envelope = new Envelope();
		for (LineSegment lineSegment : lineSegmentList) {
			envelope.expandToInclude(lineSegment.p0);
			envelope.expandToInclude(lineSegment.p1);
		}
		
		return envelope;
	}
}
