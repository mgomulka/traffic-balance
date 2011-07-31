package pl.edu.agh.utils;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Predicate;

public class Collections {

	public static <T> List<T> concatLists(List<T>... lists) {
		List<T> result = newArrayList();

		for (List<T> list : lists) {
			result.addAll(list);
		}

		return result;
	}

	public static <T> List<T> removeAdjacentDuplicates(List<T> list) {
		return removeAdjacentDuplicates(list, null);
	}

	public static <T> List<T> removeAdjacentDuplicates(List<T> list, Comparator<T> comparator) {
		if (list.size() < 2) {
			return list;
		}

		List<T> listWithoutDuplicates = newArrayList();

		ListIterator<T> first = list.listIterator(1);
		ListIterator<T> second = list.listIterator(0);

		T firstElement = null;
		T secondElement = null;

		while (first.hasNext()) {
			firstElement = first.next();
			secondElement = second.next();

			if (!areEqual(firstElement, secondElement, comparator)) {
				listWithoutDuplicates.add(secondElement);
			}
		}

		listWithoutDuplicates.add(firstElement);

		list.clear();
		list.addAll(listWithoutDuplicates);

		return list;
	}

	public static <T> int getNumberOfElementsWithoutAdjacentDuplicates(List<T> list) {
		return getNumberOfElementsWithoutAdjacentDuplicates(list, null);
	}

	public static <T> int getNumberOfElementsWithoutAdjacentDuplicates(List<T> list, Comparator<T> comparator) {
		if (list.size() < 2) {
			return list.size();
		}

		int elementsNumber = 0;

		ListIterator<T> first = list.listIterator(1);
		ListIterator<T> second = list.listIterator(0);

		T firstElement = null;
		T secondElement = null;

		while (first.hasNext()) {
			firstElement = first.next();
			secondElement = second.next();

			if (!areEqual(firstElement, secondElement, comparator)) {
				++elementsNumber;
			}
		}

		return elementsNumber + 1;
	}

	private static <T> boolean areEqual(T element1, T element2, Comparator<T> comparator) {
		return comparator != null ? (comparator.compare(element1, element2) == 0) : element1.equals(element2);
	}

	public static <T> ListIterator<T> moveIteratorToNextElementMatchingPredicate(ListIterator<T> iterator,
			Predicate<T> predicate) {
		boolean foundElementsMatchingPredicate = false;

		while (iterator.hasNext() && !foundElementsMatchingPredicate) {
			if (predicate.apply(iterator.next())) {
				foundElementsMatchingPredicate = true;
			}
		}

		if (foundElementsMatchingPredicate) {
			iterator.previous();
		}

		return iterator;
	}
}
