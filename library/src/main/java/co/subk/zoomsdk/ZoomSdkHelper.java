package co.subk.zoomsdk;

import us.zoom.sdk.ZoomVideoSDK;

public class ZoomSdkHelper {
    public static final int RENDER_TYPE_ZOOMRENDERER = 0;

    public static final int RENDER_TYPE_OPENGLES = 1;

    public final static int REQUEST_SHARE_SCREEN_PERMISSION = 1001;

    public final static int REQUEST_SYSTEM_ALERT_WINDOW = 1002;

    public final static int REQUEST_SELECT_ORIGINAL_PIC = 1003;

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
