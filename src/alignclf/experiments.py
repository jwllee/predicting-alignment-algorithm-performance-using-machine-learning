#!/usr/bin/env python3


import os, logging, sys, subprocess, json


from alignclf.constants import *


__all__ = [
    'ProMPluginExecutor',
    'RunnerFactory'
]


logger = logging.getLogger(__name__)


class ProMPluginExecutor:

    def __init__(self, basedir, configs_fp, mem,
                 prom_jar, prom_pkg, plugin_jar, main_class, logfile=None):
        self.basedir = basedir
        self.configs_fp = configs_fp
        self.mem = mem
        self.prom_jar = prom_jar
        self.prom_pkg = prom_pkg
        self.plugin_jar = plugin_jar
        self.main_class = main_class
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
                  '{main_class} {configs_fpath}'.format(
            classpath=classpath,
            prom_pkg=self.prom_pkg,
            memory=self.mem,
            main_class=self.main_class,
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

    def build_runner(self, configs):
        if RUNNER_TYPE not in configs:
            logger.error('Runner type not in {}'.format(configs))
            exit(1)

        _map = {
            RunnerFactory.MINIMAL: MinimalRunner
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

            prom_configs_fn = 'configs{}.json'.format(_id)
            prom_configs_fp = os.path.join(outdir, prom_configs_fn)

            with open(prom_configs_fp, 'w') as f:
                json.dump(prom_configs, f)

            # make and run prom executor
            logfile_fn = '{}.log'.format(_id)
            logfile_fp = os.path.join(outdir, logfile_fn)

            with open(logfile_fp, 'w') as f:

                executor = ProMPluginExecutor(
                    basedir=self.configs[BASEDIR],
                    configs_fp=prom_configs_fp,
                    mem=self.configs[MEMORY],
                    prom_jar=self.configs[PROM_JAR],
                    prom_pkg=self.configs[PROM_PKG],
                    plugin_jar=self.configs[PLUGIN_JAR],
                    main_class=self.configs[MAIN_CLASS],
                    logfile=f
                )

                executor.execute()

        logger.info('All done!')
