#!/usr/bin/env python


import os, sys, subprocess


def make_classpath(prom_pkg, prom_jar, plugin_jar):
    # add all libraries in lib by listing directory of prom_pkg
    classpath = '.{pathsep}{prom_jar}{pathsep}{plugin_jar}'.format(
        pathsep=os.pathsep,
        prom_jar=prom_jar,
        plugin_jar=plugin_jar
    )

    for f in os.listdir(prom_pkg):
        if '.jar' in f:
            jar_fp = os.path.join('.', prom_pkg, f)
            classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep,
                                                 jar=jar_fp)

    # add packages jars, might need to go two levels down
    for d in os.listdir('packages'):
        dirpath = os.path.join('.', 'packages', d)

        if not os.path.isdir(dirpath):
            continue

        for f in os.listdir(dirpath):
            dirpath1 = os.path.join(dirpath, f)

            if '.jar' in f:
                jar_fp = os.path.join(dirpath, f)
                classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep,
                                                     jar=jar_fp)

            # this might be another directory containing jars
            elif os.path.isdir(dirpath1):

                for f1 in os.listdir(dirpath1):

                    if '.jar' in f1:
                        jar_fp = os.path.join(dirpath1, f1)
                        classpath += '{pathsep}{jar}'.format(pathsep=os.pathsep,
                                                             jar=jar_fp)

    return classpath


def execute(prom_pkg, prom_jar, plugin_jar, main_class, decomposition_fp, net_fp, out_fp):
    classpath = make_classpath(prom_pkg, prom_jar, plugin_jar)
    command = 'java -classpath {classpath} ' \
              '-Djava.library.path={prom_pkg} ' \
              '-Djava.util.Arrays.useLegacyMergeSort=true ' \
              '-Xmx{memory}G ' \
              '{main_class} {decomposition_fp} {net_fp} {out_fp}'.format(
        classpath=classpath,
        prom_pkg=prom_pkg,
        memory=8,
        main_class=main_class,
        decomposition_fp=decomposition_fp,
        net_fp=net_fp,
        out_fp=out_fp,
        pathsep=os.pathsep
    )

    print('Executing:\n{}'.format(command))

    subprocess.call(command, shell=True)
