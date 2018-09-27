#!/usr/bin/env python3


import os, logging, sys, subprocess


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
                  '-Xmx{memory}G -XX:MaxPermSize=256m ' \
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
