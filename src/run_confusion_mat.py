#!/usr/bin/env python


import os, sys, argparse, pickle, time
import pandas as pd
import numpy as np
from sklearn import dummy
from sklearn.model_selection import GridSearchCV, train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from datetime import datetime
import matplotlib.pyplot as plt
import itertools as itls


from alignclf import utils


idx = pd.IndexSlice


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
    for time_col in time_cols:
        df.loc[:, (time_col, 'astar')] /= 1000000
        df.loc[:, (time_col, 'inc3')] /= 1000000
        df.loc[:, (time_col, 'recomp-astar')] /= 1000000
        df.loc[:, (time_col, 'recomp-inc3')] /= 1000000

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


def plot_confusion_matrix(cm, classes, normalized=False, cmap=plt.cm.Blues):
    if normalized:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print('Normalized confusion matrix')
    else:
        print('Confusion matrix, without normalization')

    print(cm)

    fig, ax = plt.subplots(figsize=(10, 8))

    if normalized:
        img = ax.imshow(cm, interpolation='nearest', cmap=cmap, vmin=0.0, vmax=1.0)
    else:
        img = ax.imshow(cm, interpolation='nearest', cmap=cmap)

    cbar = fig.colorbar(img)
    cbar.ax.tick_params(labelsize=15)
    tick_marks = np.arange(len(classes))
    ax.set_xticks(tick_marks)
    ax.set_yticks(tick_marks)
    ax.set_xticklabels(classes)
    ax.set_yticklabels(classes)

    fmt = '.2f' if normalized else 'd'
    thresh = cm.max() / 2.
    for i, j in itls.product(range(cm.shape[0]), range(cm.shape[1])):
        ax.text(j, i, format(cm[i, j], fmt),
                horizontalalignment='center',
                color='white' if cm[i, j] > thresh else 'black',
                fontsize=15)
        ax.set_ylabel('True label', fontsize=15)
        ax.set_xlabel('Predicted label', fontsize=15)

    for tick in ax.xaxis.get_major_ticks():
        tick.label.set_fontsize(15)
        tick.label.set_rotation(45)
    for tick in ax.yaxis.get_major_ticks():
        tick.label.set_fontsize(15)

    fig.tight_layout()

    return fig, ax


def str2bool(v):
    if v.lower() in ['true', 'false']:
        return True if v.lower() == 'true' else False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('-fp', action='store',
                        dest='fp',
                        help='File path to dataset')
    parser.add_argument('-c', action='store',
                        dest='clf_fp',
                        help='File path to classifier')
    parser.add_argument('-o', action='store',
                        dest='outdir',
                        help='Output directory')
    parser.add_argument('-k', action='store', type=str2bool,
                        dest='filter_k',
                        help='Whether to filter dataframe')

    args = parser.parse_args()

    if args.fp is None or args.outdir is None or args.clf_fp is None or args.filter_k is None:
        print('Run as python ./run_confusion_mat.py -fp [data.csv] -c [clf.sav] -k [true/false] -o [outdir]')
        exit(0)

    if not os.path.isfile(args.fp):
        print('{} is not a file'.format(args.fp))
        exit(0)

    if not os.path.isdir(args.outdir):
        print('{} is not a directory'.format(args.outdir))

    if not os.path.isfile(args.clf_fp):
        print('{} is not a file'.format(args.clf_fp))

    start = time.time()

    with open(args.clf_fp, 'rb') as f:
        clf = pickle.load(f)

    class_map = {
        'astar': 0,
        'inc3': 1,
        'recomp-astar': 2,
        'recomp-inc3': 3
    }

    print('Data file: {}'.format(args.fp))

    print('Running confusion matrix construction on {}'.format(args.fp))

    print('Mapping from algorithm to int: {}\n'.format(class_map))

    k = 2
    df = import_data(args.fp, convert_time=True)
    print('Before filtering: {} rows'.format(df.shape[0]))
    if args.filter_k:
        df = filter_row_by_k_time_diff(df, k)
    print('Filter row by requiring at least {} times time difference: {} rows'.format(k, df.shape[0]))
    # df = df.iloc[:10000, :]

    # print(df.columns[3])

    columns = df.loc[:, idx['model_trace_features', :]].columns.get_level_values(level=1)
    columns = get_feature_cols(list(columns))

    X = df.loc[:, idx['model_trace_features', columns]]
    y = df.loc[:, ('Min', 'Total Time including setup (s)')].map(class_map)

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.4, random_state=0)

    print('Dataset: {} rows'.format(X.shape[0]))
    print('Training set: {} rows'.format(X_train.shape[0]))
    print('Test set: {} rows'.format(X_test.shape[0]))

    if isinstance(clf, GridSearchCV):
        clf = clf.best_estimator_

    y_pred = clf.predict(X_test)
    cnf_matrix = confusion_matrix(y_test, y_pred)
    np.set_printoptions(precision=2)

    print('Confusion mat: \n{}'.format(cnf_matrix))

    # make sure that classification report is the same
    out_fp = os.path.join(args.outdir, 'clf-report-check.txt')
    with open(out_fp, 'w') as f:
        clf_report = classification_report(y_test, y_pred)
        print('Classification report: \n{}'.format(clf_report))
        print(clf_report, file=f)

    class_names = [
        'CLASSIC', 'CLASSIC-SP', 'RECOMPOSE', 'RECOMPOSE-SP'
    ]
    fig, ax = plot_confusion_matrix(cnf_matrix, classes=class_names)
    out_fp = os.path.join(args.outdir, 'cnf-mat.svg')
    fig.savefig(out_fp, bbox_inches='tight', rasterized=True)

    out_fp = os.path.join(args.outdir, 'cnf-mat.txt')
    cnf_mat_df = pd.DataFrame(cnf_matrix)
    cnf_mat_df['data_class'] = class_names
    cnf_mat_df.set_index('data_class', inplace=True)
    cnf_mat_df.columns = class_names
    cnf_mat_df.to_csv(out_fp, index=True)

    end = time.time()
    print('Experiment took: {:.2f}s'.format(end - start))
    print('Experiment took: {:.2f}s'.format(end - start))
