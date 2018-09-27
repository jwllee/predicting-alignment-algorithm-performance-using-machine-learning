#!/usr/bin/env bash

# activate the virtual environment
VENV="alignclf-venv/bin/activate"
. ${VENV}

which python

cd prom-nightly

PYTHON=python
RUN=../src/run.py
CONFIG=../configs/minimal/configs.json
LOGGINGCONFIGS=../src/logging.json
TIME_FILE="../times.txt"

N=5

# variables containing spaces need to be surrounded with double quotes so that
# they are passed as one single variable!
for i in $(seq 1 $N)
do
    echo "Iteration $i"
    ../scripts/time -v -a -o ${TIME_FILE} $PYTHON $RUN -c "${CONFIG}" -l "${LOGGINGCONFIGS}"
done

echo "All done!"
