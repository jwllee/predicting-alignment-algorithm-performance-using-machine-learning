#!/usr/bin/env python

import click, os, json, argparse
from shutil import copyfile
import itertools as itls


from alignclf.constants import *


def make_pbs_script(job_name, wd, shell_fpath, job_configs, outpath):
    script = [
        '#PBS -N {}'.format(job_name),
        '#PBS -q {}'.format(job_configs['queue_name']),
        '#PBS -l {}'.format(job_configs['config']),
        '#PBS -l walltime={}'.format(job_configs['walltime']),
    ]

    if 'module' in job_configs:
        # add modules
        script.append('')
        for module in job_configs['module']:
            script.append('module load {}'.format(module))

    script.append('')
    script.append('cd {}'.format(wd))
    script.append('sh {}'.format(shell_fpath))

    with open(outpath, 'w') as f:
        for line in script:
            f.write(line)
            f.write('\n')


def make_pbs_scripts(wd, job_config_fpath, config_dir):
    pbs_scripts = []

    for d in os.listdir(config_dir):
        dirpath = os.path.join(config_dir, d)

        if not os.path.isdir(dirpath):
            continue

        shell_script = os.path.join(dirpath, 'batch.sh')
        job_name = d
        outpath = os.path.join(dirpath, 'job.pbs')
        pbs_scripts.append(outpath)

        with open(job_config_fpath, 'r') as f:
            job_config = json.load(f)
            make_pbs_script(job_name, wd, shell_script, job_config, outpath)

    for path in pbs_scripts:
        print(path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--wd', action='store',
                        dest='wd',
                        help='Working directory of PBS job')

    parser.add_argument('--job_config', action='store',
                        dest='job_config',
                        help='Configuration of PBS job')

    parser.add_argument('--config_dir', action='store',
                        dest='config_dir',
                        help='Configuration directory')

    args = parser.parse_args()

    err_cond = [
        args.wd is None,
        args.job_config is None,
        args.config_dir is None
    ]

    if any(err_cond):
        print('Run as python ./make-pbs.py --wd [working-directory] --job_config [job-config] --config_dir [config-dir]')
        exit(0)

    make_pbs_scripts(args.wd, args.job_config, args.config_dir)
