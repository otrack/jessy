#!/bin/bash

#Script to setup the virtual platform for DISTEM
#Usage : ./distem_platform_setup.sh walltime(hh:mm:ss) node1 latency(in ms) #servers #clients node2 latency(in ms) #servers #clients
#Eg : ./distem_platform-setup.sh 3:00:00 us 10 2 2 asia 20 2 2 europe 2 2

#set -x
#count=`expr $# - 1`
pnodes=`expr $# / 4`
#echo $pnodes, $#

#echo "Reserving nodes(physical) on Grid5000 ..."
#oarsub -t deploy -l slash_22=1+nodes=$pnodes,walltime=4:00:00 -I
#export OAR_JOB_ID=`oarstat | grep dmalikireddy | cut -d' ' -f1`
#exit
#oarsub -C $OAR_JOB_ID
echo "****************** Reservation already done ********************"
echo ""
echo ""

echo "Deploying on the reserved nodes ..."
kadeploy3 -f $OAR_NODE_FILE -e wheezy-x64-big -k
echo "******************* Deployment done ********************"
echo ""
echo ""

ip=`g5k-subnets -sp`
echo "Virtual IP reserved :"$ip
echo "Installing Distem and setting up the platform ..."
distem-bootstrap --git --node-list $OAR_NODE_FILE -- /home/dmalikireddy/distemjessy/datacenter-internet.rb $@ $ip | tee tmpdistem
echo "*********************** Platform Setup done ***********************"
echo ""
echo ""

echo "Transferring files to coordinator node(HOST)..."
var=`tail -n 1 tmpdistem`
a=($var)
scp -r /home/dmalikireddy/distemjessy root@${a[1]}:/root
echo "********************** Transfer complete********************"
echo ""

echo "Logging into the coordinator for experiments..."
ssh root@${a[1]}
