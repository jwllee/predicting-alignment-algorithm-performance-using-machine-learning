#!/usr/bin/env python


import argparse, time, os, logging, subprocess

from alignclf.utils import setup_logging


logger = logging.getLogger(__name__)


def make_classpath(prom_pkg, jar_fpath, base_dir='.'):
    classpath = '{base_dir}{pathsep}{jar_fpath}'.format(
        base_dir=base_dir,
        pathsep=os.pathsep,
        jar_fpath=jar_fpath
    )

    for f in os.listdir(prom_pkg):
        if f.endswith('.jar'):
            jar_fp = os.path.join(base_dir, prom_pkg, f)
            classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep, jar=jar_fp)

    pkg_dir = os.path.join(base_dir, 'packages')
    for d in os.listdir(pkg_dir):
        dirpath = os.path.join(pkg_dir, d)

        if not os.path.isdir(dirpath):
            continue

        for f in os.listdir(dirpath):
            dirpath1 = os.path.join(dirpath, f)

            if f.endswith('.jar'):
                jar_fp = os.path.join(dirpath, f)
                classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep, jar=jar_fp)

            elif os.path.isdir(dirpath1):
                for f1 in os.listdir(dirpath1):
                    if f1.endswith('.jar'):
                        jar_fp = os.path.join(dirpath1, f1)
                        classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep, jar=jar_fp)

    return classpath




if __name__ == '__main__':
    setup_logging(default_path='')

    parser = argparse.ArgumentParser()

    parser.add_argument('-l', action='store',
                        dest='log_fpath',
                        help='File path of event log')

    parser.add_argument('-c', action='store',
                        dest='clf_type',
                        help='Log classifier type')

    parser.add_argument('-o', action='store',
                        dest='log_outpath',
                        help='Destination file path of modified event log')

    parser.add_argument('-j', action='store', nargs='?',
                        dest='jar_name', default='set-log-classifier.jar',
                        help='Jar file name for setting log classifier')

    parser.add_argument('-m', action='store', nargs='?',
                        dest='main_class', default='org.processmining.experiments.log.SetLogClassifier',
                        help='Main class name of log classifier setter')

    args = parser.parse_args()

    err_cond = [
        args.log_outpath is None,
        args.clf_type is None,
        args.log_outpath is None
    ]
    if any(err_cond):
        print('Run as python ./set_log_clf.py -l [log_fpath] -c [clf_type] -o [log_outpath]')
        exit(0)

    memory = 6
    prom_dir = os.path.join('.', 'prom-nightly')
    jar_fpath = os.path.join(prom_dir, args.jar_name)
    prom_pkg = os.path.join(prom_dir, 'ProM651_lib')
    classpath = make_classpath(prom_pkg, jar_fpath, prom_dir)

    logger.info('Executing...')

    command = 'java -classpath {classpath} ' \
              '-Djava.util.Arrays.useLegacyMergeSort=true ' \
              '-Xmx{memory}G ' \
              '{main_class} {log_fpath} {clf_type} {log_outpath}'.format(
        classpath=classpath,
        memory=memory,
        main_class=args.main_class,
        log_fpath = args.log_fpath,
        clf_type=args.clf_type,
        log_outpath=args.log_outpath
    )

    logger.info('Calling {}'.format(command))

    subprocess.call(command, shell=True)
