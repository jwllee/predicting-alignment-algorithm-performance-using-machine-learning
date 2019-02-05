#!/usr/bin/env python3


import os, sys
import pandas as pd
import numpy as np
import functools as fct


from . import utils
from .analyze import StatsColname as Col


RESULT_DIR = 'result_dir'
IS_VALID = 'is_valid'
N_LOG_MOVE = 'n_log_move'
N_MODEL_MOVE = 'n_model_move'
N_SYNC_MOVE = 'n_sync_move'
N_INVIS_MOVE = 'n_invis_move'


cols_to_add = [
    Col.SP_LABEL.value,
    IS_VALID,
    Col.TOTAL_TIME.value,
    Col.ALIGNMENT_COMPUTE_TIME.value,
    Col.ALIGNMENT_COST.value,
    N_SYNC_MOVE,
    N_LOG_MOVE,
    N_MODEL_MOVE,
    N_INVIS_MOVE,
    Col.MARKINGS_POLLED.value,
    Col.MARKINGS_CLOSED.value,
    Col.MARKINGS_QUEUED.value,
    Col.MARKINGS_REACHED.value,
    Col.HEURISTICS_COMPUTED.value,
    Col.HEURISTICS_ESTIMATED.value,
    Col.HEURISTICS_DERIVED.value,
    Col.N_SPLITS.value,
    Col.N_RESTARTS.value,
    Col.CONSTRAINTSET_SIZE.value,
    RESULT_DIR
]


def process_recomposing_df(df):
    utils.check_isinstance(df, pd.DataFrame)

    to_rename = {
        'merged_alignment_n_log_move': N_LOG_MOVE,
        'merged_alignment_n_model_move': N_MODEL_MOVE,
        'merged_alignment_n_sync_move': N_SYNC_MOVE,
        'merged_alignment_n_invis_move': N_INVIS_MOVE,
        'merged_alignment_cost': Col.ALIGNMENT_COST.value
    }
    df.drop(Col.ALIGNMENT_COST.value, axis=1, inplace=True)
    df.rename(columns=to_rename, inplace=True)
    df[IS_VALID] = df[Col.ALIGNMENT_EXITCODE.value] == 1
    df = df[(df[Col.SP_LABEL.value] != 'Empty')]
    subdf = df[cols_to_add]

    return subdf


def process_df(df):
    utils.check_isinstance(df, pd.DataFrame)

    df[IS_VALID] = df[Col.ALIGNMENT_EXITCODE.value] == 1
    df = df[(df[Col.SP_LABEL.value] != 'Empty')]
    subdf = df[cols_to_add]

    return subdf


def _reduce_merge_df_list(df_list, on):
    return fct.reduce(lambda df1, df2: df1.merge(df2, on=on), df_list)


def _make_multilevel_column(columns):
    level_0 = []
    level_1 = []

    for col in columns:
        if col == Col.SP_LABEL.value:
            col_level_0 = col
            col_level_1 = ''
        else:
            col_level_0, col_level_1 = col.rsplit('_', 1)
        level_0.append(col_level_0)
        level_1.append(col_level_1)

    tuples = list(zip(level_0, level_1))
    index = pd.MultiIndex.from_tuples(tuples)
    return index


def to_clf_df(df_dict):
    """Combines the different alignment result dataframes computed under different algorithms into one single dataframe
    so that classification tasks can be done.

    :param df_dict: dictionary that maps algorithm name to its result dataframe
    :return: combined dataframe
    """
    key_list = list(df_dict.keys())
    df_list = [df_dict[key] for key in key_list]
    col_order = df_list[0].columns
    df_list = [df[col_order] for df in df_list]
    algo_0 = key_list[0]
    shape = df_dict[algo_0].shape

    # print('Key list: {}'.format(key_list))

    for algo, df in df_dict.items():
        if RESULT_DIR not in df.columns:
            msg = 'result dataframe does not have result_dir as a column'
            utils.raise_assert_detail(msg, 'df', type(df))
        if df.shape != shape:
            msg = 'result dataframe shape is not {} but has {}'.format(shape, df.shape)
            utils.raise_assert_detail(msg, 'df', type(df))
        if 'Empty' in df[Col.SP_LABEL.value].unique():
            msg = 'result dataframe should not have empty trace alignment for comparison with recomposing approach'
            utils.raise_assert_detail(msg, 'df', type(df))

    for i in range(len(key_list)):
        algo = key_list[i]
        df = df_list[i]

        rename_cols = {c: '{}_{}'.format(c, algo) for c in df.columns}
        del rename_cols[Col.SP_LABEL.value]
        df.rename(columns=rename_cols, inplace=True)

    merged_df = _reduce_merge_df_list(df_list, Col.SP_LABEL.value)
    merged_df.columns = _make_multilevel_column(merged_df.columns)
    reorder_cols = [(Col.SP_LABEL.value, '')]
    for col in cols_to_add[1:]:
        for algo_type in key_list:
            reorder_cols.append((col, algo_type))
    merged_df = merged_df.loc[:,reorder_cols]

    to_drop = [
        Col.SP_LABEL.value, IS_VALID, N_SYNC_MOVE, Col.ALIGNMENT_COST.value,
        N_LOG_MOVE, N_MODEL_MOVE, N_INVIS_MOVE, RESULT_DIR
    ]
    dropped_df = merged_df.drop(to_drop, axis=1, level=0)
    grouped = dropped_df.groupby(level=0, axis=1).apply(lambda df: df.apply(lambda row: row.idxmin()[1], axis = 1))
    grouped.columns = pd.MultiIndex.from_product([['Min'], grouped.columns])
    merged_df = pd.concat([merged_df, grouped], axis=1)

    return merged_df
