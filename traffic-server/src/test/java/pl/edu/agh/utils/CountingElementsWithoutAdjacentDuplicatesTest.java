package pl.edu.agh.utils;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CountingElementsWithoutAdjacentDuplicatesTest {

	@Test
	public void forListWithLessThanTwoElementsReturnsItsSize() {
		List<Integer> list = Arrays.asList(1);
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, list.size());
		
	}
	
	@Test
	public void forListWithTheSameElementsReturnsOne() {
		List<Integer> list = newArrayList(Arrays.asList(1, 1, 1, 1, 1));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, 1);
	}
	
	@Test
	public void forListWithNoAdjacentDuplicatesReturnsItsSize() {
		List<Integer> list = newArrayList(Arrays.asList(1, 2, 3, 2, 1));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, list.size());
	}
	
	@Test
	public void forListWithDuplicatesAtBeginningCountsProperly() {
		List<Integer> list = newArrayList(Arrays.asList(1, 1, 1, 2));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, 2);
	}
	
	@Test
	public void forListWithDuplicatesAtEndCountsProperly() {
		List<Integer> list = newArrayList(Arrays.asList(1, 2, 2, 2));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, 2);
	}
	
	@Test
	public void forListWithDuplicatesInsideCountsProperly() {
		List<Integer> list = newArrayList(Arrays.asList(1, 2, 2, 1));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, 3);
	}
	
	@Test
	public void forListWith2DifferentElementsReturnsItsSize() {
		List<Integer> list = newArrayList(Arrays.asList(1, 2));
		
		int elementsNumber = Collections.getNumberOfElementsWithoutAdjacentDuplicates(list);
		
		assertEquals(elementsNumber, 2);
	}
}
