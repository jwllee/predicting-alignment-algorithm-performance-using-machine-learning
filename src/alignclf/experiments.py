#!/usr/bin/env python3


import os, logging, sys, subprocess, json, time


from alignclf.constants import *
from alignclf import utils


__all__ = [
    'ProMPluginExecutor',
    'RunnerFactory'
]


logger = logging.getLogger(__name__)


class ProMPluginExecutor:

    def __init__(self, basedir, configs_fp, mem,
                 prom_jar, prom_pkg, plugin_jar, main_class,
                 prom_logfile, logfile=None):
        self.basedir = basedir
        self.configs_fp = configs_fp
        self.mem = mem
        self.prom_jar = prom_jar
        self.prom_pkg = prom_pkg
        self.plugin_jar = plugin_jar
        self.main_class = main_class
        self.prom_logfile = prom_logfile
        self.logfile = logfile

    def make_classpath(self):
        # add all libraries in lib by listing directory of prom_pkg
        classpath = '.{pathsep}{prom_jar}{pathsep}{plugin_jar}'.format(
            pathsep=os.pathsep,
            prom_jar=self.prom_jar,
            plugin_jar=self.plugin_jar
        )

        for f in os.listdir(self.prom_pkg):
            if '.jar' in f:
                jar_fp = os.path.join('.', self.prom_pkg, f)
                classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep,
                                                     jar=jar_fp)

        logger.debug('Classpath: {}'.format(classpath))

        return classpath

    def execute(self):
        logger.info('Executing...')

        classpath = self.make_classpath()
        command = 'java -classpath {classpath} ' \
                  '-Djava.library.path={prom_pkg} ' \
                  '-Djava.util.Arrays.useLegacyMergeSort=true ' \
                  '-Xmx{memory}G ' \
                  '{main_class} {prom_logfile} {configs_fpath}'.format(
            classpath=classpath,
            prom_pkg=self.prom_pkg,
            memory=self.mem,
            main_class=self.main_class,
            prom_logfile=self.prom_logfile,
            configs_fpath=self.configs_fp
        )

        logger.info('Calling: {}'.format(command))
        logger.info('Current working directory: {}'.format(os.getcwd()))

        if self.logfile == None:
            subprocess.call(command, shell=True)
        else:
            subprocess.call(command, shell=True, stdout=self.logfile)


class RunnerFactory:
    MINIMAL = 'minimal'
    RECOMPOSING = 'recomposing'
    MONOLITHIC = 'monolithic'

    def build_runner(self, configs):
        if RUNNER_TYPE not in configs:
            logger.error('Runner type not in {}'.format(configs))
            exit(1)

        _map = {
            RunnerFactory.MINIMAL: MinimalRunner,
            RunnerFactory.RECOMPOSING: RecomposingReplayRunner,
            RunnerFactory.MONOLITHIC: MonolithicReplayRunner
        }

        return _map[configs[RUNNER_TYPE]](configs)


class MinimalRunner:

    def __init__(self, configs):
        self.configs = configs

    def run(self):

        logger.info('Running minimal runner...')

        data_list = list()
        to_run_fp = os.path.join(self.configs[BASEDIR],
                                 self.configs[DATA_TO_RUN])

        with open(to_run_fp, 'r') as f:
            cnt = 0

            for line in f:
                model, log = line.split(',')
                model = model.strip()
                log = log.strip()

                logger.debug('Model {}: {}, Log {}: {}'.format(cnt, model,
                                                               cnt, log))

                data_list.append((model, log))
                cnt += 1

        data_dir = os.path.join(self.configs[BASEDIR], self.configs[DATA_DIR])

        for _id, to_run in enumerate(data_list):
            start = time.time()

            model, log = to_run
            model_fp = '.'.join([model, self.configs[MODEL_EXT]])
            model_fp = os.path.join(data_dir, model_fp)
            log_fp = '.'.join([log, self.configs[LOG_EXT]])
            log_fp = os.path.join(data_dir, log_fp)

            # create the ProM configuration json
            outdir = os.path.join(self.configs[OUTDIR], str(_id))
            os.makedirs(outdir)

            outfile_fp = os.path.join(outdir, 'results.txt')

            prom_configs = {
                'netPath': model_fp,
                'logPath': log_fp,
                'outFile': outfile_fp
            }

            prom_configs_fn = 'configs-{}.json'.format(_id)
            prom_configs_fp = os.path.join(outdir, prom_configs_fn)

            with open(prom_configs_fp, 'w') as f:
                json.dump(prom_configs, f)

            # make and run prom executor
            logfile_fn = '{}.log'.format(_id)
            logfile_fp = os.path.join(outdir, logfile_fn)
            prom_logfile_fn = 'prom-{}.log'.format(_id)
            prom_logfile_fp = os.path.join(outdir, prom_logfile_fn)

            with open(logfile_fp, 'w') as f:

                executor = ProMPluginExecutor(
                    basedir=self.configs[BASEDIR],
                    configs_fp=prom_configs_fp,
                    mem=self.configs[MEMORY],
                    prom_jar=self.configs[PROM_JAR],
                    prom_pkg=self.configs[PROM_PKG],
                    plugin_jar=self.configs[PLUGIN_JAR],
                    main_class=self.configs[MAIN_CLASS],
                    prom_logfile=prom_logfile_fp,
                    logfile=f
                )

                executor.execute()

            end = time.time()
            logger.info('[time] Run {} took {} seconds.'.format(_id, end - start))

        logger.info('All done!')


