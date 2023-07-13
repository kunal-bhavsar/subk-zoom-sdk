package co.subk.zoomsdk;

import us.zoom.sdk.ZoomVideoSDK;

public class ZoomSdkHelper {
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
