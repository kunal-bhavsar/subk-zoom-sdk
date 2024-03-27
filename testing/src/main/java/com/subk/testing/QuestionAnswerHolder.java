package com.subk.testing;

import java.util.ArrayList;
import java.util.List;

import co.subk.zoomsdk.meeting.models.Answer;

public class QuestionAnswerHolder {
    private static QuestionAnswerHolder instance;
    private List<Answer> questionAnswers;

    private QuestionAnswerHolder() {
        questionAnswers = new ArrayList<>();
    }

    public static synchronized QuestionAnswerHolder getInstance() {
        if (instance == null) {
            instance = new QuestionAnswerHolder();
        }
        return instance;
    }

    public List<Answer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(List<Answer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }
}
