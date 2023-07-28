package co.subk.zoomsdk;

import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_OPENGLES;
import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_ZOOMRENDERER;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Random;

import co.subk.zoomsdk.cmd.CmdFeedbackPushRequest;
import co.subk.zoomsdk.cmd.CmdHandler;
import co.subk.zoomsdk.cmd.CmdHelper;
import co.subk.zoomsdk.cmd.CmdLowerThirdRequest;
import co.subk.zoomsdk.cmd.CmdRequest;
import co.subk.zoomsdk.meeting.BaseMeetingActivity;
import co.subk.zoomsdk.meeting.LowerThirdSettingFragment;
import co.subk.zoomsdk.meeting.feedback.data.FeedbackDataManager;
import co.subk.zoomsdk.meeting.feedback.view.FeedbackSubmitDialog;
import co.subk.zoomsdk.meeting.rawdata.RawDataRenderer;
import co.subk.zoomsdk.meeting.util.AudioRawDataUtil;
import co.subk.zoomsdk.meeting.util.SharePreferenceUtil;
import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoView;

public class MeetingActivity extends BaseMeetingActivity {

    private static final String TAG = "MeetingActivity";

    ZoomVideoSDKVideoView zoomCanvas;

    RawDataRenderer rawDataRenderer;

    private FrameLayout videoContain;

    private AudioRawDataUtil audioRawDataUtil;

    ImageView icon_screenshot;
    protected final static int REQUEST_VIDEO_AUDIO_CODE = 1010;

    TextView username;

    private CmdHandler mFeedbackPushHandler = new CmdHandler() {
        @Override
        public void onCmdReceived(CmdRequest request) {
            if (request instanceof CmdFeedbackPushRequest) {
                FeedbackSubmitDialog.show(MeetingActivity.this);
            }
        }
    };

    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        audioRawDataUtil = new AudioRawDataUtil(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        CmdHelper.getInstance().addListener(mFeedbackPushHandler);
        FeedbackDataManager.getInstance().startListenerFeedbackData();


    }

    @Override
    public void onSessionJoin() {
        super.onSessionJoin();
//        audioRawDataUtil.subscribeAudio();
        startMeetingService();
    }

    @Override
    public void onCommandChannelConnectResult(boolean isSuccess) {
        super.onCommandChannelConnectResult(isSuccess);
        if (isSuccess) {
            String name = SharePreferenceUtil.readString(this, LowerThirdSettingFragment.NAME_KEY, "");
            if (name != null && !name.isEmpty()) {
                String company = SharePreferenceUtil.readString(this, LowerThirdSettingFragment.COMPANY_KEY, "");
                int rgbIndex = SharePreferenceUtil.readInt(this, LowerThirdSettingFragment.RGB_KEY, 0);

                final CmdLowerThirdRequest request = new CmdLowerThirdRequest();
                request.user = null;
                request.name = name;
                request.companyName = company;
                request.rgb = CmdLowerThirdRequest.getColorTypeFromIndex(rgbIndex);

                CmdHelper.getInstance().sendCommand(request);
            }
        }
    }

