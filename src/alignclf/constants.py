#!/usr/bin/env python


__all__ = [
    'RUNNER_TYPE',
    'BASEDIR',
    'DATA_DIR',
    'RESULT_DIR',
    'EXPERIMENT_NAME',
    'OUTDIR',
    'DATA_TO_RUN',

    'MODEL_EXT',
    'LOG_EXT',
    'MEMORY',
    'PROM_JAR',
    'PROM_PKG',
    'PLUGIN_JAR',
    'MAIN_CLASS',

    'REPLAY_CONFIG',
    'MOVE_ON_LOG_COSTS',
    'MOVE_ON_MODEL_COSTS',
    'GLOBAL_DURATION',
    'LOCAL_DURATION',
    'INTERVAL_RELATIVE',
    'INTERVAL_ABSOLUTE',
    'MAX_CONFLICTS',
    'ALIGNMENT_PERCENTAGE',
    'NOF_ITERATIONS',
    'USE_HIDE_N_REDUCE',
    'DECOMPOSITION',
    'PREFER_BORDER_TRANS',
    'ADD_CONFLICT_ONLY_ONCE',
    'RECOMPOSE_STRATEGY',
    'LOG_CREATION_STRATEGY',

    'DEBUG',
    'PRINT_ALIGNMENT',
    'DEADLINE',
    'TIMEOUT_PER_TRACE_IN_SEC',
    'MOVE_SORT',
    'USE_INT',
    'PARTIAL_ORDER',
    'PREFER_EXACT',
    'QUEUE_SORT'
]


# runner configuration parameters
RUNNER_TYPE = 'runner_type'
BASEDIR = 'basedir'
DATA_DIR = 'data_dir'
RESULT_DIR = 'result_dir'
EXPERIMENT_NAME = 'experiment_name'
OUTDIR = 'outdir'
DATA_TO_RUN = 'data_to_run'

MODEL_EXT = 'model_ext'
LOG_EXT = 'log_ext'
MEMORY = 'memory'
PROM_JAR = 'prom_jar'
PROM_PKG = 'prom_pkg'
PLUGIN_JAR = 'plugin_jar'
MAIN_CLASS = 'main_class'

# recomposing replay configurations
REPLAY_CONFIG = 'replay_config'
MOVE_ON_LOG_COSTS = 'move_on_log_costs'
MOVE_ON_MODEL_COSTS = 'move_on_model_costs'
GLOBAL_DURATION = 'global_duration'
LOCAL_DURATION = 'local_duration'
INTERVAL_RELATIVE = 'interval_relative'
INTERVAL_ABSOLUTE = 'interval_absolute'
MAX_CONFLICTS = 'max_conflicts'
ALIGNMENT_PERCENTAGE = 'alignment_percentage'
NOF_ITERATIONS = 'nof_iterations'
USE_HIDE_N_REDUCE = 'use_hide_n_reduce'
DECOMPOSITION = 'decomposition'
PREFER_BORDER_TRANS = 'prefer_border_trans'
ADD_CONFLICT_ONLY_ONCE = 'add_conflict_only_once'
RECOMPOSE_STRATEGY = 'recompose_strategy'
LOG_CREATION_STRATEGY = 'log_creation_strategy'

# monolithic replay configurations
DEBUG = 'debug'
PRINT_ALIGNMENT = 'print_alignment'
DEADLINE = 'deadline'
TIMEOUT_PER_TRACE_IN_SEC = 'timeout_per_trace_in_sec'
MOVE_SORT = 'move_sort'
USE_INT = 'use_int'
PARTIAL_ORDER = 'partial_order'
PREFER_EXACT = 'prefer_exact'
QUEUE_SORT = 'queue_sort'
