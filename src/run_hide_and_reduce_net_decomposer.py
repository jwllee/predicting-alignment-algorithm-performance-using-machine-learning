#!/usr/bin/env python


from alignclf import hide_and_reduce_net_decomposer as decomposer
import os, sys


if __name__ == '__main__':
    data_dir = os.path.join(
        '..', 'data', 'net', 'BPI2018'
    )
    prom_pkg = 'ProM651_lib'
    plugin_jar = 'hide-and-reduce-net-decomposer.jar'
    prom_jar = 'ProM651.jar'
    main_class = 'org.processmining.alignclf.HideAndReduceNetDecomposerBoot'

    net_dir = os.path.join(data_dir, 'net1')
    out_dir = os.path.join(net_dir, 'decomposition', 'sese_25')

    if not os.path.exists(out_dir):
        os.mkdir(out_dir)

    decomposition_fp =  os.path.join(net_dir, 'decomposition', 'net1.sese_25')
    net_fp = os.path.join(net_dir, 'net1.apnml')
    out_fp = os.path.join(out_dir, 'sese_25.apna')

    decomposer.execute(
        prom_pkg,
        prom_jar,
        plugin_jar,
        main_class,
        decomposition_fp,
        net_fp,
        out_fp
    )

    print('Finished decomposition!')


