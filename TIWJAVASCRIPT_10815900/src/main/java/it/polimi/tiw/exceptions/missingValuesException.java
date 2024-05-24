package it.polimi.tiw.exceptions;

public class missingValuesException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public missingValuesException() {
        super();
    }

	public missingValuesException(String message) {
		super(message);
	}
}