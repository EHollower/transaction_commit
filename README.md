# Transaction Commit / Two-Phase Commit

This repository contains a university concurrent programming project about the Transaction Commit problem, implemented through the Two-Phase Commit protocol.

The project combines a formal TLA+ model with Java and Go implementations, plus testing and verification artifacts.

## Repository Map

- `tla/`: TLA+ specifications, TLC configs, and verification screenshots.
- `java/`: Java implementation of Two-Phase Commit.
- `go/`: Go implementation and buggy Go experiment.
- `tests/junit/`: JUnit tests for the Java implementation.
- `tests/JavaPathFinder/`: JavaPathFinder models and screenshots.
- `prompts/`: AI prompt logs.
- `.github/workflows/`: CI verification workflow.
- `docs/`: LaTeX report and presentation sources.

## Main Safety Property

```text
No participant may commit while another participant aborts in the same transaction.
```

The main bug demonstrated in the project is premature commit:

```text
Correct: commit only if every participant prepared.
Buggy:   commit after at least one participant prepared.
```

## Quick Commands

Run Java/JUnit tests:

```bash
mvn test
```

Run the correct TLA+ model:

```bash
java -XX:+UseParallelGC -cp tools/tla2tools.jar tlc2.TLC -config tla/TwoPhase.cfg tla/TwoPhase.tla
```

Run the buggy TLA+ model:

```bash
java -XX:+UseParallelGC -cp tools/tla2tools.jar tlc2.TLC -config tla/TwoPhaseBuggy.cfg tla/TwoPhaseBuggy.tla
```

Run Go examples:

```bash
go run go/TwoPhase.go --rm=0,0,0
go run go/TwoPhase.go --rm=0,0,1
go run go/TwoPhaseAlgorithmLogicBug.go --rm=0,0,1 --bufferedChannel
```

## More Details

- Java tests: `tests/junit/README.md`
- JavaPathFinder: `tests/JavaPathFinder/README.md`
- Report and presentation: `docs/`
- AI prompts: `prompts/ai_generation_log.md`
