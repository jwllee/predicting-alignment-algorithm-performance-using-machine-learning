#!/usr/bin/env python

import os, sys, argparse, pickle, time
import pandas as pd
import numpy as np
from sklearn import tree
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


# ranges of parameters to grid search
CRITERION = ['gini', 'entropy']
MAX_DEPTH = list(range(3, 15))
MIN_SAMPLES_LEAF = list(range(10, 110, 10))
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


@timeit(on=True, verbose=False)
def perform_gridsearch(X, y, n_folds=5, file=sys.stdout):
    params = {
        'criterion': CRITERION,
        'max_depth': MAX_DEPTH,
        'min_samples_leaf': MIN_SAMPLES_LEAF
    }

    print('Param search space: \n{!r}\n'.format(params), file=file)

    n_proc = mp.cpu_count() - 1
    value_count = get_uniq_count(y)
    proportions = {key: val / len(y) for key, val in value_count.items()}
    class_weight = 'balanced'

    print('n_folds: {}, '
          'n_proc: {}, '
          'value_count: {}, '
          'proportion: {!r}, '
          'class_weight: {}'.format(n_folds, n_proc, value_count, proportions, class_weight), file=file)

    tree_clf = tree.DecisionTreeClassifier(class_weight=class_weight, random_state=0)
    clf = GridSearchCV(tree_clf, params, n_jobs=n_proc, cv=n_folds)
    clf.fit(X=X, y=y)

    print('Best parameter set found on development set\n', file=file)
    print(clf.best_params_, file=file)
    print('Best score: {:.3f}\n'.format(clf.best_score_), file=file)
    print('Grid scores on development set: \n', file=file)
    means = clf.cv_results_['mean_test_score']
    stds = clf.cv_results_['std_test_score']
    for mean, std, params in zip(means, stds, clf.cv_results_['params']):
        print('{:.3f} (+/-{:.3f}) for {!r}'.format(mean, std, params), file=file)
    print(file=file)

    return clf


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
    outdir = '_'.join([dt, 'tree_testing']) if TESTING else '_'.join([dt, 'tree'])
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

        dot_fp = os.path.join(fold_dir, 'tree.dot')
        tree_fp = os.path.join(fold_dir, 'gridsearch.sav')

        X_train, X_test = X[train_index, 1:], X[test_index, 1:] # exclude benchmark_id
        y_train, y_test = y[train_index], y[test_index]

        print('Training set: {} rows'.format(X_train.shape[0]), file=file)
        print('Test set: {} rows'.format(X_test.shape[0]), file=file)

        scores = ['precision', 'recall']

        clf = perform_gridsearch(X_train, y_train, n_folds=int(N_FOLDS), file=file)

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

        # export tree
        feature_names = df.iloc[:, 1:-10].columns
        target_names = ['CLASSIC', 'CLASSIC-SP', 'RECOMPOSE', 'RECOMPOSE-SP']
        tree.export_graphviz(clf.best_estimator_, out_file=dot_fp,
                            feature_names=feature_names,
                            class_names=target_names,
                            filled=True, rounded=True,
                            special_characters=True)

        # save trained model
        with open(tree_fp, 'wb') as f:
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
