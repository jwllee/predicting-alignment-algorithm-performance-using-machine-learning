#!/usr/bin/env python


import os, sys, logging, json, logging.config, csv


__all__ = [
    'setup_logging'
]


def writeheader(fp, header):
    with open(fp, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(header)


def __get_keypath(key, _dict, level=None, paths=None):
    if level is None:
        level = []

    if paths is None:
        paths = []

    # each path is a list
    if key in _dict:
        path = level + [key]
        paths.append(path)

    # recursively search in children which are dict
    for key0, val in _dict.items():
        if isinstance(val, dict):
            level0 = level + [key0]
            paths = paths + __get_keypath(key, val, level=level0)

    return paths


def append_to_dir(val, path, _dict):
    if len(path) == 1:
        # just update
        key = path[0]
        _dict[key] = val + _dict[key]
    else:
        # go one level deeper
        _dict = _dict[path.pop(0)]
        append_to_dir(val, path, _dict)


def setup_logging(logdir='.',
                  default_path='logging.json',
                  default_level=logging.INFO,
                  env_key='LOG_CFS'):
    path = default_path
    value = os.getenv(env_key, None)

    if value:
        path = value

    print('Logger configuration filepath: {}'.format(path))

    if os.path.exists(path):
        print('Loading logger configurations...')

        with open(path, 'rt') as f:
            config = json.load(f)

        # configure the output directory of handlers
        paths = __get_keypath('filename', config)

        for path in paths:
            # update with logdir
            to_update = logdir + os.sep
            append_to_dir(to_update, path, config)

        logging.config.dictConfig(config)

    else:
        # load the basic configuration
        logging.basicConfig(level=default_level)
