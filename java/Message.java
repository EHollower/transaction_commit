import java.util.Objects;

public final class Message {
    public enum Type {
        PREPARE,
        PREPARED,
        COMMIT,
        ABORT
    }

    private final Type type;
    private final String participantId;

    private Message(Type type, String participantId) {
        this.type = Objects.requireNonNull(type, "type");
        this.participantId = participantId;
    }

    public static Message prepare(String participantId) {
        return new Message(Type.PREPARE, participantId);
    }

    public static Message prepared(String participantId) {
        return new Message(Type.PREPARED, participantId);
    }

    public static Message commit() {
        return new Message(Type.COMMIT, null);
    }

    public static Message abort() {
        return new Message(Type.ABORT, null);
    }

    public Type type() {
        return type;
    }

    public String participantId() {
        return participantId;
    }

    @Override
    public String toString() {
        if (participantId == null) {
            return "Message{" + "type=" + type + '}';
        }
        return "Message{" + "type=" + type + ", participantId='" + participantId + "'}";
    }
}
