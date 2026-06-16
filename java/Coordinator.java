import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class Coordinator {
    private final List<Participant> participants;
    private final boolean commitAfterAnyPrepared;
    private final Set<String> preparedParticipants;
    private CoordinatorState state;

    public Coordinator(Collection<Participant> participants) {
        this(participants, false);
    }

    private Coordinator(Collection<Participant> participants, boolean commitAfterAnyPrepared) {
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("at least one participant is required");
        }
        this.participants = List.copyOf(participants);
        this.commitAfterAnyPrepared = commitAfterAnyPrepared;
        this.preparedParticipants = new LinkedHashSet<>();
        this.state = CoordinatorState.INIT;
    }

    public static Coordinator buggyCommitAfterAnyPrepared(Collection<Participant> participants) {
        return new Coordinator(participants, true);
    }

    public synchronized CoordinatorState state() {
        return state;
    }

    public synchronized Set<String> preparedParticipants() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(preparedParticipants));
    }

    public TransactionResult execute(NetworkSimulator network) {
        Objects.requireNonNull(network, "network");
        ensureInit();

        List<CompletableFuture<Message>> votes = participants.stream()
            .map(network::sendPrepare)
            .toList();

        for (CompletableFuture<Message> vote : votes) {
            recordVote(vote);
        }

        Message decision = shouldCommit() ? Message.commit() : Message.abort();
        synchronized (this) {
            state = decision.type() == Message.Type.COMMIT
                ? CoordinatorState.COMMITTED
                : CoordinatorState.ABORTED;
        }

        network.broadcastDecision(decision, participants);
        return new TransactionResult(state(), preparedParticipants(), participantStates());
    }

    private synchronized void ensureInit() {
        if (state != CoordinatorState.INIT) {
            throw new IllegalStateException("transaction already decided: " + state);
        }
    }

    private void recordVote(CompletableFuture<Message> vote) {
        try {
            Message message = vote.join();
            if (message.type() == Message.Type.PREPARED) {
                synchronized (this) {
                    preparedParticipants.add(message.participantId());
                }
            }
        } catch (CompletionException ignored) {
            // Missing or failed participants are treated as abort votes.
        }
    }

    private synchronized boolean shouldCommit() {
        if (commitAfterAnyPrepared) {
            return !preparedParticipants.isEmpty();
        }
        return preparedParticipants.size() == participants.size();
    }

    private Map<String, ParticipantState> participantStates() {
        Map<String, ParticipantState> states = new LinkedHashMap<>();
        for (Participant participant : participants) {
            states.put(participant.id(), participant.state());
        }
        return states;
    }

    public static final class TransactionResult {
        private final CoordinatorState coordinatorState;
        private final Set<String> preparedParticipants;
        private final Map<String, ParticipantState> participantStates;

        private TransactionResult(
            CoordinatorState coordinatorState,
            Set<String> preparedParticipants,
            Map<String, ParticipantState> participantStates
        ) {
            this.coordinatorState = coordinatorState;
            this.preparedParticipants = Set.copyOf(preparedParticipants);
            this.participantStates = Collections.unmodifiableMap(new LinkedHashMap<>(participantStates));
        }

        public CoordinatorState coordinatorState() {
            return coordinatorState;
        }

        public Set<String> preparedParticipants() {
            return preparedParticipants;
        }

        public Map<String, ParticipantState> participantStates() {
            return participantStates;
        }

        public boolean isConsistent() {
            return !(participantStates.containsValue(ParticipantState.COMMITTED)
                && participantStates.containsValue(ParticipantState.ABORTED));
        }

        public boolean allParticipantsInState(ParticipantState expectedState) {
            return participantStates.values().stream().allMatch(expectedState::equals);
        }

        @Override
        public String toString() {
            return "TransactionResult{"
                + "coordinatorState=" + coordinatorState
                + ", preparedParticipants=" + preparedParticipants
                + ", participantStates=" + participantStates
                + '}';
        }
    }
}
