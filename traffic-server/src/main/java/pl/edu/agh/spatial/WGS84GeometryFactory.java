package pl.edu.agh.spatial;

import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence.Double;

@Component
public class WGS84GeometryFactory extends GeometryFactory {
	private static final int DIMENSION_NUMBER = 2;
	private static final int WGS84_SRID = 4326;

	public WGS84GeometryFactory() {
		super(new PrecisionModel(), WGS84_SRID);
	}

	public Point createPoint(double x, double y) {
		return createPoint(new Double(new double[] { x, y }, DIMENSION_NUMBER));
	}
}
