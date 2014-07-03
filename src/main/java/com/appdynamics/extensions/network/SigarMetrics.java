package com.appdynamics.extensions.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Tcp;

/**
 * Uses Sigar to fetch network metrics
 * 
 * @author Florencio Sarmiento
 *
 */
public class SigarMetrics {
	
	public static final Logger LOGGER = Logger.getLogger("com.singularity.extensions.network.SigarMetrics");
	
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
		for (String netInterfaceName : networkInterfaces) {
			netInterfaceMap.put(netInterfaceName, getNetworkInterfaceStat(sigar, netInterfaceName));
		}
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
