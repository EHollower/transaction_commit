# Transaction Commit / Two-Phase Commit

This project models, implements, and tests the Transaction Commit problem using Two-Phase Commit.

The project contains:

- TLA+ specifications checked with TLC;
- a Java implementation of the protocol;
- JUnit tests for the Java implementation;
- JavaPathFinder models for Java-side state exploration;
- screenshots showing successful checks and detected bugs;
- an AI usage log.

## Directory Structure

```text
tla/                    TLA+ models, TLC configs, screenshots
java/                   Java implementation
tests/junit/            JUnit tests and test screenshot
tests/JavaPathFinder/   JPF models, configs, screenshots
prompts/                AI usage log
.github/workflows/      Reproducible CI checks
```

## Main Safety Property

The main safety property is transaction consistency:

```text
No participant may commit while another participant aborts in the same transaction.
```

The intentional bug used for the experiments is premature commit.

Correct condition:

```tla
tmPrepared = RM
```

Buggy condition:

```tla
tmPrepared # {}
```

The buggy coordinator commits after at least one participant prepares, instead of waiting for all participants.

## Running TLC

The verified local TLC version was:

```text
TLC2 Version 2.19 of 08 August 2024 (rev: 5a47802)
```

Download TLC if needed:

```bash
mkdir -p tools
curl -L -o tools/tla2tools.jar https://github.com/tlaplus/tlaplus/releases/download/v1.7.4/tla2tools.jar
```

Run the correct model:

```bash
java -XX:+UseParallelGC -cp tools/tla2tools.jar tlc2.TLC -config tla/TwoPhase.cfg tla/TwoPhase.tla
```

Expected result:

```text
Model checking completed. No error has been found.
```

Run the buggy model:

```bash
java -XX:+UseParallelGC -cp tools/tla2tools.jar tlc2.TLC -config tla/TwoPhaseBuggy.cfg tla/TwoPhaseBuggy.tla
```

Expected result:

```text
Error: Invariant TPConsistent is violated.
```

Screenshots:

- `tla/screenshots/invariant_ok.png`
- `tla/screenshots/state_space.png`
- `tla/screenshots/bug_counterexample.png`

## Running Java Tests

Run the JUnit tests:

```bash
mvn test
```

Expected result:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Screenshot:

- `tests/junit/java_tests_ok.png`

Run the Java demo:

```bash
javac -d /tmp/transaction_commit_classes java/*.java
java -cp /tmp/transaction_commit_classes Main
```

## Running JavaPathFinder

JavaPathFinder works best with Java 11.

The tested JPF revision was:

```text
2bf98722c49a8149f9a6f68acb9edc6ba5cdd557
```

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

Compile the JPF models:

```bash
mkdir -p tests/JavaPathFinder/build
javac --release 11 -cp tools/jpf-core/build/jpf.jar -d tests/JavaPathFinder/build tests/JavaPathFinder/CorrectTwoPhaseJpf.java tests/JavaPathFinder/BuggyTwoPhaseJpf.java
```

Run the correct JPF model:

```bash
tools/jpf-core/bin/jpf tests/JavaPathFinder/correct_two_phase.jpf
```

Expected result:

```text
no errors detected
```

Run the buggy JPF model:

```bash
tools/jpf-core/bin/jpf tests/JavaPathFinder/buggy_two_phase.jpf
```

Expected result:

```text
java.lang.AssertionError: Inconsistent decision: committed and aborted participants exist
```

Screenshots:

- `tests/JavaPathFinder/jpf_correct_ok.png`
- `tests/JavaPathFinder/jpf_bug_found.png`

## TLA+ To Java Mapping

| TLA+ element | Java representation |
| --- | --- |
| `rmState[rm]` | `Participant.state` |
| `tmState` | `Coordinator.state` |
| `tmPrepared` | `Coordinator.preparedParticipants` |
| `RMPrepare` | `Participant.onPrepare` |
| `TMRcvPrepared` | `Coordinator.recordVote` |
| `TMCommit` | `Coordinator.shouldCommit` and commit decision |
| `TMAbort` | abort decision in `Coordinator.execute` |
| `RMRcvCommitMsg` | `Participant.receiveDecision(Message.commit())` |
| `RMRcvAbortMsg` | `Participant.receiveDecision(Message.abort())` |

One abstraction difference: the TLA+ model stores sent messages in a monotonically growing `msgs` set, while the Java implementation represents message exchange with asynchronous method execution and futures.

## Scope And Limitations

The Java implementation is a minimal in-memory safety-oriented model, not a production Two-Phase Commit system.

Limitations:

- no persistent coordinator log;
- no transaction identifiers;
- no crash recovery after prepare;
- no durable participant recovery;
- simplified timeout/failure model;
- no real network transport.

## CI Workflow

GitHub Actions workflow:

```text
.github/workflows/verify.yml
```

It runs TLC, JUnit, and JavaPathFinder checks. It verifies that correct models pass and intentionally buggy models fail with the expected errors.

## Attribution

The `TCommit.tla` and `TwoPhase.tla` specifications are based on the classic TLA+ Transaction Commit / Two-Phase Commit examples by Leslie Lamport and the TLA+ project materials. This repository uses them as the formal-model starting point for the course project.
