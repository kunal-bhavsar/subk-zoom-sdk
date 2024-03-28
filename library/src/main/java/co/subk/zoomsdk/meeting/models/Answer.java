package co.subk.zoomsdk.meeting.models;

import com.google.gson.annotations.SerializedName;

public class Answer {
    @SerializedName("id")
    private String id;

    @SerializedName("answer")
    private String answer;

    public Answer(String id, String answer) {
        this.id = id;
        this.answer = answer;
    }

}