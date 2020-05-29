/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.network.input.MetricConfig;
import com.appdynamics.extensions.network.input.Stat;
import com.appdynamics.extensions.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;

import static com.appdynamics.extensions.network.util.MetricUtil.defaultValueToZeroIfNullOrNegative;
import static com.appdynamics.extensions.network.util.MetricUtil.isValueNullOrNegative;

/**
 * Collects network metrics in this order: 
 * 
 * 1. Script metrics (if available)
 * 2. Sigar metrics
 * 3. Default to zero if failing the above
 * 
 * @author Florencio Sarmiento
 *
 */
public class NetworkMetricsCollector {
	
	public static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(NetworkMetricsCollector.class);
	
	private List<Metric> metrics = Lists.newArrayList();
	
	private SigarMetrics sigarMetrics;
	
	private ScriptMetrics scriptMetrics;
	
	private Set<String> networkInterfaces;
	private Stat.Stats statsFromMetricsXml;
	private String metricPrefix;
	private static ObjectMapper objectMapper = new ObjectMapper();

	public NetworkMetricsCollector(SigarMetrics sigarMetrics,
								   ScriptMetrics scriptMetrics, Set<String> networkInterfaces, Stat.Stats statsFromMetricsXml, String metricPrefix) {
		
		if (sigarMetrics == null || networkInterfaces == null || networkInterfaces.isEmpty()) {
			throw new IllegalArgumentException("Sigar metrics and network interfaces must be provided");
		}
		
		this.sigarMetrics = sigarMetrics;
		this.scriptMetrics = scriptMetrics;
		this.networkInterfaces = networkInterfaces;
		this.statsFromMetricsXml = statsFromMetricsXml;
		this.metricPrefix = metricPrefix;
	}
	
	public List<Metric> collectMetrics() {
		for (Stat stat : statsFromMetricsXml.getStats()) {
			if (StringUtils.hasText(stat.getName()) && stat.getName().equalsIgnoreCase("networkInterfaceMetrics")) {
				collectNetInterfaceMetrics(stat.getMetricConfig());
			}
			else if (StringUtils.hasText(stat.getName())) {
				collectTcpMetrics(stat.getName(), stat.getMetricConfig());
			} else {
				collectOtherMetrics(stat.getMetricConfig());
			}
		}


		collectSrtiptMetrics();
		return metrics;
	}
	
	private void collectSrtiptMetrics() {
		Iterator<String> itr= scriptMetrics.getMetrics().keySet().iterator();
		while(itr.hasNext()){
			String metricName= itr.next();
			//TODO: Write Test Cases
			boolean isSigarOverride = false;
			for(Metrics sigarMetric : Metrics.values()){
				if(sigarMetric.getDisplayName().equals(metricName)){
					isSigarOverride =true;
					break;
				}
			}
			if(!isSigarOverride){
				// add script metric if it is not Sigar override.
				addMetric(metricName, scriptMetrics.getMetricValue(metricName), null);
			}
		}		
	}

	private void collectNetInterfaceMetrics(MetricConfig[] metricConfigs) {
		Map<String, MetricConfig> metricsFromXml = Maps.newHashMap();
		for (MetricConfig metricConfig : metricConfigs) {
			metricsFromXml.put(metricConfig.getAttr(), metricConfig);
		}
		for (String netInterfaceName : networkInterfaces) {
			addRxBytesMetric(netInterfaceName, metricsFromXml);
			addRxDroppedMetric(netInterfaceName, metricsFromXml);
			addRxErrorsMetric(netInterfaceName, metricsFromXml);
			addRxFrameMetric(netInterfaceName, metricsFromXml);
			addRxOverrunsMetric(netInterfaceName, metricsFromXml);
			addRxPacketsMetric(netInterfaceName, metricsFromXml);
			addSpeedMetric(netInterfaceName, metricsFromXml);
			addTxBytesMetric(netInterfaceName, metricsFromXml);
			addTxCarrierMetric(netInterfaceName, metricsFromXml);
			addTxCollisionsMetric(netInterfaceName, metricsFromXml);
			addTxDroppedMetric(netInterfaceName, metricsFromXml);
			addTxErrorsMetric(netInterfaceName, metricsFromXml);
			addTxOverrunsMetric(netInterfaceName, metricsFromXml);
			addTxPacketsMetric(netInterfaceName, metricsFromXml);
		}
	}
	
