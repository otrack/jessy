#!/bin/sh

SSHCMD="oarsh" # ssh or oarsh for oar equiped cluster
scriptdir="/home/msaeida/jessy_script"
workingdir="/tmp/jessy_exec"

nodes=("cluster1u1" "cluster1u2" "cluster1u4" "cluster1u5" "cluster1u6" "cluster1u7" "cluster1u8" "cluster1u9" "cluster1u11" "cluster1u12" "cluster1u13" "cluster1u14" "cluster1u15" "cluster1u16" "cluster1u17" "cluster1u18" "cluster1u19" "cluster1u20" "cluster1u21" "cluster1u22" "cluster1u23" "cluster1u24")
servers=("cluster1u1" ) #"cluster1u2" "cluster1u4" "cluster1u5")
clients=("cluster1u24" ) #"cluster1u23" "cluster1u22")

# Experience settings

system=jessy

if [[ ${system} == "cassandra"  ]];
then
    clientclass=com.yahoo.ycsb.CassandraClient10;
    classpath=${scriptdir}/jessy.jar
    for jar in ${scriptdir}/cassandra/lib/*.jar; do
	classpath=$classpath:$jar
    done
fi;

if [[ ${system} == "jessy"  ]];
then
    clientclass=com.yahoo.ycsb.JessyDBClient;
    classpath=${scriptdir}/commons-lang.jar:${scriptdir}/log4j.jar:${scriptdir}/jessy.jar:${scriptdir}/fractal.jar:${scriptdir}/je.jar:${scriptdir}/db.jar:${scriptdir}/concurrentlinkedhashmap.jar;
fi;



# Client specific settings 

workloadType="-load"
workloadName="transactionalworkloada"
nthreads=30