    private ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected:");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected:");
        }
    };

    private void startMeetingService() {

       /* try {
            Intent myIntent = new Intent(this,Class.forName("co.subk.sarthi.NotificationService"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(myIntent);
            } else {
                startService(myIntent);
            }

            if(null!=serviceConnection){
                bindService(myIntent,serviceConnection, BIND_AUTO_CREATE);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/
        /*Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        if(null!=serviceConnection){
            bindService(intent,serviceConnection, BIND_AUTO_CREATE);
        }*/
    }

    private void stopMeetingService() {
        /*Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);*/
        /*try {
            Intent myIntent = new Intent(this,Class.forName("co.subk.sarthi.NotificationService"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopService(myIntent);
            } else {
                stopService(myIntent);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/
        try {
            if (null != serviceConnection) {
                unbindService(serviceConnection);
            }
            serviceConnection = null;
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }

    @Override
    public void onSessionLeave() {
        super.onSessionLeave();
        audioRawDataUtil.unSubscribe();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
    }

    @Override
    public void finish() {
        super.finish();
        stopMeetingService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CmdHelper.getInstance().removeListener(emojiHandler);
        CmdHelper.getInstance().removeListener(lowerThirdHandler);
//        stopMeetingService();
        handler.removeCallbacks(runnable);
        CmdHelper.getInstance().removeListener(mFeedbackPushHandler);
        FeedbackDataManager.getInstance().stopListenerFeedbackData();
        FeedbackDataManager.getInstance().clear();
    }

    @Override
    protected void initView() {
        super.initView();
        username = findViewById(R.id.username);
        icon_screenshot = findViewById(R.id.icon_screenshot);
        videoContain = findViewById(R.id.big_video_contain);
        videoContain.setOnClickListener(onEmptyContentClick);
        chatListView.setOnClickListener(onEmptyContentClick);

        icon_screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickScreenshot();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void capturePicture() {
        Bitmap bmp = Bitmap.createBitmap(zoomCanvas.getWidth(), zoomCanvas.getHeight(), Bitmap.Config.ARGB_8888);
        PixelCopy.request(surfaceView, bmp, i -> {
            Log.e("asdsa", String.valueOf(bmp.getHeight()));
            //imageView.setImageBitmap(bmp); //"iv_Result" is the image view
        }, new Handler(Looper.getMainLooper()));
    }

    View.OnClickListener onEmptyContentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!ZoomVideoSDK.getInstance().isInSession()) {
                return;
            }
            boolean isShow = actionBar.getVisibility() == View.VISIBLE;
            toggleView(!isShow);
//            if (BuildConfig.DEBUG) {
//                changeResolution();
//            }
        }
    };

    private void changeResolution() {
        if (renderType == RENDER_TYPE_OPENGLES) {
            int resolution = new Random().nextInt(3);
            resolution++;
            if (resolution > ZoomVideoSDKVideoResolution.VideoResolution_360P.getValue()) {
                resolution = 0;
            }
            ZoomVideoSDKVideoResolution size = ZoomVideoSDKVideoResolution.fromValue(resolution);
            Log.d(TAG, "changeResolution:" + size);
            if (null == currentShareUser && null != mActiveUser) {
                mActiveUser.getVideoPipe().subscribe(size, rawDataRenderer);
            }
        }
    }

    @Override
    public void onItemClick() {
        if (!ZoomVideoSDK.getInstance().isInSession()) {
            return;
        }
        boolean isShow = actionBar.getVisibility() == View.VISIBLE;
        toggleView(!isShow);
    }

    protected void toggleView(boolean show) {
        if (!show) {
            if (keyBoardLayout.isKeyBoardShow()) {
                keyBoardLayout.dismissChat(true);
                return;
            }
        }
        actionBar.setVisibility(show ? View.VISIBLE : View.GONE);
        chatListView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initMeeting() {
        ZoomVideoSDK.getInstance().addListener(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            zoomCanvas = new ZoomVideoSDKVideoView(this, !renderWithSurfaceView);
            videoContain.addView(zoomCanvas, 0, params);
        } else {
            rawDataRenderer = new RawDataRenderer(this);
            videoContain.addView(rawDataRenderer, 0, params);
        }

        ZoomVideoSDKUser mySelf = ZoomVideoSDK.getInstance().getSession().getMySelf();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                username.setText(mySelf.getUserName());
            }
        }, 5000);

        subscribeVideoByUser(mySelf);
        refreshFps();
        CmdHelper.getInstance().addListener(lowerThirdHandler);
        CmdHelper.getInstance().addListener(emojiHandler);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != mActiveUser) {
                if (mActiveUser == currentShareUser) {
                    updateFps(mActiveUser.getShareStatisticInfo());
                } else {
                    updateFps(mActiveUser.getVideoStatisticInfo());
                }
            }

            refreshFps();
        }
    };


    private void refreshFps() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 500);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_meeting;
    }


    @Override
    public void onClickSwitchShare(View view) {
        if (null != currentShareUser) {
            updateVideoAvatar(true);
            subscribeShareByUser(currentShareUser);
            selectAndScrollToUser(mActiveUser);
        }
    }

    protected void unSubscribe() {
        if (null != currentShareUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                currentShareUser.getVideoCanvas().unSubscribe(zoomCanvas);
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                currentShareUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }

        if (null != mActiveUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                mActiveUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }
    }

    protected void subscribeVideoByUser(ZoomVideoSDKUser user) {
        username.setText(user.getUserName());
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            ZoomVideoSDKVideoAspect aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_LetterBox;
            if (ZoomVideoSDK.getInstance().isInSession()) {
                aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original;
            }
            if (null != currentShareUser) {
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            }
            user.getVideoCanvas().unSubscribe(zoomCanvas);
            int ret=user.getVideoCanvas().subscribe(zoomCanvas, aspect);
            if(ret!= ZoomVideoSDKErrors.Errors_Success)

            {
                Toast.makeText(this,"subscribe error:"+ret,Toast.LENGTH_LONG).show();
            }
        } else {
            if (ZoomVideoSDK.getInstance().isInSession()) {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            } else {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Full_Filled);
            }
            if (null != currentShareUser) {
                currentShareUser.getSharePipe().unSubscribe(rawDataRenderer);
            }
            user.getVideoPipe().unSubscribe(rawDataRenderer);
            int ret= user.getVideoPipe().subscribe(ZoomVideoSDKVideoResolution.VideoResolution_1080P, rawDataRenderer);
            if(ret!= ZoomVideoSDKErrors.Errors_Success)
            {
                Toast.makeText(this,"subscribe error:"+ret,Toast.LENGTH_LONG).show();
            }
        }
        mActiveUser = user;
        onUserActive(mActiveUser);

        if (null != user.getVideoStatus()) {
            updateVideoAvatar(user.getVideoStatus().isOn());
        }

        if (null != currentShareUser) {
            btnViewShare.setVisibility(View.VISIBLE);
        } else {
            btnViewShare.setVisibility(View.GONE);
        }
    }


    protected void subscribeShareByUser(ZoomVideoSDKUser user) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            }
            user.getShareCanvas().subscribe(zoomCanvas, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original);
        } else {
            rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            rawDataRenderer.subscribe(user, ZoomVideoSDKVideoResolution.VideoResolution_1080P, true);
        }
        mActiveUser = user;
        onUserActive(mActiveUser);
        btnViewShare.setVisibility(View.GONE);
    }

    private void updateVideoAvatar(boolean isOn) {
        if (isOn) {
            videoOffView.setVisibility(View.GONE);
        } else {
            videoOffView.setVisibility(View.VISIBLE);
            text_fps.setVisibility(View.GONE);
            videoOffView.setImageResource(R.drawable.zm_conf_no_avatar);
        }
    }


    @Override
    public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {
        super.onUserLeave(userHelper, userList);
        if (null == mActiveUser || userList.contains(mActiveUser)) {
            subscribeVideoByUser(session.getMySelf());
            selectAndScrollToUser(session.getMySelf());
        }
    }

    @Override
    public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {
        super.onUserVideoStatusChanged(videoHelper, userList);

        if (null != mActiveUser && userList.contains(mActiveUser)) {
            updateVideoAvatar(mActiveUser.getVideoStatus().isOn());
//            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
//                if (null==currentShareUser&&mActiveUser.getVideoStatus().isOn()) {
//                    subscribeVideoByUser(mActiveUser);
//                    adapter.notifyDataSetChanged();
//                }
//            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // requestPermission(REQUEST_VIDEO_AUDIO_CODE);
    }

    protected boolean requestPermission(int code) {

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }


        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MeetingActivity.this, permissions, code);
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_AUDIO_CODE) {
            if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    /*&& (Build.VERSION.SDK_INT >= 31 && getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)*/)) {
                onPermissionGranted();
            }
            else
            {
                requestPermission(REQUEST_VIDEO_AUDIO_CODE);
            }
        }
    }

    protected void onPermissionGranted() {}

    @Override
    protected void onStartShareView() {
        super.onStartShareView();
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
            }
        } else {
            rawDataRenderer.unSubscribe();
        }
        adapter.clear(false);
    }

    @Override
    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        super.onUserShareStatusChanged(shareHelper, userInfo, status);
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            if (userInfo != session.getMySelf()) {
                subscribeShareByUser(userInfo);
                updateVideoAvatar(true);
                selectAndScrollToUser(userInfo);
            } else {
                if (!ZoomVideoSDK.getInstance().getShareHelper().isScreenSharingOut()) {
                    unSubscribe();
                    adapter.clear(false);
                }
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            currentShareUser = null;
            subscribeVideoByUser(userInfo);
            if (adapter.getItemCount() == 0) {
                adapter.addAll();
            }
            selectAndScrollToUser(userInfo);
        }
    }

    public void onClickScreenshot() {
        //Toast.makeText(this, "on click Screenhsot", Toast.LENGTH_SHORT).show();
        // requestPermission(REQUEST_VIDEO_AUDIO_CODE);
       /* if (ActivityCompat.checkSelfPermission(MeetingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MeetingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MeetingActivity.this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) MeetingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        }
        else
        {*/
        //takeScreenshot();
        //}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //   capturePicture();
        }

    }
    private void takeScreenshot() {
        Date now = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        String fileName =
                String.valueOf(System.currentTimeMillis()).replaceAll(":", ".") + ".jpg";

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/"  + fileName/*+ now + ".jpg"*/;

            // create bitmap screen capture
            View v1 = zoomCanvas/*getWindow().getDecorView().getRootView()*/;
            v1.setDrawingCacheEnabled(true);
            // Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());



            final Bitmap bitmap = Bitmap.createBitmap(zoomCanvas.getWidth(), zoomCanvas.getHeight(),
                    Bitmap.Config.ARGB_8888);
            // Make the request to copy.

           /* PixelCopy.request(zoomCanvas, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    Log.e(TAG, bitmap.toString());
                    //saveBitmapToFile(bitmap, contactId);
                } else {
                    Toast.makeText(this, "Faild", Toast.LENGTH_SHORT).show();
                }
            }, new Handler());
*/


            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }



    @Override
    public void onBackPressed() {
    }

//    @Override
//    protected ZoomVideoSDKRawDataPipeDelegate getMultiStreamDelegate() {
//        return rawDataRenderer;
//    }
//
//    @Override
//    protected ZoomVideoSDKVideoView getMultiStreamVideoView() {
//        return zoomCanvas;
//    }
}
