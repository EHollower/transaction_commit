# AI Generation Log

This file summarizes how AI assistance was used in the project. Entries are summaries, not exact transcripts.

## 1. Assignment Understanding

Asked the AI to inspect the project PDF and explain the required deliverables.

Result: clarified that the project needs a TLA+ model, two implementations, tests, a detected bug, documentation, presentation, and AI usage log.

## 2. TLA+ Verification

Asked how to run TLC on Arch Linux and verify `TwoPhase.tla`.

Result: used `tla2tools.jar` and ran:

```bash
java -XX:+UseParallelGC -cp tools/tla2tools.jar tlc2.TLC -config tla/TwoPhase.cfg tla/TwoPhase.tla
```

Screenshots:

- `tla/screenshots/invariant_ok.png`
- `tla/screenshots/state_space.png`

## 3. Consistency Invariant

Asked how to check the consistency property from `TCommit.tla`.

Result: added alias:

```tla
TPConsistent == TC!TCConsistent
```

and checked:

```tla
INVARIANTS TPTypeOK TPConsistent
```

## 4. Buggy TLA+ Model

Asked the AI to create a buggy version for tool-based error detection.

Result: created:

- `tla/TwoPhaseBuggy.tla`
- `tla/TwoPhaseBuggy.cfg`

Bug introduced:

```tla
tmPrepared # {}
```

instead of:

```tla
tmPrepared = RM
```

TLC detected:

```text
Error: Invariant TPConsistent is violated.
```

Screenshot:

- `tla/screenshots/bug_counterexample.png`

## 5. Java Implementation

Asked the AI to generate the Java implementation of Two-Phase Commit.

Result: created:

- `java/Coordinator.java`
- `java/Participant.java`
- `java/Message.java`
- `java/NetworkSimulator.java`
- `java/FailureInjector.java`
- `java/Main.java`

## 6. JUnit Tests

Asked the AI to generate Java tests.

Result: created:

- `pom.xml`
- `tests/junit/TwoPhaseCommitTest.java`

Tests cover commit, abort, participant failure, duplicate messages, and buggy coordinator behavior.

Verified with:

```bash
mvn test
```

Result:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Screenshot:

- `tests/junit/java_tests_ok.png`

## 7. JavaPathFinder

Asked the AI to add JavaPathFinder artifacts.

Result: created a small JPF-compatible model instead of running JPF directly on the full Java implementation.

Files:

- `tests/JavaPathFinder/CorrectTwoPhaseJpf.java`
- `tests/JavaPathFinder/BuggyTwoPhaseJpf.java`
- `tests/JavaPathFinder/correct_two_phase.jpf`
- `tests/JavaPathFinder/buggy_two_phase.jpf`

Screenshots:

- `tests/JavaPathFinder/jpf_correct_ok.png`
- `tests/JavaPathFinder/jpf_bug_found.png`

## 8. GitHub Actions Workflow

Asked the AI to add a reproducible CI workflow for the project.

Result: created `.github/workflows/verify.yml`, which runs TLC, JUnit, and JavaPathFinder checks. The workflow verifies that the correct models pass and the intentionally buggy models fail with the expected errors.

## Note

AI-generated code was reviewed and tested manually using TLC, JUnit, and JavaPathFinder.
