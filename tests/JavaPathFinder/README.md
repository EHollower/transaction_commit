# JavaPathFinder Tests

Small JavaPathFinder-compatible models for the Two-Phase Commit safety property.

Property checked:

```text
No participant may commit while another participant aborts.
```

Files:

- `CorrectTwoPhaseJpf.java`: correct model, should pass.
- `BuggyTwoPhaseJpf.java`: buggy model, should fail.
- `correct_two_phase.jpf`: JPF config for the correct model.
- `buggy_two_phase.jpf`: JPF config for the buggy model.

Run from project root:

```bash
mkdir -p tests/JavaPathFinder/build
javac -d tests/JavaPathFinder/build tests/JavaPathFinder/CorrectTwoPhaseJpf.java tests/JavaPathFinder/BuggyTwoPhaseJpf.java

tools/jpf-core/bin/jpf tests/JavaPathFinder/correct_two_phase.jpf
tools/jpf-core/bin/jpf tests/JavaPathFinder/buggy_two_phase.jpf
```

Screenshots:

- `jpf_correct_ok.png`: correct model passes.
- `jpf_bug_found.png`: buggy model fails with an assertion error.
