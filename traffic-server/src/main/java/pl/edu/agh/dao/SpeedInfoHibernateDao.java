package pl.edu.agh.dao;

import org.springframework.stereotype.Component;

import pl.edu.agh.model.SpeedInfo;

@Component
public class SpeedInfoHibernateDao extends AbstractDao implements SpeedInfoDao {

	@Override
	public void save(SpeedInfo speedInfo) {
		getCurrentSession().save(speedInfo);
	}

}
