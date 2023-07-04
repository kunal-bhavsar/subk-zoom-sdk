package co.subk.zoomsdk.meeting.feedback.util;

import co.subk.zoomsdk.cmd.CmdFeedbackPushRequest;
import co.subk.zoomsdk.cmd.CmdFeedbackSubmitRequest;
import co.subk.zoomsdk.cmd.CmdHelper;
import co.subk.zoomsdk.cmd.FeedbackType;

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
