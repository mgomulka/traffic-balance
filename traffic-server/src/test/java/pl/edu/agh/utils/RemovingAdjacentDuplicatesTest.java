package pl.edu.agh.utils;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class RemovingAdjacentDuplicatesTest {

	@Test
	public void forListWithLessThanTwoElementsDoesNotModifyIt() {
		List<Integer> actual = Arrays.asList(1);
		List<Integer> expected = newArrayList(actual);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void forListWithTheSameElementsReturnsListWithOneElement() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 1, 1, 1, 1));
		List<Integer> expected = Arrays.asList(1);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void forListWithNoAdjacentDuplicatesDoesNotModifyIt() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 2, 3, 2, 1));
		List<Integer> expected = newArrayList(actual);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void forListWithDuplicatesAtBeginningRemovesThem() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 1, 1, 2));
		List<Integer> expected = Arrays.asList(1, 2);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void forListWithDuplicatesAtEndRemovesThem() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 2, 2, 2));
		List<Integer> expected = Arrays.asList(1, 2);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void forListWithDuplicatesInsideRemovesThem() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 2, 2, 1));
		List<Integer> expected = Arrays.asList(1, 2, 1);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void forListWith2DifferentElementsDoesNotModyfyList() {
		List<Integer> actual = newArrayList(Arrays.asList(1, 2));
		List<Integer> expected = Arrays.asList(1, 2);
		
		actual = Collections.removeAdjacentDuplicates(actual);
		
		assertEquals(expected, actual);
	}
}
