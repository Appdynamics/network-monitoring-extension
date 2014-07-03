# Network Monitoring Extension  

##Use Case

Use for monitoring network related metrics.

It uses the machine agent's Sigar library to retrieve the metrics, however these metrics (all or partial) can be overriden through scripting if required.

This extension only works with standalone machine agent. 

**Note : By default, the Machine agent and AppServer agent can only send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).**

##Installation
1. Run 'mvn clean install' from network-monitoring-extension directory
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

metricPrefix:  "Custom Metrics|Network|"
~~~~

###Script File
There maybe cases where Sigar doesn't work on your environment or you've a preferred way of retrieving the metrics. In any case, you have the flexibility to override some or all metrics using scripting.

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


##Metrics
Metric path is typically: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Network|** followed by the individual metrics below:

###Network Interface
**Note: <network_interface> is replaced with the actual network interface name, e.g eth0**

| Metric | Description |
| ----- | ----- |
| <network_interface>&#124;RX Bytes | |
| <network_interface>&#124;RX Dropped | |
| <network_interface>&#124;RX Errors | |
| <network_interface>&#124;RX Frame | |
| <network_interface>&#124;RX Overruns | |
| <network_interface>&#124;RX Packets | |
| <network_interface>&#124;Speed | |
| <network_interface>&#124;TX Bytes | |
| <network_interface>&#124;TX Carrier | |
| <network_interface>&#124;TX Collision | |
| <network_interface>&#124;TX Dropped | |
| <network_interface>&#124;TX Errors | |
| <network_interface>&#124;TX Overruns | |
| <network_interface>&#124;TX Packets | |

###TCP
| Metric | Description |
| ----- | ----- |
| TCP&#124;Active Opens | |
| TCP&#124;Attempt Fails | |
| TCP&#124;Conn Established | |
| TCP&#124;Resets Received | |
| TCP&#124;Bad Segments | |
| TCP&#124;Segments Received | |
| TCP&#124;Inbound Total | |
| TCP&#124;Resets Sent | |
| TCP&#124;Segments Sent | |
| TCP&#124;Outbound Total | |
| TCP&#124;Passive Opens | |
| TCP&#124;Segments Retransmitted | |

###TCP State
| Metric | Description |
| ----- | ----- |
| TCP&#124;State&#124;Bound | |
| TCP&#124;State&#124;Close Wait | |
| TCP&#124;State&#124;Closed | |
| TCP&#124;State&#124;Closing | |
| TCP&#124;State&#124;Establised | |
| TCP&#124;State&#124;Fin Wait1 | |
| TCP&#124;State&#124;Fin Wait2 | |
| TCP&#124;State&#124;Idle | |
| TCP&#124;State&#124;Last Ack | |
| TCP&#124;State&#124;Listen | |
| TCP&#124;State&#124;Syn Recv | |
| TCP&#124;State&#124;Syn Sent | |
| TCP&#124;State&#124;Time Wait | |

###Other
| Metric | Description |
| ----- | ----- |
| All Inbound Total | |
| All Outbound Total | |

##Platform Tested

| Platform | Version |
| ----- | |
| Ubuntu | 12.04 LTS |
| Windows | 7 |
| Mac OSX | 10.9.1 |

##Agent Compatibility

| Version |
| ----- |
| 3.7.11+ |

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).

