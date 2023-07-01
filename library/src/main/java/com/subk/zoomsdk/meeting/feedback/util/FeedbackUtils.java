package com.subk.zoomsdk.meeting.feedback.util;

import com.subk.zoomsdk.cmd.CmdFeedbackPushRequest;
import com.subk.zoomsdk.cmd.CmdFeedbackSubmitRequest;
import com.subk.zoomsdk.cmd.CmdHelper;
import com.subk.zoomsdk.cmd.FeedbackType;

public class FeedbackUtils {
    public static void submitFeedback(FeedbackType type) {
        CmdFeedbackSubmitRequest request = new CmdFeedbackSubmitRequest();
        request.feedbackType = type;
        CmdHelper.getInstance().sendCommand(request);
    }

    public static void pushFeedback() {
        CmdFeedbackPushRequest request = new CmdFeedbackPushRequest();
        CmdHelper.getInstance().sendCommand(request);
    }
}
