package pl.edu.agh.dao;

import java.util.List;

import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.model.entity.Way;
import pl.edu.agh.model.entity.WayWithSpeedInfo;
import pl.edu.agh.spatial.WGS84GeometryFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Component
public class WayHibernateDao extends AbstractDao implements WayDao {

	private static final double BOUNDING_BOX_SIZE = 0.1;
	@Autowired
	private WGS84GeometryFactory geometryFactory;

	@Override
	public Way findNearestWay(Point point) {
		SQLQuery query = getCurrentSession().createSQLQuery(
				"select way.*, " + Way.DISCRIMINATOR_VALUE + " as discriminator from " + Way.TABLE_NAME
						+ " as way where the_geom && :box order by ST_Distance(the_geom, :point)");
		query.setMaxResults(1);
		query.setParameter("point", point, SpatialTypes.GEOMETRY);
		query.setParameter("box", getExtendedBoundingBox(point, BOUNDING_BOX_SIZE), SpatialTypes.GEOMETRY);

		query.addEntity("way", Way.class);

		return (Way) query.uniqueResult();
	}

	private Geometry getExtendedBoundingBox(Point point, double boxSize) {
		Envelope envelope = point.getEnvelopeInternal();
		envelope.expandBy(boxSize);
		return geometryFactory.toGeometry(envelope);
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
	public List<WayWithSpeedInfo> getTrafficData(Point point) {
		SQLQuery query = getCurrentSession()
				.createSQLQuery(
						"select way.*, traffic.direct_way_speed, traffic.reverse_way_speed, "
								+ WayWithSpeedInfo.DISCRIMINATOR_VALUE
								+ " as discriminator from "
								+ Way.TABLE_NAME
								+ " as way inner join traffic_info as traffic on way.gid = traffic.way_gid where way.the_geom && :box");
		query.setParameter("box", getExtendedBoundingBox(point, 0.1), SpatialTypes.GEOMETRY);

		query.addEntity("way", WayWithSpeedInfo.class);

		return (List<WayWithSpeedInfo>) query.list();
	}

}
