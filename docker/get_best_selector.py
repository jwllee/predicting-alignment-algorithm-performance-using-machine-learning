import os, sys
import pandas as pd
import numpy as np


DATASET_FP = os.path.join('..', 'data', 'python', 'original.csv')
DATASET_FP = os.path.join('..', 'data', 'python', '2xdiff.csv')


def compute_parscore(y_time, y_valid, const=10):
    # get valid series
    penalized_time = list(map(lambda v, t: t if v else const * t, y_valid, y_time))
    return np.mean(penalized_time)


if __name__ == '__main__':
    df = pd.read_csv(DATASET_FP)

    y_time = df['astar_time']
    y_valid = df['astar_is_valid']
    sbparscore = compute_parscore(y_time, y_valid)
    sbsolved = np.sum(y_valid)
    print('Total: {:.2f}us'.format(y_time.sum()))
    print('PAR10: {:.2f}us'.format(sbparscore))
    print('Solved: {}'.format(sbsolved))

    # the astar is the best solver
    y_time = df['inc3_time']
    y_valid = df['inc3_is_valid']
    sbparscore = compute_parscore(y_time, y_valid)
    sbsolved = np.sum(y_valid)
    print('Total: {:.2f}us'.format(y_time.sum()))
    print('PAR10: {:.2f}us'.format(sbparscore))
    print('Solved: {}'.format(sbsolved))

    y_time = df['recomp-astar_time']
    y_valid = df['recomp-astar_is_valid']
    sbparscore = compute_parscore(y_time, y_valid)
    sbsolved = np.sum(y_valid)
    print('Total: {:.2f}us'.format(y_time.sum()))
    print('PAR10: {:.2f}us'.format(sbparscore))
    print('Solved: {}'.format(sbsolved))

    y_time = df['recomp-inc3_time']
    y_valid = df['recomp-inc3_is_valid']
    sbparscore = compute_parscore(y_time, y_valid)
    sbsolved = np.sum(y_valid)
    print('Total: {:.2f}us'.format(y_time.sum()))
    print('PAR10: {:.2f}us'.format(sbparscore))
    print('Solved: {}'.format(sbsolved))
