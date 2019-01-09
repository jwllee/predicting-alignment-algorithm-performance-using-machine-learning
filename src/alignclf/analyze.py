#!/usr/bin/env python


import pandas as pd
import numpy as np
import os, sys, enum, re, json, logging, time
import functools as fct
from .utils import timeit


logger = logging.getLogger(__name__)


__all__ = [
    'RecomposeReplayResultProcessor',
    'MonolithicReplayResultProcessor'
]


class AlignExitCode(enum.Enum):
    OPTIMAL_ALIGNMENT = 1
    FAILED_ALIGNMENT = 2
    ENABLING_BLOCKED_BY_OUTPUT = 4
    COST_FUNCTION_OVERFLOW = 8
    HEURISTIC_FUNCTION_OVERFLOW = 16
    TIMEOUT_REACHED = 32
    STATE_LIMITED_REACHED = 64
    COST_LIMIT_REACHED = 128
    CANCELED = 256


class StatsColname(enum.Enum):
    SP_LABEL = 'SP label'
    ALIGNMENT_EXITCODE = 'Exit code for alignment'
    ALIGNMENT_COST = 'Cost of the alignment'
    TRANS_FIRED = 'Transitions fired'
    MARKINGS_POLLED = 'Markings polled from queue'
    MARKINGS_CLOSED = 'Markings added to closed set'
    MARKINGS_QUEUED = 'Markings queued'
    MARKINGS_REACHED = 'Markings reached'
    HEURISTICS_COMPUTED = 'Heuristics computed'
    HEURISTICS_ESTIMATED = 'Heuristics estimated'
    HEURISTICS_DERIVED = 'Heuristics derived'
    ALIGNMENT_COMPUTE_TIME = 'Time to compute alignment (us)'
    HEURISTICS_COMPUTE_TIME = 'Time to compute heuristics (us)'
    SETUP_TIME = 'Time to setup algorithm (us)'
    TOTAL_TIME = 'Total Time including setup (us)'
    N_SPLITS = 'Number of splits when splitting marking'
    LOG_MOVE_COST = 'Log move cost of alignment'
    MODEL_MOVE_COST = 'Model move cost of alignment'
    SYNC_MOVE_COST = 'Synchronous move cost of alignment'
    PREPROCESS_TIME = 'Pre-processing time (us)'
    CONSTRAINTSET_SIZE = 'Size of the constraintset'
    N_RESTARTS = 'Number of times replay was restarted'
    MEMORY_TOTAL = 'total Memory (MB)'
    MAX_QUEUE_LENGTH = 'Maximum queue length (elts)'
    MAX_QUEUE_CAPACITY = 'Maximum queue capacity (elts)'
    MAX_VISITED_SET_CAPACITY = 'Maximum capacity visited set (elts)'
    MEMORY_APPROX_PEAK = 'Approximate peak memory used (kb)'
    MEMORY_MAX = 'max Memory (MB)'


class MonolithicConfigToGrab(enum.Enum):
    __order__ = 'MODEL ' \
                'LOG ' \
                'ALGORITHM_TYPE ' \
                'DEBUG ' \
                'MOVE_ON_LOG_COSTS ' \
                'MOVE_ON_MODEL_COSTS ' \
                'TIMEOUT_PER_TRACE_IN_SECS ' \
                'MOVE_SORT ' \
                'QUEUE_SORT ' \
                'USE_INT ' \
                'MAXIMUM_NUMBER_OF_STATES ' \
                'COST_UPPER_BOUND ' \
                'PREPROCESS_USING_PLACE_BASED_CONSTRAINTS ' \
                'PREFER_EXACT ' \
                'PARTIALLY_ORDER_EVENTS ' \
                'INITIAL_SPLIT ' \
                'N_THREADS'

    MODEL = 'model'
    LOG = 'log'
    ALGORITHM_TYPE = 'algorithmType'
    DEBUG = 'debug'
    MOVE_ON_LOG_COSTS = 'moveOnLogCosts'
    MOVE_ON_MODEL_COSTS = 'moveOnModelCosts'
    TIMEOUT_PER_TRACE_IN_SECS = 'timeoutPerTraceInSecs'
    MOVE_SORT = 'moveSort'
    QUEUE_SORT = 'queueSort'
    USE_INT = 'useInt'
    MAXIMUM_NUMBER_OF_STATES = 'maximumNumberOfStates'
    COST_UPPER_BOUND = 'costUpperBound'
    PREPROCESS_USING_PLACE_BASED_CONSTRAINTS = 'preProcessUsingPlaceBasedConstraints'
    PREFER_EXACT = 'preferExact'
    PARTIALLY_ORDER_EVENTS = 'partiallyOrderEvents'
    INITIAL_SPLIT = 'initialSplits'
    N_THREADS = 'nThreads'


class RecomposeConfigToGrab(enum.Enum):
    __order__ = 'MODEL ' \
                'LOG ' \
                'ALGORITHM_TYPE ' \
                'DEBUG ' \
                'MOVE_ON_LOG_COSTS ' \
                'MOVE_ON_MODEL_COSTS ' \
                'TIMEOUT_PER_TRACE_IN_SECS ' \
                'MOVE_SORT ' \
                'QUEUE_SORT ' \
                'USE_INT ' \
                'MAXIMUM_NUMBER_OF_STATES ' \
                'COST_UPPER_BOUND ' \
                'PREPROCESS_USING_PLACE_BASED_CONSTRAINTS ' \
                'PREFER_EXACT ' \
                'PARTIALLY_ORDER_EVENTS ' \
                'INITIAL_SPLIT ' \
                'N_THREADS ' \
                'GLOBAL_DURATION ' \
                'ALIGNMENT_PERCENTAGE ' \
                'INTERVAL_ABSOLUTE ' \
                'INTERVAL_RELATIVE ' \
                'MAX_CONFLICTS ' \
                'USE_HIDE_AND_REDUCE_ABSTRACTION ' \
                'PREFER_BORDER_TRANS ' \
                'DECOMPOSITION ' \
                'LOG_CREATION_STRATEGY ' \
                'RECOMPOSE_STRATEGY'

    MODEL = 'model'
    LOG = 'log'
    ALGORITHM_TYPE = 'algorithmType'
    DEBUG = 'debug'
    MOVE_ON_LOG_COSTS = 'moveOnLogCosts'
    MOVE_ON_MODEL_COSTS = 'moveOnModelCosts'
    TIMEOUT_PER_TRACE_IN_SECS = 'timeoutPerTraceInSecs'
    MOVE_SORT = 'moveSort'
    QUEUE_SORT = 'queueSort'
    USE_INT = 'useInt'
    MAXIMUM_NUMBER_OF_STATES = 'maximumNumberOfStates'
    COST_UPPER_BOUND = 'costUpperBound'
    PREPROCESS_USING_PLACE_BASED_CONSTRAINTS = 'preProcessUsingPlaceBasedConstraints'
    PREFER_EXACT = 'preferExact'
    PARTIALLY_ORDER_EVENTS = 'partiallyOrderEvents'
    INITIAL_SPLIT = 'initialSplits'
    N_THREADS = 'nThreads'

    GLOBAL_DURATION = 'globalDuration'
    ALIGNMENT_PERCENTAGE = 'alignmentPercentage'
    INTERVAL_ABSOLUTE = 'intervalAbsolute'
    INTERVAL_RELATIVE = 'intervalRelative'
    MAX_CONFLICTS = 'maxConflicts'

    USE_HIDE_AND_REDUCE_ABSTRACTION = 'useHideAndReduceAbstraction'
    PREFER_BORDER_TRANS = 'preferBorderTransitions'
    DECOMPOSITION = 'decomposition'
    LOG_CREATION_STRATEGY = 'logCreationStrategy'
    RECOMPOSE_STRATEGY = 'recomposeStrategy'


