package pl.edu.agh.dao;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import pl.edu.agh.model.SpeedInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/conf/utils.xml",
		"file:src/main/webapp/WEB-INF/conf/db.xml", "file:src/main/webapp/WEB-INF/conf/dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class SpeedInfoDaoTest {

	@Autowired
	private SpeedInfoDao speedInfoDao;

	
	@Test
	public void canSaveSpeedInfo() {
		SpeedInfo info = new SpeedInfo();
		info.setWayGid(1);
		info.setDirectWaySpeed(20.0);
		info.setTime(new Date());
		
		speedInfoDao.save(info);
	}
	
}
