import gov.nasa.jpf.vm.Verify;

public final class CorrectTwoPhaseJpf {
    private static final int WORKING = 0;
    private static final int PREPARED = 1;
    private static final int COMMITTED = 2;
    private static final int ABORTED = 3;
    private static final int PARTICIPANT_COUNT = 3;

    private CorrectTwoPhaseJpf() {
    }

    public static void main(String[] args) throws InterruptedException {
        ParticipantModel[] participants = new ParticipantModel[PARTICIPANT_COUNT];
        Thread[] participantThreads = new Thread[PARTICIPANT_COUNT];

        for (int i = 0; i < PARTICIPANT_COUNT; i++) {
            participants[i] = new ParticipantModel();
            participantThreads[i] = new Thread(new ParticipantVote(participants[i]));
            participantThreads[i].start();
        }

        for (Thread participantThread : participantThreads) {
            participantThread.join();
        }

        int preparedCount = 0;
        for (ParticipantModel participant : participants) {
            if (participant.state == PREPARED) {
                preparedCount++;
            }
        }

        boolean commit = preparedCount == PARTICIPANT_COUNT;
        for (ParticipantModel participant : participants) {
            participant.receiveDecision(commit);
        }

        assertConsistent(participants);
    }

    private static void assertConsistent(ParticipantModel[] participants) {
        boolean hasCommitted = false;
        boolean hasAborted = false;

        for (ParticipantModel participant : participants) {
            hasCommitted = hasCommitted || participant.state == COMMITTED;
            hasAborted = hasAborted || participant.state == ABORTED;
        }

        if (hasCommitted && hasAborted) {
            throw new AssertionError("Inconsistent decision: committed and aborted participants exist");
        }
    }

    private static final class ParticipantVote implements Runnable {
        private final ParticipantModel participant;

        private ParticipantVote(ParticipantModel participant) {
            this.participant = participant;
        }

        @Override
        public void run() {
            if (Verify.getBoolean()) {
                participant.prepare();
            } else {
                participant.abort();
            }
        }
    }

    private static final class ParticipantModel {
        private int state = WORKING;

        private void prepare() {
            state = PREPARED;
        }

        private void abort() {
            state = ABORTED;
        }

        private void receiveDecision(boolean commit) {
            if (commit) {
                state = COMMITTED;
            } else {
                state = ABORTED;
            }
        }
    }
}
