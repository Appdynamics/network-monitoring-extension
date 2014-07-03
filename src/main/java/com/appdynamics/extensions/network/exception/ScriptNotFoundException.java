package com.appdynamics.extensions.network.exception;

public class ScriptNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = -9126966838401209593L;

	public ScriptNotFoundException() {
	}

	public ScriptNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptNotFoundException(String message) {
		super(message);
	}

	public ScriptNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
