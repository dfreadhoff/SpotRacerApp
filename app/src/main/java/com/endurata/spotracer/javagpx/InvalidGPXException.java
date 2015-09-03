package com.endurata.spotracer.javagpx;

public class InvalidGPXException extends Exception {
	public InvalidGPXException(String message) {
		super(message);
	}
	public InvalidGPXException(Exception e) {
		super(e);
	}
}
