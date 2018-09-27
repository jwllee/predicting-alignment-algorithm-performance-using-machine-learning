#!/usr/bin/env bash

cd prom-nightly

PYTHON=python
RUN=../src/run.py
CONFIG=../configs/minimal/configs.json
LOGGINGCONFIGS=../src/logging.json

# variables containing spaces need to be surrounded with double quotes so that
# they are passed as one single variable!
$PYTHON $RUN -c "${CONFIG}" -l "${LOGGINGCONFIGS}"

echo "All done!"
