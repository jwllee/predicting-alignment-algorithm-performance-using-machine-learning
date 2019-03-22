#!/usr/bin/env python


import os, sys, time, argparse
import pandas as pd
import numpy as np
from sklearn.metrics import classification_report, confusion_matrix
from datetime import datetime
import matplotlib
import matplotlib.pyplot as plt
plt.switch_backend('agg')
import itertools as itls


OUTDIR = '.'
DATASET_FP = os.path.join('.', 'prediction.csv')


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
        ax.set_ylabel('true label', fontsize=15)
        ax.set_xlabel('predicted label', fontsize=15)

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


def compute_parscore(y_algo, y_time, y_valid, const=10):
    # get valid series
    penalized_time = list(map(lambda v, t: t if v else const * t, y_valid, y_time))
    return np.mean(penalized_time)


if __name__ == '__main__':

    df = pd.read_csv(DATASET_FP)
    y_test = df['min_algo']
    y_pred = df['pred_algo']
    cnf_matrix = confusion_matrix(y_test, y_pred)
    np.set_printoptions(precision=2)

    print('Confusion mat: \n{}'.format(cnf_matrix))

    out_fp = os.path.join(OUTDIR, 'clf-report.txt')
    with open(out_fp, 'w') as f:
        clf_report = classification_report(y_test, y_pred)
        print('Classification report: \n{}'.format(clf_report))
        print(clf_report, file=f)

    class_names = [
        'CLASSIC',
        'CLASSIC-SP',
        'RECOMPOSE',
        'RECOMPOSE-SP'
    ]

    fig, ax = plot_confusion_matrix(cnf_matrix, classes=class_names)
    out_fp = os.path.join(OUTDIR, 'cnf-mat.svg')
    fig.savefig(out_fp, bbox_inches='tight', rasterized=True)

    fig, ax = plot_confusion_matrix(cnf_matrix, classes=class_names, normalized=True)
    out_fp = os.path.join(OUTDIR, 'cnf-mat-normalized.svg')
    fig.savefig(out_fp, bbox_inches='tight', rasterized=True)

    out_fp = os.path.join(OUTDIR, 'cnf-mat.txt')
    cnf_mat_df = pd.DataFrame(cnf_matrix)
    cnf_mat_df['data_class'] = class_names
    cnf_mat_df.set_index('data_class', inplace=True)
    cnf_mat_df.columns = class_names
    cnf_mat_df.to_csv(out_fp, index=True)

    # compute PAR10
    vbsparscore = np.mean(df['min_time'])
    vbssolved = df.shape[0]
    print('Virtual Best Selector total: {:.2f}us'.format(df['min_time'].sum()))
    print('Virtual Best Selector PAR10: {:.2f}us'.format(vbsparscore))
    print('Virtual Best Selector solved: {}'.format(vbssolved))

    y_algo = df['pred_algo']
    y_time = df['pred_time']
    y_valid = df['pred_is_valid']

    parscore = compute_parscore(y_algo, y_time, y_valid)
    solved = np.sum(y_valid)
    print('Total: {:.2f}us'.format(y_time.sum()))
    print('par10: {:.2f}us'.format(parscore))
    print('solved: {}'.format(solved))

    out_fp = os.path.join(OUTDIR, 'score-summary.txt')
    with open(out_fp, 'w') as f:
        print('Virtual Best Selector total: {:.2f}us'.format(df['min_time'].sum()), file=f)
        print('Virtual Best Selector PAR10: {:.2f}us'.format(vbsparscore), file=f)
        print('Virtual Best Selector solved: {}'.format(vbssolved), file=f)
        print('Total: {:.2f}us'.format(y_time.sum()), file=f)
        print('par10: {:.2f}us'.format(parscore), file=f)
        print('solved: {}'.format(solved), file=f)
