package pl.edu.agh.utils;

import java.util.List;

import pl.edu.agh.model.RectD;
import pl.edu.agh.model.SimpleLocationInfo;

public class GeometryUtils {

	public static RectD createBoundingBox(SimpleLocationInfo boxCenter, double boxSize) {
		return new RectD(boxCenter.getLongitude() - boxSize, boxCenter.getLatitude() + boxSize,
				boxCenter.getLongitude() + boxSize, boxCenter.getLatitude() - boxSize);

	}

	public static boolean existsPointInsideRect(List<SimpleLocationInfo> points, RectD rectD) {
		for (SimpleLocationInfo point : points) {
			if (rectD.contains(point)) {
				return true;
			}
		}

		return false;
	}
	
	public static double euklideanDist(SimpleLocationInfo d1, SimpleLocationInfo d2) {
		
		return Math.sqrt(Math.pow(d1.getLatitude() - d2.getLatitude(),2.0) + Math.pow(d1.getLongitude() - d2.getLongitude(), 2.0));
	}
	
}
