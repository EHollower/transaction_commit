public final class FailureInjector {
    private FailureInjector() {
    }

    public static Participant preparedParticipant(String id) {
        return new Participant(id);
    }

    public static Participant abortingParticipant(String id) {
        return new Participant(id, false, true);
    }

    public static Participant crashedParticipant(String id) {
        return new Participant(id, true, false);
    }

    public static void deliverDecisionTwice(Participant participant, Message decision) {
        participant.receiveDecision(decision);
        participant.receiveDecision(decision);
    }
}
