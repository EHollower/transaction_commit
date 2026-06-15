import java.util.Objects;

enum ParticipantState {
    WORKING,
    PREPARED,
    COMMITTED,
    ABORTED
}

public final class Participant {
    private final String id;
    private final boolean votePrepared;
    private final boolean reachable;
    private ParticipantState state;

    public Participant(String id) {
        this(id, true, true);
    }

    public Participant(String id, boolean votePrepared, boolean reachable) {
        this.id = Objects.requireNonNull(id, "id");
        this.votePrepared = votePrepared;
        this.reachable = reachable;
        this.state = ParticipantState.WORKING;
    }

    public String id() {
        return id;
    }

    public synchronized ParticipantState state() {
        return state;
    }

    public synchronized Message onPrepare(Message message) {
        if (message.type() != Message.Type.PREPARE) {
            throw new IllegalArgumentException("participant can only prepare from a PREPARE message");
        }
        if (!id.equals(message.participantId())) {
            throw new IllegalArgumentException("prepare message sent to the wrong participant");
        }
        if (!reachable) {
            throw new IllegalStateException("participant " + id + " is not reachable");
        }
        if (state == ParticipantState.PREPARED) {
            return Message.prepared(id);
        }
        if (state != ParticipantState.WORKING) {
            return Message.abort();
        }

        if (votePrepared) {
            state = ParticipantState.PREPARED;
            return Message.prepared(id);
        }

        state = ParticipantState.ABORTED;
        return Message.abort();
    }

    public synchronized void receiveDecision(Message decision) {
        switch (decision.type()) {
            case COMMIT -> receiveCommit();
            case ABORT -> receiveAbort();
            default -> throw new IllegalArgumentException("decision must be COMMIT or ABORT");
        }
    }

    private void receiveCommit() {
        if (state == ParticipantState.PREPARED || state == ParticipantState.COMMITTED) {
            state = ParticipantState.COMMITTED;
        }
        if (state == ParticipantState.WORKING) {
            state = ParticipantState.ABORTED;
        }
    }

    private void receiveAbort() {
        if (state != ParticipantState.COMMITTED) {
            state = ParticipantState.ABORTED;
        }
    }

    @Override
    public synchronized String toString() {
        return "Participant{" + "id='" + id + "', state=" + state + '}';
    }
}
