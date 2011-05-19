package pl.edu.agh.utils;

import pl.edu.agh.exception.BusinessException;

public class Preconditions {
	
	public static <T> T checkNotNull(T reference, Enum<?> error) {
		if (reference == null) {
			throw new BusinessException(error);
		}

		return reference;
	}
}
