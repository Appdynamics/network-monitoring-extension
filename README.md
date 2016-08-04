# Network Monitoring Extension  

##Use Case

Use for monitoring network related metrics.

It uses the machine agent's Sigar library to retrieve the metrics, however these metrics (all or partial) can be overriden through scripting if required.

This extension only works with standalone machine agent. 

**Note : By default, the Machine agent and AppServer agent can only send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).**

##Installation
1. To build from source, clone this repository and run 'mvn clean install'. This will produce a NetworkMonitor-VERSION.zip in the target directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/network-monitoring-extension/releases).
2. Copy and unzip NetworkMonitor.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yaml file and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

##Configuration
###config.yaml
**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Param | Description |
| ----- | ----- |
| networkInterfaces | The network interface to monitor. To monitor multiple interfaces, separate the values using comma, e.g. eth0, eth1, eth2  |
| overrideMetricsUsingScriptFile | If set to true, it will execute the defined script for the OS to retrieve metrics. Default value is false.|
| scriptTimeoutInSec | The timeout in seconds of script execution.|
| scriptFiles (osType) | Type of OS: It's either '**windows**' for Windows or '**unixBase**' for other OS such as Linux, MAC, Solaris, etc.|
| scriptFiles (filePath) | The path of the script file. Either provide a relative path from the machine agent's installation dir or an absolute path. Two script templates are provided, one for each osType.|
| deltaMetrics | Comma separated full path of the metrics for which delta (present - previous) needs to be computed |
| metricPrefix | The path prefix for viewing metrics in the metric browser. Default is "Custom Metrics&#124;Network&#124;"|

**Below is an example config for monitoring multiple interfaces with enabled metrics scripting override:**

~~~~
networkInterfaces: [ eth0, eth1
]

overrideMetricsUsingScriptFile: true

scriptTimeoutInSec: 60

scriptFiles:
  - osType: windows
    filePath: monitors/NetworkMonitor/scripts/windows-metrics.bat
  - osType: unixBase
    filePath: monitors/NetworkMonitor/scripts/unix-base-metrics.sh

# Comma separated full path of the metrics for which delta (present - previous) needs to be computed.
# For eg. deltaMetrics: ["TCP|State|Bound", "TCP|Segments Retransmitted"]
deltaMetrics: []

metricPrefix:  "Custom Metrics|Network|"
~~~~

###Script File
There maybe cases where Sigar doesn't work on your environment or you've a preferred way of retrieving the metrics. In any case, you have the flexibility to override some or all metrics using scripting.

There are two script templates provided:

1. unix-base-metrics.sh for non-windows environment
2. windows-metrics.bat for windows environment

Update the relevant script template by uncommenting out the metrics you wish to override and provide the value.

Below is an example override on some TCP State metrics in unix-base-metrics.sh:

~~~~
...
echo "name=TCP|State|Close Wait,value=" `netstat -an | grep -i CLOSE_WAIT | wc -l`
echo "name=TCP|State|Establised,value=" `netstat -an | grep -i ESTABLISHED | wc -l`
echo "name=TCP|State|Fin Wait1,value=" `netstat -an | grep -i FIN_WAIT_1 | wc -l`
echo "name=TCP|State|Fin Wait2,value=" `netstat -an | grep -i FIN_WAIT_2 | wc -l`
echo "name=TCP|State|Listen,value=" `netstat -an | grep -i LISTEN | wc -l`
...
~~~~

Below is an example override for some custom metric in windows-metric.bat:

~~~
...
set cmd="netstat -an | find "192.168.31" /c"
FOR /F %%i IN (' %cmd% ') DO SET val=%%i
echo name=Autotrader^|Listen,value=%val%
...
~~~


##Metrics
Metrics value reported is the computed delta value (present value - previous value)
Metric path is typically: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Network|** followed by the individual metrics below:

###Network Interface
**Note: \<network_interface\> is replaced with the actual network interface name, e.g eth0**

