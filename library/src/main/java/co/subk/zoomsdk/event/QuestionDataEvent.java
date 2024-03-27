package co.subk.zoomsdk.event;

public class QuestionDataEvent {
    public final String id;
    public final String token;

    public QuestionDataEvent(String uuId, String clientToken) {
        this.id = uuId;
        this.token = clientToken;
    }
}
