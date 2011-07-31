package pl.edu.agh.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.model.Way;
import pl.edu.agh.model.WayWithSpeedInfo;
import pl.edu.agh.spatial.WGS84GeometryFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/conf/utils.xml",
		"file:src/main/webapp/WEB-INF/conf/db.xml", "file:src/main/webapp/WEB-INF/conf/dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class WayDaoTest {

	@Autowired
	private WayDao wayDao;

	@Autowired
	private WGS84GeometryFactory geometryFactory;

	@Test
	public void canReadNearestWay() {
		Way way = wayDao.findNearestWay(geometryFactory.createPoint(20.235386, 49.594534));
		assertNotNull(way);
	}
	
	@Test
	public void canCalculateRoute() {
		List<Way> route = wayDao.findRoute(119, 133, true);
		assertFalse(route.isEmpty());
	}
	
	@Test
	public void canReadTrafficInfo() {
		List<WayWithSpeedInfo> trafficInfo = wayDao.getTrafficData(geometryFactory.createPoint(19.9245182, 50.0651936), 0.1);
		assertFalse(trafficInfo.isEmpty());
	}
	
	@Test
	public void canGetWaysInsideBox() {
		Point point = geometryFactory.createPoint(20.235386, 49.594534);
		Envelope envelope = point.getEnvelopeInternal();
		envelope.expandBy(0.002);
		
		List<Way> ways = wayDao.getWaysInsideBox(geometryFactory.toGeometry(envelope));
		assertFalse(ways.isEmpty());
	}
}
