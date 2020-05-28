/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.network.NetworkConstants.DEFAULT_METRIC_PREFIX;

/**
 * Monitors network related metrics
 * 
 * @author Florencio Sarmiento
 *
 */
public class NetworkMonitor extends ABaseMonitor {
	
	public static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(NetworkMonitor.class);

	@Override
	protected String getDefaultMetricPrefix() {
		return DEFAULT_METRIC_PREFIX;
	}

	@Override
	public String getMonitorName() {
		return "Network";
	}

	@Override
	protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
    	NetworkMonitorTask networkMonitorTask = new NetworkMonitorTask(getContextConfiguration(), tasksExecutionServiceProvider.getMetricWriteHelper());
    	tasksExecutionServiceProvider.submit("NetworkTask", networkMonitorTask);

	}

	@Override
	protected List<Map<String, ?>> getServers() {
		List<Map<String, ?>> networkInterfaces = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("networkInterfaces");
		List<Map<String, ?>> anInterface = new ArrayList<>();
		anInterface.add(networkInterfaces.get(0));
		AssertUtils.assertNotNull(anInterface, "The 'networkInterfaces' section in config.yml is not configured");
		return anInterface;
	}
}
