#!/usr/bin/env bash

# THIS FILE IS GENERATED AUTOMATICALLY

VENV=alignclf-venv/bin/activate
. ${VENV}

which python

cd prom-nightly

PYTHON=python
RUN=../src/run.py
CONFIG=../configs/BPI2018-net5-ILP/inc3-sese_25-score-strict/configs.json
LOGGINGCONFIGS=../src/logging.json
TIME_FILE="../times.txt"
N=1

for i in $(seq 1 $N) 
do
   echo "Iteration $i"
   ../scripts/time -a -o ${TIME_FILE} -f "Command: %C\n[mm:ss.ms]: %E\n" $PYTHON $RUN -c "${CONFIG}" -l "${LOGGINGCONFIGS}"
   sleep 5
done

echo "All done!"
