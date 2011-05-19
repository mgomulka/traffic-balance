package pl.edu.agh.dao;

import java.util.List;

import pl.edu.agh.model.entity.Way;

import com.vividsolutions.jts.geom.Point;

public interface WayDao {
	
	public Way findNearestWay(Point point); 
	
	public List<Way> findRoute(Integer startIndex, Integer endIndex);
}
