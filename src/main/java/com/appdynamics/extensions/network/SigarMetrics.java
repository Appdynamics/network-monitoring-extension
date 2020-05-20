/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.network;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Maps;
import org.hyperic.sigar.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Uses Sigar to fetch network metrics
 * 
 * @author Florencio Sarmiento
 *
 */
public class SigarMetrics {
	
	public static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(SigarMetrics.class);
	
	private Map<String, NetInterfaceStat> netInterfaceMap = new HashMap<String, NetInterfaceStat>();
	
	private Tcp tcp;
	
	private NetStat netStat;

	public SigarMetrics(Set<String> networkInterfaces) {
		Sigar sigar = new Sigar();
		initialiseNetInterfaceMap(sigar, networkInterfaces);
		initialiseTcp(sigar);
		initialiseNetStat(sigar);
	}
	
	public Tcp getTcp() {
		return tcp;
	}

	public NetStat getNetStat() {
		return netStat;
	}

	public NetInterfaceStat getNetInterfaceStat(String netInterfaceName) {
		return netInterfaceMap.get(netInterfaceName);
	}

	private void initialiseNetInterfaceMap(Sigar sigar, Set<String> networkInterfaces) {
		Map<String, String> networkInterfaceMapOfDescriptionAndName = logAndPopulateNetInterfaces(sigar);
		String os = System.getProperty("os.name").toLowerCase();
		for (String netInterfaceName : networkInterfaces) {
			if (os.contains("win")) {
				String networkInterfaceActualName = networkInterfaceMapOfDescriptionAndName.get(netInterfaceName);
				if (networkInterfaceActualName != null) {
					netInterfaceMap.put(netInterfaceName, getNetworkInterfaceStat(sigar, networkInterfaceActualName));
				}
			} else {
				netInterfaceMap.put(netInterfaceName, getNetworkInterfaceStat(sigar, netInterfaceName));
			}
		}
	}

	private Map<String, String> logAndPopulateNetInterfaces(Sigar sigar) {
		//linux: (eth1, eth1)
		//windows: ("﻿Intel(R) PRO/1000 MT Desktop Adapter", eth6)
		Map<String, String> networkInterfaceMapOfDescriptionAndName = Maps.newHashMap();
		try {
			String [] interfaces = sigar.getNetInterfaceList();
			for (String networkInterface : interfaces) {
				NetInterfaceConfig interfaceConfig = sigar.getNetInterfaceConfig(networkInterface);
				if (interfaceConfig != null) {
					if (! (NetFlags.isAnyAddress(interfaceConfig.getAddress()) || NetFlags.isLoopback(interfaceConfig.getAddress()))) {
						LOGGER.debug("Network interface Details - Name:" + interfaceConfig.getName() + " Description:" + interfaceConfig.getDescription() + " Address:" + interfaceConfig.getAddress() + " Type:" + interfaceConfig.getType());
						networkInterfaceMapOfDescriptionAndName.put(interfaceConfig.getDescription(), interfaceConfig.getName());
					}
				}
			}
		} catch (SigarException e) {
			LOGGER.error("Error while trying to fetch Network Interface List using Sigar " + e);
		}
		return networkInterfaceMapOfDescriptionAndName;
	}

	private NetInterfaceStat getNetworkInterfaceStat(Sigar sigar, String netInterfaceName) {
		NetInterfaceStat netInterfaceStat = null;
		
		try {
			netInterfaceStat = sigar.getNetInterfaceStat(netInterfaceName);
			
		} catch (SigarException ex) {
			LOGGER.error(String.format("Unable to retrieve network interface stat for %s:",
					netInterfaceName), ex);
			
		} catch (UnsatisfiedLinkError err) {
			LOGGER.error(String.format(
					"Unable to retrieve network interface stat for %s. OS specific Sigar lib not found. Use scripting to retrieve metrics.",
					netInterfaceName));
		}
		
		return netInterfaceStat;
	}
	
	private void initialiseTcp(Sigar sigar) {
		try {
			tcp = sigar.getTcp();
			
		} catch (SigarException ex) {
			LOGGER.error("Unable to retrieve tcp details:", ex);
			
		} catch (UnsatisfiedLinkError err) {
			LOGGER.error("Unable to retrieve tcp details. OS specific Sigar lib not found. Use scripting to retrieve metrics.");
		}
	}
	
	private void initialiseNetStat(Sigar sigar) {
		try {
			netStat = sigar.getNetStat();
			
		} catch (SigarException ex) {
			LOGGER.error("Unable to retrieve netstat details:", ex);
			
		}catch (UnsatisfiedLinkError err) {
			LOGGER.error("Unable to retrieve netstat details. OS specific Sigar lib not found. Use scripting to retrieve metrics.");
		}
	}
	
}
