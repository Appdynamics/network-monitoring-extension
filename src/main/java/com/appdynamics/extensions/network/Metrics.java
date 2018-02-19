/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import static com.appdynamics.extensions.network.NetworkConstants.*;

/**
 * Enums of metrics names including path (excluding metric prefix)
 * 
 * @author Florencio Sarmiento
 *
 */
public enum Metrics {
	
	// Network card metrics
	NIC_RX_BYTES("%s" + DEFAULT_DELIMETER + "RX Bytes"),
    NIC_RX_KILOBYTES("%s" + DEFAULT_DELIMETER + "RX KB"),
	NIC_RX_DROPPED("%s" + DEFAULT_DELIMETER + "RX Dropped"),
	NIC_RX_ERRORS("%s" + DEFAULT_DELIMETER + "RX Errors"),
	NIC_RX_FRAME("%s" + DEFAULT_DELIMETER + "RX Frame"),
	NIC_RX_OVERRUNS("%s" + DEFAULT_DELIMETER + "RX Overruns"),
	NIC_RX_PACKETS("%s" + DEFAULT_DELIMETER + "RX Packets"),
	NIC_SPEED("%s" + DEFAULT_DELIMETER + "Speed"),
	NIC_TX_BYTES("%s" + DEFAULT_DELIMETER + "TX Bytes"),
    NIC_TX_KILOBYTES("%s" + DEFAULT_DELIMETER + "TX KB"),
	NIC_TX_CARRIER("%s" + DEFAULT_DELIMETER + "TX Carrier"),
	NIC_TX_COLLISIONS("%s" + DEFAULT_DELIMETER + "TX Collision"),
	NIC_TX_DROPPED("%s" + DEFAULT_DELIMETER + "TX Dropped"),
	NIC_TX_ERRORS("%s" + DEFAULT_DELIMETER + "TX Errors"),
	NIC_TX_OVERRUNS("%s" + DEFAULT_DELIMETER + "TX Overruns"),
	NIC_TX_PACKETS("%s" + DEFAULT_DELIMETER + "TX Packets"),
	
	// TCP metrics
	TCP_ACTIVE_OPENS(TCP + DEFAULT_DELIMETER + "Active Opens"),
	TCP_ATTEMPT_FAILS(TCP + DEFAULT_DELIMETER + "Attempt Fails"),
	TCP_CURRENT_ESTABLISHED(TCP + DEFAULT_DELIMETER + "Conn Established"),
	TCP_ESTABLISHED_RESETS(TCP + DEFAULT_DELIMETER + "Resets Received"),
	TCP_IN_ERRORS(TCP + DEFAULT_DELIMETER + "Bad Segments"),
	TCP_IN_SEGMENTS(TCP + DEFAULT_DELIMETER + "Segments Received"),
	TCP_OUT_RESETS(TCP + DEFAULT_DELIMETER + "Resets Sent"),
	TCP_OUT_SEGMENTS(TCP + DEFAULT_DELIMETER + "Segments Sent"),
	TCP_PASSIVE_OPENS(TCP + DEFAULT_DELIMETER + "Passive Opens"),
	TCP_RETRANS_SEGMENTS(TCP + DEFAULT_DELIMETER + "Segments Retransmitted"),
	TCP_INBOUND_TOTAL(TCP + DEFAULT_DELIMETER + "Inbound Total"),
	TCP_OUTBOUND_TOTAL(TCP + DEFAULT_DELIMETER + "Outbound Total"),
	
	// TCP States
	TCP_STATE_BOUND(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Bound"),
	TCP_STATE_CLOSED(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Closed"),
	TCP_STATE_CLOSE_WAIT(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Close Wait"),
	TCP_STATE_CLOSING(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Closing"),
	TCP_STATE_ESTABLISHED(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Establised"),
	TCP_STATE_FIN_WAIT1(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Fin Wait1"),
	TCP_STATE_FIN_WAIT2(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Fin Wait2"),
	TCP_STATE_IDLE(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Idle"),
	TCP_STATE_LAST_ACK(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Last Ack"),
	TCP_STATE_LISTEN(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Listen"),
	TCP_STATE_SYN_RECV(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Syn Recv"),
	TCP_STATE_SYN_SENT(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Syn Sent"),
	TCP_STATE_TIME_WAIT(TCP + DEFAULT_DELIMETER + STATE + DEFAULT_DELIMETER + "Time Wait"),
	
	// Other
	ALL_INBOUND_TOTAL("All Inbound Total"),
	ALL_OUTBOUND_TOTAL("All Outbound Total");
	
	private String displayName;
	
	Metrics (String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
