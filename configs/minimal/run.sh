#!/usr/bin/env bash

# activate the virtual environment
VENV="alignclf-venv/bin/activate"
source ${VENV}

pip install .

cd prom-nightly

PYTHON=python
RUN=../src/run.py
CONFIG=../configs/minimal/configs.json
LOGGINGCONFIGS=../src/logging.json

# variables containing spaces need to be surrounded with double quotes so that
# they are passed as one single variable!
/usr/bin/time -v $PYTHON $RUN -c "${CONFIG}" -l "${LOGGINGCONFIGS}"

echo "All done!"
