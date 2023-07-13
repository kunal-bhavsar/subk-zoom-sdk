package co.subk.zoomsdk;

import android.content.Context;

import co.subk.zoomsdk.meeting.exceptions.ZoomInitializationException;
import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKInitParams;
import us.zoom.sdk.ZoomVideoSDKRawDataMemoryMode;

public class ZoomSdkInitializer {
    public static String initialize(Context context, boolean enableLog) throws ZoomInitializationException {
        ZoomVideoSDKInitParams params = new ZoomVideoSDKInitParams();
        params.domain = "zoom.us";
        params.enableLog = enableLog;
        params.videoRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.audioRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.shareRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;

        int ret = ZoomVideoSDK.getInstance().initialize(context, params);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            throw new ZoomInitializationException(ret);
        }

        return ZoomVideoSDK.getInstance().getSDKVersion();
    }
}
