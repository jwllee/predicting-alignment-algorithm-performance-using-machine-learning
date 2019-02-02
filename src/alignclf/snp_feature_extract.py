#!/usr/bin/env python


import podspy.log as logpkg
from podspy.conformance import alignment
import pytest, time
import numpy as np
import pandas as pd
import multiprocessing as mp

from . import net_feature_extract, utils


@utils.timeit(on=False)
def extract_features(trace_name, trace, net, init, final):
    # build snp
    trace_net, trace_init, trace_final = alignment.to_trace_net(trace_name, trace)
    mapping = {t:t.label for t in net.transitions}      # assume event just have transition label
    snp, snp_init, snp_final = alignment.to_sync_net_product(trace_net, trace_init, trace_final,
                                                             net, init, final, mapping)

    feature_dict = net_feature_extract.extract_features(snp)

    return feature_dict


def trace_to_feature_series(trace, net, init, final):
    trace_id = trace[logpkg.CASEID].values[0]
    events = trace[logpkg.ACTIVITY]
    feature_dict = extract_features(trace_id, events, net, init, final)
    feature_ss = pd.Series(feature_dict)
    return feature_ss


def _apply_df(args):
    df, net, init, final, log_time = args
    n_caseids = df[logpkg.CASEID].unique().shape[0]
    start = time.time()
    feature_df = df.groupby(logpkg.CASEID).apply(lambda df: trace_to_feature_series(df, net, init, final))
    end = time.time()
    if log_time:
        print('Extracting feature from {} caseids took: {:.2f}s'.format(n_caseids, end - start))
    return feature_df


