import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        runSuccessfulCommit();
        runAbortScenario();
    }

    private static void runSuccessfulCommit() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.preparedParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        try (NetworkSimulator network = new NetworkSimulator()) {
            Coordinator.TransactionResult result = new Coordinator(participants).execute(network);
            System.out.println("Successful transaction: " + result);
        }
    }

    private static void runAbortScenario() {
        List<Participant> participants = List.of(
            FailureInjector.preparedParticipant("r1"),
            FailureInjector.abortingParticipant("r2"),
            FailureInjector.preparedParticipant("r3")
        );

        try (NetworkSimulator network = new NetworkSimulator()) {
            Coordinator.TransactionResult result = new Coordinator(participants).execute(network);
            System.out.println("Aborted transaction: " + result);
        }
    }
}
