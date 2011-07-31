package pl.edu.agh.logic;

import java.util.Comparator;

public class PathComparatorByCost implements Comparator<Path> {

	public static final PathComparatorByCost COMPARATOR = new PathComparatorByCost();
	
	@Override
	public int compare(Path path1, Path path2) {
		return Double.compare(path1.getCost(), path2.getCost());
	}
	
	

}
