#!/bin/sh

echo "Hello World"

PS_OUTPUT=`ps -ef | grep java`

echo "name=lo0|RX Bytes, value=1"
echo "name=lo0|TX Packets, value=2"

echo "   name=   TCP|   ACTIVE Opens, value=35608909898   "
echo "name=TCP|Inbound Total, value=4"

echo "name=TCP|State|Bound, value=5"
echo "name=TCP|State|Close Wait, value=6.5"

echo "name=All Inbound Total, value=7"
echo "name=All Outbound Total, value=8"
echo "name=TCP|State|Closing, value=100000"
echo "name=Dodgy, value=3d"

echo "$PS_OUTPUT"
