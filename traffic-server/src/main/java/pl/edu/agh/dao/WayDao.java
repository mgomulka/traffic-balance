package pl.edu.agh.dao;

import java.util.List;

import pl.edu.agh.model.Way;
import pl.edu.agh.model.WayWithSpeedInfo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public interface WayDao {
	
	public Way findNearestWay(Point point); 
	
	public List<Way> findRoute(Integer startIndex, Integer endIndex, boolean useTrafficDataToRoute);
	
	public List<WayWithSpeedInfo> getTrafficData(Point point, double radius);
	
	public List<Way> getWaysInsideBox(Geometry box);
}
