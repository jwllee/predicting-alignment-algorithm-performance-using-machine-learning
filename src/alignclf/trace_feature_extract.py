#!/usr/bin/env python


import podspy
import podspy.log
import numpy as np
import pandas as pd
import collections as cols
import functools as fct

# trace features
TRACE_LENGTH = 'trace_length'
N_ACT = 'n_activity'
ACT_REPEAT_MEAN = 'activity_repeat_mean'
ACT_REPEAT_STD = 'activity_repeat_std'


def extract_features(trace):
    features = {
        TRACE_LENGTH: get_trace_length(trace),
        N_ACT: get_n_act(trace),
        ACT_REPEAT_MEAN: get_act_repeat_mean(trace),
        ACT_REPEAT_STD: get_act_repeat_std(trace)
    }

    return features


def get_trace_length(trace):
    return len(trace)


def get_n_act(trace):
    if isinstance(trace, pd.Series):
        n_act = len(trace.unique())
    else:
        n_act = len(set(trace))
    return n_act


def get_act_repeat_mean(trace):
    if isinstance(trace, pd.Series):
        result = trace.value_counts().mean()
    else:
        counter = cols.Counter(trace)
        result = sum(counter.values()) / len(counter.keys())
    return result


def get_act_repeat_std(trace):
    if isinstance(trace, pd.Series):
        result = trace.value_counts().std()
    else:
        counter = cols.Counter(trace)
        result = np.std(list(counter.values()), ddof=1)
    return result
