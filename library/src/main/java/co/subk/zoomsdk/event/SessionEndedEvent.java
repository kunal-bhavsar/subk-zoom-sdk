package co.subk.zoomsdk.event;

public class SessionEndedEvent {
    public final String taskId;

    public SessionEndedEvent(String taskId) {
        this.taskId = taskId;
    }
}
