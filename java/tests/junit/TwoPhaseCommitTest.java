import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TwoPhaseCommitTest {
    @Test
    void allParticipantsPrepare_thenCommit() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.preparedParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        Coordinator.TransactionResult result = run(new Coordinator(participants));

        assertEquals(CoordinatorState.COMMITTED, result.coordinatorState());
        assertTrue(result.allParticipantsInState(ParticipantState.COMMITTED));
        assertTrue(result.isConsistent());
    }

    @Test
    void oneParticipantAborts_thenGlobalAbort() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.abortingParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        Coordinator.TransactionResult result = run(new Coordinator(participants));

        assertEquals(CoordinatorState.ABORTED, result.coordinatorState());
        assertTrue(result.allParticipantsInState(ParticipantState.ABORTED));
        assertTrue(result.isConsistent());
    }

    @Test
    void participantFailureBeforePrepare_thenAbort() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.crashedParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        Coordinator.TransactionResult result = run(new Coordinator(participants));

        assertEquals(CoordinatorState.ABORTED, result.coordinatorState());
        assertFalse(result.participantStates().containsValue(ParticipantState.COMMITTED));
        assertTrue(result.isConsistent());
    }

    @Test
    void duplicateMessagesDoNotBreakState() {
        Participant participant = FailureInjector.preparedParticipant("r1");

        participant.onPrepare(Message.prepare("r1"));
        FailureInjector.deliverDecisionTwice(participant, Message.commit());

        assertEquals(ParticipantState.COMMITTED, participant.state());

        FailureInjector.deliverDecisionTwice(participant, Message.commit());

        assertEquals(ParticipantState.COMMITTED, participant.state());
    }

    @Test
    void buggyCoordinatorCommitsWithoutAllPrepared_detected() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.abortingParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        Coordinator.TransactionResult result = run(Coordinator.buggyCommitAfterAnyPrepared(participants));

        assertEquals(CoordinatorState.COMMITTED, result.coordinatorState());
        assertFalse(result.isConsistent());
        assertEquals(ParticipantState.COMMITTED, result.participantStates().get("r1"));
        assertEquals(ParticipantState.ABORTED, result.participantStates().get("r2"));
    }

    private Coordinator.TransactionResult run(Coordinator coordinator) {
        try (NetworkSimulator network = new NetworkSimulator()) {
            return coordinator.execute(network);
        }
    }
}
