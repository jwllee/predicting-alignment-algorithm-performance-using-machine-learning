#!/usr/bin/env python


import pandas as pd
import numpy as np
import os, sys, logging, re, json
import functools as fct


logger = logging.getLogger(__name__)


__all__ = [
    'RecomposingReplayResultProcessor',
    'MonolithicReplayResultProcessor'
]


class ExitCodeInterpreter:
    OPTIMAL_ALIGNMENT = 1
    FAILED_ALIGNMENT = 2
    ENABLING_BLOCKED_BY_OUTPUT = 4
    COST_FUNCTION_OVERFLOW = 8
    HEURISTIC_FUNCTION_OVERFLOW = 16
    TIMEOUT_REACHED = 32
    STATE_LIMITED_REACHED = 64
    COST_LIMIT_REACHED = 128
    CANCELED = 256

    @staticmethod
    def join_exitcodes(codes):
        return fct.reduce(lambda x, y: x | y, codes)


class ReplayResultProcessor:
    PROM_LOG_FILENAME = 'prom.log'
    CONFIG_FILENAME = 'configs.json'

    SP_LABEL = 'SP label'
    ALIGNMENT_EXITCODE = 'Exit code for alignment'
    ALIGNMENT_COST = 'Cost of the alignment'
    TRANS_FIRED = 'Transitions fired'
    MARKINGS_POLLED = 'Markings polled from queue',
    MARKINGS_CLOSED = 'Markings added to closed set',
    MARKINGS_QUEUED = 'Markings queued',
    MARKINGS_REACHED = 'Markings reached',
    HEURISTICS_COMPUTED = 'Heuristics computed',
    HEURISTICS_ESTIMATED = 'Heuristics estimated',
    HEURISTICS_DERIVED = 'Heuristics derived',
    ALIGNMENT_COMPUTE_TIME = 'Time to compute alignment (us)',
    HEURISTICS_COMPUTE_TIME = 'Time to compute heuristics (us)',
    SETUP_TIME = 'Time to setup algorithm (us)',
    TOTAL_TIME = 'Total Time including setup (us)',
    N_SPLITS = 'Number of splits when splitting marking',
    LOG_MOVE_COST = 'Log move cost of alignment',
    MODEL_MOVE_COST = 'Model move cost of alignment',
    SYNC_MOVE_COST = 'Synchronous move cost of alignment',
    PREPROCESS_TIME = 'Pre-processing time (us)',
    CONSTRAINTSET_SIZE = 'Size of the constraintset',
    N_RESTARTS = 'Number of times replay was restarted',
    MEMORY_TOTAL = 'total Memory (MB)'
    MAX_QUEUE_LENGTH = 'Maximum queue length (elts)',
    MAX_QUEUE_CAPACITY = 'Maximum queue capacity (elts)',
    MAX_VISITED_SET_CAPACITY = 'Maximum capacity visited set (elts)',
    MEMORY_APPROX_PEAK = 'Approximate peak memory used (kb)',
    MEMORY_MAX = 'max Memory (MB)'

    N_ALIGNS = 'n_alignments_with_empty'
    N_VALID_ALIGNS = 'n_valid_alignments_with_empty'

    def __init__(self):
        self.log_size = -1

    def get_log_stats(self, trace_stats_df):
        # compute the number of valid alignments
        n_aligns = trace_stats_df.shape[0]
        n_valid_aligns = trace_stats_df[self.ALIGNMENT_EXITCODE].value_counts().loc[ExitCodeInterpreter.OPTIMAL_ALIGNMENT]

        # exclude the empty alignment for computing log alignment cost
        no_empty_df = trace_stats_df[trace_stats_df[self.SP_LABEL] != 'Empty']

        assert no_empty_df.shape[0] == trace_stats_df.shape[0] - 1, 'dataframe excluding the empty trace has one row less'

        to_sum = [
            'Cost of the alignment',
            'Transitions fired',
            'Markings polled from queue',
            'Markings added to closed set',
            'Markings queued',
            'Markings reached',
            'Heuristics computed',
            'Heuristics estimated',
            'Heuristics derived',
            'Time to compute alignment (us)',
            'Time to compute heuristics (us)',
            'Time to setup algorithm (us)',
            'Total Time including setup (us)',
            'Number of splits when splitting marking',
            'Log move cost of alignment',
            'Model move cost of alignment',
            'Synchronous move cost of alignment',
            'Pre-processing time (us)',
            'Size of the constraintset',
            'Number of times replay was restarted',
            'total Memory (MB)',
        ]
        to_max = [
            'Maximum queue length (elts)',
            'Maximum queue capacity (elts)',
            'Maximum capacity visited set (elts)',
            'max Memory (MB)'
        ]

        log_sum_df = no_empty_df[to_sum].sum(axis=0).to_frame().transpose()
        log_max_df = no_empty_df[to_max].max(axis=0).to_frame().transpose()
        log_stats_df = pd.concat([log_sum_df, log_max_df], axis=1)
        log_stats_df[self.N_ALIGNS] = n_aligns
        log_stats_df[self.N_VALID_ALIGNS] = n_valid_aligns

        return log_stats_df


