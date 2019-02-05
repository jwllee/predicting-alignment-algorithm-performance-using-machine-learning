#!/usr/bin/env bash

OUTDIR=./paper/img

RESULTDIR_DUMMY=ml-results/ICPM-results/2019-02-05_08-49-46-221271_dummy
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_DUMMY}/gridsearch.sav -k false -o ${RESULTDIR_DUMMY}
inkscape -D -z --file=${RESULTDIR_DUMMY}/cnf-mat.svg --export-pdf=${RESULTDIR_DUMMY}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DUMMY}/cnf-mat.svg --export-pdf=${OUTDIR}/dummy-cnf-mat.pdf --export-latex

RESULTDIR_DUMMY_UNIQ=ml-results/ICPM-results/2019-02-05_08-51-14-659764_dummy
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_DUMMY_UNIQ}/gridsearch.sav -k false -o ${RESULTDIR_DUMMY_UNIQ}
inkscape -D -z --file=${RESULTDIR_DUMMY_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_DUMMY_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DUMMY_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/dummy-uniq-cnf-mat.pdf --export-latex

RESULTDIR_DUMMY_FILTER=ml-results/ICPM-results/2019-02-05_08-52-04-717260_dummy
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_DUMMY_FILTER}/gridsearch.sav -k true -o ${RESULTDIR_DUMMY_FILTER}
inkscape -D -z --file=${RESULTDIR_DUMMY_FILTER}/cnf-mat.svg --export-pdf=${RESULTDIR_DUMMY_FILTER}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DUMMY_FILTER}/cnf-mat.svg --export-pdf=${OUTDIR}/dummy-filter-cnf-mat.pdf --export-latex

RESULTDIR_DUMMY_FILTER_UNIQ=ml-results/ICPM-results/2019-02-05_08-51-41-690655_dummy
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_DUMMY_FILTER_UNIQ}/gridsearch.sav -k true -o ${RESULTDIR_DUMMY_FILTER_UNIQ}
inkscape -D -z --file=${RESULTDIR_DUMMY_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_DUMMY_FILTER_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DUMMY_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/dummy-filter-uniq-cnf-mat.pdf --export-latex

# decision tree
RESULTDIR_DT=ml-results/ICPM-results/2019-02-04_16-34-05-212336_tree
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_DT}/gridsearch.sav -k false -o ${RESULTDIR_DT}
inkscape -D -z --file=${RESULTDIR_DT}/cnf-mat.svg --export-pdf=${RESULTDIR_DT}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DT}/cnf-mat.svg --export-pdf=${OUTDIR}/dt-cnf-mat.pdf --export-latex
 
RESULTDIR_DT_UNIQ=ml-results/ICPM-results/2019-02-05_08-49-14-625083_tree
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_DT_UNIQ}/gridsearch.sav -k false -o ${RESULTDIR_DT_UNIQ}
inkscape -D -z --file=${RESULTDIR_DT_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_DT_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DT_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/dt-uniq-cnf-mat.pdf --export-latex
 
RESULTDIR_DT_FILTER=ml-results/ICPM-results/2019-02-05_10-22-10-795220_tree
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_DT_FILTER}/gridsearch.sav -k true -o ${RESULTDIR_DT_FILTER}
inkscape -D -z --file=${RESULTDIR_DT_FILTER}/cnf-mat.svg --export-pdf=${RESULTDIR_DT_FILTER}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DT_FILTER}/cnf-mat.svg --export-pdf=${OUTDIR}/dt-filter-cnf-mat.pdf --export-latex
 
RESULTDIR_DT_FILTER_UNIQ=ml-results/ICPM-results/2019-02-04_17-36-10-314906_tree
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_DT_FILTER_UNIQ}/gridsearch.sav -k true -o ${RESULTDIR_DT_FILTER_UNIQ}
inkscape -D -z --file=${RESULTDIR_DT_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_DT_FILTER_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_DT_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/dt-filter-uniq-cnf-mat.pdf --export-latex
 
 
# random forest
RESULTDIR_RF=ml-results/ICPM-results/2019-02-04_17-38-47-430856_random-forest
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_RF}/forest.sav -k false -o ${RESULTDIR_RF}
inkscape -D -z --file=${RESULTDIR_RF}/cnf-mat.svg --export-pdf=${RESULTDIR_RF}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_RF}/cnf-mat.svg --export-pdf=${OUTDIR}/rf-cnf-mat.pdf --export-latex
 
RESULTDIR_RF_UNIQ=ml-results/ICPM-results/2019-02-05_10-33-03-398044_random-forest
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_RF_UNIQ}/forest.sav -k false -o ${RESULTDIR_RF_UNIQ}
inkscape -D -z --file=${RESULTDIR_RF_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_RF_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_RF_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/rf-uniq-cnf-mat.pdf --export-latex
 
RESULTDIR_RF_FILTER=ml-results/ICPM-results/2019-02-04_17-40-52-125116_random-forest
python src/run_confusion_mat.py -fp prediction-data/final-data.csv -c ${RESULTDIR_RF_FILTER}/forest.sav -k true -o ${RESULTDIR_RF_FILTER}
inkscape -D -z --file=${RESULTDIR_RF_FILTER}/cnf-mat.svg --export-pdf=${RESULTDIR_RF_FILTER}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_RF_FILTER}/cnf-mat.svg --export-pdf=${OUTDIR}/rf-filter-cnf-mat.pdf --export-latex

RESULTDIR_RF_FILTER_UNIQ=ml-results/ICPM-results/2019-02-04_17-47-38-089238_random-forest
python src/run_confusion_mat.py -fp prediction-data/uniq-final-data.csv -c ${RESULTDIR_RF_FILTER_UNIQ}/forest.sav -k true -o ${RESULTDIR_RF_FILTER_UNIQ}
inkscape -D -z --file=${RESULTDIR_RF_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${RESULTDIR_RF_FILTER_UNIQ}/cnf-mat.pdf --export-latex
inkscape -D -z --file=${RESULTDIR_RF_FILTER_UNIQ}/cnf-mat.svg --export-pdf=${OUTDIR}/rf-filter-uniq-cnf-mat.pdf --export-latex
