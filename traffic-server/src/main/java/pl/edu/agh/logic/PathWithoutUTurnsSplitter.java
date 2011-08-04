package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.List;
import java.util.ListIterator;

public class PathWithoutUTurnsSplitter extends PathSplitter {

	public PathWithoutUTurnsSplitter(PathSplitter parent) {
		super(parent);
	}

	public PathWithoutUTurnsSplitter() {
		super(null);
	}

	@Override
	protected List<Path> doSplit(Path path) {
		List<Path> pathsWithoutUTurns = newLinkedList();
		List<PointMatching> matchings = path.getMatchings();

		ListIterator<PointMatching> pathBegin = matchings.listIterator();
		pathBegin = PathUtils.nextNonUTurnRoad(pathBegin, matchings);

		while (pathBegin.hasNext()) {
			ListIterator<PointMatching> pathEnd = matchings.listIterator(pathBegin.nextIndex());
			pathEnd = PathUtils.nextUTurnRoad(pathEnd, matchings);

			pathsWithoutUTurns.add(Path.createPath(matchings.subList(pathBegin.nextIndex(), pathEnd.nextIndex())));

			pathBegin = PathUtils.nextNonUTurnRoad(pathEnd, matchings);
		}

		return pathsWithoutUTurns;
	}
}
