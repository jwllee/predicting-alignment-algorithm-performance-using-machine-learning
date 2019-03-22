#!/usr/bin/env python

import os, sys, argparse, pickle, time
import pandas as pd
import numpy as np
from sklearn import dummy
import sklearn
from sklearn.model_selection import GridSearchCV, train_test_split, StratifiedKFold
from sklearn.metrics import classification_report
import multiprocessing as mp
from datetime import datetime

idx = pd.IndexSlice


def timeit(on=True, verbose=False):
    def real_timeit(func):
        def timed(*args, **kwargs):
            start_time = time.time()
            if verbose:
                print('Starting {}'.format(func.__name__))
            result = func(*args, **kwargs)
            end_time = time.time()
            took = end_time - start_time
            if on:
                msg = '{} took {:.2f} secs'.format(func.__name__, took)
                print(msg)
            return result
        return timed
    return real_timeit


RANDOM_STATE = 0
N_FOLDS = 5


# directories as ran with provided Dockerfile
TESTING = False
DATASET_FP = os.path.join('..', 'data', 'python', 'original.csv')
OUTDIR = os.path.join('.', 'results')


def ns_to_s(df):
    time_cols = list(filter(lambda c: 'time' in c.lower() and not 'number of times' in c.lower(), df.columns.get_level_values(level=0)))
    for time_col in time_cols:
        df.loc[:, time_col] /= 1000000
        df.loc[:, time_col] /= 1000000
        df.loc[:, time_col] /= 1000000
        df.loc[:, time_col] /= 1000000


def import_data(fp, convert_time=True):
    df = pd.read_csv(fp)

    if convert_time:
        ns_to_s(df)

    return df


def get_uniq_count(l):
    uniq, count = np.unique(l, return_counts=True)
    return dict(zip(uniq, count))


def perform_test(X, y, clf, file=sys.stdout):
    print('Detailed classification report:\n', file=file)
    print('The model is trained on the full development set.', file=file)
    print('The scores are computed on the full evaluation set.\n', file=file)
    y_true, y_pred = y, clf.predict(X)
    print(classification_report(y_true, y_pred), file=file)
    print(file=file)
    return y_pred


if __name__ == '__main__':
    # create a folder
    dt = datetime.now().strftime('%Y-%m-%d_%H-%M-%S-%f')
    outdir = '_'.join([dt, 'rand_all_testing']) if TESTING else '_'.join([dt, 'rand_all'])
    outdir = os.path.join(OUTDIR, outdir)

    os.makedirs(outdir)

    print_fp = os.path.join(outdir, 'printout.txt')
    file = open(print_fp, 'w')
    start = time.time()

    class_map = {
        'astar': 0,
        'inc3': 1,
        'recomp-astar': 2,
        'recomp-inc3': 3
    }
    rev_class_map = {
        0: 'astar',
        1: 'inc3',
        2: 'recomp-astar',
        3: 'recomp-inc3'
    }

    print('Running classification experiment on {}'.format(DATASET_FP), file=file)
    print('Mapping from algorithm to int: {}\n'.format(class_map), file=file)

    df = import_data(DATASET_FP, convert_time=False)

    if TESTING:
        print('Test with small dataset...', file=file)
        df = df.iloc[:10000, :]

    print('{} feature columns: \n{}'.format(len(df.columns), df.columns), file=file)

    X = df.iloc[:, :-10].values
    y = df.iloc[:, -2].map(class_map).values

    print('Dataset: {} rows'.format(X.shape[0]), file=file)

    # create stratified folds
    skf = StratifiedKFold(n_splits=N_FOLDS, shuffle=True, random_state=RANDOM_STATE)
    fold_ind = 0

    result_df_list = list()

    for train_index, test_index in skf.split(X, y):
        print('Starting experiment of fold {}'.format(fold_ind), file=file)
        start_fold = time.time()

        # make separate directory to store results
        fold_dir = os.path.join(outdir, 'fold-{}'.format(fold_ind))
        os.mkdir(fold_dir)

        clf_fp = os.path.join(fold_dir, 'rand.sav')

        X_train, X_test = X[train_index, 1:], X[test_index, 1:] # exclude benchmark_id
        y_train, y_test = y[train_index], y[test_index]

        print('Training set: {} rows'.format(X_train.shape[0]), file=file)
        print('Test set: {} rows'.format(X_test.shape[0]), file=file)

        scores = ['precision', 'recall']

        strategy = 'stratified'
        print('Dummy classifier strategy: {}'.format(strategy))
        clf = dummy.DummyClassifier(strategy, random_state=RANDOM_STATE)
        clf.fit(X_train, y_train)

        y_pred = perform_test(X_test, y_test, clf, file)

        # map back to class names
        y_pred_mapped = list(map(lambda v: rev_class_map[v], y_pred))

        # output the required result_df
        benchmark_ids = X[test_index, 0]
        result_df = pd.DataFrame({
            'benchmark_id': benchmark_ids,
            'pred_algo': y_pred_mapped
        })
        result_df_list.append(result_df)

        # save trained model
        with open(clf_fp, 'wb') as f:
            pickle.dump(clf, f)

        end_fold = time.time()
        print('Experiment {} took: {:.2f}s'.format(fold_ind, end_fold - start_fold), file=file)
        print('Experiment {} took: {:.2f}s'.format(fold_ind, end_fold - start_fold))
        fold_ind += 1

    # join the result_dfs
    result_df = pd.concat(result_df_list, axis=0)
    to_merge_cols = [
        'benchmark_id',
        'min_algo',
        'min_time'
    ]
    to_merge = df.loc[:, to_merge_cols]
    result_df = pd.merge(to_merge, result_df, on='benchmark_id')
    result_df = result_df.sort_values('benchmark_id', ascending=True).reset_index(drop=True)

    # compute the pred_time and pred_is_valid
    perf_cols = [
        'benchmark_id',
        'astar_time',
        'inc3_time',
        'recomp-astar_time',
        'recomp-inc3_time'
    ]
    perf_df = df.loc[:, perf_cols]
    perf_df = perf_df.sort_values('benchmark_id', ascending=True).reset_index(drop=True)
    perf_df.rename(columns={
        'astar_time': 'astar',
        'inc3_time': 'inc3',
        'recomp-astar_time': 'recomp-astar',
        'recomp-inc3_time': 'recomp-inc3'
    }, inplace=True)

    valid_cols = [
        'benchmark_id',
        'astar_is_valid',
        'inc3_is_valid',
        'recomp-astar_is_valid',
        'recomp-inc3_is_valid'
    ]
    valid_df = df.loc[:, valid_cols]
    valid_df = valid_df.sort_values('benchmark_id', ascending=True).reset_index(drop=True)
    valid_df.rename(columns={
        'astar_is_valid': 'astar',
        'inc3_is_valid': 'inc3',
        'recomp-astar_is_valid': 'recomp-astar',
        'recomp-inc3_is_valid': 'recomp-inc3'
    }, inplace=True)

    to_select = result_df.loc[:, 'pred_algo']
    result_df['pred_time'] = perf_df.lookup(to_select.index, to_select.values)
    result_df['pred_is_valid'] = valid_df.lookup(to_select.index, to_select.values)
    
    # output the result_df
    result_df_fp = os.path.join(outdir, 'prediction.csv')
    result_df.to_csv(result_df_fp, index=False)

    end = time.time()
    print('Experiment took: {:.2f}s'.format(end - start), file=file)
    print('Experiment took: {:.2f}s'.format(end - start))
    file.close()
