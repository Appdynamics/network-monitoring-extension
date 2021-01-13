#!/bin/sh

############################################################################
# Uncomment out the metrics you wish to override and provide the relevant value
############################################################################

############################################################################
# Network interface metrics
# replace <network_interface> with the interface name provided in config.yml
# e.g. echo "name=eth1|RX Bytes,value="
# To provide metrics for different network interfaces, 
# copy and paste the relevant metrics for each interface
############################################################################
#echo "name=<network_interface>|RX Bytes,value="
#echo "name=<network_interface>|RX Dropped,value="
#echo "name=<network_interface>|RX Errors,value="
#echo "name=<network_interface>|RX Frame,value="
#echo "name=<network_interface>|RX Overruns,value="
#echo "name=<network_interface>|RX Packets,value="
#echo "name=<network_interface>|Speed,value="
#echo "name=<network_interface>|TX Bytes,value="
#echo "name=<network_interface>|TX Carrier,value="
#echo "name=<network_interface>|TX Collision,value="
#echo "name=<network_interface>|TX Dropped,value="
#echo "name=<network_interface>|TX Errors,value="
#echo "name=<network_interface>|TX Overruns,value="
#echo "name=<network_interface>|TX Packets,value="

############################################################################
#TCP metrics
############################################################################
echo "name=TCP|Active Opens,value=$(netstat -np TCP | grep \"ESTABLISHED\" | wc -l)"
#echo "name=TCP|Attempt Fails,value="
#echo "name=TCP|Conn Established,value="
#echo "name=TCP|Resets Received,value="
#echo "name=TCP|Bad Segments,value="
#echo "name=TCP|Segments Received,value="
#echo "name=TCP|Inbound Total,value="
#echo "name=TCP|Resets Sent,value="
#echo "name=TCP|Segments Sent,value="
#echo "name=TCP|Outbound Total,value="
#echo "name=TCP|Passive Opens,value="
#echo "name=TCP|Segments Retransmitted,value="

############################################################################
#TCP State metrics
############################################################################
#echo "name=TCP|State|Bound,value="
#echo "name=TCP|State|Close Wait,value="
#echo "name=TCP|State|Closed,value="
#echo "name=TCP|State|Closing,value="
#echo "name=TCP|State|Establised,value="
#echo "name=TCP|State|Fin Wait1,value="
#echo "name=TCP|State|Fin Wait2,value="
#echo "name=TCP|State|Idle,value="
#echo "name=TCP|State|Last Ack,value="
#echo "name=TCP|State|Listen,value="
#echo "name=TCP|State|Syn Recv,value="
#echo "name=TCP|State|Syn Sent,value="
#echo "name=TCP|State|Time Wait,value="

############################################################################
#Other metrics
############################################################################
#echo "name=All Inbound Total,value="
#echo "name=All Outbound Total,value="