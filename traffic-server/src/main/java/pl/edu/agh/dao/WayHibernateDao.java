package pl.edu.agh.dao;

import java.util.List;

import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.model.entity.Way;
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
				"select way.* from " + Way.TABLE_NAME
						+ " as way where the_geom && :box order by ST_Distance(the_geom, :point)");
		query.setMaxResults(1);
		query.setParameter("point", point, SpatialTypes.GEOMETRY);
		query.setParameter("box", getExtendedBoundingBox(point), SpatialTypes.GEOMETRY);

		query.addEntity("way", Way.class);

		return (Way) query.uniqueResult();
	}

	private Geometry getExtendedBoundingBox(Point point) {
		Envelope envelope = point.getEnvelopeInternal();
		envelope.expandBy(BOUNDING_BOX_SIZE);
		return geometryFactory.toGeometry(envelope);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Way> findRoute(Integer startIndex, Integer endIndex) {
		SQLQuery query = getCurrentSession().createSQLQuery(
				"select way.* from astar_sp_delta_cc_directed(:ways_table_name, :start_index, :end_index, :box_size, :cost_column, :is_directed, :has_reverse_cost) as route inner join "
						+ Way.TABLE_NAME + " as way on route.gid = way.gid");
		
		query.setString("ways_table_name", Way.VIEW_NAME);
		query.setInteger("start_index", startIndex);
		query.setInteger("end_index", endIndex);
		query.setDouble("box_size", BOUNDING_BOX_SIZE);
		query.setString("cost_column", Way.VIEW_COL_COST);
		query.setBoolean("is_directed", true);
		query.setBoolean("has_reverse_cost", true);
		
		query.addEntity("way", Way.class);
		
		return (List<Way>) query.list();
	}

}
