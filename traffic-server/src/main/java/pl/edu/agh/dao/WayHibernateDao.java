package pl.edu.agh.dao;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Component;

import pl.edu.agh.model.Way;
import pl.edu.agh.model.WayWithSpeedInfo;
import pl.edu.agh.spatial.GeometryHelper;
import pl.edu.agh.spatial.SpatialTypes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Component
public class WayHibernateDao extends AbstractDao implements WayDao {

	private static final String EMPTY = "";

	private static final int MOTORWAY_CLASS_ID = 101;
	private static final int PRIMARY_LINK_CLASS_ID = 107;
	private static final int SECONDARY_CLASS_ID = 108;
	private static final int TETRIARY_CLASS_ID = 109;
	private static final int ROUNDABOUNT_CLASS_ID = 401;

	private static final double BOUNDING_BOX_SIZE = 0.1;
	
	private static final double MAX_RADIUS = 0.17247492;
	private static final double PRIMARY_RADIUS = 0.08623746;
	private static final double SECONDARY_RADIUS = 0.04311873;
	private static final double TETRIARY_RADIUS = 0.021559365;
	

	@Override
	public Way findNearestWay(Point point) {
		SQLQuery query = getCurrentSession().createSQLQuery(
				"select way.*, " + Way.DISCRIMINATOR_VALUE + " as discriminator from " + Way.TABLE_NAME
						+ " as way where the_geom && :box order by ST_Distance(the_geom, :point)");
		query.setMaxResults(1);
		query.setParameter("point", point, SpatialTypes.GEOMETRY);
		query.setParameter("box", GeometryHelper.getExpandedEnvelopeAsGeometry(point, BOUNDING_BOX_SIZE), SpatialTypes.GEOMETRY);

		query.addEntity("way", Way.class);

		return (Way) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Way> findRoute(Integer startIndex, Integer endIndex, boolean useTrafficDataToRoute) {
		SQLQuery query = getCurrentSession()
				.createSQLQuery(
						"select way.*, "
								+ Way.DISCRIMINATOR_VALUE
								+ " as discriminator from astar_sp_delta_cc_directed(:ways_view_name, :start_index, :end_index, :box_size, :cost_column, :is_directed, :has_reverse_cost) as route inner join "
								+ Way.TABLE_NAME + " as way on route.gid = way.gid");

		query.setString("ways_view_name", useTrafficDataToRoute ? Way.WAYS_WITH_TRAFFIC_COSTS_VIEW_NAME
				: Way.WAYS_WITH_LENGTH_COSTS_VIEW_NAME);
		query.setInteger("start_index", startIndex);
		query.setInteger("end_index", endIndex);
		query.setDouble("box_size", BOUNDING_BOX_SIZE);
		query.setString("cost_column", Way.VIEW_COL_COST);
		query.setBoolean("is_directed", true);
		query.setBoolean("has_reverse_cost", true);

		query.addEntity("way", Way.class);

		return (List<Way>) query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WayWithSpeedInfo> getTrafficData(Point point, double radius) {
		if (radius > MAX_RADIUS) {
			return newArrayList();
		}
		
		SQLQuery query = getCurrentSession()
				.createSQLQuery(
						"select way.*, traffic.direct_way_speed, traffic.reverse_way_speed, "
								+ WayWithSpeedInfo.DISCRIMINATOR_VALUE
								+ " as discriminator from "
								+ Way.TABLE_NAME
								+ " as way inner join traffic_info as traffic on way.gid = traffic.way_gid where way.the_geom && :box" + getWayClassCondition(radius));
		query.setParameter("box", GeometryHelper.getExpandedEnvelopeAsGeometry(point, radius), SpatialTypes.GEOMETRY);

		query.addEntity("way", WayWithSpeedInfo.class);

		return (List<WayWithSpeedInfo>) query.list();
	}

	private String getWayClassCondition(double radius) {
		if (radius >= PRIMARY_RADIUS) {
			return " and way.class_id between " + MOTORWAY_CLASS_ID + " and " + PRIMARY_LINK_CLASS_ID;
		}
		if (radius >= SECONDARY_RADIUS) {
			return " and (way.class_id between " + MOTORWAY_CLASS_ID + " and " + SECONDARY_CLASS_ID
					+ " or way.class_id = " + ROUNDABOUNT_CLASS_ID + ")";
		}
		if (radius >= TETRIARY_RADIUS) {
			return " and (way.class_id between " + MOTORWAY_CLASS_ID + " and " + TETRIARY_CLASS_ID
					+ " or way.class_id = " + ROUNDABOUNT_CLASS_ID + ")";
		}
		return EMPTY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Way> getWaysInsideBox(Geometry box) {
		SQLQuery query = getCurrentSession().createSQLQuery(
				"select way.*, " + Way.DISCRIMINATOR_VALUE + " as discriminator from " + Way.TABLE_NAME
						+ " as way where the_geom && :box");
		query.setParameter("box", box, SpatialTypes.GEOMETRY);

		query.addEntity("way", Way.class);

		return (List<Way>) query.list();
	}

}
