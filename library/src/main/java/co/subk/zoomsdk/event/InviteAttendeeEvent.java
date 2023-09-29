package co.subk.zoomsdk.event;

import co.subk.zoomsdk.ZoomSdkHelper;

public class InviteAttendeeEvent {
    public final String taskId;

    public InviteAttendeeEvent(String taskId) {
        this.taskId = taskId;
    }
}
