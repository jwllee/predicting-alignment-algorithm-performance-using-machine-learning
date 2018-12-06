#!/usr/bin/env python


import os, logging
from alignclf import set_log_clf
from alignclf.utils import setup_logging


logger = logging.getLogger(__name__)


if __name__ == '__main__':
    setup_logging(default_path='')

    datadir = os.path.join('.', 'data', 'synthetic', 'BPI18', 'net5')
    dirty_datadir = os.path.join(datadir, 'dirty')

    clf_type = 'concept-name'
    memory = 6
    prom_dir = os.path.join('.', 'prom-nightly')
    prom_pkg = os.path.join(prom_dir, 'ProM651_lib')
    jar_name = 'set-log-classifier.jar'
    jar_fpath = os.path.join(prom_dir, jar_name)
    main_class = 'org.processmining.experiments.log.SetLogClassifier'

    log_folders = [
        'l1000',
        'l2000',
        'l5000'
    ]

    for folder in log_folders:
        dirpath = os.path.join(dirty_datadir, folder)

        for f in os.listdir(dirpath):
            log_fpath = os.path.join(dirpath, f)
            log_name = f.strip('.xes.gz')
            log_name_modified = log_name + '-' + folder
            log_outpath = os.path.join(datadir, log_name_modified + '.xes.gz')

            logger.info('Modified log name: {}'.format(log_name_modified))
            logger.info('Log outpath: {}'.format(log_outpath))

            set_log_clf.set_log_clf(log_fpath, clf_type, log_outpath, memory,
                                    jar_fpath, main_class, prom_dir, prom_pkg)
