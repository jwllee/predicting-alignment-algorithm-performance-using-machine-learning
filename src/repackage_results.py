#!/usr/bin/env python


import os, sys, json, shutil, time
import pandas as pd
import numpy as np


def parse_alignment_dir(in_dirpath):
    alignment_dict = dict()

    for f in os.listdir(in_dirpath):
        fpath = os.path.join(in_dirpath, f)

        if not os.path.isfile(fpath):
            print('Skipping non-alignment file: {}'.format(fpath))
            continue

        with open(fpath) as f:
            lines = f.readlines()

            try:
                exitcode = float(lines[1])
                repr_cid = lines[3].strip()
                caseids = lines[5].split(',')
                caseids = list(map(lambda cid: cid.strip(), caseids))

                # check if it is an empty alignment
                if len(lines) > 7:
                    alignment_i = lines[7:]
                    alignment_i = list(map(lambda t: t.strip().split(','), alignment_i))
                else:
                    alignment_i = []

                alignment_dict_i = {
                    'exitcode': exitcode,
                    'representative_caseid': repr_cid,
                    'case_id': caseids,
                    'alignment': alignment_i
                }

                if repr_cid in alignment_dict:
                    raise ValueError('Representative caseid {} has > 1 alignments'.format(repr_cid))

                alignment_dict[repr_cid] = alignment_dict_i
            except Exception as e:
                print('Error with parsing alignment {} in {}: {}'.format(f, in_dirpath, e))

    return alignment_dict


def make_mono_dirname(orig_dirname, configs_dict):
    mono_ind = orig_dirname.find('mono')

    assert mono_ind > -1, 'Cannot find mono in dirname {}'.format(orig_dirname)

    datetime_part = orig_dirname[:mono_ind]
    dataset_name = 'BPI2018'
    model_name = configs_dict['model']
    ilp = 'ILP' if configs_dict['useInt'] else 'nILP'
    algo_name = configs_dict['algorithmType']

    name_part = '-'.join([
        dataset_name,
        model_name,
        ilp,
        algo_name,
        'mono'
    ])

    new_dirname = datetime_part + name_part

    return new_dirname


def repackage_mono_results(new_base_dir):
    base_dir = os.path.join('/', 'home', 'jonathan', 'github-repos', '2018', '2018-09-26_align-algo-clf',
                            'results-agg', 'clst-net1-repeat', 'net1-inc3-repeat')

    if not os.path.exists(base_dir):
        raise ValueError('Monolithic replay directory does not exist: {}'.format(base_dir))

    mono_dirname = [dirname for dirname in os.listdir(base_dir) if 'mono' in dirname]

    total_dirs = len(mono_dirname)
    cnt = 0

    for dirname in mono_dirname:
        print('Repackaging directory {}/{}: {}'.format(cnt + 1, total_dirs, dirname))

        # get a random configs dict
        replay_0 = os.path.join(base_dir, dirname, '0')
        replay_0_configs_fpath = os.path.join(replay_0, 'configs.json')
        with open(replay_0_configs_fpath) as f:
            configs_dict = json.load(f)

        old_dirpath = os.path.join(base_dir, dirname)
        new_dirname = make_mono_dirname(dirname, configs_dict)
        new_dirpath = os.path.join(new_base_dir, new_dirname)

        if not os.path.exists(new_dirpath):
            os.mkdir(new_dirpath)

        for f in os.listdir(old_dirpath):
            fpath = os.path.join(old_dirpath, f)

            if os.path.isfile(fpath):
                # copy to new directory
                new_fpath = os.path.join(new_dirpath, f)
                shutil.copyfile(fpath, new_fpath)

            else: # is directory
                old_subdirpath = fpath
                new_subdirpath = os.path.join(new_dirpath, f)
                os.mkdir(new_subdirpath)

                for ff in os.listdir(old_subdirpath):
                    ffpath = os.path.join(old_subdirpath, ff)

                    if os.path.isfile(ffpath):
                        new_ffpath = os.path.join(new_subdirpath, ff)
                        shutil.copyfile(ffpath, new_ffpath)
                    else: # should be the alignment directory
                        if ff != 'alignment':
                            raise ValueError('{} should be the alignment directory'.format(ff))

                        alignment_list = parse_alignment_dir(ffpath)
                        alignment_fp = os.path.join(new_subdirpath, 'alignments.json')

                        with open(alignment_fp, 'w') as f:
                            json.dump(alignment_list, f, indent=4)

        # if cnt > 2:
        #     break

        cnt += 1


