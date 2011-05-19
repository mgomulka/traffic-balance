package pl.edu.agh.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import com.google.common.base.Objects;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

@Entity
@Table(name = Way.TABLE_NAME)
@Immutable
public class Way implements Serializable {

	public static final String TABLE_NAME = "ways";
	public static final String VIEW_NAME = "ways_with_costs";

	public static final String COL_GID = "gid";
	public static final String COL_CLASS_ID = "class_id";
	public static final String COL_LENGTH = "length";
	public static final String COL_NAME = "name";
	public static final String COL_START_X = "x1";
	public static final String COL_START_Y = "y1";
	public static final String COL_END_X = "x2";
	public static final String COL_END_Y = "y2";
	public static final String COL_REVERSE_COST = "reverse_cost";
	public static final String COL_RULE = "rule";
	public static final String COL_TO_COST = "to_cost";
	public static final String COL_GEOM = "the_geom";
	public static final String COL_SOURCE = "source";
	public static final String COL_TARGET = "target";
	public static final String VIEW_COL_COST = "way_cost";

	@Id
	@Column(name = COL_GID)
	private Integer gid;
	@Column(name = COL_CLASS_ID)
	private Integer classId;
	@Column(name = COL_LENGTH)
	private double length;
	@Column(name = COL_NAME)
	private String name;
	@Column(name = COL_START_X)
	private double startX;
	@Column(name = COL_START_Y)
	private double startY;
	@Column(name = COL_END_X)
	private double endX;
	@Column(name = COL_END_Y)
	private double endY;
	@Column(name = COL_REVERSE_COST)
	private double reverseCost;
	@Column(name = COL_SOURCE)
	private Integer source;
	@Column(name = COL_TARGET)
	private Integer target;
	@Column(name = COL_RULE)
	private String rule;
	@Column(name = COL_TO_COST)
	private Double toCost;
	@Column(name = COL_GEOM)
	@Type(type = "org.hibernatespatial.GeometryUserType")
	private Geometry geometry;

	public Integer getGid() {
		return gid;
	}

	public Integer getClassId() {
		return classId;
	}

	public double getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public double getStartX() {
		return startX;
	}

	public double getStartY() {
		return startY;
	}

	public double getEndX() {
		return endX;
	}

	public double getEndY() {
		return endY;
	}

	public double getReverseCost() {
		return reverseCost;
	}

	public Integer getSource() {
		return source;
	}

	public Integer getTarget() {
		return target;
	}

	public String getRule() {
		return rule;
	}

	public Double getToCost() {
		return toCost;
	}

	public Geometry getGeometry() {
		return geometry;
	}
	
	@Transient
	public LineString getLineString() {
		return (LineString) ((MultiLineString) geometry).getGeometryN(0);
	}
	
	@Transient
	public Point getStartPoint() {
		return getLineString().getStartPoint();
	}
	
	@Transient
	public Point getEndPoint() {
		return getLineString().getEndPoint();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(gid);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Way)) {
			return false;
		}
		
		Way other = (Way) o;
		return Objects.equal(this.gid, other.gid);
	}
	
	

}
