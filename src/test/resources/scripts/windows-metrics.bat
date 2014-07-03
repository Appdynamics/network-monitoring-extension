@echo off

rem ***************************************************************************
rem Uncomment out the metrics you wish to override and provide the relevant value
rem ***************************************************************************

rem ***************************************************************************
rem Network interface metrics
rem replace <network_interface> with the interface name provided in config.yaml
rem to provide metrics for different network interfaces, 
rem copy and paste the relevant metrics for each interface
rem ***************************************************************************
 
rem echo name=<network_interface>^|RX Bytes,value=
rem echo name=<network_interface>^|RX Dropped,value=
rem echo name=<network_interface>^|RX Errors,value=
rem echo name=<network_interface>^|RX Frame,value=
rem echo name=<network_interface>^|RX Overruns,value=
rem echo name=<network_interface>^|RX Packets,value=
rem echo name=<network_interface>^|Speed,value=
rem echo name=<network_interface>^|TX Bytes,value=
rem echo name=<network_interface>^|TX Carrier,value=
rem echo name=<network_interface>^|TX Collision,value=
rem echo name=<network_interface>^|TX Dropped,value=
rem echo name=<network_interface>^|TX Errors,value=
rem echo name=<network_interface>^|TX Overruns,value=
rem echo name=<network_interface>^|TX Packets,value=

rem ***************************************************************************
rem TCP metrics
rem ***************************************************************************
rem echo name=TCP^|Active Opens,value=
rem echo name=TCP^|Attempt Fails,value=
rem echo name=TCP^|Conn Established,value=
rem echo name=TCP^|Resets Received,value=
rem echo name=TCP^|Bad Segments,value=
rem echo name=TCP^|Segments Received,value=
rem echo name=TCP^|Inbound Total,value=
rem echo name=TCP^|Resets Sent,value=
rem echo name=TCP^|Segments Sent,value=
rem echo name=TCP^|Outbound Total,value=
rem echo name=TCP^|Passive Opens,value=
rem echo name=TCP^|Segments Retransmitted,value=

rem ***************************************************************************
rem TCP State metrics
rem ***************************************************************************
rem echo name=TCP^|State^|Bound,value=
rem echo name=TCP^|State^|Close Wait,value=
rem echo name=TCP^|State^|Closed,value=
rem echo name=TCP^|State^|Closing,value=
rem echo name=TCP^|State^|Establised,value=
rem echo name=TCP^|State^|Fin Wait1,value=
rem echo name=TCP^|State^|Fin Wait2,value=
rem echo name=TCP^|State^|Idle,value=
rem echo name=TCP^|State^|Last Ack,value=
rem echo name=TCP^|State^|Listen,value=
rem echo name=TCP^|State^|Syn Recv,value=
rem echo name=TCP^|State^|Syn Sent,value=
rem echo name=TCP^|State^|Time Wait,value=

rem ***************************************************************************
rem Other metrics
rem ***************************************************************************
rem echo name=All Inbound Total,value=
rem echo name=All Outbound Total,value=