def repackage_replay_iter_directory(dirname, old_base_dirpath, new_base_dirpath):
    old_dirpath = os.path.join(old_base_dirpath, dirname)
    new_dirpath = os.path.join(new_base_dirpath, dirname)

    if not os.path.exists(new_dirpath):
        os.mkdir(new_dirpath)

    # copy all the files first
    for f in os.listdir(old_dirpath):
        fpath = os.path.join(old_dirpath, f)

        if not os.path.isfile(fpath):
            continue

        new_fpath = os.path.join(new_dirpath, f)
        shutil.copyfile(fpath, new_fpath)

    # repackage alignments directory
    alignment_old_dirpath = os.path.join(old_dirpath, 'alignments')
    alignment_new_dirpath = os.path.join(new_dirpath, 'alignments')

    if not os.path.isdir(alignment_old_dirpath):
        raise ValueError('No alignment directory in {}'.format(old_dirpath))

    if not os.path.exists(alignment_new_dirpath):
        os.mkdir(alignment_new_dirpath)

    for d in os.listdir(alignment_old_dirpath):
        dirpath = os.path.join(alignment_old_dirpath, d)

        if os.path.isfile(dirpath):
            raise ValueError('Should not have file {} in directory {}'.format(d, alignment_old_dirpath))

        sub_ind = d.replace('subalign-', '')
        subalignment_dict = parse_alignment_dir(dirpath)
        subalignment_fp = os.path.join(alignment_new_dirpath, 'subalignment-{}.json'.format(sub_ind))

        with open(subalignment_fp, 'w') as f:
            json.dump(subalignment_dict, f, indent=4)

    # repackage subnets and filtered subnets and stats directory
    filtered_old_dirpath = os.path.join(old_dirpath, 'filtered-subnets')
    subnets_old_dirpath = os.path.join(old_dirpath, 'replay-subnets')
    stats_old_dirpath = os.path.join(old_dirpath, 'stats')

    if not os.path.isdir(filtered_old_dirpath):
        raise ValueError('No filtered subnets directory in {}'.format(filtered_old_dirpath))
    if not os.path.isdir(subnets_old_dirpath):
        raise ValueError('No replay subnets directory in {}'.format(subnets_old_dirpath))
    if not os.path.isdir(stats_old_dirpath):
        raise ValueError('No stats directory in {}'.format(stats_old_dirpath))

    filtered_new_dirpath = os.path.join(new_dirpath, 'filtered-subnets')
    subnets_new_dirpath = os.path.join(new_dirpath, 'replay-subnets')
    stats_new_dirpath = os.path.join(new_dirpath, 'stats')

    to_proc = {
        filtered_old_dirpath: filtered_new_dirpath,
        subnets_old_dirpath: subnets_new_dirpath,
        stats_old_dirpath: stats_new_dirpath
    }

    for old_subdirpath, new_subdirpath in to_proc.items():
        if not os.path.exists(new_subdirpath):
            os.mkdir(new_subdirpath)

        for f in os.listdir(old_subdirpath):
            # copy to new directory
            fpath = os.path.join(old_subdirpath, f)

            if not os.path.isfile(fpath):
                raise ValueError('There should not have directory {} in {}'.format(f, old_subdirpath))

            new_fpath = os.path.join(new_subdirpath, f)
            shutil.copyfile(fpath, new_fpath)


