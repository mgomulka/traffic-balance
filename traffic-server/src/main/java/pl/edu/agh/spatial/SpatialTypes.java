package pl.edu.agh.spatial;

import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;

public class SpatialTypes {

	public static final Type GEOMETRY = new CustomType(GeometryUserType.class, null);
}
