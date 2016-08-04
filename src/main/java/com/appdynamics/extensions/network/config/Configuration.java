package com.appdynamics.extensions.network.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

public class Configuration {
	
	private String metricPrefix;
	
	private boolean overrideMetricsUsingScriptFile;
	
	private Set<String> networkInterfaces;
	
	private List<ScriptFile> scriptFiles;
	
	private int scriptTimeoutInSec;

	private Set<String> deltaMetrics;
	
	public String getMetricPrefix() {
		return metricPrefix;
	}

	public void setMetricPrefix(String metricPrefix) {
		if (StringUtils.isNotBlank(metricPrefix)) {
			this.metricPrefix = metricPrefix.trim();
			
		} else {
			this.metricPrefix = null;
			
		}
	}

	public boolean isOverrideMetricsUsingScriptFile() {
		return overrideMetricsUsingScriptFile;
	}

	public void setOverrideMetricsUsingScriptFile(
			boolean overrideMetricsUsingScriptFile) {
		this.overrideMetricsUsingScriptFile = overrideMetricsUsingScriptFile;
	}

	public Set<String> getNetworkInterfaces() {
		return networkInterfaces;
	}

	public void setNetworkInterfaces(Set<String> networkInterfaces) {
		Set<String> tmpNetworkInterfaces = null;
		
		if (networkInterfaces != null) {
			tmpNetworkInterfaces = new HashSet<String>();
			
			for (String networkInterface : networkInterfaces) {
				if (StringUtils.isNotBlank(networkInterface)) {
					tmpNetworkInterfaces.add(networkInterface.trim().toLowerCase());
				}
			}
		}
		
		this.networkInterfaces = tmpNetworkInterfaces;
	}

	public List<ScriptFile> getScriptFiles() {
		return scriptFiles != null ? scriptFiles : new ArrayList<ScriptFile>();
	}

	public void setScriptFiles(List<ScriptFile> scriptFiles) {
		this.scriptFiles = scriptFiles;
	}

	public int getScriptTimeoutInSec() {
		return scriptTimeoutInSec;
	}

	public void setScriptTimeoutInSec(int scriptTimeoutInSec) {
		this.scriptTimeoutInSec = scriptTimeoutInSec;
	}

	public Set<String> getDeltaMetrics() {
		if(deltaMetrics == null){
			deltaMetrics = Sets.newHashSet();
		}
		return deltaMetrics;
	}

	public void setDeltaMetrics(Set<String> deltaMetrics) {
		this.deltaMetrics = deltaMetrics;
	}
}
