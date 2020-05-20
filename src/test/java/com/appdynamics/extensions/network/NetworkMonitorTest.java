/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.util.PathResolver;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.Tcp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static com.appdynamics.extensions.network.Metrics.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworkMonitor.class, PathResolver.class})
@PowerMockIgnore({"org.apache.*, javax.xml.*"})
public class NetworkMonitorTest {
	
	@Mock
	private MetricWriter mockMetricWriter;
	
	private NetworkMonitor classUnderTest;
	
	private Map<Metrics, Long> sigarMetricsTestValues;
	
	private Map<Metrics, Long> scriptMetricsTestValues;
	
	@Before
	public void setUp() throws Exception {
		classUnderTest = spy(new NetworkMonitor());
		mockStatic(PathResolver.class);
		when(PathResolver.resolveDirectory(AManagedMonitor.class)).thenReturn(new File("./target"));
		whenNew(MetricWriter.class).withArguments(any(AManagedMonitor.class), anyString()).thenReturn(mockMetricWriter);
		setSigarMetricsTestValues();
		setScriptMetricsTestValues();
	}
	
	@Test(expected=TaskExecutionException.class)
	public void testWithNoArgs() throws TaskExecutionException {
		classUnderTest.execute(null, null);
	}
	
	@Test(expected=TaskExecutionException.class)
	public void testWithEmptyArgs() throws TaskExecutionException {
		classUnderTest.execute(new HashMap<String, String>(), null);
	}
	
	@Test(expected=TaskExecutionException.class)
	public void testWithNonExistentConfigFile() throws TaskExecutionException {
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/no_config.yml");
		
		classUnderTest.execute(args, null);
	}
	
