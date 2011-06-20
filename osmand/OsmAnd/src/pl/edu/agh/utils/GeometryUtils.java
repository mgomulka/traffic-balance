package pl.edu.agh.utils;

import java.util.List;

import pl.edu.agh.model.RectD;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.Vector;

public class GeometryUtils {
	
	public enum Direction {
		LEFT, RIGHT;
	}

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
	
	public static Vector translate(Vector vector, double distance, Direction direction) {
		Vector translatingVector;
		
		if (vector.getX() == 0) {
			translatingVector = new Vector((int)Math.round(distance), 0);
		} else if (vector.getY() == 0) {
			translatingVector = new Vector(0, (int)Math.round(distance));
		} else {
			double dy = distance / Math.sqrt((vector.getY() * vector.getY()) / (vector.getX() * vector.getX()) + 1);
			double dx = -(vector.getY() / vector.getX()) * dy;
			translatingVector = new Vector((int) Math.round(dx), (int) Math.round(dy));
		}
		
		if (((determinant(vector, translatingVector) < 0) && (direction == Direction.RIGHT)) || ((determinant(vector, translatingVector) > 0) && (direction == Direction.LEFT))) {
			vector.translate(translatingVector);
			return vector;
		} else {
			vector.translate(translatingVector.reverse());
			return vector;
		}
	}
	
	public static int determinant(Vector v1, Vector v2) {
		return v1.getX() * v2.getY() - v1.getY() * v2.getX();
	}
}