	private void collectTcpMetrics(String statname, MetricConfig[] metricConfigs) {
		Map<String, MetricConfig> metricsFromXml = Maps.newHashMap();
		for (MetricConfig metricConfig : metricConfigs) {
			metricsFromXml.put(metricConfig.getAttr(), metricConfig);
		}
		addTcpActiveOpensMetric(statname, metricsFromXml);
		addTcpAttemptFailsMetric(statname, metricsFromXml);
		addTcpCurrentEstablishedMetric(statname, metricsFromXml);
		addTcpEstablishedResetsMetric(statname, metricsFromXml);
		addTcpInErrorsMetric(statname, metricsFromXml);
		addTcpInSegmentsMetric(statname, metricsFromXml);
		addTcpOutResetsMetric(statname, metricsFromXml);
		addTcpOutSegmentsMetric(statname, metricsFromXml);
		addTcpPassiveOpensMetric(statname, metricsFromXml);
		addTcpRetransSegmentsMetric(statname, metricsFromXml);
		addTcpInboundTotalMetric(statname, metricsFromXml);
		addTcpOutboundTotalMetric(statname, metricsFromXml);
		addTcpStateBoundMetric(statname, metricsFromXml);
		addTcpStateClosedMetric(statname, metricsFromXml);
		addTcpStateCloseWaitMetric(statname, metricsFromXml);
		addTcpStateClosingMetric(statname, metricsFromXml);
		addTcpStateEstablishedMetric(statname, metricsFromXml);
		addTcpStateFinWait1Metric(statname, metricsFromXml);
		addTcpStateFinWait2Metric(statname, metricsFromXml);
		addTcpStateIdleMetric(statname, metricsFromXml);
		addTcpStateLastAckMetric(statname, metricsFromXml);
		addTcpStateListenMetric(statname, metricsFromXml);
		addTcpStateSynRecvMetric(statname, metricsFromXml);
		addTcpStateSynSentMetric(statname, metricsFromXml);
		addTcpStateTimeWaitMetric(statname, metricsFromXml);
	}
	
	private void collectOtherMetrics(MetricConfig[] metricConfigs) {
		Map<String, MetricConfig> metricsFromXml = Maps.newHashMap();
		for (MetricConfig metricConfig : metricConfigs) {
			metricsFromXml.put(metricConfig.getAttr(), metricConfig);
		}
		addAllInboundTotalMetric(metricsFromXml);
		addAllOutboundTotalMetric(metricsFromXml);
	}
	
