package pl.edu.agh.spatial;

import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

@Component
public class WGS84GeometryFactory extends GeometryFactory {
	private static final int WGS84_SRID = 4326;

	public WGS84GeometryFactory() {
		super(new PrecisionModel(), WGS84_SRID);
	}

	public Point createPoint(double x, double y) {
		return createPoint(new Coordinate(x, y));
	}
}
