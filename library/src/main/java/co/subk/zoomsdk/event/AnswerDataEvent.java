package co.subk.zoomsdk.event;

import java.util.List;

import co.subk.zoomsdk.meeting.models.Answer;

public class AnswerDataEvent {
    public final String id;
    public final String token;
    private List<Answer> questionAnswers;

    public AnswerDataEvent(String taskId, String clientToken, List<Answer> questionAnswers) {
        this.id = taskId;
        this.token = clientToken;
        this.questionAnswers = questionAnswers;
    }

    public List<Answer> getQuestionAnswers() {
        return questionAnswers;
    }
}
