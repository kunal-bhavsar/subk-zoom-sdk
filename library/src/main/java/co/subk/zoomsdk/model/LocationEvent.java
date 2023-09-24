package co.subk.zoomsdk.model;

public class LocationEvent extends SubkEvent {
    public String latitude;
    public String longitude;
    public String accuracy;

    public LocationEvent(String eventName, String latitude, String longitude, String accuracy) {
        super(eventName);
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
}
