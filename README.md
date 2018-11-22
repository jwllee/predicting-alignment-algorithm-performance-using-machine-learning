# alignment-algorithm-classifier
A classifier to predict best performing alignment algorithm to compute the alignment between a model and log trace.


| Stats to retrieve | Level | Destination | Monolithic | Recomposing |
| --- | --- | --- | --- | --- |
| clock time | log | `i\log-stats.csv` | `i\prom.log` | `i\prom.log` |
| total alignment cost | log | `i\log-stats.csv` | `i\prom.log` | `i\python.log` |
| replay configurations | iteration trace log | all `i\x-stats.csv` | `i\configs.json` | `i\configs.json` |
| no. of valid alignments | log | `i\log-stats.csv` | `i\alignment-stats.csv` *remember &#x7c;alignments&#x7c; <= &#x7c;traces&#x7c;* | `i\alignment-stats.csv` *need to equal monolithic stats* |
| log size | log | `i\log-stats.csv` | `log.xes` | `log.xes` |
| duplicate alignment flag and id | iteration trace log | `i\trace-x-stats.csv` | no need | read below |
| no. of rejected alignments | iter-stats log | `i\log-stats.csv` | no need | `i\rejected.csv` filtered by alignment ids |
| no. of to-align alignments | iter-stats log | `i\log-stats.csv` | no need | `i\to-align.csv` filtered by alignment ids |  
| no. of valid alignments | iter-stats log | `i\log-stats.csv` | no need | `i\valid.csv` filtered by alignment ids |
| no. of traces aligned | iter-stats | `i\prom-iter-stats.csv` | no need | done in prom |
| no. of subnets | `i\prom-iter-stats.csv` | no need | done in prom |
| no. of recomposing activity | `i\prom-iter-stats.csv` | no need | done in prom |
| no. of border activity | `i\prom-iter-stats.csv` | no need | done in prom |
| no. of excluded trace | `i\prom-iter-stats.csv` | no need | done in prom |
| trace iteration stats | | `i\trace-iter-stats.csv` | no need | `i\iter-1...n` |
| trace level stats | | `i\trace-stats.csv` | no need | `i\trace-iter-stats.csv` |
| log level stats | | `i\log-stats.csv` | `i\trace-stats.csv` | `i\trace-stats.csv` | 

#### How iteration statistics are processed per alignment for recomposing replay 
| Categories | Stats | Notes |
| --- | --- | --- |
| **join by OR**   | Exit code for alignment | alignment is rejected if it is problematic at any iteration, so ORing is ok |
| **sum**          | Transitions fired | this is only sort of helpful since hide-and-reduce abstraction makes things unmappable between monolithic and decomposed replay |
|                  | Markings polled from queue | |
|                  | Markings added to closed set | |
|                  | Markings queued | | 
|                  | Markings reached | |
|                  | Heuristics computed | |
|                  | Heuristics estimated | |
|                  | Heuristics derived | |
|                  | Time to compute alignment (us) | |
|                  | Time to compute heuristics (us) | |
|                  | Time to setup algorithm (us) | |
|                  | Total Time including setup (us) | |
|                  | Number of splits when splitting marking | |
|                  | Log move cost of alignment | |
|                  | Model move cost of alignment | |
|                  | Synchronous move cost of alignment | |
|                  | Pre-processing time (us) | |
|                  | Size of the constraintset | |
|                  | Number of times replay was restarted | |
|                  | total Memory (MB) | |
| **max**          | Maximum queue length (elts) | |
|                  | Maximum queue capacity (elts) | |
|                  | Approximate peak memory used (kb) | |
|                  | max Memory (MB) | |
| **not included** | Length of the alignment found | |
|                  | Length of the orignal trace | |
|                  | Places in the synchronous product | |
|                  | Transtions in the synchronous product | |
|                  | Splitpoints | |

#### Modification to prom jars
- [x] add no. of log traces per iteration
- [x] change alignments.csv to trace-stats.csv for consistency at monolithic replayer
- [x] change replay parameter names of monolithic replayer for consistency
- [x] add missing parameter names to monolithic replayer
- [x] add alignment exit codes to alignment csv files
- [x] add representative alignment id to alignment csv files
- [x] add hide-and-reduce subnets per iteration
- [x] update log alignment prom jar
- [x] update recomposing prom jar
- [x] update monolithic prom jar 

#### Recomposing replay iteration stats
- no. of log traces: except of the first iteration, log recomposition strategy can exclude some of the log traces
- duplicate alignment flag and id: decomposed replay can make two different alignments the same, but this can make monolithic and decomposed replay incomparable on the log level. Make sure that all alignments are reported in stats but add a flag if it is a duplicate and add the original alignment id.
- various statistics about hide-and-reduce subnets and filtered subnets

#### Helpful explanations and reminders
- us: microseconds
- spelling mistakes with statistics categories exist to correspond exactly to the actual code. For example, `Length of the orignal trace`.
- clock time refers to the time from the start of the Java program till the end
- traces are unique and multiple cases can have the same trace
- in `prom-iter-stats.csv`, open, valid, and rejected alignment counts should sum to no. of aligned trace 