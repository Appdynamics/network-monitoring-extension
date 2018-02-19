/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import static com.appdynamics.extensions.network.Metrics.*;
import static com.appdynamics.extensions.network.NetworkConstants.METRIC_OUTPUT_PATTERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.network.Metrics;
import com.appdynamics.extensions.network.ScriptMetrics;
import com.appdynamics.extensions.network.ScriptMetricsExecutor;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScriptMetricsExecutor.class, PathResolver.class})
@PowerMockIgnore({"org.apache.*, javax.xml.*"})
public class ScriptMetricsExecutorTest {
	
	private ScriptMetricsExecutor classUnderTest;
	
	@Before
	public void setUp() throws Exception {
		classUnderTest = spy(new ScriptMetricsExecutor());
		mockStatic(PathResolver.class);
		when(PathResolver.resolveDirectory(AManagedMonitor.class)).thenReturn(new File("./target"));
	}
	
	@Test
	public void testMetricOutputPatternNotValid() throws Exception {
		String[] invalidOutputs = {
				 "name",
				 "name,value",
				 "name=",
				 "name=test",
				 "name=, value=",
				 "name=test with non-numeric value, value=ddd",
				 "name=test with double comma,, value=1",
				 "dname=test, dvalue=1",
				 "value=3, name=test"
		};
		
		Pattern pattern = Pattern.compile(METRIC_OUTPUT_PATTERN);
		
		for (String invalidOutput : invalidOutputs) {
			Boolean result = Whitebox.invokeMethod(classUnderTest, "isMatchesMetricOutputPattern",
					invalidOutput, pattern);
			
			assertNotNull(result);
			assertFalse(invalidOutput + " should've been invalid", result);
		}
	}
	
	@Test
	public void testMetricOutputPatternIsValid() throws Exception {
		String[] validOutputs = {
				 "name=simple,value=1",
				 "name  =  simple with spaces, value = 1  ",
				 "name=with decimal value, value=1.0",
				 "name=with negative value, value= -1",
				 "name=TCP|State|Closing, value=3",
				 "name=Test | Percentage % , value=5",
				 "NAME=Test|Caps lock , VALUE=5",
				 "nAmE=Test|Camel case , vAlUe=5",
				 " name=Test|With heading/trailing spaces , value=1 "
		};
		
		Pattern pattern = Pattern.compile(METRIC_OUTPUT_PATTERN);
		
		for (String validOutput : validOutputs) {
			Boolean result = Whitebox.invokeMethod(classUnderTest, "isMatchesMetricOutputPattern",
					validOutput, pattern);
			
			assertNotNull(result);
			assertTrue(validOutput + " should've matched", result);
		}
	}
	
