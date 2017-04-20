package org.example.bcp;

public class RecordProcessingException extends Exception {
	private static final long serialVersionUID = -7745221656943818979L;
	
	public RecordProcessingException() {
		super();
	}
	
	public RecordProcessingException(String message) {
		super(message);
	}

	public RecordProcessingException(Throwable t) {
		super(t);
	}
	
	public RecordProcessingException(String message, Throwable t) {
		super(message, t);
	}
}