	private void addRxBytesMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_KILOBYTES)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_KILOBYTES ;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxBytes());
			}
			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_KILOBYTES);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addRxDroppedMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_DROPPED)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_DROPPED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxDropped());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_DROPPED);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}

	}
	
	private void addRxErrorsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_ERRORS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_ERRORS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxErrors());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_ERRORS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addRxOverrunsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_OVERRUNS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_OVERRUNS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxOverruns());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_OVERRUNS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addRxFrameMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_FRAME)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_FRAME;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxFrame());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_FRAME);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addRxPacketsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_RX_PACKETS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_RX_PACKETS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxPackets());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_RX_PACKETS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addSpeedMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_SPEED)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_SPEED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getSpeed());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_SPEED);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxBytesMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_KILOBYTES)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_KILOBYTES;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxBytes());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_KILOBYTES);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxCarrierMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_CARRIER)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_CARRIER;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxCarrier());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_CARRIER);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxCollisionsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_COLLISIONS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_COLLISIONS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxCollisions());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_COLLISIONS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxDroppedMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_DROPPED)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_DROPPED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxDropped());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_DROPPED);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxErrorsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_ERRORS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_ERRORS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxErrors());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_ERRORS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxOverrunsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_OVERRUNS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_OVERRUNS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxOverruns());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_OVERRUNS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTxPacketsMetric(String netInterfaceName, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.NIC_TX_PACKETS)) {
			BigInteger value = null;

			String metricName = netInterfaceName + "|" + NetworkConstants.NIC_TX_PACKETS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxPackets());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.NIC_TX_PACKETS);

			addMetric(netInterfaceName + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpActiveOpensMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_ACTIVE_OPENS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_ACTIVE_OPENS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getActiveOpens());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_ACTIVE_OPENS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}

	}
	
	private void addTcpAttemptFailsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_ATTEMPT_FAILS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_ATTEMPT_FAILS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getAttemptFails());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_ATTEMPT_FAILS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpCurrentEstablishedMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_CURRENT_ESTABLISHED)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_CURRENT_ESTABLISHED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getCurrEstab());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_CURRENT_ESTABLISHED);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpEstablishedResetsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_ESTABLISHED_RESETS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_ESTABLISHED_RESETS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getEstabResets());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_ESTABLISHED_RESETS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpInErrorsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_IN_ERRORS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_IN_ERRORS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getInErrs());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_IN_ERRORS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpOutResetsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_OUT_RESETS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_OUT_RESETS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getOutRsts());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_OUT_RESETS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpInSegmentsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_IN_SEGMENTS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_IN_SEGMENTS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getInSegs());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_IN_SEGMENTS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpOutSegmentsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_OUT_SEGMENTS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_OUT_SEGMENTS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getOutSegs());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_OUT_SEGMENTS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpPassiveOpensMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_PASSIVE_OPENS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_PASSIVE_OPENS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getPassiveOpens());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_PASSIVE_OPENS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpRetransSegmentsMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_RETRANS_SEGMENTS)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_RETRANS_SEGMENTS;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getTcp() != null) {
				value = BigInteger.valueOf(sigarMetrics.getTcp().getRetransSegs());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_RETRANS_SEGMENTS);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpInboundTotalMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_INBOUND_TOTAL)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_INBOUND_TOTAL;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpInboundTotal());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_INBOUND_TOTAL);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpOutboundTotalMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_OUTBOUND_TOTAL)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_OUTBOUND_TOTAL;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpOutboundTotal());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_OUTBOUND_TOTAL);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateBoundMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_BOUND)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_BOUND;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpBound());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_BOUND);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateClosedMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_CLOSED)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_CLOSED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpClose());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_CLOSED);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateCloseWaitMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_CLOSE_WAIT)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_CLOSE_WAIT;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpCloseWait());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_CLOSE_WAIT);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateClosingMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_CLOSING)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_CLOSING;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpClosing());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_CLOSING);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}

	private void addTcpStateEstablishedMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_ESTABLISHED)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_ESTABLISHED;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpEstablished());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_ESTABLISHED);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateFinWait1Metric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_FIN_WAIT1)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_FIN_WAIT1;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpFinWait1());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_FIN_WAIT1);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateFinWait2Metric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_FIN_WAIT2)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_FIN_WAIT2;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpFinWait2());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_FIN_WAIT2);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateIdleMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_IDLE)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_IDLE;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpIdle());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_IDLE);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateLastAckMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_LAST_ACK)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_LAST_ACK;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpLastAck());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_LAST_ACK);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateListenMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_LISTEN)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_LISTEN;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpListen());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_LISTEN);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateSynRecvMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_SYN_RECV)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_SYN_RECV;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpSynRecv());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_SYN_RECV);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateSynSentMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_SYN_SENT)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_SYN_SENT;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpSynSent());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_SYN_SENT);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addTcpStateTimeWaitMetric(String statname, Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.TCP_STATE_TIME_WAIT)) {
			BigInteger value = null;

			String metricName = statname + "|" + NetworkConstants.TCP_STATE_TIME_WAIT;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpTimeWait());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.TCP_STATE_TIME_WAIT);

			addMetric(statname + "|" + metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addAllInboundTotalMetric(Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.ALL_INBOUND_TOTAL)) {
			BigInteger value = null;

			String metricName = NetworkConstants.ALL_INBOUND_TOTAL;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getAllInboundTotal());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.ALL_INBOUND_TOTAL);

			addMetric(metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private void addAllOutboundTotalMetric(Map<String, MetricConfig> metricsFromXml) {
		if (metricsFromXml.containsKey(NetworkConstants.ALL_OUTBOUND_TOTAL)) {
			BigInteger value = null;

			String metricName = NetworkConstants.ALL_OUTBOUND_TOTAL;

			if (isInScriptMetrics(metricName)) {
				value = scriptMetrics.getMetricValue(metricName);

			} else if (sigarMetrics.getNetStat() != null) {
				value = BigInteger.valueOf(sigarMetrics.getNetStat().getAllOutboundTotal());
			}

			MetricConfig metricConfig = metricsFromXml.get(NetworkConstants.ALL_OUTBOUND_TOTAL);

			addMetric(metricConfig.getAttr(), value, metricConfig);
		}
	}
	
	private boolean isInScriptMetrics(String metricName) {
		return scriptMetrics != null && scriptMetrics.isContains(metricName);
	}
	
	private void addMetric(String metricName, BigInteger value, MetricConfig metricConfig) {
		if (!isValueNullOrNegative(value)) {
			if (metricConfig != null) {
				Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
				Metric metric = new Metric(metricName, String.valueOf(value), metricPrefix + "|" + metricName, propertiesMap);
				metrics.add(metric);
			} else {
				Metric metric = new Metric(metricName, String.valueOf(value), metricPrefix + "|" + metricName);
				metrics.add(metric);
			}

		} else {
			debugLog("[%s] value is [%s]", metricName, value);
		}
	}
	
	private String getFormattedMetricName(Metrics metric, String netInterfaceName) {
		return String.format(metric.getDisplayName(), netInterfaceName);
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
