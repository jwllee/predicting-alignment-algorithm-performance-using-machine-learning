# Predicting Alignment Algorithm Performance Using Machine Learning
Repository for code of the paper. Instruction on replicating the experiments is included.

## Things included:
- ProM jars of CLASSIC, CLASSIC-SP, RECOMPOSE, RECOMPOSE-SP algorithms
- Experimental setup for running experiments using the algorithms
- Experimental setup for training predictive algorithms
- Jupyter notebooks for the statistics and images of the paper

*Note:* unfortunately due to the file size limit on GitHub repository, the data and trained predictive model cannot be included.

## Settings for alignment algorithms
### General settings
- move_on_log_costs: 10
- move_on_model_costs: 4
- move_sort: false
- prefer_exact: true
- n_threads: 1
- use_int: false
- queue_sort: true
- maximum_number_of_states: MAX_INT
- cost_upper_bound: MAX_INT

### A\* algorithm
- just using the general seetings

### SP algorithm
- initial_split: 3

### Recomposing A\* algorithm
- alignment_percentage: 100
- use_hide_and_reduction_abstraction: true
- global_duration: 300000000
- local_duration: 300000
- alignment_percentage: 100
- add_conflict_only_once: true
- log_creation_strategy: strict exclude by conflicts
- max_conflicts: 100
- interval_absolute: 0
- timeout_per_trace_in_secs: 300

### Recomposing SP algorithm
- combination of both SP algorithm and recomposing A\* algorithm configurations

## How to produce performance data?
1. Install python package into virtual environment
```
virtualenv -p python3.6 alignclf-venv
source alignclf-venv/bin/activate
python setup.py install
```
2. Create configuration files to run alignment algorithms using CLI
```
make-configs // follow the instructions
```
3. Run the bash scripts to perform alignment experiments
```
sh configs/batch.sh
```
4. Convert the raw data to useful format

During the alignment experiments, a number of information is stored, e.g., alignment performance data, and the actual alignments.

Due to a mistake of mine, each alignment is stored in a separate json. Having many small files is bad for storage, so run the following python script to join directories of alignment json files as one single file:
```
python src/repackage_results.py // remember to go in and replace the directories
```
5. Create classification task csv file
```
python src/run_create_clf_data.py // once again need to specify the result directories, one thing to note is that a directory should have results of all algorithms
```

## Extract the model-trace features
```
python src/run_extract_feature.py // change the directories in the file if necessary
```

## Merge feature data and performance data
Run the jupyter notebook jupyter/merge-feature-data-and-performance-data.ipynb

## Training predictive models
- Dummy classifier
```
python src/run_dummy_clf.py -f [data.csv] -o [outdir]
```
- Decision tree
```
python src/run_decision_tree_clf.py -f [data.csv] -n [n_folds] -o [outdir] // can go in file to change what parameters to optimize
```
- Random forest
```
python src/run_random_forest.py -f [data.csv] -o [outdir] // can go in file to change range of number of estimators to search through
```

## Producing confusion matrices
```
sh scripts/run_cnf_mat.sh
```

#### Helpful explanations and reminders
- us: microseconds
- clock time refers to the time from the start of the Java program till the end
- traces are unique and multiple cases can have the same trace