class RecomposingReplayRunner:
    HEADER = ['_id', 'log_path', 'model_path', 'log', 'model', 'monolithic', 'decomposition',
              'recompose_strategy', 'log_creation_strategy', 'prefer_border_trans', 'add_conflict_only_once',
              'use_hide_n_reduce', 'global_duration', 'local_duration', 'log_move_cost', 'model_move_cost',
              'relative_interval', 'absolute_interval', 'max_conflict', 'alignment_percentage',
              'max_iteration', 'cost_interval_lo', 'cost_interval_hi',
              'fitness_lo', 'fitness_hi', 'aligned', 'to_align', 'rejected', 'total_traces',
              'recomposition_steps_taken', 'total_time_taken', 'total_align_time', 'total_generated_states',
              'total_queued_states', 'total_traversed_arcs', 'avg_generated_states', 'avg_queued_states',
              'avg_traversed_arcs']

    def __init__(self, configs):
        self.configs = configs

    def run(self):

        logger.info('Running recomposing replay runner...')

        data_list = list()
        to_run_fp = os.path.join(self.configs[BASEDIR],
                                 self.configs[DATA_TO_RUN])

        with open(to_run_fp, 'r') as f:
            cnt = 0

            for line in f:
                model_dir, model, log = line.split(',')
                model_dir = None if model_dir.strip() == '' else model_dir.strip()
                model = model.strip()
                log = log.strip()

                logger.debug('Model {}: {}, Log {}: {}'.format(cnt, model,
                                                               cnt, log))

                data_list.append((model_dir, model, log))
                cnt += 1

        data_dir = os.path.join(self.configs[BASEDIR], self.configs[DATA_DIR])

        outfile_fp = os.path.join(self.configs[OUTDIR], 'results.csv')
        utils.writeheader(outfile_fp, RecomposingReplayRunner.HEADER)

        for _id, to_run in enumerate(data_list):
            start = time.time()

            model_dir, model, log = to_run
            model_fp = '.'.join([model, self.configs[MODEL_EXT]])
            log_fp = '.'.join([log, self.configs[LOG_EXT]])

            if model_dir is not None:
                model_fp = os.path.join(data_dir, model_dir, model_fp)
                log_fp = os.path.join(data_dir, model_dir, log_fp)
            else:
                model_fp = os.path.join(data_dir, model_fp)
                log_fp = os.path.join(data_dir, log_fp)

            # create the ProM configuration json
            outdir_i = os.path.join(self.configs[OUTDIR], str(_id))
            os.makedirs(outdir_i)

            init_decomp_fp = model + '.' + self.configs['decomposition']
            init_decomp_fp = os.path.join(data_dir, model, 'decomposition', init_decomp_fp)

            prom_configs = {
                'log': log,
                'model': model,
                'modelPath': model_fp,
                'logPath': log_fp,
                'iteration': _id,
                'outFile': outfile_fp,
                'resultDir': self.configs[OUTDIR],

                # replay params
                'globalDuration': self.configs[GLOBAL_DURATION],
                'localDuration': self.configs[LOCAL_DURATION],
                'moveOnLogCosts': self.configs[MOVE_ON_LOG_COSTS],
                'moveOnModelCosts': self.configs[MOVE_ON_MODEL_COSTS],
                'intervalRelative': self.configs[INTERVAL_RELATIVE],
                'intervalAbsolute': self.configs[INTERVAL_ABSOLUTE],
                'maxConflicts': self.configs[MAX_CONFLICTS],
                'alignmentPercentage': self.configs[ALIGNMENT_PERCENTAGE],
                'nofIterations': self.configs[NOF_ITERATIONS],
                'useHideAndReduceAbstraction': self.configs[USE_HIDE_N_REDUCE],
                'decomposition': self.configs[DECOMPOSITION],
                'initDecompFile': init_decomp_fp,
                'recomposeStrategy': self.configs[RECOMPOSE_STRATEGY],
                'logCreationStrategy': self.configs[LOG_CREATION_STRATEGY],
                'preferBorderTransitions': self.configs[PREFER_BORDER_TRANS],
                'addConflictOnlyOnce': self.configs[ADD_CONFLICT_ONLY_ONCE]
            }

            prom_configs_fn = 'configs.json'
            prom_configs_fp = os.path.join(outdir_i, prom_configs_fn)

            with open(prom_configs_fp, 'w') as f:
                json.dump(prom_configs, f)

            # make and run prom executor
            logfile_fn = '{}.log'.format('python')
            logfile_fp = os.path.join(outdir_i, logfile_fn)
            prom_logfile_fn = 'prom.log'
            prom_logfile_fp = os.path.join(outdir_i, prom_logfile_fn)

            with open(logfile_fp, 'w') as f:

                executor = ProMPluginExecutor(
                    basedir=self.configs[BASEDIR],
                    configs_fp=prom_configs_fp,
                    mem=self.configs[MEMORY],
                    prom_jar=self.configs[PROM_JAR],
                    prom_pkg=self.configs[PROM_PKG],
                    plugin_jar=self.configs[PLUGIN_JAR],
                    main_class=self.configs[MAIN_CLASS],
                    prom_logfile=prom_logfile_fp,
                    logfile=f
                )

                executor.execute()

            end = time.time()
            logger.info('[time] Run {} took {} seconds.'.format(_id, end - start))

        logger.info('All done!')


