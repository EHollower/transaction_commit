import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NetworkSimulator implements AutoCloseable {
    private final ExecutorService executor;
    private final long delayMillis;

    public NetworkSimulator() {
        this(Runtime.getRuntime().availableProcessors(), 0);
    }

    public NetworkSimulator(int workerCount, long delayMillis) {
        this.executor = Executors.newFixedThreadPool(Math.max(1, workerCount));
        this.delayMillis = Math.max(0, delayMillis);
    }

    public CompletableFuture<Message> sendPrepare(Participant participant) {
        return CompletableFuture.supplyAsync(() -> {
            sleepIfNeeded();
            return participant.onPrepare(Message.prepare(participant.id()));
        }, executor);
    }

    public void broadcastDecision(Message decision, Collection<Participant> participants) {
        List<CompletableFuture<Void>> deliveries = participants.stream()
            .map(participant -> CompletableFuture.runAsync(() -> {
                sleepIfNeeded();
                participant.receiveDecision(decision);
            }, executor))
            .toList();

        deliveries.forEach(CompletableFuture::join);
    }

    private void sleepIfNeeded() {
        if (delayMillis == 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("network delivery interrupted", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
