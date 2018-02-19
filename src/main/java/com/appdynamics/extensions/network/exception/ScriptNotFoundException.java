/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

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
