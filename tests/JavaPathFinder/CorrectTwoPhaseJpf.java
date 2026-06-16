public final class CorrectTwoPhaseJpf {
    private static final int WORKING = 0;
    private static final int PREPARED = 1;
    private static final int COMMITTED = 2;
    private static final int ABORTED = 3;
    private static final int PARTICIPANT_COUNT = 3;

    private CorrectTwoPhaseJpf() {
    }

    public static void main(String[] args) {
        for (int voteMask = 0; voteMask < (1 << PARTICIPANT_COUNT); voteMask++) {
            verifyTransaction(voteMask);
        }
    }

    private static void verifyTransaction(int voteMask) {
        int[] rmState = {WORKING, WORKING, WORKING};
        int preparedCount = 0;

        for (int rm = 0; rm < rmState.length; rm++) {
            if (votesPrepared(voteMask, rm)) {
                rmState[rm] = PREPARED;
                preparedCount++;
            } else {
                rmState[rm] = ABORTED;
            }
        }

        boolean commit = preparedCount == rmState.length;
        for (int rm = 0; rm < rmState.length; rm++) {
            if (commit) {
                rmState[rm] = COMMITTED;
            } else {
                rmState[rm] = ABORTED;
            }
        }

        assertConsistent(rmState, voteMask);
    }

    private static boolean votesPrepared(int voteMask, int rm) {
        return (voteMask & (1 << rm)) != 0;
    }

    private static void assertConsistent(int[] rmState, int voteMask) {
        boolean hasCommitted = false;
        boolean hasAborted = false;

        for (int state : rmState) {
            hasCommitted = hasCommitted || state == COMMITTED;
            hasAborted = hasAborted || state == ABORTED;
        }

        if (hasCommitted && hasAborted) {
            throw new AssertionError("Inconsistent decision for voteMask=" + voteMask);
        }
    }
}
