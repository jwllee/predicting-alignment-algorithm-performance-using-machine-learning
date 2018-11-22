#!/usr/bin/env python


import os, sys
import pandas as pd
import numpy as np
from alignclf import analysis


if __name__ == '__main__':
    agg_result_dir = os.path.join('.', 'results-agg', '2018-11-13')
    recomposing_processor = analysis.RecomposingReplayResultProcessor()
    recomposing_processor.process_directory(agg_result_dir)

    monolithic_processor = analysis.MonolithicReplayResultProcessor()
    monolithic_processor.process_directory(agg_result_dir)