@utils.timeit()
def extract_features_from_logtable(logtable, net, init, final, parallelize=False):
    assert isinstance(logtable, logpkg.LogTable)

    if parallelize:
        caseids = logtable.event_df[logpkg.CASEID].unique()
        n_proc = mp.cpu_count() - 1
        caseid_partitions = np.array_split(caseids, n_proc, axis=0)

        pool = mp.Pool(processes=n_proc)
        event_df = logtable.event_df
        args = [(
            event_df.loc[(event_df[logpkg.CASEID].isin(caseid_partitions[i])),:],
            net, init, final, True
        ) for i in range(n_proc)]
        result = pool.map(_apply_df, args)
        feature_df = pd.concat(result, axis=0)
        feature_df = feature_df.reset_index()
    else:
        feature_df = logtable.event_df.groupby(logpkg.CASEID).apply(lambda df: trace_to_feature_series(df, net, init, final))
        feature_df = feature_df.reset_index()

    col_order = [
        logpkg.CASEID,
        # net_feature_extract.N_TRAN,
        # net_feature_extract.N_INV_TRAN,
        # net_feature_extract.N_DUP_TRAN,
        # net_feature_extract.N_UNIQ_TRAN,
        # net_feature_extract.INV_TRAN_IN_DEG_MEAN,
        # net_feature_extract.INV_TRAN_IN_DEG_STD,
        # net_feature_extract.INV_TRAN_OUT_DEG_MEAN,
        # net_feature_extract.INV_TRAN_OUT_DEG_STD,
        # net_feature_extract.UNIQ_TRAN_IN_DEG_MEAN,
        # net_feature_extract.UNIQ_TRAN_IN_DEG_STD,
        # net_feature_extract.UNIQ_TRAN_OUT_DEG_MEAN,
        # net_feature_extract.UNIQ_TRAN_OUT_DEG_STD,
        # net_feature_extract.DUP_TRAN_IN_DEG_MEAN,
        # net_feature_extract.DUP_TRAN_IN_DEG_STD,
        # net_feature_extract.DUP_TRAN_OUT_DEG_MEAN,
        # net_feature_extract.DUP_TRAN_OUT_DEG_STD,
        # net_feature_extract.PLACE_IN_DEG_MEAN,
        # net_feature_extract.PLACE_IN_DEG_STD,
        # net_feature_extract.PLACE_OUT_DEG_MEAN,
        # net_feature_extract.PLACE_OUT_DEG_STD,
        # net_feature_extract.N_PLACE,
        # net_feature_extract.N_ARC,
        # net_feature_extract.N_AND_SPLIT,
        # net_feature_extract.N_XOR_SPLIT,
        # net_feature_extract.N_BICONNECTED_COMPONENT
        net_feature_extract.PLACE_IN_DEG_MIN,
        net_feature_extract.PLACE_IN_DEG_MAX,
        net_feature_extract.PLACE_OUT_DEG_MIN,
        net_feature_extract.PLACE_OUT_DEG_MAX,
        net_feature_extract.INV_TRAN_IN_DEG_MIN,
        net_feature_extract.INV_TRAN_IN_DEG_MAX,
        net_feature_extract.INV_TRAN_OUT_DEG_MIN,
        net_feature_extract.INV_TRAN_OUT_DEG_MAX,
        net_feature_extract.DUP_TRAN_IN_DEG_MIN,
        net_feature_extract.DUP_TRAN_IN_DEG_MAX,
        net_feature_extract.DUP_TRAN_OUT_DEG_MIN,
        net_feature_extract.DUP_TRAN_OUT_DEG_MAX,
        net_feature_extract.UNIQ_TRAN_IN_DEG_MIN,
        net_feature_extract.UNIQ_TRAN_IN_DEG_MAX,
        net_feature_extract.UNIQ_TRAN_OUT_DEG_MIN,
        net_feature_extract.UNIQ_TRAN_OUT_DEG_MAX,
        net_feature_extract.N_PLACE_ONE_IN_DEG,
        net_feature_extract.N_PLACE_ONE_OUT_DEG,
        net_feature_extract.N_PLACE_TWO_IN_DEG,
        net_feature_extract.N_PLACE_TWO_OUT_DEG,
        net_feature_extract.N_PLACE_THREE_IN_DEG,
        net_feature_extract.N_PLACE_THREE_OUT_DEG,
        net_feature_extract.N_PLACE_MORE_THAN_FIVE_IN_DEG,
        net_feature_extract.N_PLACE_MORE_THAN_FIVE_OUT_DEG,
        net_feature_extract.N_INV_TRAN_ONE_IN_DEG,
        net_feature_extract.N_INV_TRAN_ONE_OUT_DEG,
        net_feature_extract.N_INV_TRAN_TWO_IN_DEG,
        net_feature_extract.N_INV_TRAN_TWO_OUT_DEG,
        net_feature_extract.N_INV_TRAN_THREE_IN_DEG,
        net_feature_extract.N_INV_TRAN_THREE_OUT_DEG,
        net_feature_extract.N_INV_TRAN_MORE_THAN_FIVE_IN_DEG,
        net_feature_extract.N_INV_TRAN_MORE_THAN_FIVE_OUT_DEG,
        net_feature_extract.N_DUP_TRAN_ONE_IN_DEG,
        net_feature_extract.N_DUP_TRAN_ONE_OUT_DEG,
        net_feature_extract.N_DUP_TRAN_TWO_IN_DEG,
        net_feature_extract.N_DUP_TRAN_TWO_OUT_DEG,
        net_feature_extract.N_DUP_TRAN_THREE_IN_DEG,
        net_feature_extract.N_DUP_TRAN_THREE_OUT_DEG,
        net_feature_extract.N_DUP_TRAN_MORE_THAN_FIVE_IN_DEG,
        net_feature_extract.N_DUP_TRAN_MORE_THAN_FIVE_OUT_DEG,
        net_feature_extract.N_UNIQ_TRAN_ONE_IN_DEG,
        net_feature_extract.N_UNIQ_TRAN_ONE_OUT_DEG,
        net_feature_extract.N_UNIQ_TRAN_TWO_IN_DEG,
        net_feature_extract.N_UNIQ_TRAN_TWO_OUT_DEG,
        net_feature_extract.N_UNIQ_TRAN_THREE_IN_DEG,
        net_feature_extract.N_UNIQ_TRAN_THREE_OUT_DEG,
        net_feature_extract.N_UNIQ_TRAN_MORE_THAN_FIVE_IN_DEG,
        net_feature_extract.N_UNIQ_TRAN_MORE_THAN_FIVE_OUT_DEG
    ]
    feature_df = feature_df[col_order]

    # rename columns
    renamed = {col: 'snp_{}'.format(col) for col in feature_df.columns if col != logpkg.CASEID}
    feature_df.rename(columns=renamed, inplace=True)
    return feature_df
