package co.subk.zoomsdk.model;

public class InternetEvent extends SubkEvent {
    public boolean isOnline;

    public InternetEvent(String eventName, boolean isOnline) {
        super(eventName);
        this.isOnline = isOnline;
    }
}
