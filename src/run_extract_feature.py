#!/usr/bin/env python


import os, sys, time
import pandas as pd
import numpy as np


from alignclf.feature_extract import *
from alignclf import utils
import podspy.petrinet as petripkg
import podspy.log as logpkg


if __name__ == '__main__':
    for dataset in ['IS2017', 'BPI2018']:
        data_dir = os.path.join('.', 'data', 'net', dataset)

        for net_name in os.listdir(data_dir):
            net_dir = os.path.join(data_dir, net_name)

            net_start = time.time()
            print('Extracting feature from {}'.format(net_dir))

            if not os.path.isdir(net_dir):
                continue

            apn_fp = os.path.join(net_dir, '{}.apnml'.format(net_name))
            assert os.path.isfile(apn_fp), '{} is not a net file'.format(apn_fp)

            pnml = petripkg.import_apnml(apn_fp)
            net, init, finals = petripkg.pnml_to_pn(pnml)
            final = list(finals)[0]
            assert len(finals) == 1, 'has more than 1 final marking'
            utils.check_isinstance(net, petripkg.Petrinet)

            decomposition_dir = os.path.join(net_dir, 'decomposition')
            decomposition_dir_dict = {d:os.path.join(decomposition_dir, d) for d in os.listdir(decomposition_dir)}
            decomposition_dir_dict = {key:val for key, val in decomposition_dir_dict.items() if os.path.isdir(val)}

            decomposition_dict = dict()
            for name, dirpath in decomposition_dir_dict.items():
                apn_array_fp = os.path.join(dirpath, '{}.apna'.format(name))
                with open(apn_array_fp) as f:
                    apn_array = petripkg.import_apna(f)
                decomposition_dict[name] = apn_array

            feature_df_list = list()
            # lognames = filter(lambda fname: fname.endswith('.xes.gz'), os.listdir(net_dir))
            if dataset == 'BPI2018':
                log_fnames = filter(lambda fname: fname.endswith('l5000.xes.gz'), os.listdir(net_dir))
            else:
                log_fnames = filter(lambda fname: fname.endswith('.xes'), os.listdir(net_dir))

            for log_fname in list(log_fnames):
                log_start = time.time()
                print('Extracting feature from log {}'.format(log_fname))

                log_fp = os.path.join(net_dir, log_fname)
                logtable = logpkg.import_log_table(log_fp)
                logtable.event_df.rename(columns={'concept:name': logpkg.ACTIVITY}, inplace=True)

                # event_df = logtable.event_df
                # caseids = event_df[logpkg.CASEID].unique()[:10]
                # logtable.event_df = event_df[(event_df[logpkg.CASEID]).isin(caseids)]

                # print(logtable.event_df.head())

                log_name = log_fname.split('.')[0]
                feature_df_i = extract_feature_df(log_name, logtable, net, init, final, decomposition_dict)
                feature_df_list.append(feature_df_i)

                log_end = time.time()
                print('Took {:.2f}s to process {}'.format(log_end - log_start, log_fname))

            feature_df = pd.concat(feature_df_list, axis=0)
            utils.check_isinstance(feature_df, pd.DataFrame)

            # print(feature_df.head())

            out_fp = os.path.join(net_dir, '{}-feature.csv'.format(net_name))
            feature_df.to_csv(out_fp, index=False)

            net_end = time.time()
            print('Took {:.2f}s to process {}'.format(net_end - net_start, net_name))
