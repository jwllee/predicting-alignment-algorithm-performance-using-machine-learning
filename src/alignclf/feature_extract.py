#!/usr/bin/env python


from . import net_feature_extract, snp_feature_extract, trace_feature_extract, utils
import podspy.log as logpkg
import podspy.petrinet as petripkg

import os, sys
import pandas as pd
import numpy as np


__all__ = [
    'extract_feature_df'
]


@utils.timeit(on=True, verbose=False)
def extract_net_feature_ss(net):
    net_feature_dict = net_feature_extract.extract_features(net)
    net_feature_ss = pd.Series(net_feature_dict)
    col_order = [
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
    net_feature_ss = net_feature_ss[col_order]
    return net_feature_ss


def extract_decomposition_feature_ss(decomposition):
    feature_dict = net_feature_extract.extract_features_from_decomposition(decomposition)
    feature_ss = pd.Series(feature_dict)
    col_order = [
        net_feature_extract.N_SUBNET,
        net_feature_extract.SUBNET_N_TRAN_MEAN,
        net_feature_extract.SUBNET_N_TRAN_STD,
        net_feature_extract.SUBNET_N_INV_TRAN_MEAN,
        net_feature_extract.SUBNET_N_INV_TRAN_STD,
        net_feature_extract.SUBNET_N_DUP_TRAN_MEAN,
        net_feature_extract.SUBNET_N_DUP_TRAN_STD,
        net_feature_extract.SUBNET_N_UNIQ_TRAN_MEAN,
        net_feature_extract.SUBNET_N_UNIQ_TRAN_STD,
        net_feature_extract.SUBNET_N_PLACE_MEAN,
        net_feature_extract.SUBNET_N_PLACE_STD,
        net_feature_extract.SUBNET_N_ARC_MEAN,
        net_feature_extract.SUBNET_N_ARC_STD
    ]
    feature_ss = feature_ss[col_order]
    return feature_ss


def repeat_series_values(ss, n):
    new_shape = (1, ss.shape[0])
    reshaped = np.reshape(ss.values, new_shape)
    repeated = np.repeat(reshaped, n, axis=0)
    return pd.DataFrame(repeated, columns=ss.index)


def extract_feature_df(logname, logtable, net, init, final, decompositions):
    utils.check_isinstance(decompositions, dict)
    utils.check_isinstance(logtable, logpkg.LogTable)
    utils.check_isinstance(net, petripkg.Petrinet)
    utils.check_isinstance(init, petripkg.Marking)
    utils.check_isinstance(final, petripkg.Marking)

    net_feature_ss = extract_net_feature_ss(net)
    log_feature_df = trace_feature_extract.extract_features_from_logtable(logtable)
    snp_feature_df = snp_feature_extract.extract_features_from_logtable(logtable, net, init, final, parallelize=True)

    msg = 'log ({}) and snp ({}) feature dfs should have same no. of rows'.format(log_feature_df.shape, snp_feature_df.shape)
    assert log_feature_df.shape[0] == snp_feature_df.shape[0], msg

    n_rows = log_feature_df.shape[0]

    # join all dataframes as one
    result_df = pd.merge(log_feature_df, snp_feature_df, on=logpkg.CASEID)
    net_feature_df = repeat_series_values(net_feature_ss, n_rows)
    result_df = pd.concat([result_df, net_feature_df], axis=1)

    caseids = log_feature_df[logpkg.CASEID]

    decomposition_feature_df_list = list()
    for name, apn_array in decompositions.items():
        decomposition_feature_ss = extract_decomposition_feature_ss(apn_array)
        decomposition_feature_ss['decomposition'] = name

        # add the caseids
        decomposition_feature_df_i = repeat_series_values(decomposition_feature_ss, n_rows)
        decomposition_feature_df_i[logpkg.CASEID] = caseids
        decomposition_feature_df_list.append(decomposition_feature_df_i)

    decomposition_feature_df = pd.concat(decomposition_feature_df_list, axis=0)
    result_df = pd.merge(result_df, decomposition_feature_df, on=logpkg.CASEID)
    result_df = result_df.reset_index(drop=True)

    result_df['log'] = logname

    return result_df



