# Network Monitoring Extension  

##  Use Case

Use for monitoring network related metrics.

It uses the machine agent's Sigar library to retrieve the metrics, however these metrics (all or partial) can be overriden through scripting if required.

This extension only works with standalone machine agent. 

**Note : By default, the Machine agent and AppServer agent can only send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).**

## Prerequisite

Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

## Installation
1. To build from source, clone this repository and run 'mvn clean install'. This will produce a NetworkMonitor-VERSION.zip in the target directory.
2. Copy and unzip NetworkMonitor.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yml file and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

Please place the extension in the "monitors" directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration
### config.yml
**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Param | Description |
| ----- | ----- |
| networkInterfaces | The network interface to monitor. To monitor multiple interfaces, separate the values using comma, e.g. eth0, eth1, eth2  |
| overrideMetricsUsingScriptFile | If set to true, it will execute the defined script for the OS to retrieve metrics. Default value is false.|
| scriptTimeoutInSec | The timeout in seconds of script execution.|
| scriptFiles (osType) | Type of OS: It's either '**windows**' for Windows or '**unixBase**' for other OS such as Linux, MAC, Solaris, etc.|
| scriptFiles (filePath) | The path of the script file. Either provide a relative path from the machine agent's installation dir or an absolute path. Two script templates are provided, one for each osType.|
| metricPrefix | The path prefix for viewing metrics in the metric browser. |

**Below is an example config for monitoring multiple interfaces with enabled metrics scripting override:**

~~~~
# Linux:, networkInterfaces are of the form eth*
# Windows: Please use the network interface full name from Device Manager (﻿Intel(R) PRO/1000 MT Desktop Adapter)
networkInterfaces: [ "eth0", "﻿Intel(R) PRO/1000 MT Desktop Adapter"
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

### Script File
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


## Metrics
Metrics value reported is the computed delta value (present value - previous value)
Metric path is typically: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Network|** followed by the individual metrics below:

### Network Interface
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

### TCP
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

### TCP State
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

### Other
| Metric | Description |
| ----- | ----- |
| All Inbound Total | Total number of incoming connections |
| All Outbound Total | Total number of outgoing connections |

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-use-the-Extensions-WorkBench/ta-p/30130).

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) to contact the support team.

## Support Tickets

If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) you have not been able to get your extension working, please file a ticket with the following information:

1. Stop the running machine agent.
2. Delete all existing logs under <MachineAgent>/logs.
3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
    ```
    <logger name="com.singularity">
    <logger name="com.appdynamics">
   ```
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
   Attach the zipped <MachineAgent>/conf/* directory.
5. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory.

For any support related questions, you can also contact help@appdynamics.com.


## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/network-monitoring-extension)

## Platform Tested

| Platform | Version |
| ----- | ----- |
| Ubuntu | 12.04 LTS |
| Windows | 7 |
| Mac OSX | 10.9.1 |

## Version

| Name                        |  Version                    | 
| :---------------------------| :---------------------------|
| Extension Version:          | 2.1                   |
| Controller Compatibility:   | 4.5 or Later                |
| Agent Compatibility:   | 4.5.13 or Later                |
| Last updated On:            | 07/01/2021          |
| List of changes to this extension| [Change log](https://github.com/Appdynamics/network-monitoring-extension/blob/master/Changelog.md)
