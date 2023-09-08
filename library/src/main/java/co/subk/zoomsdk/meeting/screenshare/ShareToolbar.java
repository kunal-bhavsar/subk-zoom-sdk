package co.subk.zoomsdk.meeting.screenshare;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import co.subk.zoomsdk.R;
import us.zoom.sdk.ZoomVideoSDK;

public class ShareToolbar {
    private final static String TAG = "ShareToolbar";

    public interface Listener {
        void onClickStopShare();
    }

    private final WindowManager mWindowManager;

    private final Context mContext;

    private View contentView;
    private View stopShareLayout;
    private View shareAudioLayout;
    private Listener mListener;
    private Display mDisplay;

    float mLastRawX = -1f;
    float mLastRawY = -1f;

    public ShareToolbar(Listener listener, Context context) {
        mListener = listener;
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
    }

    private void init() {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_share_toolbar, null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        stopShareLayout = contentView.findViewById(R.id.stop_share_layout);
        shareAudioLayout = contentView.findViewById(R.id.audio_share_layout);

        if (stopShareLayout != null) {
            stopShareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClickStopShare();
                    }
                    destroy();
                }
            });
        }

        if (null != shareAudioLayout) {
            ImageView imageView = contentView.findViewById(R.id.img_share_audio);
            shareAudioLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean shareAudio = ZoomVideoSDK.getInstance().getShareHelper().isShareDeviceAudioEnabled();
                    ZoomVideoSDK.getInstance().getShareHelper().enableShareDeviceAudio(!shareAudio);
                    shareAudio = ZoomVideoSDK.getInstance().getShareHelper().isShareDeviceAudioEnabled();
                    Log.d(TAG, "shareAudio:" + shareAudio);
                    imageView.setImageResource(shareAudio ? R.drawable.zm_screenshare_audio_enable : R.drawable.zm_screenshare_audio_disable);
                }
            });
        }
    }

    public void destroy() {
        if (null != mWindowManager) {
            if (null != contentView) {
                mWindowManager.removeView(contentView);
                contentView = null;
            }
        }
    }

    public void showToolbar() {

        if (null == contentView) {
            init();
        }
        contentView.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = getWindowLayoutParamsType();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        int height = contentView.getHeight();
        if (height == 0) {
            height = 150;
        }
        layoutParams.x = 100;
        layoutParams.y = mDisplay.getHeight() - 100 - height;
        mWindowManager.addView(contentView, layoutParams);

    }

    private int getWindowLayoutParamsType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (Settings.canDrawOverlays(mContext))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_TOAST;
            }
        }
    }
}