	@Test(expected=TaskExecutionException.class)
	public void testWithInvalidConfigFile() throws TaskExecutionException {
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/invalid_config.yml");
		
		classUnderTest.execute(args, null);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFailureWithSigarAndScriptMetricsResultInZeroMetrics() throws Exception {
		SigarMetrics mockSigarMetrics = mock(SigarMetrics.class);
		whenNew(SigarMetrics.class).withArguments(any(Set.class)).thenReturn(mockSigarMetrics);
		when(mockSigarMetrics.getNetInterfaceStat(any(String.class))).thenReturn(null);
		when(mockSigarMetrics.getNetStat()).thenReturn(null);
		when(mockSigarMetrics.getTcp()).thenReturn(null);
		
		ScriptMetricsExecutor mockScriptMetricsCollector = getMockScriptMetricsExecutor();
		whenNew(ScriptMetricsExecutor.class).withNoArguments().thenReturn(mockScriptMetricsCollector);
		when(mockScriptMetricsCollector.executeAndCollectScriptMetrics(any(List.class), anyLong()))
			.thenThrow(new TimeoutException());
		
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/config_script_override.yml");
		
		classUnderTest.execute(args, null);
		
		long zeroValue = 0;
		
		verifyMetric("Custom Metrics|Network|eth1|RX Bytes", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|RX Dropped", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|RX Errors", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|RX Frame", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|RX Overruns", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|RX Packets", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|Speed", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Bytes", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Carrier", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Collision", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Dropped", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Errors", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Overruns", zeroValue);
		verifyMetric("Custom Metrics|Network|eth1|TX Packets", zeroValue);
		
		verifyMetric("Custom Metrics|Network|TCP|Active Opens", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Attempt Fails", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Conn Established", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Resets Received", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Bad Segments", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Segments Received", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Inbound Total", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Resets Sent", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Segments Sent", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Outbound Total", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Passive Opens", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|Segments Retransmitted", zeroValue);
		
		verifyMetric("Custom Metrics|Network|TCP|State|Bound", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Close Wait", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Closed", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Closing", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Establised", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait1", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait2", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Idle", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Last Ack", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Listen", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Recv", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Sent", zeroValue);
		verifyMetric("Custom Metrics|Network|TCP|State|Time Wait", zeroValue);
		
		verifyMetric("Custom Metrics|Network|All Inbound Total", zeroValue);
		verifyMetric("Custom Metrics|Network|All Outbound Total", zeroValue);
	}
	
	@Test
	public void testAllMetricsFromSigar() throws Exception {
		SigarMetrics mockSigarMetrics = getMockSigarMetrics();
		whenNew(SigarMetrics.class).withArguments(any(Set.class)).thenReturn(mockSigarMetrics);
		
		ScriptMetricsExecutor mockScriptMetricsCollector = getMockScriptMetricsExecutor();
		whenNew(ScriptMetricsExecutor.class).withNoArguments().thenReturn(mockScriptMetricsCollector);
		
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/config.yml");
		
		classUnderTest.execute(args, null);
		
		verifyMetric("Custom Metrics|Network|eth1|RX Bytes", sigarMetricsTestValues.get(NIC_RX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|RX Dropped", sigarMetricsTestValues.get(NIC_RX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|RX Errors", sigarMetricsTestValues.get(NIC_RX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|RX Frame", sigarMetricsTestValues.get(NIC_RX_FRAME));
		verifyMetric("Custom Metrics|Network|eth1|RX Overruns", sigarMetricsTestValues.get(NIC_RX_OVERRUNS));
		verifyMetric("Custom Metrics|Network|eth1|RX Packets", sigarMetricsTestValues.get(NIC_RX_PACKETS));
		verifyMetric("Custom Metrics|Network|eth1|Speed", sigarMetricsTestValues.get(NIC_SPEED));
		verifyMetric("Custom Metrics|Network|eth1|TX Bytes", sigarMetricsTestValues.get(NIC_TX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|TX Carrier", sigarMetricsTestValues.get(NIC_TX_CARRIER));
		verifyMetric("Custom Metrics|Network|eth1|TX Collision", sigarMetricsTestValues.get(NIC_TX_COLLISIONS));
		verifyMetric("Custom Metrics|Network|eth1|TX Dropped", sigarMetricsTestValues.get(NIC_TX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|TX Errors", sigarMetricsTestValues.get(NIC_TX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|TX Overruns", sigarMetricsTestValues.get(NIC_TX_OVERRUNS));
		verifyMetric("Custom Metrics|Network|eth1|TX Packets", sigarMetricsTestValues.get(NIC_TX_PACKETS));
		
		verifyMetric("Custom Metrics|Network|TCP|Active Opens", sigarMetricsTestValues.get(TCP_ACTIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Attempt Fails", sigarMetricsTestValues.get(TCP_ATTEMPT_FAILS));
		verifyMetric("Custom Metrics|Network|TCP|Conn Established", sigarMetricsTestValues.get(TCP_CURRENT_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|Resets Received", sigarMetricsTestValues.get(TCP_ESTABLISHED_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Bad Segments", sigarMetricsTestValues.get(TCP_IN_ERRORS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Received", sigarMetricsTestValues.get(TCP_IN_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Inbound Total", sigarMetricsTestValues.get(TCP_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|Resets Sent", sigarMetricsTestValues.get(TCP_OUT_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Sent", sigarMetricsTestValues.get(TCP_OUT_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Outbound Total", sigarMetricsTestValues.get(TCP_OUTBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|Passive Opens", sigarMetricsTestValues.get(TCP_PASSIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Retransmitted", sigarMetricsTestValues.get(TCP_RETRANS_SEGMENTS));
		
		verifyMetric("Custom Metrics|Network|TCP|State|Bound", sigarMetricsTestValues.get(TCP_STATE_BOUND));
		verifyMetric("Custom Metrics|Network|TCP|State|Close Wait", sigarMetricsTestValues.get(TCP_STATE_CLOSE_WAIT));
		verifyMetric("Custom Metrics|Network|TCP|State|Closed", sigarMetricsTestValues.get(TCP_STATE_CLOSED));
		verifyMetric("Custom Metrics|Network|TCP|State|Closing", sigarMetricsTestValues.get(TCP_STATE_CLOSING));
		verifyMetric("Custom Metrics|Network|TCP|State|Establised", sigarMetricsTestValues.get(TCP_STATE_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait1", sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT1));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait2", sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT2));
		verifyMetric("Custom Metrics|Network|TCP|State|Idle", sigarMetricsTestValues.get(TCP_STATE_IDLE));
		verifyMetric("Custom Metrics|Network|TCP|State|Last Ack", sigarMetricsTestValues.get(TCP_STATE_LAST_ACK));
		verifyMetric("Custom Metrics|Network|TCP|State|Listen", sigarMetricsTestValues.get(TCP_STATE_LISTEN));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Recv", sigarMetricsTestValues.get(TCP_STATE_SYN_RECV));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Sent", sigarMetricsTestValues.get(TCP_STATE_SYN_SENT));
		verifyMetric("Custom Metrics|Network|TCP|State|Time Wait", sigarMetricsTestValues.get(TCP_STATE_TIME_WAIT));
		
		verifyMetric("Custom Metrics|Network|All Inbound Total", sigarMetricsTestValues.get(ALL_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|All Outbound Total", sigarMetricsTestValues.get(ALL_OUTBOUND_TOTAL));
		
		verifyNew(ScriptMetricsExecutor.class, never()).withNoArguments();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPartialMetricsFromSigarAndScript() throws Exception {
		SigarMetrics mockSigarMetrics = getMockSigarMetrics();
		whenNew(SigarMetrics.class).withArguments(any(Set.class)).thenReturn(mockSigarMetrics);
		
		ScriptMetricsExecutor mockScriptMetricsCollector = getMockScriptMetricsExecutor();
		whenNew(ScriptMetricsExecutor.class).withNoArguments().thenReturn(mockScriptMetricsCollector);
		when(mockScriptMetricsCollector.executeAndCollectScriptMetrics(any(List.class), anyLong()))
				.thenReturn(getScriptMetricsWithPartialMetrics());
		
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/config_script_override.yml");
		
		classUnderTest.execute(args, null);
		
		// metrics from sigar
		verifyMetric("Custom Metrics|Network|eth1|RX Errors", sigarMetricsTestValues.get(NIC_RX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|RX Frame", sigarMetricsTestValues.get(NIC_RX_FRAME));
		verifyMetric("Custom Metrics|Network|eth1|RX Overruns", sigarMetricsTestValues.get(NIC_RX_OVERRUNS));
		verifyMetric("Custom Metrics|Network|eth1|RX Packets", sigarMetricsTestValues.get(NIC_RX_PACKETS));
		verifyMetric("Custom Metrics|Network|eth1|Speed", sigarMetricsTestValues.get(NIC_SPEED));
		verifyMetric("Custom Metrics|Network|eth1|TX Bytes", sigarMetricsTestValues.get(NIC_TX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|TX Carrier", sigarMetricsTestValues.get(NIC_TX_CARRIER));
		verifyMetric("Custom Metrics|Network|eth1|TX Collision", sigarMetricsTestValues.get(NIC_TX_COLLISIONS));
		verifyMetric("Custom Metrics|Network|eth1|TX Dropped", sigarMetricsTestValues.get(NIC_TX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|TX Errors", sigarMetricsTestValues.get(NIC_TX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|TX Overruns", sigarMetricsTestValues.get(NIC_TX_OVERRUNS));
		
		verifyMetric("Custom Metrics|Network|TCP|Attempt Fails", sigarMetricsTestValues.get(TCP_ATTEMPT_FAILS));
		verifyMetric("Custom Metrics|Network|TCP|Conn Established", sigarMetricsTestValues.get(TCP_CURRENT_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|Resets Received", sigarMetricsTestValues.get(TCP_ESTABLISHED_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Bad Segments", sigarMetricsTestValues.get(TCP_IN_ERRORS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Received", sigarMetricsTestValues.get(TCP_IN_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Resets Sent", sigarMetricsTestValues.get(TCP_OUT_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Sent", sigarMetricsTestValues.get(TCP_OUT_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Outbound Total", sigarMetricsTestValues.get(TCP_OUTBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|Passive Opens", sigarMetricsTestValues.get(TCP_PASSIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Retransmitted", sigarMetricsTestValues.get(TCP_RETRANS_SEGMENTS));
		
		verifyMetric("Custom Metrics|Network|TCP|State|Close Wait", sigarMetricsTestValues.get(TCP_STATE_CLOSE_WAIT));
		verifyMetric("Custom Metrics|Network|TCP|State|Closed", sigarMetricsTestValues.get(TCP_STATE_CLOSED));
		verifyMetric("Custom Metrics|Network|TCP|State|Closing", sigarMetricsTestValues.get(TCP_STATE_CLOSING));
		verifyMetric("Custom Metrics|Network|TCP|State|Establised", sigarMetricsTestValues.get(TCP_STATE_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait1", sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT1));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait2", sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT2));
		verifyMetric("Custom Metrics|Network|TCP|State|Idle", sigarMetricsTestValues.get(TCP_STATE_IDLE));
		verifyMetric("Custom Metrics|Network|TCP|State|Last Ack", sigarMetricsTestValues.get(TCP_STATE_LAST_ACK));
		verifyMetric("Custom Metrics|Network|TCP|State|Listen", sigarMetricsTestValues.get(TCP_STATE_LISTEN));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Sent", sigarMetricsTestValues.get(TCP_STATE_SYN_SENT));
		
		// metrics from script
		verifyMetric("Custom Metrics|Network|eth1|RX Bytes", scriptMetricsTestValues.get(NIC_RX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|RX Dropped", scriptMetricsTestValues.get(NIC_RX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|TX Packets", scriptMetricsTestValues.get(NIC_TX_PACKETS));
		verifyMetric("Custom Metrics|Network|TCP|Active Opens", scriptMetricsTestValues.get(TCP_ACTIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Inbound Total", scriptMetricsTestValues.get(TCP_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|State|Bound", scriptMetricsTestValues.get(TCP_STATE_BOUND));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Recv", scriptMetricsTestValues.get(TCP_STATE_SYN_RECV));
		verifyMetric("Custom Metrics|Network|TCP|State|Time Wait", scriptMetricsTestValues.get(TCP_STATE_TIME_WAIT));
		verifyMetric("Custom Metrics|Network|All Inbound Total", scriptMetricsTestValues.get(ALL_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|All Outbound Total", scriptMetricsTestValues.get(ALL_OUTBOUND_TOTAL));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAllMetricsFromScript() throws Exception {
		SigarMetrics mockSigarMetrics = getMockSigarMetrics();
		whenNew(SigarMetrics.class).withArguments(any(Set.class)).thenReturn(mockSigarMetrics);
		
		ScriptMetricsExecutor mockScriptMetricsCollector = getMockScriptMetricsExecutor();
		whenNew(ScriptMetricsExecutor.class).withNoArguments().thenReturn(mockScriptMetricsCollector);
		when(mockScriptMetricsCollector.executeAndCollectScriptMetrics(any(List.class), anyLong()))
				.thenReturn(getScriptMetricsWithFullMetrics());
		
		Map<String, String> args = Maps.newHashMap();
		args.put("config-file","src/test/resources/conf/config_script_override.yml");
		
		classUnderTest.execute(args, null);
		
		verifyMetric("Custom Metrics|Network|eth1|RX Bytes", scriptMetricsTestValues.get(NIC_RX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|RX Dropped", scriptMetricsTestValues.get(NIC_RX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|RX Errors", scriptMetricsTestValues.get(NIC_RX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|RX Frame", scriptMetricsTestValues.get(NIC_RX_FRAME));
		verifyMetric("Custom Metrics|Network|eth1|RX Overruns", scriptMetricsTestValues.get(NIC_RX_OVERRUNS));
		verifyMetric("Custom Metrics|Network|eth1|RX Packets", scriptMetricsTestValues.get(NIC_RX_PACKETS));
		verifyMetric("Custom Metrics|Network|eth1|Speed", scriptMetricsTestValues.get(NIC_SPEED));
		verifyMetric("Custom Metrics|Network|eth1|TX Bytes", scriptMetricsTestValues.get(NIC_TX_BYTES));
		verifyMetric("Custom Metrics|Network|eth1|TX Carrier", scriptMetricsTestValues.get(NIC_TX_CARRIER));
		verifyMetric("Custom Metrics|Network|eth1|TX Collision", scriptMetricsTestValues.get(NIC_TX_COLLISIONS));
		verifyMetric("Custom Metrics|Network|eth1|TX Dropped", scriptMetricsTestValues.get(NIC_TX_DROPPED));
		verifyMetric("Custom Metrics|Network|eth1|TX Errors", scriptMetricsTestValues.get(NIC_TX_ERRORS));
		verifyMetric("Custom Metrics|Network|eth1|TX Overruns", scriptMetricsTestValues.get(NIC_TX_OVERRUNS));
		verifyMetric("Custom Metrics|Network|eth1|TX Packets", scriptMetricsTestValues.get(NIC_TX_PACKETS));
		
		verifyMetric("Custom Metrics|Network|TCP|Active Opens", scriptMetricsTestValues.get(TCP_ACTIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Attempt Fails", scriptMetricsTestValues.get(TCP_ATTEMPT_FAILS));
		verifyMetric("Custom Metrics|Network|TCP|Conn Established", scriptMetricsTestValues.get(TCP_CURRENT_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|Resets Received", scriptMetricsTestValues.get(TCP_ESTABLISHED_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Bad Segments", scriptMetricsTestValues.get(TCP_IN_ERRORS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Received", scriptMetricsTestValues.get(TCP_IN_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Inbound Total", scriptMetricsTestValues.get(TCP_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|Resets Sent", scriptMetricsTestValues.get(TCP_OUT_RESETS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Sent", scriptMetricsTestValues.get(TCP_OUT_SEGMENTS));
		verifyMetric("Custom Metrics|Network|TCP|Outbound Total", scriptMetricsTestValues.get(TCP_OUTBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|TCP|Passive Opens", scriptMetricsTestValues.get(TCP_PASSIVE_OPENS));
		verifyMetric("Custom Metrics|Network|TCP|Segments Retransmitted", scriptMetricsTestValues.get(TCP_RETRANS_SEGMENTS));
		
		verifyMetric("Custom Metrics|Network|TCP|State|Bound", scriptMetricsTestValues.get(TCP_STATE_BOUND));
		verifyMetric("Custom Metrics|Network|TCP|State|Close Wait", scriptMetricsTestValues.get(TCP_STATE_CLOSE_WAIT));
		verifyMetric("Custom Metrics|Network|TCP|State|Closed", scriptMetricsTestValues.get(TCP_STATE_CLOSED));
		verifyMetric("Custom Metrics|Network|TCP|State|Closing", scriptMetricsTestValues.get(TCP_STATE_CLOSING));
		verifyMetric("Custom Metrics|Network|TCP|State|Establised", scriptMetricsTestValues.get(TCP_STATE_ESTABLISHED));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait1", scriptMetricsTestValues.get(TCP_STATE_FIN_WAIT1));
		verifyMetric("Custom Metrics|Network|TCP|State|Fin Wait2", scriptMetricsTestValues.get(TCP_STATE_FIN_WAIT2));
		verifyMetric("Custom Metrics|Network|TCP|State|Idle", scriptMetricsTestValues.get(TCP_STATE_IDLE));
		verifyMetric("Custom Metrics|Network|TCP|State|Last Ack", scriptMetricsTestValues.get(TCP_STATE_LAST_ACK));
		verifyMetric("Custom Metrics|Network|TCP|State|Listen", scriptMetricsTestValues.get(TCP_STATE_LISTEN));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Recv", scriptMetricsTestValues.get(TCP_STATE_SYN_RECV));
		verifyMetric("Custom Metrics|Network|TCP|State|Syn Sent", scriptMetricsTestValues.get(TCP_STATE_SYN_SENT));
		verifyMetric("Custom Metrics|Network|TCP|State|Time Wait", scriptMetricsTestValues.get(TCP_STATE_TIME_WAIT));
		
		verifyMetric("Custom Metrics|Network|All Inbound Total", scriptMetricsTestValues.get(ALL_INBOUND_TOTAL));
		verifyMetric("Custom Metrics|Network|All Outbound Total", scriptMetricsTestValues.get(ALL_OUTBOUND_TOTAL));
		
	}
	
	private ScriptMetrics getScriptMetricsWithPartialMetrics() {
		ScriptMetrics scriptMetrics = new ScriptMetrics();
		scriptMetrics.addMetric("eth1|RX Bytes", BigInteger.valueOf(scriptMetricsTestValues.get(NIC_RX_BYTES)));
		scriptMetrics.addMetric("eth1|RX Dropped", BigInteger.valueOf(scriptMetricsTestValues.get(NIC_RX_DROPPED)));
		scriptMetrics.addMetric("eth1|TX Packets", BigInteger.valueOf(scriptMetricsTestValues.get(NIC_TX_PACKETS)));
		scriptMetrics.addMetric("TCP|Active Opens", BigInteger.valueOf(scriptMetricsTestValues.get(TCP_ACTIVE_OPENS)));
		scriptMetrics.addMetric("TCP|Inbound Total", BigInteger.valueOf(scriptMetricsTestValues.get(TCP_INBOUND_TOTAL)));
		scriptMetrics.addMetric("TCP|State|Bound", BigInteger.valueOf(scriptMetricsTestValues.get(TCP_STATE_BOUND)));
		scriptMetrics.addMetric("TCP|State|Syn Recv", BigInteger.valueOf(scriptMetricsTestValues.get(TCP_STATE_SYN_RECV)));
		scriptMetrics.addMetric("TCP|State|Time Wait", BigInteger.valueOf(scriptMetricsTestValues.get(TCP_STATE_TIME_WAIT)));
		scriptMetrics.addMetric("All Inbound Total", BigInteger.valueOf(scriptMetricsTestValues.get(ALL_INBOUND_TOTAL)));
		scriptMetrics.addMetric("All Outbound Total", BigInteger.valueOf(scriptMetricsTestValues.get(ALL_OUTBOUND_TOTAL)));
		return scriptMetrics;
	}
	
	private ScriptMetrics getScriptMetricsWithFullMetrics() {
		ScriptMetrics scriptMetrics = new ScriptMetrics();
		
		for (Metrics metric : Metrics.values()) {
			scriptMetrics.addMetric(String.format(metric.getDisplayName(), "eth1"), 
					BigInteger.valueOf(scriptMetricsTestValues.get(metric)));
		}

		return scriptMetrics;
	}
	
	private SigarMetrics getMockSigarMetrics() {
		SigarMetrics mockSigarMetrics = mock(SigarMetrics.class);
		
		NetInterfaceStat mockNetInterfaceStat = getMockNetInterfaceStat();
		when(mockSigarMetrics.getNetInterfaceStat(any(String.class))).thenReturn(mockNetInterfaceStat);
		
		NetStat mockNetStat = getMockNetStat();
		when(mockSigarMetrics.getNetStat()).thenReturn(mockNetStat);
		
		Tcp mockTcp = getMockTcp();
		when(mockSigarMetrics.getTcp()).thenReturn(mockTcp);
		
		return mockSigarMetrics;
	}
	
	private ScriptMetricsExecutor getMockScriptMetricsExecutor() {
		ScriptMetricsExecutor mockScriptMetricsCollector = mock(ScriptMetricsExecutor.class);
		return mockScriptMetricsCollector;
	}
	
	private Tcp getMockTcp() {
		Tcp mockTcp = mock(Tcp.class);
		when(mockTcp.getActiveOpens()).thenReturn(sigarMetricsTestValues.get(TCP_ACTIVE_OPENS));
		when(mockTcp.getAttemptFails()).thenReturn(sigarMetricsTestValues.get(TCP_ATTEMPT_FAILS));
		when(mockTcp.getCurrEstab()).thenReturn(sigarMetricsTestValues.get(TCP_CURRENT_ESTABLISHED));
		when(mockTcp.getEstabResets()).thenReturn(sigarMetricsTestValues.get(TCP_ESTABLISHED_RESETS));
		when(mockTcp.getInErrs()).thenReturn(sigarMetricsTestValues.get(TCP_IN_ERRORS));
		when(mockTcp.getInSegs()).thenReturn(sigarMetricsTestValues.get(TCP_IN_SEGMENTS));
		when(mockTcp.getOutRsts()).thenReturn(sigarMetricsTestValues.get(TCP_OUT_RESETS));
		when(mockTcp.getOutSegs()).thenReturn(sigarMetricsTestValues.get(TCP_OUT_SEGMENTS));
		when(mockTcp.getPassiveOpens()).thenReturn(sigarMetricsTestValues.get(TCP_PASSIVE_OPENS));
		when(mockTcp.getRetransSegs()).thenReturn(sigarMetricsTestValues.get(TCP_RETRANS_SEGMENTS));
		return mockTcp;
	}
	
	private NetStat getMockNetStat() {
		NetStat mockNetStat = mock(NetStat.class);
		when(mockNetStat.getAllInboundTotal()).thenReturn(sigarMetricsTestValues.get(ALL_INBOUND_TOTAL).intValue());
		when(mockNetStat.getAllOutboundTotal()).thenReturn(sigarMetricsTestValues.get(ALL_OUTBOUND_TOTAL).intValue());
		when(mockNetStat.getTcpBound()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_BOUND).intValue());
		when(mockNetStat.getTcpClose()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_CLOSED).intValue());
		when(mockNetStat.getTcpCloseWait()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_CLOSE_WAIT).intValue());
		when(mockNetStat.getTcpClosing()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_CLOSING).intValue());
		when(mockNetStat.getTcpEstablished()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_ESTABLISHED).intValue());
		when(mockNetStat.getTcpFinWait1()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT1).intValue());
		when(mockNetStat.getTcpFinWait2()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_FIN_WAIT2).intValue());
		when(mockNetStat.getTcpIdle()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_IDLE).intValue());
		when(mockNetStat.getTcpInboundTotal()).thenReturn(sigarMetricsTestValues.get(TCP_INBOUND_TOTAL).intValue());
		when(mockNetStat.getTcpLastAck()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_LAST_ACK).intValue());
		when(mockNetStat.getTcpListen()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_LISTEN).intValue());
		when(mockNetStat.getTcpOutboundTotal()).thenReturn(sigarMetricsTestValues.get(TCP_OUTBOUND_TOTAL).intValue());
		when(mockNetStat.getTcpSynRecv()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_SYN_RECV).intValue());
		when(mockNetStat.getTcpSynSent()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_SYN_SENT).intValue());
		when(mockNetStat.getTcpTimeWait()).thenReturn(sigarMetricsTestValues.get(TCP_STATE_TIME_WAIT).intValue());
		return mockNetStat;
	}
	
	private NetInterfaceStat getMockNetInterfaceStat() {
		NetInterfaceStat mockNetInterfaceStat = mock(NetInterfaceStat.class);
		when(mockNetInterfaceStat.getRxBytes()).thenReturn(sigarMetricsTestValues.get(NIC_RX_BYTES));
		when(mockNetInterfaceStat.getRxDropped()).thenReturn(sigarMetricsTestValues.get(NIC_RX_DROPPED));
		when(mockNetInterfaceStat.getRxErrors()).thenReturn(sigarMetricsTestValues.get(NIC_RX_ERRORS));
		when(mockNetInterfaceStat.getRxFrame()).thenReturn(sigarMetricsTestValues.get(NIC_RX_FRAME));
		when(mockNetInterfaceStat.getRxOverruns()).thenReturn(sigarMetricsTestValues.get(NIC_RX_OVERRUNS));
		when(mockNetInterfaceStat.getRxPackets()).thenReturn(sigarMetricsTestValues.get(NIC_RX_PACKETS));
		when(mockNetInterfaceStat.getSpeed()).thenReturn(sigarMetricsTestValues.get(NIC_SPEED));
		when(mockNetInterfaceStat.getTxBytes()).thenReturn(sigarMetricsTestValues.get(NIC_TX_BYTES));
		when(mockNetInterfaceStat.getTxCarrier()).thenReturn(sigarMetricsTestValues.get(NIC_TX_CARRIER));
		when(mockNetInterfaceStat.getTxCollisions()).thenReturn(sigarMetricsTestValues.get(NIC_TX_COLLISIONS));
		when(mockNetInterfaceStat.getTxDropped()).thenReturn(sigarMetricsTestValues.get(NIC_TX_DROPPED));
		when(mockNetInterfaceStat.getTxErrors()).thenReturn(sigarMetricsTestValues.get(NIC_TX_ERRORS));
		when(mockNetInterfaceStat.getTxOverruns()).thenReturn(sigarMetricsTestValues.get(NIC_TX_OVERRUNS));
		when(mockNetInterfaceStat.getTxPackets()).thenReturn(sigarMetricsTestValues.get(NIC_TX_PACKETS));
		return mockNetInterfaceStat;
	}
	
	private void setSigarMetricsTestValues() {
		sigarMetricsTestValues = new HashMap<Metrics, Long>();
		sigarMetricsTestValues.put(NIC_RX_BYTES, 1L);
		sigarMetricsTestValues.put(NIC_RX_DROPPED, 2L);
		sigarMetricsTestValues.put(NIC_RX_ERRORS, 3L);
		sigarMetricsTestValues.put(NIC_RX_FRAME, 4L);
		sigarMetricsTestValues.put(NIC_RX_OVERRUNS, 5L);
		sigarMetricsTestValues.put(NIC_RX_PACKETS, 6L);
		sigarMetricsTestValues.put(NIC_SPEED, 7L);
		sigarMetricsTestValues.put(NIC_TX_BYTES, 8L);
		sigarMetricsTestValues.put(NIC_TX_CARRIER, 9L);
		sigarMetricsTestValues.put(NIC_TX_COLLISIONS, 10L);
		sigarMetricsTestValues.put(NIC_TX_DROPPED, 11L);
		sigarMetricsTestValues.put(NIC_TX_ERRORS, 12L);
		sigarMetricsTestValues.put(NIC_TX_OVERRUNS, 13L);
		sigarMetricsTestValues.put(NIC_TX_PACKETS, 14L);
		
		sigarMetricsTestValues.put(TCP_ACTIVE_OPENS, 15L);
		sigarMetricsTestValues.put(TCP_ATTEMPT_FAILS, 16L);
		sigarMetricsTestValues.put(TCP_CURRENT_ESTABLISHED, 17L);
		sigarMetricsTestValues.put(TCP_ESTABLISHED_RESETS, 18L);
		sigarMetricsTestValues.put(TCP_IN_ERRORS, 19L);
		sigarMetricsTestValues.put(TCP_IN_SEGMENTS, 20L);
		sigarMetricsTestValues.put(TCP_INBOUND_TOTAL, 21L);
		sigarMetricsTestValues.put(TCP_OUT_RESETS, 22L);
		sigarMetricsTestValues.put(TCP_OUT_SEGMENTS, 23L);
		sigarMetricsTestValues.put(TCP_OUTBOUND_TOTAL, 24L);
		sigarMetricsTestValues.put(TCP_PASSIVE_OPENS, 25L);
		sigarMetricsTestValues.put(TCP_RETRANS_SEGMENTS, 26L);
		
		sigarMetricsTestValues.put(TCP_STATE_BOUND, 27L);
		sigarMetricsTestValues.put(TCP_STATE_CLOSE_WAIT, 28L);
		sigarMetricsTestValues.put(TCP_STATE_CLOSED, 29L);
		sigarMetricsTestValues.put(TCP_STATE_CLOSING, 30L);
		sigarMetricsTestValues.put(TCP_STATE_ESTABLISHED, 31L);
		sigarMetricsTestValues.put(TCP_STATE_FIN_WAIT1, 32L);
		sigarMetricsTestValues.put(TCP_STATE_FIN_WAIT2, 33L);
		sigarMetricsTestValues.put(TCP_STATE_IDLE, 34L);
		sigarMetricsTestValues.put(TCP_STATE_LAST_ACK, 35L);
		sigarMetricsTestValues.put(TCP_STATE_LISTEN, 36L);
		sigarMetricsTestValues.put(TCP_STATE_SYN_RECV, 37L);
		sigarMetricsTestValues.put(TCP_STATE_SYN_SENT, 38L);
		sigarMetricsTestValues.put(TCP_STATE_TIME_WAIT, 39L);
		
		sigarMetricsTestValues.put(ALL_INBOUND_TOTAL, 40L);
		sigarMetricsTestValues.put(ALL_OUTBOUND_TOTAL, 41L);
		
	}
	
	private void setScriptMetricsTestValues() {
		scriptMetricsTestValues = new HashMap<Metrics, Long>();
		scriptMetricsTestValues.put(NIC_RX_BYTES, 10L);
		scriptMetricsTestValues.put(NIC_RX_DROPPED, 20L);
		scriptMetricsTestValues.put(NIC_RX_ERRORS, 30L);
		scriptMetricsTestValues.put(NIC_RX_FRAME, 40L);
		scriptMetricsTestValues.put(NIC_RX_OVERRUNS, 50L);
		scriptMetricsTestValues.put(NIC_RX_PACKETS, 60L);
		scriptMetricsTestValues.put(NIC_SPEED, 70L);
		scriptMetricsTestValues.put(NIC_TX_BYTES, 80L);
		scriptMetricsTestValues.put(NIC_TX_CARRIER, 90L);
		scriptMetricsTestValues.put(NIC_TX_COLLISIONS, 100L);
		scriptMetricsTestValues.put(NIC_TX_DROPPED, 110L);
		scriptMetricsTestValues.put(NIC_TX_ERRORS, 120L);
		scriptMetricsTestValues.put(NIC_TX_OVERRUNS, 130L);
		scriptMetricsTestValues.put(NIC_TX_PACKETS, 140L);
		
		scriptMetricsTestValues.put(TCP_ACTIVE_OPENS, 150L);
		scriptMetricsTestValues.put(TCP_ATTEMPT_FAILS, 160L);
		scriptMetricsTestValues.put(TCP_CURRENT_ESTABLISHED, 170L);
		scriptMetricsTestValues.put(TCP_ESTABLISHED_RESETS, 180L);
		scriptMetricsTestValues.put(TCP_IN_ERRORS, 190L);
		scriptMetricsTestValues.put(TCP_IN_SEGMENTS, 200L);
		scriptMetricsTestValues.put(TCP_INBOUND_TOTAL, 210L);
		scriptMetricsTestValues.put(TCP_OUT_RESETS, 220L);
		scriptMetricsTestValues.put(TCP_OUT_SEGMENTS, 230L);
		scriptMetricsTestValues.put(TCP_OUTBOUND_TOTAL, 240L);
		scriptMetricsTestValues.put(TCP_PASSIVE_OPENS, 250L);
		scriptMetricsTestValues.put(TCP_RETRANS_SEGMENTS, 260L);
		
		scriptMetricsTestValues.put(TCP_STATE_BOUND, 270L);
		scriptMetricsTestValues.put(TCP_STATE_CLOSE_WAIT, 280L);
		scriptMetricsTestValues.put(TCP_STATE_CLOSED, 290L);
		scriptMetricsTestValues.put(TCP_STATE_CLOSING, 300L);
		scriptMetricsTestValues.put(TCP_STATE_ESTABLISHED, 310L);
		scriptMetricsTestValues.put(TCP_STATE_FIN_WAIT1, 320L);
		scriptMetricsTestValues.put(TCP_STATE_FIN_WAIT2, 330L);
		scriptMetricsTestValues.put(TCP_STATE_IDLE, 340L);
		scriptMetricsTestValues.put(TCP_STATE_LAST_ACK, 350L);
		scriptMetricsTestValues.put(TCP_STATE_LISTEN, 360L);
		scriptMetricsTestValues.put(TCP_STATE_SYN_RECV, 370L);
		scriptMetricsTestValues.put(TCP_STATE_SYN_SENT, 380L);
		scriptMetricsTestValues.put(TCP_STATE_TIME_WAIT, 390L);
		
		scriptMetricsTestValues.put(ALL_INBOUND_TOTAL, 400L);
		scriptMetricsTestValues.put(ALL_OUTBOUND_TOTAL, 410L);
	}
	
	private void verifyMetric(String metricName, long value) throws Exception {
		verifyPrivate(classUnderTest, times(1)).invoke("printCollectiveObservedCurrent", 
				metricName, BigInteger.valueOf(value));
	}

}
