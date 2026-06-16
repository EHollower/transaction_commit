# JavaPathFinder Tests

Small threaded JavaPathFinder models for the Two-Phase Commit safety property.

Property checked:

```text
No participant may commit while another participant aborts.
```

Each participant runs in its own thread and makes a nondeterministic JPF choice with `Verify.getBoolean()`.

Files:

- `CorrectTwoPhaseJpf.java`: correct model, should pass.
- `BuggyTwoPhaseJpf.java`: buggy model, should fail.
- `correct_two_phase.jpf`: JPF config for the correct model.
- `buggy_two_phase.jpf`: JPF config for the buggy model.

Run from project root:

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p tests/JavaPathFinder/build
javac --release 11 -cp tools/jpf-core/build/jpf.jar -d tests/JavaPathFinder/build tests/JavaPathFinder/CorrectTwoPhaseJpf.java tests/JavaPathFinder/BuggyTwoPhaseJpf.java

tools/jpf-core/bin/jpf tests/JavaPathFinder/correct_two_phase.jpf
tools/jpf-core/bin/jpf tests/JavaPathFinder/buggy_two_phase.jpf
```

Screenshots:

- `jpf_correct_ok.png`: correct model passes.
- `jpf_bug_found.png`: buggy model fails with an assertion error.
