#!/usr/bin/env python


from glob import glob
from os.path import splitext, basename
from setuptools import find_packages, setup


setup(
    name='alignclf',
    version='0.1.0',
    description='Experiment setup for alignment algorithm classification',
    author='Wai Lam Jonathan Lee',
    author_email='walee@uc.cl',
    install_requires=['numpy', 'pandas', 'click', 'pygraphviz'],
    test_requires=['pytest'],
    packages=find_packages('src'),
    package_dir={'': 'src'},
    py_module=[splitext(basename(path))[0] for path in glob('src/*.py')],
    entry_points='''
        [console_scripts]
        make-configs=alignclf.configuration:make_configs
    '''
)