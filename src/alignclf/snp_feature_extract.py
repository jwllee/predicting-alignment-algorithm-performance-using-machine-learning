#!/usr/bin/env python


import podspy.log as logpkg
from podspy.conformance import alignment
import pytest
import numpy as np
import pandas as pd

from . import net_feature_extract, utils


@utils.timeit()
def extract_features(trace_name, trace, net, init, final):
    # build snp
    trace_net, trace_init, trace_final = alignment.to_trace_net(trace_name, trace)
    mapping = {t:t.label for t in net.transitions}      # assume event just have transition label
    snp, snp_init, snp_final = alignment.to_sync_net_product(trace_net, trace_init, trace_final,
                                                             net, init, final, mapping)

    feature_dict = net_feature_extract.extract_features(snp)

    return feature_dict


@utils.timeit()
def extract_features_from_logtable(logtable, net, init, final):
    assert isinstance(logtable, logpkg.LogTable)

    def trace_to_feature_series(trace, net, init, final):
        trace_id = trace[logpkg.CASEID].values[0]
        events = trace[logpkg.ACTIVITY]
        feature_dict = extract_features(trace_id, events, net, init, final)
        feature_ss = pd.Series(feature_dict)
        return feature_ss

    feature_df = logtable.event_df.groupby(logpkg.CASEID, as_index=False).apply(lambda df: trace_to_feature_series(df, net, init, final))
    feature_df = feature_df.reset_index(drop=False)
    col_order = [
        logpkg.CASEID,
        net_feature_extract.N_TRAN,
        net_feature_extract.N_INV_TRAN,
        net_feature_extract.N_DUP_TRAN,
        net_feature_extract.N_UNIQ_TRAN,
        net_feature_extract.INV_TRAN_IN_DEG_MEAN,
        net_feature_extract.INV_TRAN_IN_DEG_STD,
        net_feature_extract.INV_TRAN_OUT_DEG_MEAN,
        net_feature_extract.INV_TRAN_OUT_DEG_STD,
        net_feature_extract.UNIQ_TRAN_IN_DEG_MEAN,
        net_feature_extract.UNIQ_TRAN_IN_DEG_STD,
        net_feature_extract.UNIQ_TRAN_OUT_DEG_MEAN,
        net_feature_extract.UNIQ_TRAN_OUT_DEG_STD,
        net_feature_extract.DUP_TRAN_IN_DEG_MEAN,
        net_feature_extract.DUP_TRAN_IN_DEG_STD,
        net_feature_extract.DUP_TRAN_OUT_DEG_MEAN,
        net_feature_extract.DUP_TRAN_OUT_DEG_STD,
        net_feature_extract.PLACE_IN_DEG_MEAN,
        net_feature_extract.PLACE_IN_DEG_STD,
        net_feature_extract.PLACE_OUT_DEG_MEAN,
        net_feature_extract.PLACE_OUT_DEG_STD,
        net_feature_extract.N_PLACE,
        net_feature_extract.N_ARC,
        net_feature_extract.N_AND_SPLIT,
        net_feature_extract.N_XOR_SPLIT,
        net_feature_extract.N_BICONNECTED_COMPONENT
    ]
    feature_df = feature_df[col_order]
    return feature_df
