#!/usr/bin/env python

import click, os


@click.command()
def cli():
    click.echo('Hello world!')


@click.command()
@click.option('--dir', prompt='Script directory',
              help="The script's residing directory")
# @click.option('--name', prompt='Configuration directory name',
#               help='The directory name where the configuration resides.')
@click.option('--prefix', prompt='Prefix directory', default='..',
              help='Directory prefix so that the configuration files can be reached.')
@click.option('--n', prompt='Repeat', default=1,
              help='Number of times to repeat the experiment')
@click.option('--venv', prompt='Virtual environment', default='alignclf-venv/bin/activate',
              help='Activate script of virtual environment')
def make_run_script(dir, prefix, n, venv):
    # get the directory name
    name = dir.split(os.sep)[-1]

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
        'CONFIG={}/configs/{}/configs.json'.format(prefix, name),
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

    if not os.path.isdir(dir):
        os.makedirs(dir)

    script_fp = os.path.join(dir, 'run.sh')

    with open(script_fp, 'w') as f:
        for line in script:
            f.write(line)
            f.write('\n')
