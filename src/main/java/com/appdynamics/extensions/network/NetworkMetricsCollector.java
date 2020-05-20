/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	
	private Map<String, BigInteger> metrics = new HashMap<String, BigInteger>();
	
	private SigarMetrics sigarMetrics;
	
	private ScriptMetrics scriptMetrics;
	
	private Set<String> networkInterfaces;

	public NetworkMetricsCollector(SigarMetrics sigarMetrics,
			ScriptMetrics scriptMetrics, Set<String> networkInterfaces) {
		
		if (sigarMetrics == null || networkInterfaces == null || networkInterfaces.isEmpty()) {
			throw new IllegalArgumentException("Sigar metrics and network interfaces must be provided");
		}
		
		this.sigarMetrics = sigarMetrics;
		this.scriptMetrics = scriptMetrics;
		this.networkInterfaces = networkInterfaces;
	}
	
	public Map<String, BigInteger> collectMetrics() {
		collectNetInterfaceMetrics();
		collectTcpMetrics();
		collectOtherMetrics();
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
				addMetric(metricName, scriptMetrics.getMetricValue(metricName));
			}
		}		
	}

	private void collectNetInterfaceMetrics() {
		for (String netInterfaceName : networkInterfaces) {
			addRxBytesMetric(netInterfaceName);
			addRxDroppedMetric(netInterfaceName);
			addRxErrorsMetric(netInterfaceName);
			addRxFrameMetric(netInterfaceName);
			addRxOverrunsMetric(netInterfaceName);
			addRxPacketsMetric(netInterfaceName);
			addSpeedMetric(netInterfaceName);
			addTxBytesMetric(netInterfaceName);
			addTxCarrierMetric(netInterfaceName);
			addTxCollisionsMetric(netInterfaceName);
			addTxDroppedMetric(netInterfaceName);
			addTxErrorsMetric(netInterfaceName);
			addTxOverrunsMetric(netInterfaceName);
			addTxPacketsMetric(netInterfaceName);
		}
	}
	
	private void collectTcpMetrics() {
		addTcpActiveOpensMetric();
		addTcpAttemptFailsMetric();
		addTcpCurrentEstablishedMetric();
		addTcpEstablishedResetsMetric();
		addTcpInErrorsMetric();
		addTcpInSegmentsMetric();
		addTcpOutResetsMetric();
		addTcpOutSegmentsMetric();
		addTcpPassiveOpensMetric();
		addTcpRetransSegmentsMetric();
		addTcpInboundTotalMetric();
		addTcpOutboundTotalMetric();
		addTcpStateBoundMetric();
		addTcpStateClosedMetric();
		addTcpStateCloseWaitMetric();
		addTcpStateClosingMetric();
		addTcpStateEstablishedMetric();
		addTcpStateFinWait1Metric();
		addTcpStateFinWait2Metric();
		addTcpStateIdleMetric();
		addTcpStateLastAckMetric();
		addTcpStateListenMetric();
		addTcpStateSynRecvMetric();
		addTcpStateSynSentMetric();
		addTcpStateTimeWaitMetric();
	}
	
	private void collectOtherMetrics() {
		addAllInboundTotalMetric();
		addAllOutboundTotalMetric();
	}
	
	private void addRxBytesMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_RX_KILOBYTES, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxBytes());
		}

		addMetric(metricName, value.divide(new BigInteger("1042")));
	}
	
	private void addRxDroppedMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_RX_DROPPED, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxDropped());
		}

		addMetric(metricName, value);		
	}
	
	private void addRxErrorsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_RX_ERRORS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxErrors());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addRxOverrunsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_RX_OVERRUNS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxOverruns());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addRxFrameMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_RX_FRAME, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxFrame());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addRxPacketsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_RX_PACKETS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getRxPackets());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addSpeedMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_SPEED, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getSpeed());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addTxBytesMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_TX_KILOBYTES, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxBytes());
		}

		addMetric(metricName, value.divide(new BigInteger("1042")));
	}
	
	private void addTxCarrierMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_TX_CARRIER, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxCarrier());
		}

		addMetric(metricName, value);
	}
	
	private void addTxCollisionsMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_TX_COLLISIONS, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxCollisions());
		}

		addMetric(metricName, value);
	}
	
	private void addTxDroppedMetric(String netInterfaceName) {
		BigInteger value = null;

		String metricName = getFormattedMetricName(Metrics.NIC_TX_DROPPED, netInterfaceName);

		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);

		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxDropped());
		}

		addMetric(metricName, value);
	}
	
	private void addTxErrorsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_TX_ERRORS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxErrors());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addTxOverrunsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_TX_OVERRUNS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxOverruns());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addTxPacketsMetric(String netInterfaceName) {
		BigInteger value = null;
		
		String metricName = getFormattedMetricName(Metrics.NIC_TX_PACKETS, netInterfaceName);
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetInterfaceStat(netInterfaceName) != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetInterfaceStat(netInterfaceName).getTxPackets());
		}
		
		addMetric(metricName, value);		
	}
	
	private void addTcpActiveOpensMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_ACTIVE_OPENS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getActiveOpens());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpAttemptFailsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_ATTEMPT_FAILS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getAttemptFails());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpCurrentEstablishedMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_CURRENT_ESTABLISHED.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getCurrEstab());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpEstablishedResetsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_ESTABLISHED_RESETS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getEstabResets());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpInErrorsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_IN_ERRORS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getInErrs());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpOutResetsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_OUT_RESETS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getOutRsts());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpInSegmentsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_IN_SEGMENTS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getInSegs());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpOutSegmentsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_OUT_SEGMENTS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getOutSegs());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpPassiveOpensMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_PASSIVE_OPENS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getPassiveOpens());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpRetransSegmentsMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_RETRANS_SEGMENTS.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getTcp() != null) {
			value = BigInteger.valueOf(sigarMetrics.getTcp().getRetransSegs());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpInboundTotalMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_INBOUND_TOTAL.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpInboundTotal());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpOutboundTotalMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_OUTBOUND_TOTAL.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpOutboundTotal());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateBoundMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_BOUND.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpBound());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateClosedMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_CLOSED.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpClose());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateCloseWaitMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_CLOSE_WAIT.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpCloseWait());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateClosingMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_CLOSING.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpClosing());
		}
		
		addMetric(metricName, value);	
	}

	private void addTcpStateEstablishedMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_ESTABLISHED.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpEstablished());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateFinWait1Metric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_FIN_WAIT1.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpFinWait1());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateFinWait2Metric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_FIN_WAIT2.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpFinWait2());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateIdleMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_IDLE.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpIdle());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateLastAckMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_LAST_ACK.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpLastAck());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateListenMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_LISTEN.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpListen());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateSynRecvMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_SYN_RECV.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpSynRecv());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateSynSentMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_SYN_SENT.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpSynSent());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addTcpStateTimeWaitMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.TCP_STATE_TIME_WAIT.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getTcpTimeWait());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addAllInboundTotalMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.ALL_INBOUND_TOTAL.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getAllInboundTotal());
		}
		
		addMetric(metricName, value);	
	}
	
	private void addAllOutboundTotalMetric() {
		BigInteger value = null;
		
		String metricName = Metrics.ALL_OUTBOUND_TOTAL.getDisplayName();
		
		if (isInScriptMetrics(metricName)) {
			value = scriptMetrics.getMetricValue(metricName);
			
		} else if (sigarMetrics.getNetStat() != null) {
			value = BigInteger.valueOf(sigarMetrics.getNetStat().getAllOutboundTotal());
		}
		
		addMetric(metricName, value);	
	}
	
	private boolean isInScriptMetrics(String metricName) {
		return scriptMetrics != null && scriptMetrics.isContains(metricName);
	}
	
	private void addMetric(String metricName, BigInteger value) {
		if (isValueNullOrNegative(value)) {
			debugLog("[%s] value is [%s], defaulting to 0", metricName, value);
		}
		debugLog("Adding metric [%s]", metricName);
		metrics.put(metricName, defaultValueToZeroIfNullOrNegative(value));
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