class MonolithicReplayResultProcessor:
    PROM_LOG_FILENAME = 'prom.log'
    REPLAY_CONFIGS_FILENAME = 'configs.json'

    N_ALIGNS = 'n_aligns'
    N_VALID_ALIGNS = 'n_valid_aligns'
    CLOCK_TIME = 'clock_time(ms)'
    LOG_ALIGN_COST = 'log_align_cost'

    # additional alignment statistics
    N_LOG_MOVE = 'n_log_move'
    N_MODEL_MOVE = 'n_model_move'
    N_INVIS_MOVE = 'n_invis_move'
    N_SYNC_MOVE = 'n_sync_move'
    LOG_TRACE_LENGTH = 'log_trace_length'
    MODEL_TRACE_LENGTH = 'model_trace_length'

    def __init__(self, output_trace_df = True, output_log_df = True):
        self.output_trace_df = output_trace_df
        self.output_log_df = output_log_df

    def get_alignment_stats(self, align_dirpath, log_move_cost, model_move_cost) -> pd.DataFrame:
        """Get statistics from alignments

        :param align_dirpath: directory path to alignments
        :param log_move_cost: log move cost
        :param model_move_cost: model move cost
        :return: dataframe containing statistics on alignments
        """
        alignment_stats = dict()

        for f in os.listdir(align_dirpath):
            fpath = os.path.join(align_dirpath, f)

            if os.path.isfile(fpath):
                with open(fpath, 'r') as f:
                    _exitcode_name = f.readline()
                    exitcode = int(float(f.readline().rsplit()[0]))
                    _representative_caseid_name = f.readline()
                    representative_caseid = f.readline().rsplit()[0]
                    _caseids_name = f.readline()
                    _caseids = f.readline()
                    _move_name = f.readline()

                    if exitcode == AlignExitCode.OPTIMAL_ALIGNMENT.value:
                        cost, n_log, n_model, n_invis, n_sync, align_length, log_length, model_length = 0, 0, 0, 0, 0, 0, 0, 0

                        while True:
                            line = f.readline()

                            if not line:
                                break

                            align_length += 1

                            move_type, log_step, model_step = line.split(',')

                            if move_type == 'LMGOOD':
                                n_sync += 1
                                log_length += 1
                                model_length += 1
                            elif move_type == 'MINVI':
                                n_invis += 1
                                model_length += 1
                            elif move_type == 'L':
                                n_log += 1
                                log_length += 1
                            elif move_type == 'MREAL':
                                n_model += 1
                                model_length += 1

                        assert n_sync + n_invis + n_log + n_model == align_length, 'Sum of all move types should equal alignment length'

                        alignment_stats[representative_caseid] = [n_log, n_model, n_invis, n_sync, log_length, model_length]
                    else:
                        alignment_stats[representative_caseid] = [-1, -1, -1, -1, -1, -1, -1]

        # create dataframe
        column_names = [
            self.N_LOG_MOVE,
            self.N_MODEL_MOVE,
            self.N_INVIS_MOVE,
            self.N_SYNC_MOVE,
            self.LOG_TRACE_LENGTH,
            self.MODEL_TRACE_LENGTH
        ]
        column_values = list(zip(*list(alignment_stats.values())))
        column_dict = dict(zip(column_names, column_values))
        column_dict[StatsColname.SP_LABEL.value] = list(alignment_stats.keys())
        df = pd.DataFrame(column_dict)

        return df

    def get_clock_time(self, replay_dirpath) -> float:
        """Get clock time of replay

        :param replay_dirpath: directory path of replay results
        :return: clock time in milliseconds
        """
        SEGMENT = 'Clock time (ms): '
        clocktime = -1

        fpath = os.path.join(replay_dirpath, self.PROM_LOG_FILENAME)

        with open(fpath, 'r') as f:
            lines = f.readlines()

            for line in lines:
                if line.startswith(SEGMENT):
                    search_results = re.search(r'[^0-9]+([0-9]+\.[0-9]+)[^0-9]?', line)
                    clocktime = float(search_results.group(1))
                    break

        if clocktime < 0:
            raise RuntimeError('Cannot get clock time')

        return clocktime

    def get_log_alignment_cost(self, replay_dirpath) -> int:
        """Get log alignment cost

        :param replay_dirpath: directory path to replay results
        :return: alignment cost
        """
        SEGMENT = 'Total costs: '
        cost = -1

        fpath = os.path.join(replay_dirpath, self.PROM_LOG_FILENAME)

        with open(fpath, 'r') as f:
            lines = f.readlines()

            for line in lines:
                if line.startswith(SEGMENT):
                    search_results = re.search(r'[^0-9]+([0-9]+)[^0-9]?', line)
                    cost = int(search_results.group(1))
                    break

        if cost < 0:
            raise RuntimeError('Cannot get log alignment cost')

        return cost

    def get_replay_configs(self, replay_dirpath) -> dict:
        """Get replay configurations

        :param replay_dirpath: directory path to replay results
        :return: replay configurations
        """
        result = dict()
        fpath = os.path.join(replay_dirpath, self.REPLAY_CONFIGS_FILENAME)

        with open(fpath, 'r') as f:
            json_dict = json.load(f)

            for key in MonolithicConfigToGrab:
                result[key.value] = json_dict[key.value]

        return result

    def get_n_valid_alignments(self, trace_stats_df) -> int:
        """Get the number of valid alignments. Assuming the input is a dataframe containing alignment statistics, we
        just need to get the number of rows excluding the empty trace alignment that has optimal alignment exitcode.

        :param trace_stats_df: dataframe containing alignment statistics on the trace level
        :return: number of valid alignments
        """
        assert isinstance(trace_stats_df, pd.DataFrame)

        valid_aligns_df = trace_stats_df[(trace_stats_df[StatsColname.ALIGNMENT_EXITCODE.value] == AlignExitCode.OPTIMAL_ALIGNMENT.value)]
        return valid_aligns_df.shape[0]

    def get_log_size(self, replay_dirpath) -> int:
        """Get the size of the log, i.e., number of cases in the event log. Note that multiple cases can have the same
        unique traces.

        :param replay_dirpath: directory path to replay results
        :return: log size
        """
        SEGMENT = 'Aligned: '
        size = -1

        fpath = os.path.join(replay_dirpath, self.PROM_LOG_FILENAME)

        with open(fpath, 'r') as f:
            lines = f.readlines()

            for line in lines:
                if line.startswith(SEGMENT):
                    search_results = re.search(r'[^0-9]+([0-9]+)[^0-9]?', line)
                    size = int(search_results.group(1))

        return size

    def enrich_trace_stats(self, replay_dirpath) -> pd.DataFrame:
        """Enrich trace statistics with additional alignment statistics

        :param replay_dirpath: directory path to replay results
        :return: tdataframe containing replay statistics per trace
        """
        fpath = os.path.join(replay_dirpath, 'trace-stats.csv')
        df = pd.read_csv(fpath)

        align_dirpath = os.path.join(replay_dirpath, 'alignment')
        replay_configs = self.get_replay_configs(replay_dirpath)
        log_move_cost = int(replay_configs[MonolithicConfigToGrab.MOVE_ON_LOG_COSTS.value])
        model_move_cost = int(replay_configs[MonolithicConfigToGrab.MOVE_ON_MODEL_COSTS.value])

        alignment_stats_df = self.get_alignment_stats(align_dirpath, log_move_cost, model_move_cost)
        df = df.merge(alignment_stats_df, on=StatsColname.SP_LABEL.value, how='left')

        return df

    # getting stats dataframes
    def get_log_stats(self, replay_dirpath) -> pd.DataFrame:
        """Get the aggregated alignment statistics dataframe on the log level. Aggregation is done across all traces in
        the log.

        :param replay_dirpath: directory path to replay experiment results
        :return: dataframe containing aggregated alignment statistics
        """
        clock_time = self.get_clock_time(replay_dirpath)
        config_dict = self.get_replay_configs(replay_dirpath)
        log_align_cost = self.get_log_alignment_cost(replay_dirpath)

        trace_stats_df = self.process_trace_stats(replay_dirpath)

        # aggregate across traces
        to_sum = [
            StatsColname.ALIGNMENT_COST.value,
            StatsColname.TRANS_FIRED.value,
            StatsColname.MARKINGS_POLLED.value,
            StatsColname.MARKINGS_CLOSED.value,
            StatsColname.MARKINGS_QUEUED.value,
            StatsColname.MARKINGS_REACHED.value,
            StatsColname.HEURISTICS_COMPUTED.value,
            StatsColname.HEURISTICS_ESTIMATED.value,
            StatsColname.HEURISTICS_DERIVED.value,
            StatsColname.ALIGNMENT_COMPUTE_TIME.value,
            StatsColname.HEURISTICS_COMPUTE_TIME.value,
            StatsColname.SETUP_TIME.value,
            StatsColname.TOTAL_TIME.value,
            StatsColname.N_SPLITS.value,
            StatsColname.LOG_MOVE_COST.value,
            StatsColname.MODEL_MOVE_COST.value,
            StatsColname.SYNC_MOVE_COST.value,
            StatsColname.PREPROCESS_TIME.value,
            StatsColname.CONSTRAINTSET_SIZE.value,
            StatsColname.N_RESTARTS.value,
            StatsColname.MEMORY_TOTAL.value
        ]
        to_max = [
            StatsColname.MAX_QUEUE_LENGTH.value,
            StatsColname.MAX_QUEUE_CAPACITY.value,
            StatsColname.MAX_VISITED_SET_CAPACITY.value,
            StatsColname.MEMORY_MAX.value
        ]

        no_empty_df = trace_stats_df[trace_stats_df[StatsColname.SP_LABEL.value] != 'Empty']

        assert no_empty_df.shape[0] == trace_stats_df.shape[0] - 1, 'dataframe excluding empty trace should have 1 less row'

        log_sum_df = no_empty_df[to_sum].sum(axis=0).to_frame().transpose()
        log_max_df = no_empty_df[to_max].max(axis=0).to_frame().transpose()

        log_stats_df = pd.concat([log_sum_df, log_max_df], axis=1)
        log_stats_df[self.N_ALIGNS] = no_empty_df.shape[0]
        n_valid_aligns_df = no_empty_df[StatsColname.ALIGNMENT_EXITCODE.value].value_counts()
        if AlignExitCode.OPTIMAL_ALIGNMENT.value in n_valid_aligns_df.index:
            n_valid_aligns = n_valid_aligns_df.loc[AlignExitCode.OPTIMAL_ALIGNMENT.value]
        else:
            n_valid_aligns = 0
        log_stats_df[self.CLOCK_TIME] = clock_time
        log_stats_df[self.LOG_ALIGN_COST] = log_align_cost
        log_stats_df[self.N_VALID_ALIGNS] = n_valid_aligns

        for key, value in config_dict.items():
            log_stats_df[key] = value

        return log_stats_df


    # processing directories
    def process_trace_stats(self, replay_dirpath):
        """Process replay results on the trace level. The additional value of this method is to output the aggregated
        results as a csv file if necessary.

        :param replay_dirpath: directory path to replay experiment ersults of a model-log pair
        :return: dataframe containing alignment statistics
        """
        logger.info('Processing trace statistics in {}'.format(replay_dirpath))

        trace_stats_df = self.enrich_trace_stats(replay_dirpath)

        if self.output_trace_df:
            trace_df_fpath = os.path.join(replay_dirpath, 'trace-stats-enriched.csv')
            trace_stats_df.to_csv(trace_df_fpath, index=False)

        return trace_stats_df

    def process_directory(self, dirpath):
        """Process replay results of replay experiments on different pairs of models and logs. The additional value of
        this method is the aggregation of the results of different model-log pairs into a single dataframe. Also, it
        outputs the dataframe as a csv file if necessary.

        :param dirpath: directory path to replay experiment results of all model-log pairs
        :return: dataframe containing aggregated alignment statistics
        """
        logger.info('Processing {}'.format(dirpath))

        log_stats_df_list = list()

        for f in os.listdir(dirpath):
            replay_dirpath = os.path.join(dirpath, f)

            if not os.path.isdir(replay_dirpath):
                continue

            log_stats_df = self.get_log_stats(replay_dirpath)
            log_stats_df_list.append(log_stats_df)

        log_stats_df = pd.concat(log_stats_df_list, axis=0)

        if self.output_log_df:
            log_df_fpath = os.path.join(dirpath, 'log-stats.csv')
            log_stats_df.to_csv(log_df_fpath, index=False)

        return log_stats_df


