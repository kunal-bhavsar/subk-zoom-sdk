package co.subk.zoomsdk.event;

public class LocationEvent {
    public final String latitude;
    public final String longitude;
    public final String accuracy;

    public LocationEvent(String latitude, String longitude, String accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
}
