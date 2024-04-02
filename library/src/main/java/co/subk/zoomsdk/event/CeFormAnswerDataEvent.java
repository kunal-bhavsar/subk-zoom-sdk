package co.subk.zoomsdk.event;

import java.util.List;

import co.subk.zoomsdk.meeting.models.CeFormAnswer;


public class CeFormAnswerDataEvent {
    public final String id;
    public final String token;
    private final List<CeFormAnswer> ceFormAnswers;

    public CeFormAnswerDataEvent(String taskId, String clientToken, List<CeFormAnswer> answersList) {
        this.id = taskId;
        this.token = clientToken;
        this.ceFormAnswers = answersList;
    }

    public List<CeFormAnswer> getAnswers() {
        return ceFormAnswers;
    }
}