	@Test
	public void testProcessInputStream() throws Exception {
		InputStream testInputStream = this.getClass().getClassLoader().getResourceAsStream("test-script-output.txt");
		
		Callable<ScriptMetrics> futureScriptMetrics = Whitebox.invokeMethod(classUnderTest, "processInputStream", testInputStream);
		
		Map<String, BigInteger> resultMetrics = futureScriptMetrics.call().getMetrics();
		String testNetInterface = "lo0";
		
		assertEquals(resultMetrics.size(), Metrics.values().length);
		
		// NIC metrics
		assertEquals(BigInteger.valueOf(1302370), resultMetrics.get(getFormattedMetricName(NIC_RX_BYTES, testNetInterface)));
		assertEquals(BigInteger.valueOf(1), resultMetrics.get(getFormattedMetricName(NIC_RX_DROPPED, testNetInterface)));
		assertEquals(BigInteger.valueOf(3), resultMetrics.get(getFormattedMetricName(NIC_RX_ERRORS, testNetInterface)));
		assertEquals(BigInteger.valueOf(2), resultMetrics.get(getFormattedMetricName(NIC_RX_FRAME, testNetInterface)));
		assertEquals(BigInteger.ZERO, resultMetrics.get(getFormattedMetricName(NIC_RX_OVERRUNS, testNetInterface)));
		assertEquals(BigInteger.valueOf(9974), resultMetrics.get(getFormattedMetricName(NIC_RX_PACKETS, testNetInterface)));
		assertEquals(BigInteger.valueOf(4), resultMetrics.get(getFormattedMetricName(NIC_SPEED, testNetInterface)));
		assertEquals(BigInteger.valueOf(1302375), resultMetrics.get(getFormattedMetricName(NIC_TX_BYTES, testNetInterface)));
		assertEquals(BigInteger.ZERO, resultMetrics.get(getFormattedMetricName(NIC_TX_CARRIER, testNetInterface)));
		assertEquals(BigInteger.valueOf(5), resultMetrics.get(getFormattedMetricName(NIC_TX_COLLISIONS, testNetInterface)));
		assertEquals(BigInteger.ZERO, resultMetrics.get(getFormattedMetricName(NIC_TX_DROPPED, testNetInterface)));
		assertEquals(BigInteger.valueOf(6), resultMetrics.get(getFormattedMetricName(NIC_TX_ERRORS, testNetInterface)));
		assertEquals(BigInteger.ZERO, resultMetrics.get(getFormattedMetricName(NIC_TX_OVERRUNS, testNetInterface)));
		assertEquals(BigInteger.valueOf(10974), resultMetrics.get(getFormattedMetricName(NIC_TX_PACKETS, testNetInterface)));
		
		// TCP metrics
		assertEquals(BigInteger.valueOf(7899), resultMetrics.get(TCP_ACTIVE_OPENS.getDisplayName()));
		assertEquals(BigInteger.valueOf(213), resultMetrics.get(TCP_ATTEMPT_FAILS.getDisplayName()));
		assertEquals(BigInteger.valueOf(60), resultMetrics.get(TCP_CURRENT_ESTABLISHED.getDisplayName()));
		assertEquals(BigInteger.valueOf(343), resultMetrics.get(TCP_ESTABLISHED_RESETS.getDisplayName()));
		assertEquals(BigInteger.valueOf(33), resultMetrics.get(TCP_IN_ERRORS.getDisplayName()));
		assertEquals(BigInteger.valueOf(519887), resultMetrics.get(TCP_IN_SEGMENTS.getDisplayName()));
		assertEquals(BigInteger.valueOf(6), resultMetrics.get(TCP_INBOUND_TOTAL.getDisplayName()));
		assertEquals(BigInteger.ZERO, resultMetrics.get(TCP_OUT_RESETS.getDisplayName()));
		assertEquals(BigInteger.valueOf(438235), resultMetrics.get(TCP_OUT_SEGMENTS.getDisplayName()));
		assertEquals(BigInteger.valueOf(66), resultMetrics.get(TCP_OUTBOUND_TOTAL.getDisplayName()));
		assertEquals(BigInteger.valueOf(218), resultMetrics.get(TCP_PASSIVE_OPENS.getDisplayName()));
		assertEquals(BigInteger.valueOf(2263), resultMetrics.get(TCP_RETRANS_SEGMENTS.getDisplayName()));
		
		// TCP State metrics
		assertEquals(BigInteger.valueOf(7), resultMetrics.get(TCP_STATE_BOUND.getDisplayName()));
		assertEquals(BigInteger.valueOf(8), resultMetrics.get(TCP_STATE_CLOSE_WAIT.getDisplayName()));
		assertEquals(BigInteger.ZERO, resultMetrics.get(TCP_STATE_CLOSED.getDisplayName()));
		assertEquals(BigInteger.valueOf(5), resultMetrics.get(TCP_STATE_CLOSING.getDisplayName()));
		assertEquals(BigInteger.valueOf(52), resultMetrics.get(TCP_STATE_ESTABLISHED.getDisplayName()));
		assertEquals(BigInteger.valueOf(15), resultMetrics.get(TCP_STATE_FIN_WAIT1.getDisplayName()));
		assertEquals(BigInteger.valueOf(12), resultMetrics.get(TCP_STATE_FIN_WAIT2.getDisplayName()));
		assertEquals(BigInteger.valueOf(14), resultMetrics.get(TCP_STATE_IDLE.getDisplayName()));
		assertEquals(BigInteger.valueOf(1), resultMetrics.get(TCP_STATE_LAST_ACK.getDisplayName()));
		assertEquals(BigInteger.valueOf(11), resultMetrics.get(TCP_STATE_LISTEN.getDisplayName()));
		assertEquals(BigInteger.valueOf(16), resultMetrics.get(TCP_STATE_SYN_RECV.getDisplayName()));
		assertEquals(BigInteger.valueOf(13), resultMetrics.get(TCP_STATE_SYN_SENT.getDisplayName()));
		assertEquals(BigInteger.valueOf(6), resultMetrics.get(TCP_STATE_TIME_WAIT.getDisplayName()));
		
		// Other metrics
		assertEquals(BigInteger.valueOf(17), resultMetrics.get(ALL_INBOUND_TOTAL.getDisplayName()));
		assertEquals(BigInteger.valueOf(66), resultMetrics.get(ALL_OUTBOUND_TOTAL.getDisplayName()));
	}
		
	private String getFormattedMetricName(Metrics metric, String netInterfaceName) {
		return String.format(metric.getDisplayName(), netInterfaceName);
	}

}
