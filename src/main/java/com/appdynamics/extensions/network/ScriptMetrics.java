package com.appdynamics.extensions.network;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds the collected metrics from script
 * 
 * Uses {@TreeMap} with {@String.CASE_INSENSITIVE_ORDER} 
 * to ignore case when comparing map key (metricName)
 * 
 * @author Florencio Sarmiento
 *
 */
public class ScriptMetrics {
	
	private Map<String, BigInteger> metrics = new TreeMap<String, BigInteger>(String.CASE_INSENSITIVE_ORDER);

	public boolean isContains(String metricName) {
		 return getMetrics().containsKey(metricName);
	}
	
	public BigInteger getMetricValue(String metricName) {
		return getMetrics().get(metricName);
	}
	
	public void addMetric(String name, BigInteger value) {
		getMetrics().put(name, value);
	}
	
	public void removeMetric(String name) {
		getMetrics().remove(name);
	}
	
	public Map<String, BigInteger> getMetrics() {
		
		if (metrics == null) {
			setMetrics(new TreeMap<String, BigInteger>(String.CASE_INSENSITIVE_ORDER));
		}
		
		return metrics;
	}
	
	private void setMetrics(Map<String, BigInteger> metrics) {
		this.metrics = metrics;
	}

}
