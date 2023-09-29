package co.subk.zoomsdk.event;

public class LocationEvent {
    public final double latitude;
    public final double longitude;
    public final float accuracy;

    public final String meetingId;

    public LocationEvent(double latitude, double longitude, float accuracy, String meetingId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.meetingId = meetingId;
    }
}
