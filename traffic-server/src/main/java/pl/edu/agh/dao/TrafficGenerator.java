package pl.edu.agh.dao;

import java.util.List;
import java.util.Random;

import org.hibernate.Query;
import org.springframework.stereotype.Component;

import pl.edu.agh.model.Way;

@Component
public class TrafficGenerator extends AbstractDao {
	
	private Random rand = new Random();
	
	public void generate(List<Way> ways) {
		Double currentSpeed = rand.nextDouble() * 90;
		
		for (Way way : ways) {
			Query query = getCurrentSession().createSQLQuery("select * from traffic_info where way_gid = :way_id");
			query.setInteger("way_id", way.getGid());
			Object result = query.uniqueResult();
			if (result == null) {
				boolean change = rand.nextInt(10) > 6;
				if (change) currentSpeed = rand.nextDouble() * 90;
				
				String par = way.isOneWay() ? "null" : ":rev_speed";
				Query insertingQuery = getCurrentSession()
						.createSQLQuery(
								"insert into traffic_info(way_gid, direct_way_speed, reverse_way_speed) values (:way_id, :speed, " + par + ")");
				insertingQuery.setInteger("way_id", way.getGid());
				insertingQuery.setDouble("speed", currentSpeed);
				if (!way.isOneWay()) {
					insertingQuery.setParameter("rev_speed", currentSpeed);
				}
				insertingQuery.executeUpdate();
			}
		}
	}
}
