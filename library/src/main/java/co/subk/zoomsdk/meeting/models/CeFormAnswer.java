package co.subk.zoomsdk.meeting.models;

public class CeFormAnswer {
    private String id;

    private String answer;

    public CeFormAnswer(String id, String answer) {
        this.id = id;
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
