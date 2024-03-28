package co.subk.zoomsdk.meeting.models;

public class Answer {
    private String id;
    private String answer;

    public Answer(String questionId, String answer) {
        this.id = questionId;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}