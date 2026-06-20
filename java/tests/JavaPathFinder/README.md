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

mkdir -p java/tests/JavaPathFinder/build
javac --release 11 -cp tools/jpf-core/build/jpf.jar -d java/tests/JavaPathFinder/build java/tests/JavaPathFinder/CorrectTwoPhaseJpf.java java/tests/JavaPathFinder/BuggyTwoPhaseJpf.java

tools/jpf-core/bin/jpf java/tests/JavaPathFinder/correct_two_phase.jpf
tools/jpf-core/bin/jpf java/tests/JavaPathFinder/buggy_two_phase.jpf
```

Screenshots:

- `screenshots/jpf_correct_ok.png`: correct model passes.
- `screenshots/jpf_bug_found.png`: buggy model fails with an assertion error.
