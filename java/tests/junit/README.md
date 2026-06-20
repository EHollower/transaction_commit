# JUnit Tests

This folder contains JUnit 5 tests for the Java Two-Phase Commit implementation.

## Files

- `TwoPhaseCommitTest.java`: Java protocol tests.
- `java_tests_ok.png`: screenshot of a successful Maven test run.

## Run

From the repository root:

```bash
mvn test
```

## What Is Tested

- all participants prepare, so the coordinator commits;
- one participant aborts, so the coordinator aborts;
- participant failure before prepare causes abort;
- duplicate decision messages do not corrupt state;
- buggy coordinator commits too early and the test detects inconsistency.

## Safety Property

```text
No committed participant may coexist with an aborted participant.
```

## Expected Result

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
