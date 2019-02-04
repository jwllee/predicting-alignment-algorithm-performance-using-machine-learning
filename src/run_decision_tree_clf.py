#!/usr/bin/env python


import os, sys, argparse, pickle, time
import pandas as pd
import numpy as np
from sklearn import tree
import sklearn
from sklearn.model_selection import GridSearchCV, train_test_split
from sklearn.metrics import classification_report
import graphviz
import multiprocessing as mp
from datetime import datetime


from alignclf import utils


idx = pd.IndexSlice


# ranges of parameters to grid search
CRITERION = ['gini', 'entropy']
MAX_DEPTH = list(range(3, 15))
MIN_SAMPLES_SPLIT = list(range(50, 1100, 100))
MIN_SAMPLES_LEAF = list(range(10, 110, 10))


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
    df.loc[:, idx[tuple(time_cols), :]] /= 1000000

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


def get_uniq_count(l):
    uniq, count = np.unique(l, return_counts=True)
    return dict(zip(uniq, count))


@utils.timeit(on=True, verbose=False)
def perform_gridsearch(X, y, n_folds=5, file=sys.stdout):
    params = {
        'criterion': CRITERION,
        'max_depth': MAX_DEPTH,
        # 'min_samples_split': MIN_SAMPLES_SPLIT,
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

    if args.fp is None or args.n_folds is None or args.outdir is None:
        print('Run as python ./run_decision_tree_clf.py -f [data.csv] -n [n_folds] -o [outdir]')
        exit(0)

    if not os.path.isfile(args.fp):
        print('{} is not a file'.format(args.fp))
        exit(0)

    # create a folder
    dt = datetime.now().strftime('%Y-%m-%d_%H-%M-%S-%f')
    outdir = '_'.join([dt, 'tree'])
    outdir = os.path.join(args.outdir, outdir)

    os.makedirs(outdir)

    print_fp = os.path.join(outdir, 'printout.txt')
    dot_fp = os.path.join(outdir, 'tree.dot')
    tree_fp = os.path.join(outdir, 'gridsearch.sav')
    file = open(print_fp, 'w')
    start = time.time()

    class_map = {
        'astar': 0,
        'inc3': 1,
        'recomp-astar': 2,
        'recomp-inc3': 3
    }

    print('Mapping from algorithm to int: {}\n'.format(class_map), file=file)

    df = import_data(args.fp, convert_time=True)
    # df = df.iloc[:10000, :]

    # print(df.columns[3])

    columns = df.loc[:, idx['model_trace_features', :]].columns.get_level_values(level=1)
    columns = get_feature_cols(list(columns))

    print('feature columns: \n{}'.format(columns), file=file)

    X = df.loc[:, idx['model_trace_features', columns]]
    y = df.loc[:, ('Min', 'Total Time including setup (s)')].map(class_map)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.4, random_state=0)

    scores = ['precision', 'recall']

    clf = perform_gridsearch(X_train, y_train, n_folds=int(args.n_folds), file=file)

    perform_test(X_test, y_test, clf, file)

    # export tree
    feature_names = X.columns.get_level_values(level=1)
    target_names = ['CLASSIC', 'CLASSIC-SP', 'RECOMPOSE', 'RECOMPOSE-SP']
    tree.export_graphviz(clf.best_estimator_, out_file=dot_fp,
                         feature_names=feature_names,
                         class_names=target_names,
                         filled=True, rounded=True,
                         special_characters=True)

    # save trained model
    with open(tree_fp, 'wb') as f:
        pickle.dump(clf, f)

    end = time.time()
    print('Experiment took: {:.2f}s'.format(end - start), file=file)
    print('Experiment took: {:.2f}s'.format(end - start))
    file.close()

