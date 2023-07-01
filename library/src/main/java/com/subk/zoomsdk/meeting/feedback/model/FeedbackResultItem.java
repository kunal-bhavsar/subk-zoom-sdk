package com.subk.zoomsdk.meeting.feedback.model;

import com.subk.zoomsdk.cmd.FeedbackType;



public class FeedbackResultItem {
    public FeedbackResultItem(FeedbackType feedbackType) {
        this.feedbackType = feedbackType;
    }
    public FeedbackType feedbackType;
    public int percent;
    public int responseCount;
    public String title;
    public int iconResId;
}
