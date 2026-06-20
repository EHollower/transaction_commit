# Go implementation testing
The Go files can be run with different command line arguments to examine the behaviour of the programs.

### Run the correct TwoPhase protocol
Run with no failing resource manager:
```bash
go run go/TwoPhase.go --rm=0,0,0,0
```
Example result: `screenshots/Go_Correct_AllCommit.png`

Run with at least one failing resource manager:
```bash
go run go/TwoPhase.go --rm=0,0,1,0
```
Example result: `screenshots/Go_Correct_OneAbort.png`


### Run the buggy TwoPhase protocol
Run with at least one failing resource manager (unbuffered channel):
```bash
go run go/TwoPhaseAlgorithmLogicBug.go --rm=0,0,1,0
```
Example result: `screenshots/Go_Buggy_Unbuffered.png`

Run with at least one failing resource manager (buffered channel):
```bash
go run go/TwoPhaseAlgorithmLogicBug.go --rm=0,0,1,0 --bufferedChannel
```
Example result: `screenshots/Go_Buggy_Buffered.png`
