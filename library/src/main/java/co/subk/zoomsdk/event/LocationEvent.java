package co.subk.zoomsdk.event;

public class LocationEvent {
    public final double latitude;
    public final double longitude;
    public final float accuracy;

    public LocationEvent(double latitude, double longitude, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
}
