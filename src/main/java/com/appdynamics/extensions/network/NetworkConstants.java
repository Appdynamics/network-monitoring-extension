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

	public static final String NIC_RX_BYTES = "RX Bytes";
	public static final String NIC_RX_KILOBYTES = "RX KB";
	public static final String NIC_RX_DROPPED = "RX Dropped";
	public static final String NIC_RX_ERRORS = "RX Errors";
	public static final String NIC_RX_FRAME = "RX Frame";
	public static final String NIC_RX_OVERRUNS = "RX Overruns";
	public static final String NIC_RX_PACKETS = "RX Packets";
	public static final String NIC_SPEED = "Speed";
	public static final String NIC_TX_BYTES = "TX Bytes";
	public static final String NIC_TX_KILOBYTES = "TX KB";
	public static final String NIC_TX_CARRIER = "TX Carrier";
	public static final String NIC_TX_COLLISIONS = "TX Collision";
	public static final String NIC_TX_DROPPED = "TX Dropped";
	public static final String NIC_TX_ERRORS = "TX Errors";
	public static final String NIC_TX_OVERRUNS = "TX Overruns";
	public static final String NIC_TX_PACKETS = "TX Packets";

	// TCP metrics
	public static final String TCP_ACTIVE_OPENS = "Active Opens";
	public static final String TCP_ATTEMPT_FAILS = "Attempt Fails";
	public static final String TCP_CURRENT_ESTABLISHED = "Conn Established";
	public static final String TCP_ESTABLISHED_RESETS = "Resets Received";
	public static final String TCP_IN_ERRORS = "Bad Segments";
	public static final String TCP_IN_SEGMENTS = "Segments Received";
	public static final String TCP_OUT_RESETS = "Resets Sent";
	public static final String TCP_OUT_SEGMENTS = "Segments Sent";
	public static final String TCP_PASSIVE_OPENS = "Passive Opens";
	public static final String TCP_RETRANS_SEGMENTS = "Segments Retransmitted";
	public static final String TCP_INBOUND_TOTAL = "Inbound Total";
	public static final String TCP_OUTBOUND_TOTAL = "Outbound Total";

	// TCP States
	public static final String TCP_STATE_BOUND = "Bound";
	public static final String TCP_STATE_CLOSED = "Closed";
	public static final String TCP_STATE_CLOSE_WAIT = "Close Wait";
	public static final String TCP_STATE_CLOSING = "Closing";
	public static final String TCP_STATE_ESTABLISHED = "Establised";
	public static final String TCP_STATE_FIN_WAIT1 = "Fin Wait1";
	public static final String TCP_STATE_FIN_WAIT2 = "Fin Wait2";
	public static final String TCP_STATE_IDLE = "Idle";
	public static final String TCP_STATE_LAST_ACK = "Last Ack";
	public static final String TCP_STATE_LISTEN = "Listen";
	public static final String TCP_STATE_SYN_RECV = "Syn Recv";
	public static final String TCP_STATE_SYN_SENT = "Syn Sent";
	public static final String TCP_STATE_TIME_WAIT = "Time Wait";

	// Other
	public static final String ALL_INBOUND_TOTAL = "All Inbound Total";
	public static final String ALL_OUTBOUND_TOTAL = "All Outbound Total";
	
}
