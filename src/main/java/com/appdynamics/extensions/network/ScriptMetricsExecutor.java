/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import static com.appdynamics.extensions.network.NetworkConstants.*;
import static com.appdynamics.extensions.network.util.MetricUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.network.config.ScriptFile;
import com.appdynamics.extensions.network.exception.ScriptNotExecutableException;
import com.appdynamics.extensions.network.exception.ScriptNotFoundException;

/**
 * Executes the appropriate script and retrieves the output metrics
 * 
 * @author Florencio Sarmiento
 *
 */
public class ScriptMetricsExecutor {
	
	public static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.network.ScriptMetricsExecutor");
	
    private ExecutorService inputStreamExecutor;
    private ExecutorService processExecutor;
    
	public ScriptMetricsExecutor() {
		processExecutor = Executors.newSingleThreadExecutor();
        inputStreamExecutor = Executors.newFixedThreadPool(MAX_NUM_EXECUTOR_THREADS);
	}

	public ScriptMetrics executeAndCollectScriptMetrics(List<ScriptFile> scriptFiles, long timeout) throws Exception {
		if (scriptFiles == null || scriptFiles.isEmpty()) {
			throw new IllegalArgumentException("No script files are provided");
		}
		
		ScriptFile scriptFileForOS = getScriptFileForRunningOS(scriptFiles);
		String scriptPath = resolvePath(scriptFileForOS.getFilePath());
		File scriptToExecute = new File(scriptPath);
		checkIfScriptExistsAndExecutable(scriptToExecute);
		
		return executeScriptAndRetrieveMetrics(scriptToExecute, timeout);
	}
	
	private ScriptFile getScriptFileForRunningOS(List<ScriptFile> scriptFiles) {
		String os = System.getProperty("os.name").toLowerCase();
		
		for (ScriptFile scriptFile : scriptFiles) {
			if (os.startsWith(WINDOWS_OS)) {
				if (scriptFile.getOsType().equalsIgnoreCase(WINDOWS_OS)) {
					return scriptFile;
				}
				
			} else if (scriptFile.getOsType().equalsIgnoreCase(UNIX_BASE_OS)) {
				return scriptFile;
			}
		}
		
		throw new ScriptNotFoundException("No script found compatible for OS: " + os);
	}
	
	private void checkIfScriptExistsAndExecutable(File file) {
		if (!file.exists()) {
			throw new ScriptNotFoundException("Unable to find script " + file.getPath());
			
		} else if (!file.canExecute()) {
			throw new ScriptNotExecutableException(
					String.format("Unable to execute %s. Please check user's executable permission", file.getPath()));
		}
	}
	
	private ScriptMetrics executeScriptAndRetrieveMetrics(File scriptToRun, long timeout) throws Exception {
		debugLog("Executing script %s", scriptToRun.getPath());
		
		ScriptMetrics scriptMetrics;
		ProcessBuilder processBuilder = new ProcessBuilder(scriptToRun.getPath());
		
		Process process = null;
		
		try {
			process = processBuilder.start();
			
			Future<ScriptMetrics> futureScriptMetrics = inputStreamExecutor.submit(
					processInputStream(process.getInputStream()));
			
			int exitCode = getScriptExitCode(process, timeout);
			scriptMetrics = futureScriptMetrics.get();
			
			debugLog("Exit code for %s is %s", scriptToRun.getPath(), exitCode);
			debugLog("Metrics retrieved from script: %s", scriptMetrics.getMetrics());
			
		} finally {
			shutdownExecutorService(inputStreamExecutor);
			shutdownExecutorService(processExecutor);
			closeProcess(process);
		}
		
		return scriptMetrics;
	}
	
	private int getScriptExitCode(final Process process, long timeout) throws Exception {
		return timedCall(new Callable<Integer>() {

			public Integer call() throws Exception {
	            return process.waitFor();
	        }
	        
		}, timeout, TimeUnit.SECONDS);
	}
	
	private <T> T timedCall(Callable<T> callable, long timeout, TimeUnit timeUnit) 
			throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<T> task = new FutureTask<T>(callable);
		processExecutor.execute(task);
		return task.get(timeout, timeUnit);
	}
	
	private Callable<ScriptMetrics> processInputStream(final InputStream inputStream) throws Exception {
		return new Callable<ScriptMetrics>() {
			public ScriptMetrics call() throws Exception {
				Pattern metricPattern = Pattern.compile(METRIC_OUTPUT_PATTERN,
						Pattern.CASE_INSENSITIVE);
				
				ScriptMetrics scriptMetrics = new ScriptMetrics();
				BufferedReader bufReader = null;

				try {
					bufReader = new BufferedReader(new InputStreamReader(inputStream));

					String output;

					while ((output = bufReader.readLine()) != null) {
						if (isMatchesMetricOutputPattern(output, metricPattern)) {
							processOutputAsMetric(output, scriptMetrics);
						}
					}
					
				} finally {
					if (bufReader != null) {
						try {
							bufReader.close();
						} catch (IOException ex) {}
					}
				}

				return scriptMetrics;
			}
		};
	}
	
	private boolean isMatchesMetricOutputPattern(String output, Pattern metricPattern) {
		Matcher regexMatcher = metricPattern.matcher(output.trim());
		return regexMatcher.matches();
	}
	
	private void processOutputAsMetric(String output, ScriptMetrics scriptMetrics) {
		String[] rawMetricData = output.split(METRIC_DATA_DELIMETER);
		String metricName = extractMetricName(rawMetricData[0]);
		BigInteger value = extractMetricValue(rawMetricData[1]);
		scriptMetrics.addMetric(metricName, value);
	}
	
	/**
	 * Extract metric name and removes all unnecessary spaces
	 * 
	 * @param data - raw data
	 * @return metric name
	 */
	private String extractMetricName(String data) {
		String metricName = data.substring(data.indexOf(METRIC_VALUE_DELIMETER) + 1);
		
		metricName = metricName.replaceAll("(\\s*\\|\\s*)", DEFAULT_DELIMETER).trim();
		
		if (metricName.startsWith(DEFAULT_DELIMETER)) {
			metricName = metricName.substring(1);
		}
		
		if (metricName.endsWith(DEFAULT_DELIMETER)) {
			metricName = metricName.substring(0, metricName.length() - 1);
		}

		return metricName;
	}
	
	private BigInteger extractMetricValue(String data) {
		String strValue = data.substring(data.indexOf(METRIC_VALUE_DELIMETER) + 1).trim();
		BigInteger value = null;
		
		try {
			if (strValue.contains(".")) {
				value = BigInteger.valueOf(Math.round(Double.valueOf(strValue)));
				
			} else {
				value = BigInteger.valueOf(Long.valueOf(strValue));
			}
			
		} catch (NumberFormatException ex) {
			LOGGER.error(String.format("Unable to convert [%s] to BigInteger, defaulting to 0", strValue));
		}
		
		return defaultValueToZeroIfNullOrNegative(value);
	}
	
	private void shutdownExecutorService(ExecutorService executorService) {
		try {
			if (executorService != null
					&& (!executorService.isShutdown() || !executorService.isTerminated())) {
				executorService.shutdownNow();
			}
			
		} catch (Exception ex) {
			LOGGER.warn("Unable to shutdown the executor service:", ex);
		}
	}
	
	private void closeProcess(Process process) {
		if (process != null) {
			process.destroy();
		}
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

}
