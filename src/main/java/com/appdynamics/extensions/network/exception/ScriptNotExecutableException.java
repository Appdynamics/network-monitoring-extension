package com.appdynamics.extensions.network.exception;

public class ScriptNotExecutableException extends RuntimeException {

	private static final long serialVersionUID = 761041374910140833L;

	public ScriptNotExecutableException() {
	}

	public ScriptNotExecutableException(String paramString,
			Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}

	public ScriptNotExecutableException(String paramString) {
		super(paramString);
	}

	public ScriptNotExecutableException(Throwable paramThrowable) {
		super(paramThrowable);
	}

}
