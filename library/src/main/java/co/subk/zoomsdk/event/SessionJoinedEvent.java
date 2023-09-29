package co.subk.zoomsdk.event;

public class SessionJoinedEvent {
    public final String sessionId;

    public SessionJoinedEvent(String sessionId) {
        this.sessionId = sessionId;
    }
}
