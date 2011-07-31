package pl.edu.agh.logic;

import java.util.List;

import com.vividsolutions.jts.geom.LineString;

import pl.edu.agh.spatial.LineSegmentList;

public class Road extends LineSegmentList {

	private int source;
	private int target;
	private int id;
	private boolean reversed;

	public Road(List<LineString> segments, int id, int source, int target, boolean reversed) {
		super(segments);
		this.id = id;
		this.source = source;
		this.target = target;
		this.reversed = reversed;
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

	public int getId() {
		return id;
	}

	public boolean isReversed() {
		return reversed;
	}
	
	

}
