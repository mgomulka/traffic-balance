package pl.edu.agh.logic;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.List;
import java.util.ListIterator;

import pl.edu.agh.utils.Collections;

import com.google.common.base.Predicate;

public class PathWithoutLowQualityMatchingsSplitter extends PathSplitter {

	private double qualityThreshold;
	
	private final Predicate<PointMatching> GOOD_QUALITY_MATCHING = new Predicate<PointMatching>() {
		@Override
		public boolean apply(PointMatching matching) {
			return matching.getCost() < qualityThreshold;
		}
	};
	
	public PathWithoutLowQualityMatchingsSplitter(PathSplitter parent, double qualityThreshold) {
		super(parent);
		this.qualityThreshold = qualityThreshold;
	}

	

	public PathWithoutLowQualityMatchingsSplitter(double qualityThreshold) {
		super(null);
		this.qualityThreshold = qualityThreshold;
	}

	@Override
	protected List<Path> doSplit(Path path) {
		List<Path> pathsWithoutLowQualityMatchings = newLinkedList();
		List<PointMatching> matchings = path.getMatchings();
		
		ListIterator<PointMatching> pathBegin = Collections.moveIteratorToNextElementMatchingPredicate(
				matchings.listIterator(), GOOD_QUALITY_MATCHING);
		while (pathBegin.hasNext()) {
			ListIterator<PointMatching> pathEnd = matchings.listIterator(pathBegin.nextIndex());
			pathEnd = Collections.moveIteratorToNextElementMatchingPredicate(pathEnd, not(GOOD_QUALITY_MATCHING));

			pathsWithoutLowQualityMatchings.add(Path.createPath(matchings.subList(pathBegin.nextIndex(),
					pathEnd.nextIndex())));

			pathBegin = Collections.moveIteratorToNextElementMatchingPredicate(pathEnd, GOOD_QUALITY_MATCHING);
		}

		return pathsWithoutLowQualityMatchings;
	}

}
