package pl.edu.agh.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	protected Session getCurrentSession() {
    	return sessionFactory.getCurrentSession();
    }

}