class MonolithicReplayResultProcessor(ReplayResultProcessor):
    CONFIG_TO_GRAB = [
        'model',
        'log',
        'configuration',
        'moveSort',
        'queueSort',
        'preferExact',
        'threads',
        'useInt',
        'debug',
        'timeoutPerTraceInSec',
        'partialOrder',
    ]

    def get_clocktime(self, fpath):
        """Get replay clock time in milliseconds

        :param fpath: file path to prom logging file
        :return: clock_time
        """
        SEGMENT = 'Clock time (ms): '
        clocktime = -1

        with open(fpath, 'r') as f:
            lines = f.readlines()
            reversed_lines = lines[::-1]
            for line in reversed_lines:
                if SEGMENT in line:
                    search_result = re.search(r'[^0-9]+([0-9]+\.[0-9]+)[^0-9]?', line)
                    clocktime = float(search_result.group(1))
                    break

        if clocktime < 0:
            raise RuntimeError('Cannot get clocktime in {}'.format(fpath))

        return clocktime

    def grab_configuration(self, fpath):
        result = dict()
        to_grab = self.CONFIG_TO_GRAB

        with open(fpath, 'r') as f:
            json_dict = json.load(f)
            for key in to_grab:
                result[key] = json_dict[key]

        return result

    # process a given monolithic replay result subdirectory
    # this directory has:
    # - configs.json
    # - prom.log
    # - python.log
    def process_replay_directory(self, d):
        prom_log_fpath = os.path.join(d, self.PROM_LOG_FILENAME)
        config_fpath = os.path.join(d, self.CONFIG_FILENAME)

        clock_time = self.get_clocktime(prom_log_fpath)
        configs = self.grab_configuration(config_fpath)
        to_add = dict(configs)
        to_add['clock_time (ms)'] = clock_time

        trace_stats_fpath = os.path.join(d, 'alignment.csv')
        trace_stats_df = pd.read_csv(trace_stats_fpath)
        # exclude the empty trace
        no_empty_df = trace_stats_df[trace_stats_df[self.SP_LABEL] != 'empty']
        log_stats_df = self.get_log_stats(no_empty_df)

        # set multindex column
        n_configs = len(to_add) -  1
        n_results = log_stats_df.shape[1] + 1
        configs_level = np.repeat('configs', n_configs)
        results_level = np.repeat('results', n_results)
        configs_cols = [col for col in to_add.keys() if col != 'clock_time (ms)']
        results_cols = list(log_stats_df.columns) + ['clock_time (ms)']
        level_0 = np.concatenate((configs_level, results_level))
        level_1 = np.concatenate((configs_cols, results_cols))
        column_tuples = list(zip(level_0, level_1))
        config_index = pd.MultiIndex.from_tuples(column_tuples, names=['category', 'specific'])

        # add clocktime and configs
        for k, v in to_add.items():
            log_stats_df[k] = v

        log_stats_df = log_stats_df[level_1]
        log_stats_df.columns = config_index

        return log_stats_df

    # process a given recomposing replay result directory
    def process_directory(self, d):
        replay_experiment_dir_list = []

        for f in os.listdir(d):
            fpath = os.path.join(d, f)
            if os.path.isdir(fpath) and 'monolithic' in f:
                replay_experiment_dir_list.append(fpath)

        replay_experiment_dir_list = sorted(replay_experiment_dir_list)
        replay_dir_list = []
        log_stats_df_list = []

        for dirpath in replay_experiment_dir_list:
            for f in os.listdir(dirpath):
                fpath = os.path.join(dirpath, f)

                if os.path.isdir(fpath):
                    replay_dir_list.append(fpath)
                    log_stats_df = self.process_replay_directory(fpath)
                    log_stats_df_list.append(log_stats_df)

        logger.info('Number of replays in {}: {}'.format(d, len(replay_dir_list)))

        concat_log_stats_df = pd.concat(log_stats_df_list, axis=0)
        log_stats_fpath = os.path.join(d, 'monolithic-log-stats.csv')
        concat_log_stats_df.to_csv(log_stats_fpath, index=False)

        # print(concat_log_stats_df.head())


