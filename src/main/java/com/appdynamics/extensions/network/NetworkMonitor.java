/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.network.config.Configuration;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.appdynamics.extensions.network.NetworkConstants.*;
import static com.appdynamics.extensions.network.util.MetricUtil.resolvePath;

/**
 * Monitors network related metrics
 * 
 * @author Florencio Sarmiento
 *
 */
public class NetworkMonitor extends AManagedMonitor {
	
	public static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(NetworkMonitor.class);
	
	private String metricPrefix;

    private Cache<String, BigInteger> prevMetricsMap;

    public NetworkMonitor() {
        System.out.println(logVersion());

        prevMetricsMap = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public TaskOutput execute(Map<String, String> args,
			TaskExecutionContext arg1) throws TaskExecutionException {
		
		LOGGER.info(logVersion());
		
		if (args != null) {
			
			String configFilename = resolvePath(args.get(CONFIG_ARG));
			
			try {
				Configuration config = readConfig(configFilename);
				setMetricPrefix(config);
				collectAndPrintMetrics(config);
				
				return new TaskOutput("Network monitoring task successfully completed");
				
			} catch (FileNotFoundException ex) {
				LOGGER.error("Config file not found: " + configFilename, ex);
				
			} catch (Exception ex) {
				LOGGER.error("Unfortunately an issue has occurred: ", ex);
			}
			
		}
		
		throw new TaskExecutionException("Network monitoring task completed with failures.");
	}
	
	private void setMetricPrefix(Configuration config) {
		metricPrefix = config.getMetricPrefix();
		
		if (Strings.isNullOrEmpty(metricPrefix)) {
			metricPrefix = DEFAULT_METRIC_PREFIX;
			
		} else {
			metricPrefix = metricPrefix.trim();
			
			if (!metricPrefix.endsWith(DEFAULT_DELIMETER)) {
				metricPrefix = metricPrefix + DEFAULT_DELIMETER;
			}
		}
	}
	
	private void collectAndPrintMetrics(Configuration config) {
		ScriptMetrics scriptMetrics = getScriptMetrics(config);
		SigarMetrics sigarMetrics = new SigarMetrics(config.getNetworkInterfaces());
		
		NetworkMetricsCollector metricsCollector = new NetworkMetricsCollector
				(sigarMetrics, scriptMetrics, config.getNetworkInterfaces());
		
		uploadMetrics(config,metricsCollector.collectMetrics());
	}
	
    private Configuration readConfig(String configFilename) throws FileNotFoundException {
    	LOGGER.info("Reading config file: " + configFilename);
        Yaml yaml = new Yaml(new Constructor(Configuration.class));
        Configuration config = (Configuration) yaml.load(new FileInputStream(configFilename));
        return config;
    }
    
    private ScriptMetrics getScriptMetrics(Configuration config) {
    	ScriptMetrics scriptMetrics = null;
    	
    	if (config.isOverrideMetricsUsingScriptFile()) {
    		debugLog("Override metrics using script is enabled, attempting to retrieve metrics from script...");
    		
    		try {
    			ScriptMetricsExecutor scriptMetricsExecutor = new ScriptMetricsExecutor();
    			scriptMetrics = scriptMetricsExecutor.executeAndCollectScriptMetrics(
    					config.getScriptFiles(), getScriptTimeout(config));
    			
    		} catch (Exception ex) {
    			LOGGER.error("Unfortunately an error has occurred while fetching metrics from script", ex);
    		}
    	} 
    	
    	return scriptMetrics != null ? scriptMetrics : new ScriptMetrics();
    }
    
    private long getScriptTimeout(Configuration config) {
    	return config.getScriptTimeoutInSec() > 0 ? config.getScriptTimeoutInSec() : DEFAULT_SCRIPT_TIMEOUT_IN_SEC;
    }
	
	private void uploadMetrics(Configuration config,Map<String, BigInteger> metrics) {
		Set<String> deltaMetrics = config.getDeltaMetrics();
		LOGGER.debug("Delta metrics configured for " + Arrays.toString(deltaMetrics.toArray()));
		for (Map.Entry<String, BigInteger> metric : metrics.entrySet()) {
			if(deltaMetrics.contains(metric.getKey())){
				LOGGER.debug("Computing delta for metric " + metric.getKey());
				BigInteger prevValue = processDelta(metricPrefix + metric.getKey(), metric.getValue());
				if (prevValue != null) {
					printCollectiveAverageAverage(metricPrefix + metric.getKey(), getDeltaValue(metric.getValue(), prevValue));
				}
			}
			else {
				printCollectiveAverageAverage(metricPrefix + metric.getKey(), metric.getValue());
			}
        }
	}

    private BigInteger processDelta(String metricName, BigInteger metricValue) {
        BigInteger prevValue = prevMetricsMap.getIfPresent(metricName);
        if(metricValue != null) {
            prevMetricsMap.put(metricName, metricValue);
        }
        return prevValue;
    }

    private BigInteger getDeltaValue(BigInteger currentValue, BigInteger prevValue) {
        if(currentValue == null) {
            return prevValue;
        }
        return currentValue.subtract(prevValue);
    }
	
    private void printCollectiveObservedCurrent(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }

    private void printCollectiveAverageAverage(String metricName, BigInteger metricValue) {
        printMetric(metricName, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }
    
    private void printMetric(String metricName, BigInteger metricValue, String aggregation, String timeRollup, String cluster) {
		MetricWriter metricWriter = getMetricWriter(metricName, aggregation,
				timeRollup, cluster);
        
        String value = metricValue != null ? metricValue.toString() : BigInteger.ZERO.toString();
        
        debugLog("Sending [%s/%s/%s] metric = %s = %s",
            		aggregation, timeRollup, cluster,
                    metricName, value);
        
        metricWriter.printMetric(value);
    }

	private void debugLog(String msg, Object... params) {
		if (LOGGER.isDebugEnabled()) {
			String msgToPrint = msg;
			
			if (params != null) {
				msgToPrint = String.format(msgToPrint, params);
			} 
			
			LOGGER.debug(msgToPrint);
		}
	}

	private String logVersion() {
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		return msg;
	}
	
    public static String getImplementationVersion(){
        return NetworkMonitor.class.getPackage().getImplementationTitle();
    }
}
