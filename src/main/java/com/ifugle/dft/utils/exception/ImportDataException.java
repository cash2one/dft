package com.ifugle.dft.utils.exception;

public class ImportDataException extends Exception{
	private String message = "";
	public ImportDataException(){
		
	}
	public ImportDataException(String message) {
		super(message);
		this.message = message;
	}
	
	public void setMessage(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}

	public String toString() {
		return this.message;
	}
}