| Metric | Description |
| ----- | ----- |
| \<network_interface\>&#124;RX KiloBytes | The total kilo bytes received |
| \<network_interface\>&#124;RX Dropped | The number of dropped packets due to reception errors |
| \<network_interface\>&#124;RX Errors | The number of damaged packets received |
| \<network_interface\>&#124;RX Frame | The number received packets that experienced frame errors |
| \<network_interface\>&#124;RX Overruns | The number of received packets that experienced data overruns |
| \<network_interface\>&#124;RX Packets | The number of packets received |
| \<network_interface\>&#124;Speed | The speed in bits per second |
| \<network_interface\>&#124;TX KiloBytes | The total kilo bytes transmitted |
| \<network_interface\>&#124;TX Carrier | The number received packets that experienced loss of carriers |
| \<network_interface\>&#124;TX Collision | The number of transmitted packets that experienced Ethernet collisions |
| \<network_interface\>&#124;TX Dropped | The number of dropped transmitted packets due to transmission errors |
| \<network_interface\>&#124;TX Errors | The number of packets that experienced transmission error |
| \<network_interface\>&#124;TX Overruns | The number of transmitted packets that experienced data overruns |
| \<network_interface\>&#124;TX Packets | The number of packets transmitted |

###TCP
| Metric | Description |
| ----- | ----- |
| TCP&#124;Active Opens | The number of active opens |
| TCP&#124;Attempt Fails | The number of attempted connection failed |
| TCP&#124;Conn Established | The number of connection established |
| TCP&#124;Resets Received | The number of resets received |
| TCP&#124;Bad Segments | The number of bad segments |
| TCP&#124;Segments Received | The number of segments received |
| TCP&#124;Inbound Total | Total number of TCP inbound connections |
| TCP&#124;Resets Sent | The number of resets sent |
| TCP&#124;Segments Sent | The number of segments sent |
| TCP&#124;Outbound Total | Total number of TCP outbound connections |
| TCP&#124;Passive Opens | The number of passive opens |
| TCP&#124;Segments Retransmitted | The number of segments retransmitted |

###TCP State
| Metric | Description |
| ----- | ----- |
| TCP&#124;State&#124;Bound | The number of bound connections |
| TCP&#124;State&#124;Close Wait | The number of connections waiting for termination request from the local user |
| TCP&#124;State&#124;Closed | The number of closed connections |
| TCP&#124;State&#124;Closing | The number of connections waiting for termination acknowledgment |
| TCP&#124;State&#124;Establised | The number of open connections |
| TCP&#124;State&#124;Fin Wait1 | The number of connections waiting for termination requests from the remote TCP or acknowledgment of termination request previously sent |
| TCP&#124;State&#124;Fin Wait2 | The number of connections waiting for termination requests from the remote TCP |
| TCP&#124;State&#124;Idle | The number of connections in idle |
| TCP&#124;State&#124;Last Ack | The number of connections waiting for lask acknowledgment of termination request sent to remote TCP |
| TCP&#124;State&#124;Listen | The number of listening for connections |
| TCP&#124;State&#124;Syn Recv | The number of confirming connection requests acknowledgment after having both received and sent a connection request |
| TCP&#124;State&#124;Syn Sent | The number of waiting for a matching connection request after having sent a connection request |
| TCP&#124;State&#124;Time Wait | The number of  connections waiting for enough time to pass to be sure the remote TCP received the acknowledgment of its connection termination request|

###Other
| Metric | Description |
| ----- | ----- |
| All Inbound Total | Total number of incoming connections |
| All Outbound Total | Total number of outgoing connections |

##Platform Tested

| Platform | Version |
| ----- | ----- |
| Ubuntu | 12.04 LTS |
| Windows | 7 |
| Mac OSX | 10.9.1 |

##Agent Compatibility

| Version |
| ----- |
| 3.7.11+ |

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub

##Community

Find out more in the [AppSphere](http://community.appdynamics.com/t5/eXchange-Community-AppDynamics/Network-Monitoring-Extension/idi-p/9497) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).

