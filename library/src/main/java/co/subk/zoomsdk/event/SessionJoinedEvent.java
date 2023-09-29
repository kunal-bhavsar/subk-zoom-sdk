package co.subk.zoomsdk.event;

public class SessionJoinedEvent {
    public final String taskId;
    public final String sessionId;

    public SessionJoinedEvent(String taskId, String sessionId) {
        this.taskId = taskId;
        this.sessionId = sessionId;
    }
}
