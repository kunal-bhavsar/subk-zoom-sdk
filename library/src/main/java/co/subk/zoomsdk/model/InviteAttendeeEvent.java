package co.subk.zoomsdk.model;

public class InviteAttendeeEvent extends SubkEvent {
    public String mobile;

    public InviteAttendeeEvent(String eventName, String mobile) {
        super(eventName);
        this.mobile = mobile;
    }
}
