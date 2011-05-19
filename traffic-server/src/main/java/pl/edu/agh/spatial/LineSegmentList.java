package pl.edu.agh.spatial;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.UnmodifiableListIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class LineSegmentList implements Iterable<LineSegment> {
	
	private ImmutableList<LineSegment> lines;

	public LineSegmentList(LineString lineString) {
		ImmutableList.Builder<LineSegment> listBuilder = new Builder<LineSegment>();
		
		Coordinate[] coords = lineString.getCoordinates();
		for (int i = 1; i < coords.length; i++) {
			listBuilder.add(new LineSegment(coords[i - 1], coords[i]));
		}
		
		lines = listBuilder.build(); 
	}

	@Override
	public UnmodifiableIterator<LineSegment> iterator() {
		return lines.iterator();
	}
	
	public UnmodifiableListIterator<LineSegment> listIterator() {
		return lines.listIterator();
	}
}
