package pl.edu.agh.logic;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public abstract class PathSplitter {

	private PathSplitter parentSplitter;

	protected abstract List<Path> doSplit(Path path);

	protected PathSplitter(PathSplitter parent) {
		this.parentSplitter = parent;
	}

	public List<Path> split(Path path) {
		if (parentSplitter == null) {
			return doSplit(path);
		}

		List<Path> splittedByParent = parentSplitter.doSplit(path);
		List<Path> result = newArrayList();

		for (Path splitted : splittedByParent) {
			result.addAll(doSplit(splitted));
		}

		return result;
	}

}
