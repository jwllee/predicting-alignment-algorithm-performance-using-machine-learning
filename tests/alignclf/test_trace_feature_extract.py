#!/usr/bin/env python


import podspy.log
import pytest
import numpy as np
import pandas as pd


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
