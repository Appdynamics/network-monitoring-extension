/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

/**
 * @author Florencio Sarmiento
 *
 */
public class NetworkConstants {

	public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Network";
	
	public static final String DEFAULT_DELIMETER = "|";
	
	public static final String TCP = "TCP";
	
	public static final String STATE = "State";
	
	public static final String CONFIG_ARG = "config-file";
	
	public static final String WINDOWS_OS = "windows";
	
	public static final String UNIX_BASE_OS = "unixBase";
	
	public static final String METRIC_OUTPUT_PATTERN = "(?i)\\b(name)\\s*=([^,;]*\\w+[^,;]*),\\s*(value)\\s*=\\s*-{0,1}\\d+.*$";
	
	public static final String METRIC_DATA_DELIMETER = ",";
	
	public static final String METRIC_VALUE_DELIMETER = "=";
	
	public static final int DEFAULT_SCRIPT_TIMEOUT_IN_SEC = 60;
	
	public static final int MAX_NUM_EXECUTOR_THREADS = 2;
	
}
