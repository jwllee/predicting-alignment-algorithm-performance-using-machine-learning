#!/usr/bin/env python


import podspy.log
import pytest
import numpy as np
import pandas as pd
from pandas.testing import assert_frame_equal


from alignclf import trace_feature_extract


class TestExtractTraceFeature:
    @pytest.fixture(
        scope='function'
    )
    def trace_l5_a5(self):
        return pd.Series(['a', 'b', 'c', 'd', 'e'])

    @pytest.fixture(
        scope='function'
    )
    def trace_l6_a3(self):
        return pd.Series(['a', 'a', 'a', 'b', 'b', 'c'])

    @pytest.fixture(
        scope='function'
    )
    def logtable_3t(self):
        event_df = pd.DataFrame({
            podspy.log.CASEID: ['1', '1', '1', '1', '1',
                                '2', '2', '2',
                                '3', '3', '3', '3'],
            podspy.log.ACTIVITY: ['a', 'b', 'c', 'd', 'e',
                                  'a', 'b', 'b',
                                  'a', 'c', 'c', 'c'],
            podspy.log.COST_AMOUNT: [100, 100, 100, 100, 100,
                                     0, 0, 0,
                                     10, 10, 10, 10]
        })
        trace_df = pd.DataFrame({
            podspy.log.CASEID: ['1', '2', '3']
        })

        return podspy.log.LogTable(trace_df=trace_df, event_df=event_df)

    def test_get_trace_length(self, trace_l5_a5, trace_l6_a3):
        expected = 5
        assert trace_feature_extract.get_trace_length(trace_l5_a5) == expected

        expected = 6
        assert trace_feature_extract.get_trace_length(trace_l6_a3) == expected

    def test_get_n_act(self, trace_l5_a5, trace_l6_a3):
        expected = 5
        assert trace_feature_extract.get_n_act(trace_l5_a5) == expected

        expected = 3
        assert trace_feature_extract.get_n_act(trace_l6_a3) == expected

    def test_get_act_repeat_mean(self, trace_l5_a5, trace_l6_a3):
        expected = 1
        assert trace_feature_extract.get_act_repeat_mean(trace_l5_a5) == expected

        expected = np.mean((3, 2, 1))
        assert trace_feature_extract.get_act_repeat_mean(trace_l6_a3) == expected

    def test_get_act_repeat_std(self, trace_l5_a5, trace_l6_a3):
        expected = 0
        assert trace_feature_extract.get_act_repeat_std(trace_l5_a5) == expected

        expected = np.std((3, 2, 1), ddof=1)
        assert trace_feature_extract.get_act_repeat_std(trace_l6_a3) == expected

    def test_extract_features_from_log_table(self, logtable_3t):
        expected_act_repeat_mean = [
            np.mean([1, 1, 1, 1, 1]),
            np.mean([1, 2]),
            np.mean([1, 3])
        ]
        expected_act_repeat_std = [
            np.std([1, 1, 1, 1, 1], ddof=1),
            np.std([1, 2], ddof=1),
            np.std([1, 3], ddof=1)
        ]
        expected = pd.DataFrame({
            podspy.log.CASEID: ['1', '2', '3'],
            trace_feature_extract.TRACE_LENGTH: [5, 3, 4],
            trace_feature_extract.N_ACT: [5, 2, 2],
            trace_feature_extract.ACT_REPEAT_MEAN: expected_act_repeat_mean,
            trace_feature_extract.ACT_REPEAT_STD: expected_act_repeat_std
        })
        col_order = [
            podspy.log.CASEID,
            trace_feature_extract.TRACE_LENGTH,
            trace_feature_extract.N_ACT,
            trace_feature_extract.ACT_REPEAT_MEAN,
            trace_feature_extract.ACT_REPEAT_STD
        ]
        expected = expected[col_order]

        feature_df = trace_feature_extract.extract_features_from_logtable(logtable_3t)

        assert isinstance(feature_df, pd.DataFrame)
        assert feature_df.shape[0] == 3
        assert_frame_equal(feature_df, expected, check_dtype=False)