class RecomposeReplayResultProcessor:
    PYTHON_LOG_FILENAME = 'python.log'
    PROM_LOG_FILENAME = 'prom.log'
    REPLAY_CONFIGS_FILENAME = 'configs.json'

    SUB_NO = 'sub_no'
    SUB_DUPLICATE = 'sub_duplicate'
    SUB_REPRESENTATIVE = 'sub_representative'
    ITER_NO = 'iter_no'

    N_ALIGNS = 'n_aligns'
    N_VALID_ALIGNS = 'n_valid_aligns'
    CLOCK_TIME = 'clock_time(ms)'
    N_REPLAY_ITER = 'n_replay_iter'
    LOG_ALIGN_COST_LOWER = 'log_align_cost_lower'
    LOG_ALIGN_COST_UPPER = 'log_align_cost_upper'

    # merged alignment statistics
    MERGED_ALIGNMENT_COST = 'merged_alignment_cost'
    MERGED_ALIGNMENT_N_LOG_MOVE = 'merged_alignment_n_log_move'
    MERGED_ALIGNMENT_N_MODEL_MOVE = 'merged_alignment_n_model_move'
    MERGED_ALIGNMENT_N_INVIS_MOVE = 'merged_alignment_n_invis_move'
    MERGED_ALIGNMENT_N_SYNC_MOVE = 'merged_alignment_n_sync_move'
    MERGED_ALIGNMENT_LENGTH = 'merged_alignment_length'
    MERGED_ALIGNMENT_LOG_TRACE_LENGTH = 'merged_alignment_log_trace_length'
    MERGED_ALIGNMENT_MODEL_TRACE_LENGTH = 'merged_alignment_model_trace_length'

    def __init__(self, output_sub_df=True, output_iter_df=True, output_trace_df=True, output_log_df=True):
        self.output_sub_df = output_sub_df
        self.output_iter_df = output_iter_df
        self.output_trace_df = output_trace_df
        self.output_log_df = output_log_df

    def get_clock_time(self, replay_dirpath) -> int:
        """Get clock time of replay

        :param replay_dirpath: directory path of replay results
        :return: clock time in milliseconds
        """
        SEGMENT = 'INFO: Running boot main took: '
        clocktime = -1

        fpath = os.path.join(replay_dirpath, self.PROM_LOG_FILENAME)

        with open(fpath, 'r') as f:
            lines = f.readlines()
            reversed_lines = lines[::-1]

            for line in reversed_lines:
                if line.startswith(SEGMENT):
                    search_result = re.search(r'[^0-9]+([0-9]+)[^0-9]+', line)
                    clocktime = int(search_result.group(1))
                    break

        if clocktime < 0:
            raise RuntimeError('Cannot get clock time')

        return clocktime

    def get_log_alignment_cost(self, replay_dirpath):
        """Get log alignment cost bounds

        :param replay_dirpath: directory path to replay results
        :return: cost lower and upper bounds
        """
        SEGMENT = '[RecomposingReplayAlgorithm] Replay costs interval = '
        cost_lower, cost_upper = -1., -1.

        fpath = os.path.join(replay_dirpath, self.PYTHON_LOG_FILENAME)

        with open(fpath, 'r') as f:
            lines = f.readlines()
            reversed_lines = lines[::-1]

            for line in reversed_lines:
                if line.startswith(SEGMENT):
                    search_result = re.search(r'[^0-9]+([0-9]+\.[0-9]+),([0-9]+\.[0-9]+)[^0-9]+', line)
                    cost_lower = float(search_result.group(1))
                    cost_upper = float(search_result.group(2))
                    break

        if cost_lower == -1. or cost_upper == -1.:
            raise RuntimeError('Cannot get cost bounds')

        return cost_lower, cost_upper

    def get_replay_configs(self, replay_dirpath) -> dict:
        """Get replay configurations

        :param replay_dirpath: directory path to replay results
        :return: replay configurations
        """
        result = dict()
        fpath = os.path.join(replay_dirpath, self.REPLAY_CONFIGS_FILENAME)

        with open(fpath, 'r') as f:
            json_dict = json.load(f)

            for key in RecomposeConfigToGrab:
                result[key.value] = json_dict[key.value]

        return result

    def get_n_valid_alignments(self, trace_stats_df) -> int:
        """Get the number of valid alignments. Assuming the input is a dataframe containing alignment statistics, we
        just need to get the number of rows excluding the empty trace alignment with optimal alignment exitcode.

        :param trace_stats_df: dataframe containing alignment statistics on the trace level
        :return: number of valid alignments
        """
        assert isinstance(trace_stats_df, pd.DataFrame)

        valid_aligns_df = trace_stats_df[(trace_stats_df[StatsColname.ALIGNMENT_EXITCODE.value] == AlignExitCode.OPTIMAL_ALIGNMENT.value)]
        return valid_aligns_df.shape[0]

    def get_log_size(self, prom_iter_stats_df) -> int:
        """Get the size of the log, i.e., number of cases in the event log. Note that multiple cases can have the same
        unique trace.

        :param prom_iter_stats_df: dataframe containing statistics relating to each replay iteration produced by ProM jars
        :return: log size
        """
        first_row = prom_iter_stats_df.iloc[0,:]
        # in the first iteration all cases are aligned
        return first_row['n_trace_aligned']

    # iter-stats functions
    def get_n_rejected_alignments_iter(self) -> int:
        pass

    def get_n_to_align_alignments_iter(self) -> int:
        pass

    def get_n_valid_alignments_iter(self) -> int:
        pass

    def get_n_aligned_traces(self) -> int:
        pass

    def get_sub_iter_trace_stats(self, iter_no, iter_dirpath, trace_caseids):
        """List of dataframes where each dataframe contains alignment statistics of a pair of subnet and sublog. The list
        of dataframes are all of replay done at iteration no. i.

        :param iter_no: iteration number
        :param iter_dirpath: directory path containing information of replay at iteration iter_no
        :param trace_caseids: set of representative caseids of traces on the monolithic level
        :return: list of dataframes containing alignment statistics of subnet and sublog pairs
        """
        stats_dirpath = os.path.join(iter_dirpath, 'stats')
        align_dirpath = os.path.join(iter_dirpath, 'alignments')

        # mapping from representative caseids from subcomponents to member caseids
        # need to later duplicate rows for member caseids that are representative caseids on
        # the monolithic level
        subcomponent_caseid_map = dict()

        for f in os.listdir(align_dirpath):
            # file path to a particular subcomponent's alignments
            subcomponent_dirpath = os.path.join(align_dirpath, f)

            if os.path.isdir(subcomponent_dirpath):
                sub_no = f.replace('subalign-', '')
                caseid_map = dict()

                # iterate over the alignments of the subcomponent to get the mapping between the representative caseid
                # and the member caseids
                for align_fname in os.listdir(subcomponent_dirpath):
                    align_fpath = os.path.join(subcomponent_dirpath, align_fname)

                    with open(align_fpath, 'r') as f:
                        _exitcode_name = f.readline()
                        _exitcode = f.readline().rsplit()[0]
                        _representative_caseid_name = f.readline()
                        representative_caseid = f.readline().rsplit()[0]
                        _caseids_name = f.readline()
                        caseids = f.readline().rsplit()[0].split(',')

                        not_repr = lambda caseid: caseid != representative_caseid
                        # unique on the monolithic level
                        monolithic = lambda caseid: caseid in trace_caseids
                        filtered = filter(lambda caseid: not_repr(caseid) and monolithic(caseid), caseids)
                        caseid_map[representative_caseid] = list(filtered)

                assert sub_no not in subcomponent_caseid_map, 'Reading two subcomponents with the same sub_no: {}'.format(sub_no)
                subcomponent_caseid_map[sub_no] = caseid_map

        # get the dataframes
        df_list = list()

        # iterate through the alignment statistics files relating to the replay of each subcomponent pair (subnet and sublog)
        start = time.time()
        repeat_row_took = 0
        n_row_repeats = 0
        concat_repeat_took = 0
        for f in os.listdir(stats_dirpath):
            fpath = os.path.join(stats_dirpath, f)
            sub_no = f.replace('.csv', '')

            if os.path.isfile(fpath):
                sub_df = pd.read_csv(fpath)

                # all current rows are representative caseids on the subcomponent level
                sub_df[self.SUB_NO] = sub_no
                sub_df[self.SUB_DUPLICATE] = False
                sub_df[self.SUB_REPRESENTATIVE] = sub_df[StatsColname.SP_LABEL.value] # they are their own representatives

                assert sub_no in subcomponent_caseid_map, 'Sub no {} not in caseid map'.format(sub_no)

                # get caseid mapping and add duplicate caseids that are duplicate only on
                # the subcomponent level
                # this implementation is at least 14 times faster than the following implementation using concat
                # repeat_rows_func = lambda row, n: pd.concat([row] * n, ignore_index=True)
                repeat_rows_func = lambda row, n: pd.DataFrame(pd.np.repeat(row.values, n, axis=0), columns=row.columns)

                repeated_df_list = list()

                # need to do the duplication here because potentially a monolithically unique
                # caseid can have different representative caseid on the subcomponent level
                # E.g.
                # c0 = <a, b, c, d>, c1 = <a, b, d, c>, c2 = <a, b, e, f>
                # partition = [{a, b}, {c, d}, {e, f}]
                # r0 = <a, b>, r1 = <c, d>, r2 = <d, c>, r3 = <e, f>
                #
                # Representative mappings
                # r0 (c0): [c0, c1, c2],
                # r1 (c0): [c0],
                # r2 (c1): [c1],
                # r3 (c2): [c2]
                #
                # this means we need expand the subcomponent stats_df related to each rx before aggregating
                # across all subcomponent stats_df to get the iter_stats_df

                for repr, members in subcomponent_caseid_map[sub_no].items():
                    row = sub_df.loc[(sub_df[StatsColname.SP_LABEL.value] == repr),:]
                    n_members = len(members)

                    if n_members <= 0:
                        continue

                    sub_start = time.time()
                    repeated_df = repeat_rows_func(row, n_members)
                    repeat_row_took = time.time() - sub_start + repeat_row_took
                    n_row_repeats += n_members
                    repeated_df[self.SUB_DUPLICATE] = True
                    # no need to set SUB_REPRESENTATIVE since row[SUB_REPRESENTATIVE] = repr already

                    # update the caseids
                    repeated_df[StatsColname.SP_LABEL.value] = members
                    repeated_df_list.append(repeated_df)

                # concat as one single subcomponent dataframe
                sub_start = time.time()
                sub_df = pd.concat([sub_df] + repeated_df_list)
                concat_repeat_took = time.time() - sub_start + concat_repeat_took
                sub_df = sub_df.sort_values(self.SUB_REPRESENTATIVE)
                # logger.info('Sorting by sub representative caseid took: {} secs'.format(sub_took))
                sub_df = sub_df.reset_index(drop=True)

                df_list.append(sub_df)

        logger.info('Repeating row {} times took: {:.2f} secs'.format(n_row_repeats, repeat_row_took))
        logger.info('Concatenating repeated caseid dfs took: {:.2f} secs'.format(concat_repeat_took))
        took = time.time() - start
        logger.info('Processing all decomposed replay alignment stats files took: {:.2f} secs'.format(took))

        sub_df = pd.concat(df_list, axis=0)

        # add iteration number
        sub_df[self.ITER_NO] = iter_no

        return sub_df

    def get_iter_trace_stats(self, iter_no, iter_dirpath, trace_caseids) -> pd.DataFrame:
        """Get the aggregated alignment statistics dataframe on the trace level for a particular iteration. Aggregation
        is done across all subcomponents (subnets and sublogs)

        :param iter_no: iteration number
        :param iter_dirpath: directory path to iteration i folder
        :param trace_caseids: set of caseids of all traces, i.e., all representative caseids on the monolithic level
        :return: dataframe containing aggregated alignment statistics
        """
        to_join = [
            StatsColname.ALIGNMENT_EXITCODE.value
        ]
        to_sum = [
            StatsColname.ALIGNMENT_COST.value,
            StatsColname.TRANS_FIRED.value,
            StatsColname.MARKINGS_POLLED.value,
            StatsColname.MARKINGS_CLOSED.value,
            StatsColname.MARKINGS_QUEUED.value,
            StatsColname.MARKINGS_REACHED.value,
            StatsColname.HEURISTICS_COMPUTED.value,
            StatsColname.HEURISTICS_ESTIMATED.value,
            StatsColname.HEURISTICS_DERIVED.value,
            StatsColname.ALIGNMENT_COMPUTE_TIME.value,
            StatsColname.HEURISTICS_COMPUTE_TIME.value,
            StatsColname.SETUP_TIME.value,
            StatsColname.TOTAL_TIME.value,
            StatsColname.N_SPLITS.value,
            StatsColname.LOG_MOVE_COST.value,
            StatsColname.MODEL_MOVE_COST.value,
            StatsColname.SYNC_MOVE_COST.value,
            StatsColname.PREPROCESS_TIME.value,
            StatsColname.CONSTRAINTSET_SIZE.value,
            StatsColname.N_RESTARTS.value,
            StatsColname.MEMORY_TOTAL.value
        ]
        to_max = [
            StatsColname.MAX_QUEUE_LENGTH.value,
            StatsColname.MAX_QUEUE_CAPACITY.value,
            StatsColname.MAX_VISITED_SET_CAPACITY.value,
            StatsColname.MEMORY_MAX.value
        ]

        to_join_map = { k: lambda vals: fct.reduce(lambda x, y: x | y, vals) for k in to_join }
        to_sum_map = { k: np.sum for k in to_sum }
        to_max_map = { k: np.max for k in to_max }

        agg_map = dict()
        agg_map.update(to_join_map)
        agg_map.update(to_sum_map)
        agg_map.update(to_max_map)

        sub_df = self.process_sub_replay_iter_stats(iter_no, iter_dirpath, trace_caseids)
        sub_df = sub_df.reset_index(drop=True)

        assert isinstance(sub_df, pd.DataFrame)

        grouped = sub_df.groupby(by=StatsColname.SP_LABEL.value)
        iter_df = grouped.agg(agg_map).reset_index(drop=False)

        iter_df[self.ITER_NO] = iter_no

        return iter_df

    def get_merged_alignment_stats(self, align_dirpath, trace_caseids, log_move_cost, model_move_cost) -> pd.DataFrame:
        """Get alignment statistics from final merged alignments

        :param align_dirpath: directory path to alignments
        :param trace_caseids: set of representative caseids on the monolithic level
        :param log_move_cost: log move cost
        :param model_move_cost: model move cost
        :return: dataframe containing statistics on alignments
        """
        alignment_stats = dict()

        for f in os.listdir(align_dirpath):
            fpath = os.path.join(align_dirpath, f)

            if os.path.isfile(fpath):
                with open(fpath, 'r') as f:
                    _exitcode_name = f.readline()
                    exitcode = int(float(f.readline().rsplit()[0]))
                    _representative_caseid_name = f.readline()
                    representative_caseid = f.readline().rsplit()[0]
                    _caseids_name = f.readline()
                    _caseids = f.readline()
                    _move_name = f.readline()

                    if exitcode == AlignExitCode.OPTIMAL_ALIGNMENT.value:
                        cost, n_log, n_model, n_invis, n_sync, align_length, log_length, model_length = 0, 0, 0, 0, 0, 0, 0, 0

                        while True:
                            line = f.readline()

                            if not line:
                                break

                            align_length += 1

                            move_type, log_step, model_step = line.split(',')

                            if move_type == 'LMGOOD':
                                n_sync += 1
                                log_length += 1
                                model_length += 1
                            elif move_type == 'MINVI':
                                n_invis += 1
                                model_length += 1
                            elif move_type == 'L':
                                n_log += 1
                                log_length += 1
                            elif move_type == 'MREAL':
                                n_model += 1
                                model_length += 1

                        cost = n_log * log_move_cost + n_model * model_move_cost

                        assert n_sync + n_invis + n_log + n_model == align_length, 'Sum of all move types should equal alignment length'

                        alignment_stats[representative_caseid] = [cost, n_log, n_model, n_invis, n_sync, align_length, log_length, model_length]
                    else:
                        alignment_stats[representative_caseid] = [-1, -1, -1, -1, -1, -1, -1, -1]

        assert set(trace_caseids) == set(alignment_stats.keys()), 'Alignment cost dict keyset not equal representative caseids'

        # create dataframe
        column_names = [
            self.MERGED_ALIGNMENT_COST,
            self.MERGED_ALIGNMENT_N_LOG_MOVE,
            self.MERGED_ALIGNMENT_N_MODEL_MOVE,
            self.MERGED_ALIGNMENT_N_INVIS_MOVE,
            self.MERGED_ALIGNMENT_N_SYNC_MOVE,
            self.MERGED_ALIGNMENT_LENGTH,
            self.MERGED_ALIGNMENT_LOG_TRACE_LENGTH,
            self.MERGED_ALIGNMENT_MODEL_TRACE_LENGTH
        ]
        column_values = list(zip(*list(alignment_stats.values())))
        column_dict = dict(zip(column_names, column_values))
        column_dict[StatsColname.SP_LABEL.value] = list(alignment_stats.keys())
        df = pd.DataFrame(column_dict)

        return df

    def get_trace_stats(self, replay_dirpath, trace_caseids):
        """Get the aggregated alignment statistics dataframe on the trace level. Aggregation is done across all recomposing
        replay iterations.

        :param replay_dirpath: directory path to replay experiment results
        :param trace_caseids: set of caseids of all traces, i.e., all representative caseids on the monolithic level
        :return: dataframe containing aggregated alignment statistics
        """
        to_join = [
            StatsColname.ALIGNMENT_EXITCODE.value
        ]
        to_sum = [
            StatsColname.ALIGNMENT_COST.value,
            StatsColname.TRANS_FIRED.value,
            StatsColname.MARKINGS_POLLED.value,
            StatsColname.MARKINGS_CLOSED.value,
            StatsColname.MARKINGS_QUEUED.value,
            StatsColname.MARKINGS_REACHED.value,
            StatsColname.HEURISTICS_COMPUTED.value,
            StatsColname.HEURISTICS_ESTIMATED.value,
            StatsColname.HEURISTICS_DERIVED.value,
            StatsColname.ALIGNMENT_COMPUTE_TIME.value,
            StatsColname.HEURISTICS_COMPUTE_TIME.value,
            StatsColname.SETUP_TIME.value,
            StatsColname.TOTAL_TIME.value,
            StatsColname.N_SPLITS.value,
            StatsColname.LOG_MOVE_COST.value,
            StatsColname.MODEL_MOVE_COST.value,
            StatsColname.SYNC_MOVE_COST.value,
            StatsColname.PREPROCESS_TIME.value,
            StatsColname.CONSTRAINTSET_SIZE.value,
            StatsColname.N_RESTARTS.value,
            StatsColname.MEMORY_TOTAL.value
        ]
        to_max = [
            StatsColname.MAX_QUEUE_LENGTH.value,
            StatsColname.MAX_QUEUE_CAPACITY.value,
            StatsColname.MAX_VISITED_SET_CAPACITY.value,
            StatsColname.MEMORY_MAX.value
        ]

        to_join_map = { k: lambda vals: fct.reduce(lambda x, y: x | y, vals) for k in to_join }
        to_sum_map = { k: np.sum for k in to_sum }
        to_max_map = { k: np.max for k in to_max }

        agg_map = dict()
        agg_map.update(to_join_map)
        agg_map.update(to_sum_map)
        agg_map.update(to_max_map)

        df = self.process_replay_iter_directories(replay_dirpath, trace_caseids)

        # compute the alignment cost of all traces in trace_caseids
        align_dirpath = os.path.join(replay_dirpath, 'alignments')
        replay_configs = self.get_replay_configs(replay_dirpath)
        log_move_cost = int(replay_configs[RecomposeConfigToGrab.MOVE_ON_LOG_COSTS.value])
        model_move_cost = int(replay_configs[RecomposeConfigToGrab.MOVE_ON_MODEL_COSTS.value])

        merged_alignment_df = self.get_merged_alignment_stats(align_dirpath, trace_caseids, log_move_cost, model_move_cost)

        assert isinstance(df, pd.DataFrame)

        grouped = df.groupby(by=StatsColname.SP_LABEL.value)
        trace_stats_df = grouped.agg(agg_map)
        trace_stats_df = trace_stats_df.reset_index(drop=False)
        trace_stats_df = trace_stats_df.merge(merged_alignment_df, on=StatsColname.SP_LABEL.value, how='left')

        # compute the number of iterations each trace required
        def get_n_replay_iter_df(df):
            grouped = df.groupby(StatsColname.SP_LABEL.value)
            n_replay_iter_df = grouped.agg({self.ITER_NO: lambda vals: len(set(vals))})
            n_replay_iter_df = n_replay_iter_df.rename(columns={self.ITER_NO: self.N_REPLAY_ITER})
            n_replay_iter_df = n_replay_iter_df.reset_index(drop=False)
            return n_replay_iter_df

        n_replay_iter_df = get_n_replay_iter_df(df)
        trace_stats_df = trace_stats_df.merge(n_replay_iter_df, on=StatsColname.SP_LABEL.value, how='left')

        return trace_stats_df

    def get_log_stats(self, replay_dirpath):
        """Get the aggregated alignment statistics dataframe on the log level. Aggregation is done across all traces
        in the log.

        :param replay_dirpath: directory path to replay experiment results
        :return: dataframe containing aggregated alignment statistics
        """
        cost_lower, cost_upper = self.get_log_alignment_cost(replay_dirpath)
        clock_time = self.get_clock_time(replay_dirpath)
        config_dict = self.get_replay_configs(replay_dirpath)

        # create trace_caseids
        trace_caseids = set()
        align_dirpath = os.path.join(replay_dirpath, 'alignments')

        for f in os.listdir(align_dirpath):
            fpath = os.path.join(align_dirpath, f)

            if os.path.isfile(fpath):
                with open(fpath, 'r') as f:
                    _exitcode_name = f.readline()
                    _exitcode = f.readline()
                    _representative_caseid_name = f.readline()
                    representative_caseid = f.readline().rsplit()[0]

                    trace_caseids.add(representative_caseid)

        trace_stats_df = self.process_trace_stats(replay_dirpath, trace_caseids)

        # to correct old recomposing replay jar which has average cost interval rather than total cost interval
        # prom_iter_stats_df = pd.read_csv(os.path.join(replay_dirpath, 'prom-iter-stats.csv'))
        # cost_lower *= self.get_log_size(prom_iter_stats_df)
        # cost_upper *= self.get_log_size(prom_iter_stats_df)

        # aggregate across traces
        to_sum = [
            self.MERGED_ALIGNMENT_COST,
            StatsColname.ALIGNMENT_COST.value,
            StatsColname.TRANS_FIRED.value,
            StatsColname.MARKINGS_POLLED.value,
            StatsColname.MARKINGS_CLOSED.value,
            StatsColname.MARKINGS_QUEUED.value,
            StatsColname.MARKINGS_REACHED.value,
            StatsColname.HEURISTICS_COMPUTED.value,
            StatsColname.HEURISTICS_ESTIMATED.value,
            StatsColname.HEURISTICS_DERIVED.value,
            StatsColname.ALIGNMENT_COMPUTE_TIME.value,
            StatsColname.HEURISTICS_COMPUTE_TIME.value,
            StatsColname.SETUP_TIME.value,
            StatsColname.TOTAL_TIME.value,
            StatsColname.N_SPLITS.value,
            StatsColname.LOG_MOVE_COST.value,
            StatsColname.MODEL_MOVE_COST.value,
            StatsColname.SYNC_MOVE_COST.value,
            StatsColname.PREPROCESS_TIME.value,
            StatsColname.CONSTRAINTSET_SIZE.value,
            StatsColname.N_RESTARTS.value,
            StatsColname.MEMORY_TOTAL.value
        ]
        to_max = [
            StatsColname.MAX_QUEUE_LENGTH.value,
            StatsColname.MAX_QUEUE_CAPACITY.value,
            StatsColname.MAX_VISITED_SET_CAPACITY.value,
            StatsColname.MEMORY_MAX.value
        ]

        no_empty_df = trace_stats_df[trace_stats_df[StatsColname.SP_LABEL.value] != 'Empty']

        assert no_empty_df.shape[0] == trace_stats_df.shape[0] - 1, 'dataframe excluding empty trace should have 1 less row'

        log_sum_df = no_empty_df[to_sum].sum(axis=0).to_frame().transpose()
        log_max_df = no_empty_df[to_max].max(axis=0).to_frame().transpose()

        log_stats_df = pd.concat([log_sum_df, log_max_df], axis=1)
        log_stats_df[self.N_ALIGNS] = len(trace_caseids)
        n_valid_aligns_df = no_empty_df[StatsColname.ALIGNMENT_EXITCODE.value].value_counts()
        if AlignExitCode.OPTIMAL_ALIGNMENT.value in n_valid_aligns_df.index:
            n_valid_aligns = n_valid_aligns_df.loc[AlignExitCode.OPTIMAL_ALIGNMENT.value]
        else:
            n_valid_aligns = 0
        log_stats_df[self.N_VALID_ALIGNS] = n_valid_aligns
        log_stats_df[self.CLOCK_TIME] = clock_time
        log_stats_df[self.LOG_ALIGN_COST_LOWER] = cost_lower
        log_stats_df[self.LOG_ALIGN_COST_UPPER] = cost_upper

        for key, value in config_dict.items():
            log_stats_df[key] = value

        return log_stats_df

    @timeit
    def process_sub_replay_iter_stats(self, iter_no, iter_dirpath, trace_caseids):
        """Process decomposed replay results of a particular iteration. The additional value of this method is to output
        the dataframe as a csv file if necessary.

        :param iter_no: iteration number
        :param iter_dirpath: directory path to replay experiment results
        :param trace_caseids: set of representative caseids on the monolithic level
        :return: list of dataframes containing alignment statistics of subcomponents
        """
        logger.info('Processing decomposed replay statistics of iteration {} in {}'.format(iter_no, iter_dirpath))

        sub_df_list = self.get_sub_iter_trace_stats(iter_no, iter_dirpath, trace_caseids)

        if self.output_sub_df:
            sub_df_fpath = os.path.join(iter_dirpath, 'sub-iter-stats.csv')
            sub_df_list.to_csv(sub_df_fpath, index=False)

        return sub_df_list

    @timeit
    def process_replay_iter_directory(self, iter_no, iter_dirpath, trace_caseids):
        """Process replay results of a particular iteration with aggregation. The additional value of this method is to
        output the dataframe as a csv file if necessary.

        :param iter_no: iteration number
        :param iter_dirpath: directory path to replay experiment results of the iteration
        :param trace_caseids: set of representative caseids on the monolithic level
        :return: dataframe containing alignment statistics of subcomponents
        """
        logger.info('Processing aggregated decomposed replay statistics of iteration {} in {}'.format(iter_no, iter_dirpath))

        iter_df = self.get_iter_trace_stats(iter_no, iter_dirpath, trace_caseids)

        if self.output_iter_df:
            iter_df_fpath = os.path.join(iter_dirpath, 'iter-stats.csv')
            iter_df.to_csv(iter_df_fpath, index=False)

        return iter_df

    @timeit
    def process_replay_iter_directories(self, replay_dirpath, trace_caseids):
        """Process replay results of all recomposing replay iterations.

        :param replay_dirpath: directory path to replay experiment results
        :param trace_caseids: set of representative caseids on the monolithic level
        :return: list of dataframe containing alignment statistics of each iteration
        """
        logger.info('Processing replay statistics of all decomposed replay iterations in {}'.format(replay_dirpath))

        iter_dirpath_list = list()
        iter_trace_stats_df_list = list()

        for f in os.listdir(replay_dirpath):
            fpath = os.path.join(replay_dirpath, f)
            if 'iter-' in f and os.path.isdir(fpath):
                # get iter_no
                iter_no = f.replace('iter-', '')
                iter_dirpath_list.append((iter_no, fpath))

        iter_dirpath_list = sorted(iter_dirpath_list, key=lambda pair: pair[0])

        for iter_no, iter_dirpath in iter_dirpath_list:
            iter_df = self.process_replay_iter_directory(iter_no, iter_dirpath, trace_caseids)
            iter_trace_stats_df_list.append(iter_df)

        start = time.time()
        df = pd.concat(iter_trace_stats_df_list)
        took = time.time() - start
        logger.info('Concatenating iteration stats df took: {:.2f} secs'.format(took))

        return df

    @timeit
    def process_trace_stats(self, replay_dirpath, trace_caseids):
        """Process replay results of all recomposing replay iterations and aggregate them to get alignment statistics on
        the trace level. The additional value of this method is to output the aggregated results as a csv file if
        necessary.

        :param replay_dirpath: directory path to replay experiment results of a model-log pair
        :param trace_caseids: set of representative caseids on the monolithic level
        :return: dataframe containing aggregated alignment statistics
        """
        logger.info('Processing trace statistics in {}'.format(replay_dirpath))

        trace_stats_df = self.get_trace_stats(replay_dirpath, trace_caseids)

        if self.output_trace_df:
            trace_df_fpath = os.path.join(replay_dirpath, 'trace-stats.csv')
            trace_stats_df.to_csv(trace_df_fpath, index=False)

        return trace_stats_df

    @timeit
    def process_directory(self, dirpath):
        """Process replay results of replay experiments on different pairs of models and logs. The additional value of
        this method is the aggregation of the results of different model-log pairs into a single dataframe. Also, it
        outputs the dataframe as a csv file if necessary.

        :param dirpath: directory path to replay experiment results of all model-log pairs
        :return: dataframe containing aggregated alignment statistics
        """
        logger.info('Processing {}'.format(dirpath))

        log_stats_df_list = list()

        for f in os.listdir(dirpath):
            replay_dirpath = os.path.join(dirpath, f)

            if os.path.isdir(replay_dirpath):
                log_stats_df = self.get_log_stats(replay_dirpath)
                log_stats_df_list.append(log_stats_df)

        log_stats_df = pd.concat(log_stats_df_list, axis=0)

        if self.output_log_df:
            log_df_fpath = os.path.join(dirpath, 'log-stats.csv')
            log_stats_df.to_csv(log_df_fpath, index=False)

        return log_stats_df