def verify_reco_results():
    base_dir = os.path.join('/', 'home', 'jonathan', 'github-repos', '2018', '2018-09-26_align-algo-clf',
                            'results-agg', 'clst-net1-repeat', 'net1-astar-repeat')

    if not os.path.exists(base_dir):
        raise ValueError('Recomposing replay directory does not exists: {}'.format(base_dir))

    reco_dirname = [dirname for dirname in os.listdir(base_dir) if 'mono' not in dirname]

    total_dirs = len(reco_dirname)
    cnt = 0

    for dirname in reco_dirname:
        print('Verifying directory {}/{}: {}'.format(cnt + 1, total_dirs, dirname))

        old_dirpath = os.path.join(base_dir, dirname)
        dir_content = os.listdir(old_dirpath)

        # check replay experiments
        for i in range(16):
            if str(i) not in dir_content:
                print('{} does not have replay experiment {}'.format(dirname, i))

        if 'errors.log' not in dir_content:
            print('{} does not have error.log'.format(dirname))

        if 'info.log' not in dir_content:
            print('{} does not have info.log'.format(dirname))

        if 'log-stats.csv' not in dir_content:
            print('{} does not have log-stats.csv'.format(dirname))

        if 'results.csv' not in dir_content:
            print('{} does not have results.csv'.format(dirname))

        for f in dir_content:
            fpath = os.path.join(old_dirpath, f)

            if os.path.isfile(fpath):
                continue

            else: # is directory
                old_subdirpath = fpath
                dir_content = os.listdir(old_subdirpath)

                if 'configs.json' not in dir_content:
                    print('{} does not have configs.json'.format(old_subdirpath))

                if 'prom-iter-stats.csv' not in dir_content:
                    print('{} does not have prom-iter-stats.csv'.format(old_subdirpath))

                if 'prom.log' not in dir_content:
                    print('{} does not have prom.log'.format(old_subdirpath))

                if 'python.log' not in dir_content:
                    print('{} does not have python.log'.format(old_subdirpath))

                if 'rejected.csv' not in dir_content:
                    print('{} does not have rejected.csv'.format(old_subdirpath))

                if 'to-align.csv' not in dir_content:
                    print('{} does not have to-align.csv'.format(old_subdirpath))

                if 'trace-stats.csv' not in dir_content:
                    print('{} does not have trace-stats.csv'.format(old_subdirpath))

                if 'valid.csv' not in dir_content:
                    print('{} does not have valid.csv'.format(old_subdirpath))

                for ff in os.listdir(old_subdirpath):
                    ffpath = os.path.join(old_subdirpath, ff)

                    if os.path.isfile(ffpath):
                        continue
                    elif ff.endswith('alignments'):
                        # merged alignment folder
                        continue
                    else: # should be decomposed replay iteration folder
                        dir_content_1 = os.listdir(ffpath)
                        if 'alignments' not in dir_content_1:
                            print('{} does not have alignments'.format(ff))

                        if 'filtered-subnets' not in dir_content_1:
                            print('{} does not have filtered-subnets'.format(ff))

                        if 'replay-subnets' not in dir_content_1:
                            print('{} does not have replay-subnets'.format(ff))

                        if 'stats' not in dir_content_1:
                            print('{} does not have stats'.format(ff))

                        if 'iter-stats.csv' not in dir_content_1:
                            print('{} does not have iter-stats.csv'.format(ff))

                        if 'sub-iter-stats.csv' not in dir_content_1:
                            print('{} does not have sub-iter-stats.csv'.format(ff))

        cnt += 1


def repackage_reco_results(new_base_dir):
    base_dir = os.path.join('/', 'home', 'jonathan', 'github-repos', '2018', '2018-09-26_align-algo-clf',
                            'results-agg', 'clst-net1-repeat', 'net1-inc3-repeat')

    if not os.path.exists(base_dir):
        raise ValueError('Recomposing replay result directory does not exists: {}'.format(base_dir))

    reco_dirname = list()

    for dirname in os.listdir(base_dir):
        dirpath = os.path.join(base_dir, dirname)

        if not os.path.isdir(dirpath) or 'mono' in dirname:
            continue

        reco_dirname.append(dirname)

    total_dirs = len(reco_dirname)
    cnt = 0

    for dirname in reco_dirname:
        print('Repackaging directory {}/{}: {}'.format(cnt + 1, total_dirs, dirname))
        start = time.time()

        old_dirpath = os.path.join(base_dir, dirname)
        new_dirpath = os.path.join(new_base_dir, dirname)

        if not os.path.exists(new_dirpath):
            os.mkdir(new_dirpath)
        else:
            cnt += 1
            continue

        for f in os.listdir(old_dirpath):
            fpath = os.path.join(old_dirpath, f)

            if os.path.isfile(fpath):
                # copy to new directory
                new_fpath = os.path.join(new_dirpath, f)
                shutil.copyfile(fpath, new_fpath)

            else: # is directory
                old_subdirpath = fpath
                new_subdirpath = os.path.join(new_dirpath, f)
                os.mkdir(new_subdirpath)

                for ff in os.listdir(old_subdirpath):
                    ffpath = os.path.join(old_subdirpath, ff)

                    if os.path.isfile(ffpath):
                        new_ffpath = os.path.join(new_subdirpath, ff)
                        shutil.copyfile(ffpath, new_ffpath)
                    elif ff.endswith('alignments'):
                        # merged alignment folder
                        alignment_dict = parse_alignment_dir(ffpath)
                        alignment_fp = os.path.join(new_subdirpath, 'alignments.json')

                        with open(alignment_fp, 'w') as f:
                            json.dump(alignment_dict, f, indent=4)
                    else: # should be decomposed replay iteration folder
                        repackage_replay_iter_directory(ff, old_subdirpath, new_subdirpath)

        # if cnt > 2:
        #     break

        end = time.time()
        taken = end - start
        print('Took {:.2f}s'.format(taken))

        cnt += 1


if __name__ == '__main__':
    new_base_dir = os.path.join('/', 'home', 'jonathan', 'github-repos', '2018',
                                '2018-09-26_align-algo-clf', 'results-agg',
                                'clst-net1-repeat', 'net1-inc3-repeat-mono')

    if not os.path.exists(new_base_dir):
        os.mkdir(new_base_dir)

    repackage_mono_results(new_base_dir)
    # repackage_reco_results(new_base_dir)
    # verify_reco_results()