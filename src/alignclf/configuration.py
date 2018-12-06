#!/usr/bin/env python

import click, os, json
from shutil import copyfile
import itertools as itls


from alignclf.constants import *


def write_run_script(dir, venv, prefix, basename, name, n):

    script = [
        '#!/usr/bin/env bash',
        '',
        '# THIS FILE IS GENERATED AUTOMATICALLY',
        '',
        'VENV={}'.format(venv),
        '. ${VENV}',
        '',
        'which python',
        '',
        'cd prom-nightly',
        '',
        'PYTHON=python',
        'RUN={}/src/run.py'.format(prefix),
        'CONFIG={}/configs/{}/{}/configs.json'.format(prefix, basename, name),
        'LOGGINGCONFIGS={}/src/logging.json'.format(prefix),
        'TIME_FILE="{}/times.txt"'.format(prefix),
        'N={}'.format(n),
        '',
        'for i in $(seq 1 $N) \ndo',
        '   echo "Iteration $i"',
        '   {}/scripts/time -a -o ${{TIME_FILE}} -f "Command: %C\\n[mm:ss.ms]: %E\\n" '
        '$PYTHON $RUN -c "${{CONFIG}}" -l "${{LOGGINGCONFIGS}}"'.format(prefix),
        '   sleep 5',
        'done',
        '',
        'echo "All done!"'
    ]

    script_fp = os.path.join(dir, 'run.sh')

    with open(script_fp, 'w') as f:
        for line in script:
            f.write(line)
            f.write('\n')

    return script_fp


def make_batch_script(dir, files):
    script_fp = os.path.join(dir, 'batch.sh')

    script = [
        '#!/usr/bin/env bash',
        '',
        '# THIS FILE WAS GENERATED AUTOMATICALLY.',
        ''
    ]

    for line in files:
        script.append('sh "{}"'.format(line))

    with open(script_fp, 'w') as f:
        for line in script:
            f.write(line)
            f.write('\n')


@click.command()
@click.option('--dir', prompt='Configuration directory',
              help='Configuration directory to put the configurations')
@click.option('--data_dir', prompt='Data directory',
              help='Directory where data is')
@click.option('--data', prompt='Data file',
              help='File containing data to run')
@click.option('--name', prompt='Configuration name',
              help='Unique name to identify configuration files')
@click.option('--basic', prompt='Basic configuration',
              help='Basic configuration json file')
@click.option('--monolithic', prompt='Monolithic configuration',
              help='Monolithic configuration json file')
@click.option('--recompose', prompt='Recomposing configuration',
              help='Recomposing configuration json file')
@click.option('--prefix', prompt='Prefix directory', default='..',
              help='Directory prefix so that the configuration files can be reached.')
@click.option('--n', prompt='Repeat', default=1,
              help='Number of times to repeat the experiment')
@click.option('--venv', prompt='Virtual environment', default='alignclf-venv/bin/activate',
              help='Activate script of virtual environment')
def make_configs(dir, data_dir, data, name, basic, monolithic, recompose, prefix, n, venv):
    # create the unique configuration directory
    base_dir = os.path.join(dir, name)
    DATA_FILE = 'data.txt'
    CONFIGS_FILE = 'configs.json'

    if os.path.isdir(base_dir):
        click.echo('{} already exists! Configuration directory has to be unique.'.format(base_dir))
        return
    else:
        os.makedirs(base_dir)

    with open(basic, 'r') as f:
        basic_dict = json.load(f)

    with open(monolithic, 'r') as f:
        monolithic_dict = json.load(f)

    with open(recompose, 'r') as f:
        recompose_dict = json.load(f)

    run_scripts = []

    # add the basic configurations to monolithic and recomposing
    monolithic_dict.update(basic_dict)
    monolithic_dict[DATA_DIR] = data_dir
    recompose_dict.update(basic_dict)
    recompose_dict[DATA_DIR] = data_dir

    # make monolithic configuration
    algorithms = monolithic_dict[ALGORITHM_TYPE]

    del monolithic_dict[ALGORITHM_TYPE]

    for algo in algorithms:
        dirname = 'mono-{}'.format(algo)
        monolithic_dir = os.path.join(base_dir, dirname)
        os.makedirs(monolithic_dir)

        monolithic_dict[EXPERIMENT_NAME] = 'mono-{}'.format(algo)
        monolithic_dict[ALGORITHM_TYPE] = algo

        # copy the data.txt
        monolithic_data = os.path.join(monolithic_dir, DATA_FILE)
        copyfile(data, monolithic_data)
        monolithic_dict[DATA_TO_RUN] = monolithic_data

        monolithic_configs = os.path.join(monolithic_dir, CONFIGS_FILE)
        with open(monolithic_configs, 'w') as f:
            json.dump(monolithic_dict, f, indent=4, sort_keys=True)

        run_scripts.append(write_run_script(monolithic_dir, venv, prefix, name, dirname, n))

    # make recomposing configurations
    algorithms = recompose_dict[ALGORITHM_TYPE]
    decompositions = recompose_dict[DECOMPOSITION]
    merge_strategy = recompose_dict[RECOMPOSE_STRATEGY]
    log_strategy = recompose_dict[LOG_CREATION_STRATEGY]

    rename = {
        'Score-based': 'score',
        'Top ten most frequent conflicts set': 'mfcs_10',
        'Most frequent conflicts': 'mfc',

        'Include all': 'all',
        'Strict exclude by conflicts': 'strict',
        'Group by conflicts': 'group'
    }

    del recompose_dict[ALGORITHM_TYPE]
    del recompose_dict[DECOMPOSITION]
    del recompose_dict[RECOMPOSE_STRATEGY]
    del recompose_dict[LOG_CREATION_STRATEGY]

    combos = itls.product(algorithms, decompositions, merge_strategy, log_strategy)

    for algorithm, decomposition, merge, log in combos:
        d = rename[decomposition] if decomposition in rename else decomposition
        m = rename[merge] if merge in rename else merge
        l = rename[log] if log in rename else log

        dirname = '{}-{}-{}-{}'.format(algorithm, d, m, l)
        recompose_dir = os.path.join(base_dir, dirname)
        os.makedirs(recompose_dir)

        recompose_data = os.path.join(recompose_dir, DATA_FILE)
        copyfile(data, recompose_data)
        recompose_dict[DATA_TO_RUN] = recompose_data

        recompose_dict[ALGORITHM_TYPE] = algorithm
        recompose_dict[EXPERIMENT_NAME] = '{}-{}'.format(name, dirname)
        recompose_dict[DECOMPOSITION] = decomposition
        recompose_dict[RECOMPOSE_STRATEGY] = merge
        recompose_dict[LOG_CREATION_STRATEGY] = log

        recompose_configs = os.path.join(recompose_dir, CONFIGS_FILE)
        with open(recompose_configs, 'w') as f:
            json.dump(recompose_dict, f, indent=4, sort_keys=True)

        run_scripts.append(write_run_script(recompose_dir, venv, prefix, name, dirname, n))

    # make batch script
    make_batch_script(base_dir, run_scripts)
