/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

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
