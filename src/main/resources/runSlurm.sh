#!/bin/bash
#SBATCH --time=01:30:00
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=84
#SBATCH --mem-per-cpu=4096

# This job should only be run with --array given via command line
# NTASKS_PER_NODE may be overwritten by command line argument

#tst for HT stepsize 192:  #SBATCH --array=1-384:192
#tst #SBATCH --array=1-384:192


date
hostname

cd $SLURM_SUBMIT_DIR

classpath="matsim-episim-1.0-SNAPSHOT.jar"

echo "***"
echo "classpath: $classpath"
echo "***"

# main
main="org.matsim.run.RunFromConfig"

module load java/11.0.6
java -version

#
# Start & pin multiple processes on different physical cores of a node
#
export SLURM_CPU_BIND=none

let NTpn=--SLURM_NTASKS_PER_NODE	# all physical cores
#let NTpn=--SLURM_CPUS_ON_NODE		# all logical HT cores
let offset=$(( ${SLURM_ARRAY_TASK_ID:-1} + ${EXTRA_OFFSET:-0} ))

echo "NTpn $NTpn  offset $offset including extra offset ${EXTRA_OFFSET}"

for cid in $(seq 0 $NTpn); do
   let nID=offset+cid		# [0,...] requires array stepsize of NTpn (number of tasks to be run per node)
# arguments
   input="input/config_${SLURM_JOB_NAME}${nID}.xml"
   arguments="$input --config:controler.runId ${SLURM_JOB_NAME}${nID}"
   command="java -cp $classpath $JAVA_OPTS @jvm.options -Xmx4G $main $arguments"
   echo ""
   echo "command is $command"
   test -f $input && taskset -c $cid $command &
done
wait
