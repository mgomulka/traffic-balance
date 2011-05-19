package pl.edu.agh.exception;

public class BusinessException extends RuntimeException {

	private Enum<?> error;
	
	public BusinessException(Enum<?> error) {
		this.error = error;
	}

	public Enum<?> getError() {
		return error;
	}	
	
}
