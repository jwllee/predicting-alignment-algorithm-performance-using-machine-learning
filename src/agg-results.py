#!/usr/bin/env python


import os
from alignclf import analyze
from alignclf.utils import setup_logging


if __name__ == '__main__':
    # use default logging
    setup_logging(default_path='')
    agg_result_dir = os.path.join('.', 'results-agg', '2018-12-03')

    for dirname in os.listdir(agg_result_dir):
        replay_result_dir = os.path.join(agg_result_dir, dirname)

        if 'jupyter' in dirname:
            continue

        if 'mono' in dirname:
            monolithic_processor = analyze.MonolithicReplayResultProcessor()
            logs_stats_df = monolithic_processor.process_directory(replay_result_dir)

            print(logs_stats_df.head())
        else:
            recomposing_processor = analyze.RecomposeReplayResultProcessor()
            logs_stats_df = recomposing_processor.process_directory(replay_result_dir)

            print(logs_stats_df.head())