class RecomposingReplayResultProcessor(ReplayResultProcessor):
    CONFIG_TO_GRAB = [
        'model',
        'log',
        'algorithmType',
        'moveSort',
        'queueSort',
        'preferExact',
        'nThreads',
        'useInt',
        'debug',
        'timeoutPerTraceInSecs',
        'maximumNumberOfStates',
        'costUpperBound',
        'partiallyOrderEvents',
        'preProcessUsingPlaceBasedConstraints',
        'initialSplits',
    ]
    RECOMPOSING_CONFIG_TO_GRAB = [
        'globalDuration',
        'localDuration',
        'moveOnLogCosts',
        'moveOnModelCosts',
        'intervalRelative',
        'maxConflicts',
        'alignmentPercentage',
        'nofIterations',
        'useHideAndReduceAbstraction',
        'decomposition',
        'recomposeStrategy'
    ]
    REJECTED_FILENAME = 'rejected.csv'
    TO_ALIGN_FILENAME = 'to-align.csv'
    VALID_FILENAME = 'valid.csv'

    REJECTED = 'rejected'
    TO_ALIGN = 'to_align'
    VALID = 'valid'
    STATS = 'stats'

    # index:
    # SP label
    #
    # join:
    # - Exit code for alignment
    #
    # sum:
    # - Cost of the alignment
    # - Transitions fired
    # - Markings polled from queue
    # - Markings added to closed set
    # - Markings queued
    # - Markings reached
    # - Heuristics computed
    # - Heuristics estimated
    # - Heuristics derived
    # - Time to compute alignment (us)
    # - Time to compute heuristics (us)
    # - Time to setup algorithm (us)
    # - Total Time including setup (us)
    # - Number of splits when splitting marking
    # - Log move cost of alignment
    # - Model move cost of alignment
    # - Synchronous move cost of alignment
    # - Pre-processing time (us)
    # - Size of the constraintset
    # - Number of times replay was restarted
    # - total Memory (MB)
    #
    # max:
    # - Maximum queue length (elts)
    # - Maximum queue capacity (elts)
    # - Maximum capacity visited set (elts)
    # - Approximate peak memory used (kb)
    # - max Memory (MB)

    TO_JOIN = [
        ReplayResultProcessor.ALIGNMENT_EXITCODE
    ]
    TO_JOIN_MAP = {k : ExitCodeInterpreter.join_exitcodes for k in TO_JOIN}

    TO_SUM = [
        ReplayResultProcessor.ALIGNMENT_COST,
        ReplayResultProcessor.TRANS_FIRED,
        ReplayResultProcessor.MARKINGS_POLLED,
        ReplayResultProcessor.MARKINGS_CLOSED,
        ReplayResultProcessor.MARKINGS_QUEUED,
        ReplayResultProcessor.MARKINGS_REACHED,
        ReplayResultProcessor.HEURISTICS_COMPUTED,
        ReplayResultProcessor.HEURISTICS_ESTIMATED,
        ReplayResultProcessor.HEURISTICS_DERIVED,
        ReplayResultProcessor.ALIGNMENT_COMPUTE_TIME,
        ReplayResultProcessor.HEURISTICS_COMPUTE_TIME,
        ReplayResultProcessor.SETUP_TIME,
        ReplayResultProcessor.TOTAL_TIME,
        ReplayResultProcessor.N_SPLITS,
        ReplayResultProcessor.LOG_MOVE_COST,
        ReplayResultProcessor.MODEL_MOVE_COST,
        ReplayResultProcessor.SYNC_MOVE_COST,
        ReplayResultProcessor.PREPROCESS_TIME,
        ReplayResultProcessor.CONSTRAINTSET_SIZE,
        ReplayResultProcessor.N_RESTARTS,
        ReplayResultProcessor.MEMORY_TOTAL
    ]
    TO_SUM_MAP = {k : np.sum for k in TO_SUM}

    TO_MAX = [
        ReplayResultProcessor.MAX_QUEUE_LENGTH,
        ReplayResultProcessor.MAX_QUEUE_CAPACITY,
        ReplayResultProcessor.MAX_VISITED_SET_CAPACITY,
        ReplayResultProcessor.MEMORY_APPROX_PEAK,
        ReplayResultProcessor.MEMORY_MAX
    ]
    TO_MAX_MAP = {k : np.max for k in TO_MAX}

    def get_clocktime(self, fpath):
        """Get replay clock time in milliseconds

        :param fpath: file path to prom logging file
        :return: clock_time
        """
        SEGMENT = 'INFO: Running boot main took: '
        clocktime = -1

        with open(fpath, 'r') as f:
            lines = f.readlines()
            reversed_lines = lines[::-1]
            for line in reversed_lines:
                if SEGMENT in line:
                    search_result = re.search(r'[^0-9]+([0-9]+)[^0-9]+', line)
                    clocktime = int(search_result.group(1))
                    break

        if clocktime < 0:
            raise RuntimeError('Cannot get clocktime')

        return clocktime

    def grab_configuration(self, fpath):
        result = dict()
        to_grab = self.CONFIG_TO_GRAB + self.RECOMPOSING_CONFIG_TO_GRAB

        with open(fpath, 'r') as f:
            json_dict = json.load(f)
            for key in to_grab:
                result[key] = json_dict[key]

        return result

    def get_caseids(self, fpath):
        with open(fpath, 'r') as f:
            caseids = f.readlines()[1:]

        return caseids

    def agg_iter_stats_df_list(self, dfs, caseid_dict):
        agg_map = dict()
        to_join = [
            'Exit code for alignment',
        ]
        to_sum = [
            'Cost of the alignment',
            'Transitions fired',
            'Markings polled from queue',
            'Markings added to closed set',
            'Markings queued',
            'Markings reached',
            'Heuristics computed',
            'Heuristics estimated',
            'Heuristics derived',
            'Time to compute alignment (us)',
            'Time to compute heuristics (us)',
            'Time to setup algorithm (us)',
            'Total Time including setup (us)',
            'Number of splits when splitting marking',
            'Log move cost of alignment',
            'Model move cost of alignment',
            'Synchronous move cost of alignment',
            'Pre-processing time (us)',
            'Size of the constraintset',
            'Number of times replay was restarted',
            'total Memory (MB)',
        ]
        to_max = [
            'Maximum queue length (elts)',
            'Maximum queue capacity (elts)',
            'Maximum capacity visited set (elts)',
            'max Memory (MB)'
        ]

        to_join_map = { k: ExitCodeInterpreter.join_exitcodes for k in to_join }
        to_sum_map = { k: np.sum for k in to_sum }
        to_max_map = { k: np.max for k in to_max }

        agg_map.update(to_join_map)
        agg_map.update(to_sum_map)
        agg_map.update(to_max_map)

        df = pd.concat(dfs, axis=0)
        df = df.reset_index(drop=True)

        assert isinstance(df, pd.DataFrame)

        grouped = df.groupby(by=self.SP_LABEL)

        # statistics related to cases per iteration
        iter_df = grouped.agg(agg_map).reset_index(drop=False)
        return iter_df

    # aggregate various alignment statistics of traces across iterations
    def process_iteration_stats(self, iter_dir_list, caseid_dict):
        df_list = list()

        it = 1
        for d in iter_dir_list:
            iter_df_list = list()
            stats_dirpath = os.path.join(d, self.STATS)

            for f in os.listdir(stats_dirpath):
                stats_fpath = os.path.join(stats_dirpath, f)
                stats_df = pd.read_csv(stats_fpath)
                iter_df_list.append(stats_df)

            iter_df = self.agg_iter_stats_df_list(iter_df_list, caseid_dict)
            colnames = ['iteration'] + list(iter_df.columns)
            iter_df['iteration'] = it
            iter_df = iter_df[colnames]
            df_list.append(iter_df)
            it += 1

        # statistics related to cases per iteration
        iter_df = pd.concat(df_list, axis=0)

        return iter_df

    # d is the replay directory containing the iteration directories
    def process_iterations(self, d, caseid_dict):
        iter_dir_list = list()

        for f in os.listdir(d):
            fpath = os.path.join(d, f)
            if 'iter' in fpath and os.path.isdir(fpath):
                iter_no = int(f.replace('iter-', ''))
                iter_dir_list.append((iter_no, fpath))

        iter_dir_list = sorted(iter_dir_list, key=lambda pair: pair[0])
        iter_dir_list = [pair[1] for pair in iter_dir_list]

        logger.info('Processing {} iteration directories'.format(len(iter_dir_list)))

        iter_df = self.process_iteration_stats(iter_dir_list, caseid_dict)

        # aggregate statistics across iterations
        agg_map = dict()
        to_join = [
            'Exit code for alignment',
        ]
        to_sum = [
            'Cost of the alignment',
            'Transitions fired',
            'Markings polled from queue',
            'Markings added to closed set',
            'Markings queued',
            'Markings reached',
            'Heuristics computed',
            'Heuristics estimated',
            'Heuristics derived',
            'Time to compute alignment (us)',
            'Time to compute heuristics (us)',
            'Time to setup algorithm (us)',
            'Total Time including setup (us)',
            'Number of splits when splitting marking',
            'Log move cost of alignment',
            'Model move cost of alignment',
            'Synchronous move cost of alignment',
            'Pre-processing time (us)',
            'Size of the constraintset',
            'Number of times replay was restarted',
            'total Memory (MB)',
        ]
        to_max = [
            'Maximum queue length (elts)',
            'Maximum queue capacity (elts)',
            'Maximum capacity visited set (elts)',
            'max Memory (MB)'
        ]

        to_join_map = { k: ExitCodeInterpreter.join_exitcodes for k in to_join }
        to_sum_map = { k: np.sum for k in to_sum }
        to_max_map = { k: np.max for k in to_max }

        agg_map.update(to_join_map)
        agg_map.update(to_sum_map)
        agg_map.update(to_max_map)

        grouped = iter_df.groupby(by=self.SP_LABEL)
        agg_df = grouped.agg(agg_map).reset_index(drop=False)

        # aggregate across traces
        log_df = self.get_log_stats(agg_df)

        return iter_df, agg_df, log_df

    # process a given recomposing replay result subdirectory
    # this directory has:
    # - iteration directories
    # - configs.json
    # - prom.log
    # - python.log
    def process_replay_directory(self, d):
        prom_log_fpath = os.path.join(d, self.PROM_LOG_FILENAME)
        config_fpath = os.path.join(d, self.CONFIG_FILENAME)
        rejected_fpath = os.path.join(d, self.REJECTED_FILENAME)
        to_align_fpath = os.path.join(d, self.TO_ALIGN_FILENAME)
        valid_fpath = os.path.join(d, self.VALID_FILENAME)

        clock_time = self.get_clocktime(prom_log_fpath)
        configs = self.grab_configuration(config_fpath)
        to_add = dict(configs)
        to_add['clock_time (ms)'] = clock_time

        caseid_dict = {
            self.REJECTED: self.get_caseids(rejected_fpath),
            self.TO_ALIGN: self.get_caseids(to_align_fpath),
            self.VALID: self.get_caseids(valid_fpath)
        }

        iteration_stats_df, overall_stats_df, log_stats_df = self.process_iterations(d, caseid_dict)

        # set multindex column
        n_configs = len(to_add) -  1
        n_results = log_stats_df.shape[1] + 1
        configs_level = np.repeat('configs', n_configs)
        results_level = np.repeat('results', n_results)
        configs_cols = [col for col in to_add.keys() if col != 'clock_time (ms)']
        results_cols = list(log_stats_df.columns) + ['clock_time (ms)']
        level_0 = np.concatenate((configs_level, results_level))
        level_1 = np.concatenate((configs_cols, results_cols))
        column_tuples = list(zip(level_0, level_1))
        config_index = pd.MultiIndex.from_tuples(column_tuples, names=['category', 'specific'])

        # add clocktime and configs
        for k, v in to_add.items():
            log_stats_df[k] = v

        log_stats_df = log_stats_df[level_1]
        log_stats_df.columns = config_index

        return iteration_stats_df, overall_stats_df, log_stats_df

    # process a given recomposing replay result directory
    def process_directory(self, d):
        replay_experiment_dir_list = []

        for f in os.listdir(d):
            fpath = os.path.join(d, f)
            if os.path.isdir(fpath) and 'monolithic' not in f:
                replay_experiment_dir_list.append(fpath)

        replay_experiment_dir_list = sorted(replay_experiment_dir_list)
        replay_dir_list = []
        log_stats_df_list = []

        for dirpath in replay_experiment_dir_list:
            for f in os.listdir(dirpath):
                fpath = os.path.join(dirpath, f)

                if os.path.isdir(fpath):
                    replay_dir_list.append(fpath)
                    iter_stats_df, overall_stats_df, log_stats_df = self.process_replay_directory(fpath)

                    iter_stats_fpath = os.path.join(fpath, 'trace-iter-stats.csv')
                    overall_stats_fpath = os.path.join(fpath, 'trace-stats.csv')

                    iter_stats_df.to_csv(iter_stats_fpath, index=False)
                    overall_stats_df.to_csv(overall_stats_fpath, index=False)

                    log_stats_df_list.append(log_stats_df)

        logger.info('Number of replays in {}: {}'.format(d, len(replay_dir_list)))

        concat_log_stats_df = pd.concat(log_stats_df_list, axis=0)
        log_stats_fpath = os.path.join(d, 'recomposing-log-stats.csv')
        concat_log_stats_df.to_csv(log_stats_fpath, index=False)

        # print(concat_log_stats_df.head())
