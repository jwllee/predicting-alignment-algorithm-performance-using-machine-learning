#!/usr/bin/env python


import os, sys, argparse, pickle, time
import pandas as pd
import numpy as np
from sklearn import tree
from sklearn.model_selection import GridSearchCV, train_test_split
from sklearn.metrics import classification_report
from sklearn.ensemble import RandomForestClassifier
import graphviz
import multiprocessing as mp
from datetime import datetime


from alignclf import utils


idx = pd.IndexSlice

N_ESTIMATORS = list(range(10, 1001, 10))


def get_feature_cols(columns):
    not_min_max = lambda c: not c.endswith('min') and not c.endswith('max')
    not_one_deg = lambda c: not c.endswith('one_in_deg') and not c.endswith('one_out_deg')
    not_two_deg = lambda c: not c.endswith('two_in_deg') and not c.endswith('two_out_deg')
    not_three_deg = lambda c: not c.endswith('three_in_deg') and not c.endswith('three_out_deg')
    not_more_than_five_deg = lambda c: not c.endswith('five_in_deg') and not c.endswith('five_out_deg')

    joined_filter = lambda c: not_min_max(c) and not_one_deg(c) \
                              and not_two_deg(c) and not_three_deg(c) \
                              and not_more_than_five_deg(c)

    return list(filter(joined_filter, columns))


def ns_to_s(df):
    time_cols = list(filter(lambda c: 'time' in c.lower(), df.columns.get_level_values(level=1)))
    df.loc[:, idx[tuple(time_cols), ('astar', 'inc3', 'recomp-astar', 'recomp-inc3')]] /= 1000000

    renamed_time_cols = list(map(lambda col: col.replace('(us)', '(s)'), time_cols))
    renamed_cols_dict = dict(zip(time_cols, renamed_time_cols))
    df.rename(columns=renamed_cols_dict, level=0, inplace=True)
    df.rename(columns=renamed_cols_dict, level=1, inplace=True)


def import_data(fp, convert_time=True):
    df = pd.read_csv(fp, header=[0, 1])
    df.rename(columns={
        'Unnamed: 0_level_1': '',
        'Unnamed: 1_level_1': '',
        'Unnamed: 2_level_1': '',
        'Unnamed: 3_level_1': ''
    }, level=1, inplace=True)

    if convert_time:
        ns_to_s(df)

    df.set_index(['model', 'log', 'decomposition', 'SP label'], inplace=True)

    return df


def filter_row_by_k_time_diff(df, k):
    k_diff = df.loc[:, ('Total Time including setup (s)', 'max_diff')] >= k
    return df.loc[k_diff, :]


def get_uniq_count(l):
    uniq, count = np.unique(l, return_counts=True)
    return dict(zip(uniq, count))


def train_forest(X, y, file):
    n_procs = mp.cpu_count() - 1
    class_weight = 'balanced'

    print('no. of estimators: {}, '
          'no. of cpu: {}, '
          'class weight: {}\n'.format(N_ESTIMATORS, n_procs, class_weight), file=file)

    min_err = np.inf
    best_clf = None
    best_n_est = -1

    n_est_list = []
    oob_err_list = []

    for n_est in N_ESTIMATORS:
        clf = RandomForestClassifier(n_estimators=n_est,
                                     random_state=0, n_jobs=n_procs,
                                     class_weight=class_weight,
                                     oob_score=True)
        clf.fit(X=X, y=y)

        oob_error = 1 - clf.oob_score_
        n_est_list.append(n_est)
        oob_err_list.append(oob_error)

        if oob_error < min_err:
            min_err = oob_error
            best_clf = clf
            best_n_est = n_est

    oob_err_df = pd.DataFrame({
        'n_estimator': n_est_list,
        'oob_error': oob_err_list
    })
    print('Best n_est: {}'.format(best_n_est), file=file)

    return best_clf, oob_err_df


def perform_test(X, y, clf, file=sys.stdout):
    print('Detailed classification report:\n', file=file)
    print('The model is trained on the full development set.', file=file)
    print('The scores are computed on the full evaluation set.\n', file=file)
    y_true, y_pred = y, clf.predict(X)
    print(classification_report(y_true, y_pred), file=file)
    print(file=file)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('-fp', action='store',
                        dest='fp',
                        help='File path to dataset')
    parser.add_argument('-n', action='store',
                        dest='n_folds',
                        help='Number of folds in cross validation')
    parser.add_argument('-o', action='store',
                        dest='outdir',
                        help='Output directory')

    args = parser.parse_args()

    if args.fp is None or args.outdir is None:
        print('Run as python ./run_random_forest.py -f [data.csv] -o [outdir]')
        exit(0)

    if not os.path.isfile(args.fp):
        print('{} is not a file'.format(args.fp))
        exit(0)

    # create a folder
    dt = datetime.now().strftime('%Y-%m-%d_%H-%M-%S-%f')
    outdir = '_'.join([dt, 'random-forest'])
    outdir = os.path.join(args.outdir, outdir)

    os.makedirs(outdir)

    print_fp = os.path.join(outdir, 'printout.txt')
    forest_fp = os.path.join(outdir, 'forest.sav')
    oob_err_fp = os.path.join(outdir, 'oob-err.csv')
    file = open(print_fp, 'w')
    start = time.time()

    class_map = {
        'astar': 0,
        'inc3': 1,
        'recomp-astar': 2,
        'recomp-inc3': 3
    }

    print('Mapping from algorithm to int: {}\n'.format(class_map), file=file)

    k = 2
    df = import_data(args.fp, convert_time=True)
    print('Before filtering: {} rows'.format(df.shape[0]), file=file)
    df = filter_row_by_k_time_diff(df, k)
    print('Filter row by requiring at least {} times time difference: {} rows'.format(k, df.shape[0]), file=file)
    # df = df.iloc[:10000, :]

    # print(df.columns[3])

    columns = df.loc[:, idx['model_trace_features', :]].columns.get_level_values(level=1)
    # columns = get_feature_cols(list(columns))

    print('{} feature columns: \n{}'.format(len(columns), columns), file=file)

    X = df.loc[:, idx['model_trace_features', columns]]
    y = df.loc[:, ('Min', 'Total Time including setup (s)')].map(class_map)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.4, random_state=0)

    print('Dataset: {} rows'.format(X.shape[0]), file=file)
    print('Training set: {} rows'.format(X_train.shape[0]), file=file)
    print('Test set: {} rows'.format(X_test.shape[0]), file=file)

    best_clf, oob_err_df = train_forest(X_train, y_train, file)

    perform_test(X_test, y_test, best_clf, file)

    # save trained model
    with open(forest_fp, 'wb') as f:
        pickle.dump(best_clf, f)

    oob_err_df.to_csv(oob_err_fp, index=False)

    end = time.time()
    print('Experiment took: {:.2f}s'.format(end - start), file=file)
    print('Experiment took: {:.2f}s'.format(end - start))
    file.close()
