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
		pathBegin = nextNonUTurnRoad(pathBegin, matchings);

		while (pathBegin.hasNext()) {
			ListIterator<PointMatching> pathEnd = matchings.listIterator(pathBegin.nextIndex());
			pathEnd = nextUTurnRoad(pathEnd, matchings);

			pathsWithoutUTurns.add(Path.createPath(matchings.subList(pathBegin.nextIndex(), pathEnd.nextIndex())));

			pathBegin = nextNonUTurnRoad(pathEnd, matchings);
		}

		return pathsWithoutUTurns;
	}

	private ListIterator<PointMatching> nextNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings) {
		return nextNonUTurnRoad(current, matchings, false);
	}

	private ListIterator<PointMatching> nextNonUTurnRoad(ListIterator<PointMatching> current,
			List<PointMatching> matchings, boolean isCurrentUTurn) {
		if (!current.hasNext()) {
			return current;
		}

		ListIterator<PointMatching> nextRoadMatching = PathUtils.iteratorForNextRoadMatching(current, matchings);

		if (nextRoadMatching.hasNext()
				&& isUTurn(matchings.get(current.nextIndex()).getRoad(), matchings.get(nextRoadMatching.nextIndex())
						.getRoad())) {
			return nextNonUTurnRoad(nextRoadMatching, matchings, true);
		}

		return isCurrentUTurn ? nextRoadMatching : current;
	}

	private boolean isUTurn(Road road1, Road road2) {
		return (road1.getSource() == road2.getTarget()) && (road1.getTarget() == road2.getSource());
	}

	private ListIterator<PointMatching> nextUTurnRoad(ListIterator<PointMatching> current, List<PointMatching> matchings) {
		if (!current.hasNext()) {
			return current;
		}

		ListIterator<PointMatching> nextRoadMatching = PathUtils.iteratorForNextRoadMatching(current, matchings);

		if (!nextRoadMatching.hasNext()) {
			return nextRoadMatching;
		}

		return (isUTurn(matchings.get(current.nextIndex()).getRoad(), matchings.get(nextRoadMatching.nextIndex())
				.getRoad())) ? current : nextUTurnRoad(nextRoadMatching, matchings);
	}
}
