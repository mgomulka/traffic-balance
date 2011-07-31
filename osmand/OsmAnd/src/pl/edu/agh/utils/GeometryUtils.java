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
}
