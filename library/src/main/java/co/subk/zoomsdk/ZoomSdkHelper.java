package co.subk.zoomsdk;

import us.zoom.sdk.ZoomVideoSDK;

public class ZoomSdkHelper {
    public static final int RENDER_TYPE_ZOOMRENDERER = 0;

    public static final int RENDER_TYPE_OPENGLES = 1;

    public final static int REQUEST_SHARE_SCREEN_PERMISSION = 1001;

    public final static int REQUEST_SYSTEM_ALERT_WINDOW = 1002;

    public final static int REQUEST_SELECT_ORIGINAL_PIC = 1003;

    public final static String PARAM_USERNAME = "username";
    public final static String PARAM_TOKEN = "token";
    public final static String PARAM_PASSWORD = "password";
    public final static String PARAM_SESSION_NAME = "session_name";
    public final static String PARAM_RENDER_TYPE = "render_type";
    public final static String PARAM_TASK_ID = "task_id";
    public final static String PARAM_MEETING_ENTITY_ID = "meeting_entity_id";
    public final static String PARAM_ALLOW_TO_INVITE_ATTENDEE = "allow_to_invite_attendee";
    public final static String PARAM_ALLOW_TO_SHARE_SCREEN = "allow_to_share_screen";
    public final static String PARAM_ALLOW_TO_MUTE_AUDIO = "allow_to_mute_audio";
    public final static String PARAM_ALLOW_TO_HIDE_VIDEO = "allow_to_hide_video";
    public final static String PARAM_ALLOW_TO_END_MEETING = "allow_to_end_meeting";
    public final static String PARAM_ALLOW_TO_TAKE_SCREENSHOT = "allow_to_take_screenshot";
    public final static String PARAM_ALLOW_TO_GET_LOCATION = "allow_to_get_location";
    public static int stopShare() {
        return ZoomVideoSDK.getInstance().getShareHelper().stopShare();
    }

    public static int leaveSession(boolean input) {
        return ZoomVideoSDK.getInstance().leaveSession(input);
    }

    public static boolean isInSession() {
        return ZoomVideoSDK.getInstance().isInSession();
    }
}