class MonolithicReplayRunner:
    HEADER = ['_id', 'log_path', 'model_path', 'log', 'model', 'monolithic', 'decomposition',
              'recompose_strategy', 'log_creation_strategy', 'prefer_border_trans', 'add_conflict_only_once',
              'use_hide_n_reduce', 'global_duration', 'local_duration', 'log_move_cost', 'model_move_cost',
              'relative_interval', 'absolute_interval', 'max_conflict', 'alignment_percentage',
              'max_iteration', 'cost_interval_lo', 'cost_interval_hi',
              'fitness_lo', 'fitness_hi', 'aligned', 'to_align', 'rejected', 'total_traces',
              'recomposition_steps_taken', 'total_time_taken', 'total_align_time', 'total_generated_states',
              'total_queued_states', 'total_traversed_arcs', 'avg_generated_states', 'avg_queued_states',
              'avg_traversed_arcs']

    def __init__(self, configs):
        self.configs = configs

    def run(self):

        logger.info('Running monolithic replay runner...')

        data_list = list()
        to_run_fp = os.path.join(self.configs[BASEDIR],
                                 self.configs[DATA_TO_RUN])

        with open(to_run_fp, 'r') as f:
            cnt = 0

            for line in f:
                model_dir, model, log = line.split(',')
                model_dir = None if model_dir.strip() == '' else model_dir.strip()
                model = model.strip()
                log = log.strip()

                logger.debug('Model {}: {}, Log {}: {}'.format(cnt, model,
                                                               cnt, log))

                data_list.append((model_dir, model, log))
                cnt += 1

        data_dir = os.path.join(self.configs[BASEDIR], self.configs[DATA_DIR])

        outfile_fp = os.path.join(self.configs[OUTDIR], 'results.csv')
        utils.writeheader(outfile_fp, MonolithicReplayRunner.HEADER)

        for _id, to_run in enumerate(data_list):
            start = time.time()

            model_dir, model, log = to_run
            model_fp = '.'.join([model, self.configs[MODEL_EXT]])
            log_fp = '.'.join([log, self.configs[LOG_EXT]])

            if model_dir is not None:
                model_fp = os.path.join(data_dir, model_dir, model_fp)
                log_fp = os.path.join(data_dir, model_dir, log_fp)
            else:
                model_fp = os.path.join(data_dir, model_fp)
                log_fp = os.path.join(data_dir, log_fp)

            # create the ProM configuration json
            outdir_i = os.path.join(self.configs[OUTDIR], str(_id))
            os.makedirs(outdir_i)

            prom_configs = {
                'log': log,
                'model': model,
                'modelPath': model_fp,
                'logPath': log_fp,
                'iteration': _id,
                'outFile': outfile_fp,
                'resultDir': self.configs[OUTDIR],

                # replay params
                'configuration': self.configs[REPLAY_CONFIG],
                'moveOnLogCosts': self.configs[MOVE_ON_LOG_COSTS],
                'moveOnModelCosts': self.configs[MOVE_ON_MODEL_COSTS],
                'deadline': self.configs[DEADLINE]
            }

            prom_configs_fn = 'configs.json'
            prom_configs_fp = os.path.join(outdir_i, prom_configs_fn)

            with open(prom_configs_fp, 'w') as f:
                json.dump(prom_configs, f)

            # make and run prom executor
            logfile_fn = '{}.log'.format('python')
            logfile_fp = os.path.join(outdir_i, logfile_fn)
            prom_logfile_fn = 'prom.log'
            prom_logfile_fp = os.path.join(outdir_i, prom_logfile_fn)

            with open(logfile_fp, 'w') as f:

                executor = ProMPluginExecutor(
                    basedir=self.configs[BASEDIR],
                    configs_fp=prom_configs_fp,
                    mem=self.configs[MEMORY],
                    prom_jar=self.configs[PROM_JAR],
                    prom_pkg=self.configs[PROM_PKG],
                    plugin_jar=self.configs[PLUGIN_JAR],
                    main_class=self.configs[MAIN_CLASS],
                    prom_logfile=prom_logfile_fp,
                    logfile=f
                )

                executor.execute()

            end = time.time()
            logger.info('[time] Run {} took {} seconds.'.format(_id, end - start))

        logger.info('All done!')
