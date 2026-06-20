# Java Prompt Log

## Prompt 1: Map The Verified Model To Java

```text
Inspect the existing Two-Phase Commit model and explain how the Java implementation should correspond to it.

Focus only on the Java side.

Map:
- resource manager state to Java participant state;
- transaction manager state to Java coordinator state;
- prepared participants to a Java collection;
- prepare, commit, and abort actions to Java methods;
- commit and abort messages to Java message types.

The main safety property is:
No participant may commit while another participant aborts in the same transaction.
```

## Prompt 2: Generate The Java Implementation

```text
Generate a minimal Java implementation of Two-Phase Commit.

Create these files under `java/`:
- `Coordinator.java`
- `CoordinatorState.java`
- `Participant.java`
- `ParticipantState.java`
- `Message.java`
- `NetworkSimulator.java`
- `FailureInjector.java`
- `Main.java`

Requirements:
- use plain Java;
- keep the code small and readable;
- model participant states as WORKING, PREPARED, COMMITTED, ABORTED;
- model coordinator states as INIT, COMMITTED, ABORTED;
- participants start in WORKING;
- a participant can prepare, abort, commit, or receive an abort decision;
- the coordinator sends prepare requests to all participants;
- the coordinator commits only if every participant prepared;
- if any participant aborts, fails, or does not prepare, the coordinator aborts;
- commit and abort decisions are broadcast to every participant;
- duplicate decision messages must not corrupt state.

Also include a non-default buggy coordinator mode for tests, where the coordinator commits after any participant prepared.
```

## Prompt 3: Add JUnit Tests

```text
Add JUnit 5 tests for the Java Two-Phase Commit implementation.

Create or update:
- `pom.xml`
- `tests/junit/TwoPhaseCommitTest.java`

Use `java/` as the Java source directory and `tests/junit/` as the test source directory.

Add tests for:
- all participants prepare, so the coordinator commits and all participants commit;
- one participant aborts, so the coordinator aborts and all participants abort;
- one participant is unreachable before prepare, so the global decision is abort;
- duplicate decision messages do not change a stable committed state incorrectly;
- the intentionally buggy coordinator commits without all participants prepared and the test detects inconsistency.

The tests must check the safety property:
No committed participant may coexist with an aborted participant.
```

## Prompt 4: Improve JavaPathFinder Testing

```text
Improve the JavaPathFinder part so it checks Java-side nondeterministic or concurrent behavior, not just fixed sequential cases.

Create or update files under `tests/JavaPathFinder/`:
- `CorrectTwoPhaseJpf.java`
- `BuggyTwoPhaseJpf.java`
- `correct_two_phase.jpf`
- `buggy_two_phase.jpf`
- `README.md`

Requirements:
- each participant should run in its own thread;
- each participant should use `Verify.getBoolean()` to nondeterministically choose prepare or abort;
- the correct model commits only if all participants prepared;
- the buggy model commits if at least one participant prepared;
- both models must assert that committed and aborted participants cannot coexist in the same final state;
- the correct model should pass JavaPathFinder;
- the buggy model should fail with an assertion error.
```

## Prompt 5: Clean Up Java Warnings

```text
Inspect the Java implementation for avoidable compiler warnings.

If `CoordinatorState` or `ParticipantState` are top-level auxiliary types inside another `.java` file, move them into their own files:
- `java/CoordinatorState.java`
- `java/ParticipantState.java`

Keep behavior unchanged and make sure the code still compiles with:

`javac -Xlint:all -d /tmp/opencode/transaction_commit_lint_classes java/*.java`
```

## Prompt 6: Review Java Correctness

```text
Review only the Java implementation and Java tests.

Check:
- the coordinator commits only after all participants prepared;
- abort or participant failure leads to global abort;
- duplicate commit or abort decisions are safe;
- the buggy coordinator mode is not used by default;
- JUnit tests cover the important success and failure cases;
- JavaPathFinder explores nondeterministic participant choices and thread schedules;
- the Java code matches the safety property: no committed participant may coexist with an aborted participant.

Return only concrete issues and the smallest fixes.
```

## Prompt 7: Java Verification Commands

```text
List the exact commands needed to verify only the Java part of the project.

Include commands for:
- compiling the Java implementation with warnings enabled;
- running the JUnit tests;
- compiling the JavaPathFinder models;
- running the correct JavaPathFinder model;
- running the buggy JavaPathFinder model.
```


# Go Prompt Log

## Prompt 1: Map The Verified Model To Go

```text
i have this tla+ code: <TwoPhase.tla>, <TCommit.tla>. Inspect the existing Two-Phase Commit model and explain how a golang implementation should correspond to it. 
Focus only on the golang side.
```

## Prompt 2: Generate the Go code file

```text
can you generate a minimal golang implementation of this Two-Phase Commit?
```

## Prompt 3: Reimplement Go code to introduce concurrency

```text
can you reimplement the golang code to use real concurrency (threads)? my plan is the following: create a go implementation of the tla specification, then modify the specification to introduce a concurrency bug and generate again a go implementation. the goal is to compare the tla verification tool to concurrency tools for programming languages (go in this case). can you reimplement the go code?
```

