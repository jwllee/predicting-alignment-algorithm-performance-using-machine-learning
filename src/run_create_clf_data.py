#!/usr/bin/env python3


import os, sys, json
import numpy as np
import pandas as pd
import functools as fct
import collections as cols


from alignclf import create_clf_data


if __name__ == '__main__':
    result_dir = os.path.join('.', 'results-agg', 'clst-IS2017-2-repacked')

    # find out the subset of logs
    model_log_sets = []
    dir_map = dict()

    for d in os.listdir(result_dir):
        dirpath = os.path.join(result_dir, d)

        if not os.path.isdir(dirpath):
            continue

        model_log_set = set()

        for replay_d in os.listdir(dirpath):

            replay_dirpath = os.path.join(dirpath, replay_d)

            if not os.path.isdir(replay_dirpath):
                continue

            configs_fp = os.path.join(replay_dirpath, 'configs.json')
            with open(configs_fp) as f:
                configs_dict = json.load(f)

            log = configs_dict['log']
            model = configs_dict['model']
            if 'recomposeStrategy' in configs_dict:
                algo_type = 'recomp' + '-' + configs_dict['algorithmType']
            else:
                algo_type = configs_dict['algorithmType']

            if model not in dir_map:
                dir_map[model] = cols.defaultdict(list)

            dir_map[model][log].append((algo_type, replay_dirpath))
            model_log_set.add((model, log))

        model_log_sets.append(model_log_set)

    model_logs = list(fct.reduce(lambda s1, s2: s1.intersection(s2), model_log_sets))
    model_log_dict = cols.defaultdict(list)

    for model, log in model_logs:
        model_log_dict[model].append(log)

    # print('Model and logs: {}'.format(model_logs))
    # print('Model log set: {}'.format(model_log_sets))

    clf_df_list = list()

    for model, logs in model_log_dict.items():

        if not logs:
            continue

        for log in logs:

            result_df_dict = dict()
            for algo_type, dirpath in dir_map[model][log]:

                is_mono = 'recomp' not in algo_type

                # print('algo_type: {}'.format(algo_type))

                if is_mono:
                    result_fp = os.path.join(dirpath, 'trace-stats-enriched.csv')
                    result_df = pd.read_csv(result_fp)
                    result_df[create_clf_data.RESULT_DIR] = dirpath
                    result_df = create_clf_data.process_df(result_df)
                else:
                    result_fp = os.path.join(dirpath, 'trace-stats.csv')
                    result_df = pd.read_csv(result_fp)
                    result_df[create_clf_data.RESULT_DIR] = dirpath
                    result_df = create_clf_data.process_recomposing_df(result_df)

                result_df_dict[algo_type] = result_df

            clf_df = create_clf_data.to_clf_df(result_df_dict)

            columns = list(clf_df.columns)
            clf_df['model'] = model
            clf_df['log'] = log
            columns = [('model', ''), ('log', '')] + columns
            clf_df = clf_df[columns]

            clf_df_list.append(clf_df)

    clf_df = pd.concat(clf_df_list, axis=0)
    out_fp = os.path.join(result_dir, 'perf-results.csv')
    clf_df.to_csv(out_fp, index=False)

