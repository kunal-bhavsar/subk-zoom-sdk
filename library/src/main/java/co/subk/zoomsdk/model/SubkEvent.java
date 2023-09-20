package co.subk.zoomsdk.model;

public class SubkEvent {
    public String eventName;
    public String latitude;
    public String longitude;

    public String accuracy;

    public SubkEvent(String eventName,String latitude, String longitude, String accuracy) {
        this.eventName = eventName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
}
