package co.subk.zoomsdk.meeting;


import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_OPENGLES;
import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_ZOOMRENDERER;
import static co.subk.zoomsdk.ZoomSdkHelper.REQUEST_SELECT_ORIGINAL_PIC;
import static co.subk.zoomsdk.ZoomSdkHelper.REQUEST_SHARE_SCREEN_PERMISSION;
import static co.subk.zoomsdk.ZoomSdkHelper.REQUEST_SYSTEM_ALERT_WINDOW;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import co.subk.zoomsdk.NetworkChangeReceiver;
import co.subk.zoomsdk.R;
import co.subk.zoomsdk.model.InternetEvent;
import co.subk.zoomsdk.model.SubkEvent;
import co.subk.zoomsdk.cmd.CmdHandler;
import co.subk.zoomsdk.cmd.CmdHelper;
import co.subk.zoomsdk.cmd.CmdLowerThirdRequest;
import co.subk.zoomsdk.cmd.CmdReactionRequest;
import co.subk.zoomsdk.cmd.CmdRequest;
import co.subk.zoomsdk.cmd.EmojiReactionType;
import co.subk.zoomsdk.meeting.feedback.data.FeedbackDataManager;
import co.subk.zoomsdk.meeting.feedback.view.FeedbackResultDialog;
import co.subk.zoomsdk.meeting.feedback.view.FeedbackSubmitDialog;
import co.subk.zoomsdk.meeting.screenshare.ShareToolbar;
import co.subk.zoomsdk.meeting.util.ErrorMsgUtil;
import co.subk.zoomsdk.meeting.util.SharePreferenceUtil;
import co.subk.zoomsdk.meeting.util.ZMAdapterOsBugHelper;
import co.subk.zoomsdk.meeting.view.ChatMsgAdapter;
import co.subk.zoomsdk.meeting.view.KeyBoardLayout;
import co.subk.zoomsdk.meeting.view.UserVideoAdapter;
import co.subk.zoomsdk.meeting.util.UserHelper;
import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKAudioOption;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKAudioStatus;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType;
import us.zoom.sdk.ZoomVideoSDKDelegate;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKInitParams;
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper;
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus;
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper;
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus;
import us.zoom.sdk.ZoomVideoSDKNetworkStatus;
import us.zoom.sdk.ZoomVideoSDKPasswordHandler;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataMemoryMode;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRawDataPipeDelegate;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKSessionContext;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoOption;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoStatisticInfo;
import us.zoom.sdk.ZoomVideoSDKVideoView;


public class BaseMeetingActivity extends AppCompatActivity implements ZoomVideoSDKDelegate, ShareToolbar.Listener, KeyBoardLayout.KeyBoardListener
        , UserVideoAdapter.ItemTapListener, ChatMsgAdapter.ItemClickListener {

    protected static final String TAG = BaseMeetingActivity.class.getSimpleName();


    protected Display display;

    protected DisplayMetrics displayMetrics;

    protected RecyclerView userVideoList;

    protected LinearLayout videoListContain;

    protected UserVideoAdapter adapter;


    private Intent mScreenInfoData;

    protected ShareToolbar shareToolbar;

    protected ImageView iconShare;

    protected ImageView iconVideo;

    protected ImageView iconAudio;

    protected ImageView iconMore;

    protected TextView practiceText;

    protected TextView sessionNameText;

    // protected TextView mtvInput;

    protected ImageView iconLock;

    protected View actionBar;

    protected ScrollView actionBarScroll;

    protected View btnViewShare;

    protected KeyBoardLayout keyBoardLayout;

    protected RecyclerView chatListView;

    private ChatMsgAdapter chatMsgAdapter;

    protected String myDisplayName = "";
    protected String meetingPwd = "";
    protected String sessionName;
    protected int renderType;

    protected ImageView videoOffView;

    private View shareViewGroup;

    private ImageView shareImageView;

    protected TextView text_fps;

    protected Handler handler = new Handler(Looper.getMainLooper());

    protected boolean isActivityPaused = false;

    protected ZoomVideoSDKUser mActiveUser;

    protected ZoomVideoSDKUser currentShareUser;

    protected ZoomVideoSDKSession session;

    protected boolean renderWithSurfaceView=true;

    protected boolean showCameraControl=false;

    protected LinearLayout panelRecordBtn;
    protected ZoomVideoSDKRecordingStatus status = ZoomVideoSDKRecordingStatus.Recording_Stop;

    private BroadcastReceiver mNetworkReceiver;

    androidx.appcompat.app.AlertDialog alertDialog;
    @NonNull
    private List<CmdLowerThirdRequest> lowerThirdRequests = new ArrayList<>();
    // private LowerThirdLayout lowerThirdLayout;

    protected CmdHandler emojiHandler = new CmdHandler() {
        @Override
        public void onCmdReceived(final CmdRequest request) {
            if (request instanceof CmdReactionRequest) {
                final CmdReactionRequest cmdReactionRequest = (CmdReactionRequest)request;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (BaseMeetingActivity.this.isDestroyed()) {
                            return;
                        }
                        adapter.onEmojiReceived(cmdReactionRequest, userVideoList);
                    }
                });
            }
        }
    };

    protected CmdHandler lowerThirdHandler = new CmdHandler() {
        @Override
        public void onCmdReceived(CmdRequest request) {
            if (request instanceof CmdLowerThirdRequest) {
                final CmdLowerThirdRequest cmdLowerThirdRequest = (CmdLowerThirdRequest)request;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (BaseMeetingActivity.this.isDestroyed()) {
                            return;
                        }
                        if (cmdLowerThirdRequest.user.equals(mActiveUser)) {
                            showLowerThird(cmdLowerThirdRequest);
                        }
                        boolean existRequest = false;
                        for (CmdLowerThirdRequest item : lowerThirdRequests) {
                            if (item.user.equals(cmdLowerThirdRequest.user)) {
                                item.name = cmdLowerThirdRequest.name;
                                item.companyName = cmdLowerThirdRequest.companyName;
                                item.rgb = cmdLowerThirdRequest.rgb;
                                existRequest = true;
                                break;
                            }
                        }
                        if (!existRequest) {
                            lowerThirdRequests.add(cmdLowerThirdRequest);
                        }
                    }
                });
            }
        }
    };

    boolean isVisitFirstTime = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVisitFirstTime = true;
        if (!renderWithSurfaceView) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);
        setContentView(getLayout());
        display = ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        Bundle bundle = getIntent().getExtras();

        initZoomSDK();

        setupZoom(bundle);


        session = ZoomVideoSDK.getInstance().getSession();
        ZoomVideoSDK.getInstance().addListener(BaseMeetingActivity.this);
        parseIntent(bundle);
        initView();
        initMeeting();
        updateSessionInfo();


        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void setupZoom(Bundle bundle)
    {
        String sessionName = "",name = "",password = "",tokens = "";
        if (null != bundle) {
            name = bundle.getString("name");
            password = bundle.getString("password");
            sessionName = bundle.getString("sessionName");
            tokens = bundle.getString("tokens");
        }

        // String tokens = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwidHBjIjoiTVkgVEFzayBhbmRyb2lkIiwic2Vzc2lvbl9rZXkiOiI4MDEyNmEwNS0zZDQ4LTRlODQtOTA0YS0zZTI3NzZjZmZhN2UiLCJ1c2VyX2lkZW50aXR5IjoiOTc2OTcxNTU1OSIsInJvbGVfdHlwZSI6MSwiaWF0IjoxNjgzNDczOTQ5LCJleHAiOjE2ODM0ODExNDksImFwcEtleSI6IkhUb2VQVlZIUm5oTjRFeXZkMXN0N0ZicjdYSWZCS3NPMUJhMyIsInRva2VuRXhwIjoxNjgzNDgxMTQ5LCJwd2QiOiIxNzU3MDUyNCIsImNsb3VkX3JlY29yZGluZ19vcHRpb24iOjB9.YvTZU4_4KiT5aWvX_al_JSDE_oIMAUBx4I2UrIaOyGc";
       /* myDisplayName = "9769715559";
        meetingPwd = "17570524";
        sessionName = "MY TAsk android";
        renderType = 0;*/

        // String sessionName = "MY TAsk android";//joinMeetingResponse.getTask().getTitle();
        //UserDetails userDetails = PreferenceHandler.getInstance().readUserDetails();
        //  String name = "9769715559";//userDetails.getUserName();
        //AdditionalInfo additionalInfo = joinMeetingResponse.getMeeting().getAdditionalInfo();
        //  String password = "17570524";//additionalInfo.getPassword();
        String token =  tokens;//additionalInfo.getSignature();


        ZoomVideoSDKAudioOption audioOption = new ZoomVideoSDKAudioOption();
        audioOption.connect = true;
        audioOption.mute = false;

        ZoomVideoSDKVideoOption videoOption = new ZoomVideoSDKVideoOption();
        videoOption.localVideoOn = true;

        ZoomVideoSDKSessionContext sessionContext = new ZoomVideoSDKSessionContext();
        sessionContext.audioOption = audioOption;
        sessionContext.videoOption = videoOption;
        sessionContext.sessionName = /*"Latest Meetign Arul"*/sessionName;
        sessionContext.userName = /*"7597371013"*/name;
        sessionContext.token = /*"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwidHBjIjoiTGF0ZXN0IE1lZXRpZ24gQXJ1bCIsInNlc3Npb25fa2V5IjoiODQ1MzhlODAtYjRkZC00MWY4LWI1YzUtM2JmYTZlMWMwNjRhIiwidXNlcl9pZGVudGl0eSI6Ijc1OTczNzEwMTMiLCJyb2xlX3R5cGUiOjEsImlhdCI6MTY4ODQ3MDg3NywiZXhwIjoxNjg4NDc4MDc3LCJhcHBLZXkiOiJIVG9lUFZWSFJuaE40RXl2ZDFzdDdGYnI3WElmQktzTzFCYTMiLCJ0b2tlbkV4cCI6MTY4ODQ3ODA3NywicHdkIjoiNDEwODA5NDUiLCJjbG91ZF9yZWNvcmRpbmdfb3B0aW9uIjowfQ.htwJBF77m-jiUtMoSv2bjSV2UkXE_msI8l2XwyEmBsc"*/token;
        sessionContext.sessionPassword = /*"41080945"*/password;
        sessionContext.sessionIdleTimeoutMins = 40;

        ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().joinSession(sessionContext);
        if (null == session) {
            Log.i(BaseMeetingActivity.class.getName(), "Session name :" + sessionContext.sessionName);
            Log.i(BaseMeetingActivity.class.getName(), "User name :" + sessionContext.userName);
            Log.i(BaseMeetingActivity.class.getName(), "Token :" + sessionContext.token);
            Log.i(BaseMeetingActivity.class.getName(), "Session password :" + sessionContext.sessionPassword);
            Log.e(BaseMeetingActivity.class.getName(), "Session is NULL");
            return;
        }
    }

    protected void initZoomSDK() {
        ZoomVideoSDKInitParams params = new ZoomVideoSDKInitParams();
        params.domain = "zoom.us";
        params.enableLog = true;
        params.videoRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.audioRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.shareRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;

        int ret = ZoomVideoSDK.getInstance().initialize(this.getApplicationContext(), params);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(ret), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, getString(R.string.launch_setting_version, ZoomVideoSDK.getInstance().getSDKVersion()), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        parseIntent(bundle);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        unSubscribe();
        adapter.clear(false);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    protected void parseIntent(Bundle bundle) {
        //Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            myDisplayName = bundle.getString("name");
            meetingPwd = bundle.getString("password");
            sessionName = bundle.getString("sessionName");
            renderType = bundle.getInt("render_type", RENDER_TYPE_ZOOMRENDERER);
        }

        /* sessionContext.userName = "Arull"*//*name*//*;
        sessionContext.token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwidHBjIjoiQXJ1bCBOZXcgTWVldGluZyIsInNlc3Npb25fa2V5IjoiY2ExODIzZmQtNTZiOS00MThjLWFjNjQtZDYyZTU1MDJhYWE0IiwidXNlcl9pZGVudGl0eSI6Ijk3Njk3MTU1NTkiLCJyb2xlX3R5cGUiOjEsImlhdCI6MTY4NzU5MjU4MSwiZXhwIjoxNjg3NTk5NzgxLCJhcHBLZXkiOiJIVG9lUFZWSFJuaE40RXl2ZDFzdDdGYnI3WElmQktzTzFCYTMiLCJ0b2tlbkV4cCI6MTY4NzU5OTc4MSwicHdkIjoiNDg1OTAwMTIiLCJjbG91ZF9yZWNvcmRpbmdfb3B0aW9uIjowfQ.UAqKbXnjnmD9lDP4u-PwVZoXiWRMMJ-2wRcQBLJ1EpI"*//*token*//*;
        sessionContext.sessionPassword = "48590012"*//*password*//*;*/
       /* String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwidHBjIjoiTGF0ZXN0IE1lZXRpZ24gQXJ1bCIsInNlc3Npb25fa2V5IjoiODQ1MzhlODAtYjRkZC00MWY4LWI1YzUtM2JmYTZlMWMwNjRhIiwidXNlcl9pZGVudGl0eSI6Ijc1OTczNzEwMTMiLCJyb2xlX3R5cGUiOjEsImlhdCI6MTY4ODQ3MDg3NywiZXhwIjoxNjg4NDc4MDc3LCJhcHBLZXkiOiJIVG9lUFZWSFJuaE40RXl2ZDFzdDdGYnI3WElmQktzTzFCYTMiLCJ0b2tlbkV4cCI6MTY4ODQ3ODA3NywicHdkIjoiNDEwODA5NDUiLCJjbG91ZF9yZWNvcmRpbmdfb3B0aW9uIjowfQ.htwJBF77m-jiUtMoSv2bjSV2UkXE_msI8l2XwyEmBsc";
        myDisplayName = "7597371013";
        meetingPwd = "41080945";
        sessionName = "Latest Meetign Arul";
        renderType = 0;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isActivityPaused) {
            resumeSubscribe();
        }
        isActivityPaused = false;
        refreshRotation();
        updateActionBarLayoutParams();
        updateChatLayoutParams();

        if (ZoomVideoSDK.getInstance().isInSession()) {
            int size = UserHelper.getAllUsers().size();
            if (size > 0 && adapter.getItemCount() == 0) {
                adapter.addAll();
                updateVideoListLayout();
                refreshUserListAdapter();
            }
        }
    }

    protected void resumeSubscribe() {
        if (null != currentShareUser) {
            subscribeShareByUser(currentShareUser);
        } else if (null != mActiveUser) {
            subscribeVideoByUser(mActiveUser);
        }

        if (ZoomVideoSDK.getInstance().isInSession()) {
            List<ZoomVideoSDKUser> userInfoList = UserHelper.getAllUsers();
            if (null != userInfoList && userInfoList.size() > 0) {
                List<ZoomVideoSDKUser> list = new ArrayList<>(userInfoList.size());
                for (ZoomVideoSDKUser userInfo : userInfoList) {
                    list.add(userInfo);
                }
                adapter.onUserJoin(list);
                selectAndScrollToUser(mActiveUser);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFpsOrientation();
        refreshRotation();
        updateActionBarLayoutParams();
        updateChatLayoutParams();
        updateSmallVideoLayoutParams();
    }

    private void updateFpsOrientation() {
        text_fps.setVisibility(View.GONE);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            text_fps = findViewById(R.id.text_fps_landscape);
        } else {
            text_fps = findViewById(R.id.text_fps);
        }
        if (ZoomVideoSDK.getInstance().isInSession()) {
            text_fps.setVisibility(View.GONE); // Was Visible Here Change to GOne as per requirement on 08/07/23 @arul
        }
    }


    private void updateSmallVideoLayoutParams() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoListContain.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            videoListContain.setGravity(Gravity.CENTER);
        }
    }

    private void updateChatLayoutParams() {
        if (chatMsgAdapter.getItemCount() > 0) {
            chatListView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
        }
    }

    private void updateActionBarLayoutParams() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) actionBar.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.topMargin = (int) (35 * displayMetrics.scaledDensity);
//            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            params.bottomMargin = (int) (22 * displayMetrics.scaledDensity);
            actionBarScroll.scrollTo(0, 0);
        } else {
            params.topMargin = 0;
//            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_bottom_margin);
        }
        actionBar.setLayoutParams(params);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterNetworkChanges();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
        if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ZMAdapterOsBugHelper.getInstance().stopListenOverlayPermissionChange(this);
            }
        }
        ZoomVideoSDK.getInstance().removeListener(this);
        adapter.onDestroyed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SHARE_SCREEN_PERMISSION:
                if (resultCode != RESULT_OK) {
                    break;
                }
                startShareScreen(data);
                break;
            case REQUEST_SYSTEM_ALERT_WINDOW:
                if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ZMAdapterOsBugHelper.getInstance().stopListenOverlayPermissionChange(this);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if ((!Settings.canDrawOverlays(this)) && (!ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged() || !ZMAdapterOsBugHelper.getInstance().ismCanDraw())) {
                        return;
                    }
                }
                onStartShareScreen(mScreenInfoData);
                break;
            case REQUEST_SELECT_ORIGINAL_PIC: {
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        if (null != selectedImage) {
                            if (currentShareUser == null) {
                                shareImageView.setImageURI(selectedImage);
                                shareViewGroup.setVisibility(View.VISIBLE);
                                int ret = ZoomVideoSDK.getInstance().getShareHelper().startShareView(shareImageView);
                                Log.d(TAG, "start share " + ret);
                                if (ret == ZoomVideoSDKErrors.Errors_Success) {
                                    onStartShareView();
                                } else {
                                    shareImageView.setImageBitmap(null);
                                    shareViewGroup.setVisibility(View.GONE);
                                    boolean isLocked = ZoomVideoSDK.getInstance().getShareHelper().isShareLocked();
                                    Toast.makeText(this, "Share Fail isLocked=" + isLocked + " ret:" + ret, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Other is sharing", Toast.LENGTH_LONG).show();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                break;
            }
        }
    }

    protected void onStartShareView() {

    }

    public void onClickStopShare(View view) {
        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
    }

    public void onSingleTap(ZoomVideoSDKUser user) {
//        if (user != mActiveUser) {
        subscribeVideoByUser(user);
//        }
    }

    protected void onUserActive(ZoomVideoSDKUser user) {
        CmdLowerThirdRequest cmdLowerThirdRequest = null;
        for (CmdLowerThirdRequest request : lowerThirdRequests) {
            if (request.user.equals(user)) {
                cmdLowerThirdRequest = request;
                break;
            }
        }
        showLowerThird(cmdLowerThirdRequest);
    }

    private void showLowerThird(@Nullable CmdLowerThirdRequest request) {
        if (request != null && SharePreferenceUtil.readBoolean(this, LowerThirdSettingBottomFragment.LOWER_THIRD_KEY, false)) {
            // lowerThirdLayout.setVisibility(View.VISIBLE);
            // lowerThirdLayout.updateNameTv(request.name);
            // lowerThirdLayout.updateCompanyTv(request.companyName);
            // lowerThirdLayout.updateColor(request.rgb);
        } else {
            // lowerThirdLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onKeyBoardChange(boolean isShow, int height, int inputHeight) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) chatListView.getLayoutParams();

        if (isShow) {
            params.gravity = Gravity.START | Gravity.BOTTOM;
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_13) + height + inputHeight;
        } else {
            params.gravity = Gravity.START | Gravity.BOTTOM;
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_160);
        }
        chatListView.setLayoutParams(params);
        if (chatMsgAdapter.getItemCount() > 0) {
            chatListView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
        }
    }

    protected void onStartShareScreen(Intent data) {
        if (null == shareToolbar) {
            shareToolbar = new ShareToolbar(this,this);
        }

        EventBus.getDefault().post(new SubkEvent("Screenshare Started"));

        /*if (Build.VERSION.SDK_INT >= 29) {
            //MediaProjection  need service with foregroundServiceType mediaProjection in android Q
            boolean hasForegroundNotification = NotificationMgr.hasNotification(NotificationMgr.PT_NOTICICATION_ID);
            if (!hasForegroundNotification) {

                EventBus.getDefault().post(new CartEvent("new Cart Item"));

                *//*Intent intent = new Intent(this, NotificationService.class);
                startForegroundService(intent);*//*
            }
        }*/
        int ret = ZoomVideoSDK.getInstance().getShareHelper().startShareScreen(data);
        if (ret == ZoomVideoSDKErrors.Errors_Success) {
            shareToolbar.showToolbar();
            showDesktop();
        }
    }

    protected void showDesktop() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(home);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onClickStopShare() {
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
            showMeetingActivity();
        }
    }

    private void showMeetingActivity() {
        Intent intent = new Intent(getApplicationContext(), IntegrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setAction(IntegrationActivity.ACTION_RETURN_TO_CONF);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @SuppressLint("NewApi")
    protected void startShareScreen(Intent data) {
        if (data == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
            if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged())
                ZMAdapterOsBugHelper.getInstance().startListenOverlayPermissionChange(this);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            mScreenInfoData = data;
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            onStartShareScreen(data);
        }
    }

    protected void refreshRotation() {
        int displayRotation = display.getRotation();
        Log.d(TAG, "rotateVideo:" + displayRotation);
        ZoomVideoSDK.getInstance().getVideoHelper().rotateMyVideo(displayRotation);
    }

    protected void initMeeting() {

    }

    public void updateFps(final ZoomVideoSDKVideoStatisticInfo statisticInfo) {
        if (null == statisticInfo) {
            return;
        }
        final int fps = statisticInfo.getFps();
        text_fps.post(new Runnable() {
            @Override
            public void run() {
                if (statisticInfo.getWidth() > 0 && statisticInfo.getHeight() > 0) {
                    text_fps.setVisibility(View.GONE); // Was Visible Here Change to GOne as per requirement on 08/07/23 @arul
                    String text = statisticInfo.getWidth() + "X" + statisticInfo.getHeight() + " " + fps + " FPS";
                    if (fps < 10) {
                        text = statisticInfo.getWidth() + "X" + statisticInfo.getHeight() + "  " + fps + " FPS";
                    }
                    text_fps.setText(text);
                } else {
                    text_fps.setVisibility(View.GONE);
                }
            }
        });
    }


    protected void initView() {
        sessionNameText = findViewById(R.id.sessionName);
        // mtvInput = findViewById(R.id.tv_input);
        userVideoList = findViewById(R.id.userVideoList);
        videoListContain = findViewById(R.id.video_list_contain);
        adapter = new UserVideoAdapter(this, this, renderType);
        userVideoList.setItemViewCacheSize(0);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setItemPrefetchEnabled(false);
        userVideoList.setLayoutManager(layoutManager);
        userVideoList.setAdapter(adapter);

        text_fps = findViewById(R.id.text_fps);

        iconVideo = findViewById(R.id.icon_video);
        iconAudio = findViewById(R.id.icon_audio);
        iconMore = findViewById(R.id.icon_more);
        practiceText = findViewById(R.id.text_meeting_user_size);

        keyBoardLayout = findViewById(R.id.chat_input_layout);

        chatListView = findViewById(R.id.chat_list);

        chatListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        chatMsgAdapter = new ChatMsgAdapter(this);
        chatListView.setAdapter(chatMsgAdapter);

        keyBoardLayout.setKeyBoardListener(this);
        actionBar = findViewById(R.id.action_bar);

        iconLock = findViewById(R.id.meeting_lock_status);

        iconShare = findViewById(R.id.icon_share);
        actionBarScroll = findViewById(R.id.action_bar_scroll);

        videoOffView = findViewById(R.id.video_off_tips);

        btnViewShare = findViewById(R.id.btn_view_share);

        shareViewGroup = findViewById(R.id.share_view_group);
        shareImageView = findViewById(R.id.share_image);

        panelRecordBtn = findViewById(R.id.panelRecordBtn);

        // lowerThirdLayout = findViewById(R.id.layout_lower_third);
        // lowerThirdLayout.setVisibility(View.GONE);

        onKeyBoardChange(false, 0, 30);
        final int margin = (int) (5 * displayMetrics.scaledDensity);
        userVideoList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(margin, 0, margin, 0);
            }
        });

        userVideoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    int index = linearLayoutManager.findFirstVisibleItemPosition();
                    int left = view.getLeft();
                    if (left < 0) {
                        if (-left > view.getWidth() / 2) {
                            index = index + 1;
                            if (index == adapter.getItemCount() - 1) {
                                recyclerView.scrollBy(view.getWidth(), 0);
                            } else {
                                recyclerView.scrollBy(view.getWidth() + left + 2 * margin, 0);
                            }
                        } else {
                            recyclerView.scrollBy(left - margin, 0);
                        }
                        if (index == 0) {
                            recyclerView.scrollTo(0, 0);
                        }
                    }
                    view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    scrollVideoViewForMargin(view);

                }
            }
        });
    }

    @Override
    public void onItemClick() {

    }

    public void onClickSwitchShare(View view) {

    }

    protected int getLayout() {
        return 0;
    }

    public void onClickInfo(View view) {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_session_info);

        final TextView sessionNameText = builder.findViewById(R.id.info_session_name);
        final TextView sessionPwdText = builder.findViewById(R.id.info_session_pwd);
        final TextView sessionUserSizeText = builder.findViewById(R.id.info_user_size);
        int size = UserHelper.getAllUsers().size();
        if (size <= 0) {
            size = 1;
        }
        sessionUserSizeText.setText(size + "");

        ZoomVideoSDKSession sessionInfo = ZoomVideoSDK.getInstance().getSession();
        meetingPwd = sessionInfo.getSessionPassword();
        sessionPwdText.setText(meetingPwd);

        if (TextUtils.isEmpty(meetingPwd)) {
            sessionPwdText.setText("Not set");
            sessionPwdText.setTextColor(getResources().getColor(R.color.color_not_set));
        }

        String name = sessionInfo.getSessionName();
        if (null == name) {
            name = "";
        }
        sessionNameText.setText(name);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.show();


    }

    public void onClickEnd(View view) {
        ZoomVideoSDKUser userInfo = session.getMySelf();

        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.setContentView(R.layout.dialog_leave_alert);
        builder.findViewById(R.id.btn_leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
                releaseResource();
                int ret = ZoomVideoSDK.getInstance().leaveSession(false);
                Log.d(TAG, "leaveSession ret = " + ret);
            }
        });

        boolean end = false;
        if (null != userInfo && userInfo.isHost()) {
            ((TextView) builder.findViewById(R.id.btn_end)).setText(getString(R.string.leave_end_text));
            end = true;
        }
        final boolean endSession = end;
        builder.findViewById(R.id.btn_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
                if (endSession) {
                    releaseResource();
                    int ret = ZoomVideoSDK.getInstance().leaveSession(true);
                    Log.d(TAG, "leaveSession ret = " + ret);
                }
            }
        });
        builder.show();

    }

    private void releaseResource() {
        unSubscribe();
        adapter.clear(true);
        actionBar.setVisibility(View.GONE);
        // mtvInput.setVisibility(View.GONE);
    }

    public void onClickVideo(View view) {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getVideoStatus().isOn()) {
            ZoomVideoSDK.getInstance().getVideoHelper().stopVideo();
        } else {
            ZoomVideoSDK.getInstance().getVideoHelper().startVideo();
        }
    }


    public void onClickShare(View view) {
        ZoomVideoSDKShareHelper sdkShareHelper = ZoomVideoSDK.getInstance().getShareHelper();

        boolean isShareLocked = sdkShareHelper.isShareLocked();
        if (isShareLocked && !session.getMySelf().isHost()) {
            Toast.makeText(this, "Share is locked by host", Toast.LENGTH_SHORT).show();
            return;
        }

        if (null != currentShareUser && currentShareUser != session.getMySelf()) {
            Toast.makeText(this, "Other is shareing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentShareUser == session.getMySelf()) {
            sdkShareHelper.stopShare();
            return;
        }

        final Dialog builder = new Dialog(this, R.style.MyDialog);

        builder.setContentView(R.layout.dialog_share_view);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.findViewById(R.id.group_screen_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
                    ZoomVideoSDK.getInstance().getShareHelper().stopShare();
                    if (null != shareToolbar) {
                        shareToolbar.destroy();
                    }
                } else {
                    askScreenSharePermission();
                }
            }
        });

        builder.findViewById(R.id.group_picture_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFromGallery();
                builder.dismiss();
            }
        });
        builder.show();
    }


    private void selectFromGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_ORIGINAL_PIC);
    }

    protected void toggleView(boolean show) {

    }

    public void onClickChat(View view) {
        keyBoardLayout.showChat();
        toggleView(true);
    }

    public void onClickAudio(View view) {

        // EventBus.getDefault().post(new CartEvent("new Cart Item audio"));

        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomVideoSDKAudioStatus.ZoomVideoSDKAudioType.ZoomVideoSDKAudioType_None) {
            ZoomVideoSDK.getInstance().getAudioHelper().startAudio();
        } else {
            if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                ZoomVideoSDK.getInstance().getAudioHelper().unMuteAudio(zoomSDKUserInfo);
            } else {
                ZoomVideoSDK.getInstance().getAudioHelper().muteAudio(zoomSDKUserInfo);
            }
        }
    }

    public void onClickMoreSpeaker() {
        boolean speaker = ZoomVideoSDK.getInstance().getAudioHelper().getSpeakerStatus();
        ZoomVideoSDK.getInstance().getAudioHelper().setSpeaker(!speaker);

    }

    public void onClickMoreSwitchCamera() {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getVideoStatus().isHasVideoDevice() && zoomSDKUserInfo.getVideoStatus().isOn()) {
            ZoomVideoSDK.getInstance().getVideoHelper().switchCamera();
            refreshRotation();
        }
    }

    private void onClickStartCloudRecord() {
        int error = ZoomVideoSDK.getInstance().getRecordingHelper().startCloudRecording();
        if (error != ZoomVideoSDKErrors.Errors_Success) {
            Toast.makeText(this, "start cloud record error: " + ErrorMsgUtil.getMsgByErrorCode(error) + ". Error code: "+error, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isSpeakerOn() {
        return ZoomVideoSDK.getInstance().getAudioHelper().getSpeakerStatus();
    }



    public void onClickMore(View view) {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_more_action);

        final View llSwitchCamera = builder.findViewById(R.id.llSwitchCamera);
        final View llSpeaker = builder.findViewById(R.id.llSpeaker);
        final View llStartRecord = builder.findViewById(R.id.llStartRecord);
        final View llRecording = builder.findViewById(R.id.llRecordStatus);
        final View llFeedback = builder.findViewById(R.id.llFeedback);
        final TextView tvFeedback = builder.findViewById(R.id.tvFeedback);
        final TextView tvSpeaker = builder.findViewById(R.id.tvSpeaker);
        final ImageView ivSpeaker = builder.findViewById(R.id.ivSpeaker);

        if (zoomSDKUserInfo.getVideoStatus().isOn()) {
            llSwitchCamera.setVisibility(View.VISIBLE);
            llSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    builder.dismiss();
                    onClickMoreSwitchCamera();
                }
            });
        } else {
            llSwitchCamera.setVisibility(View.GONE);
        }
        if (canSwitchAudioSource()) {
            llSpeaker.setVisibility(View.VISIBLE);
            llSpeaker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    builder.dismiss();
                    onClickMoreSpeaker();
                }
            });
        } else {
            llSpeaker.setVisibility(View.GONE);
        }

        llRecording.setVisibility(View.GONE);
        llStartRecord.setVisibility(View.GONE);
        if (
                canStartRecord() && status != ZoomVideoSDKRecordingStatus.Recording_DiskFull) {
            if (status == ZoomVideoSDKRecordingStatus.Recording_Stop) {
                llStartRecord.setVisibility(View.VISIBLE);
                llStartRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                        onClickStartCloudRecord();
                    }
                });
            } else {
                llRecording.setVisibility(View.VISIBLE);
                final ImageView recordImg = llRecording.findViewById(R.id.imgRecording);
                final ImageView pauseRecordImg = llRecording.findViewById(R.id.btn_pause_record);
                final ImageView stopRecordImg = llRecording.findViewById(R.id.btn_stop_record);
//                final ProgressBar startRecordProgressBar = llRecording.findViewById(R.id.progressStartingRecord);
                final TextView recordStatus = llRecording.findViewById(R.id.txtRecordStatus);

                pauseRecordImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (status == ZoomVideoSDKRecordingStatus.Recording_Pause) {
                            int error = ZoomVideoSDK.getInstance().getRecordingHelper().resumeCloudRecording();
                            if (error == ZoomVideoSDKErrors.Errors_Success) {
                                pauseRecordImg.setImageResource(R.drawable.zm_record_btn_pause);
                                recordImg.setVisibility(View.VISIBLE);
                                recordStatus.setText("Recording…");
                            } else {
                                Toast.makeText(BaseMeetingActivity.this, "resume cloud record error: " + ErrorMsgUtil.getMsgByErrorCode(error) + ". Error code: "+error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            int error = ZoomVideoSDK.getInstance().getRecordingHelper().pauseCloudRecording();
                            if (error == ZoomVideoSDKErrors.Errors_Success) {
                                pauseRecordImg.setImageResource(R.drawable.zm_record_btn_resume);
                                recordImg.setVisibility(View.GONE);
                                recordStatus.setText("Recording Paused");
                            } else {
                                Toast.makeText(BaseMeetingActivity.this, "pause cloud record error: " + ErrorMsgUtil.getMsgByErrorCode(error) + ". Error code: "+error, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });


                stopRecordImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int error = ZoomVideoSDK.getInstance().getRecordingHelper().stopCloudRecording();
                        if (error == ZoomVideoSDKErrors.Errors_Success) {
                            builder.dismiss();
                        } else {
                            Toast.makeText(BaseMeetingActivity.this, "stop cloud record error: " + ErrorMsgUtil.getMsgByErrorCode(error) + ". Error code: "+error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                if (status == ZoomVideoSDKRecordingStatus.Recording_Pause) {
                    recordImg.setVisibility(View.GONE);
                    recordStatus.setText("Recording Paused");
                } else {
                    recordImg.setVisibility(View.VISIBLE);
                    recordStatus.setText("Recording…");
                }

                pauseRecordImg.setVisibility(View.VISIBLE);
                stopRecordImg.setVisibility(View.VISIBLE);
                pauseRecordImg.setImageResource(status == ZoomVideoSDKRecordingStatus.Recording_Pause ? R.drawable.zm_record_btn_resume : R.drawable.zm_record_btn_pause);
//                startRecordProgressBar.setVisibility(View.GONE);
            }
        }

        if (isSpeakerOn()) {
            tvSpeaker.setText("Turn off Speaker");
            ivSpeaker.setImageResource(R.drawable.icon_speaker_off);
        } else {
            tvSpeaker.setText("Turn on Speaker");
            ivSpeaker.setImageResource(R.drawable.icon_speaker_on);
        }

        showRaiseHand(builder);
        showEmojiPanel(builder);
        showLowerThirdBtn(builder);
        String feedbackText;
        if (ZoomVideoSDK.getInstance().getSession().getMySelf().isHost()) {
            int count = FeedbackDataManager.getInstance().getFeedbackCount();
            feedbackText = getResources().getString(R.string.more_feedbacks);
            if (count > 0) {
                feedbackText += "(" + count + ")";
            }
            tvFeedback.setText("");
        } else {
            feedbackText = getResources().getString(R.string.more_feedback_session);
        }
        tvFeedback.setText(feedbackText);
        llFeedback.setVisibility(View.GONE);
        llFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ZoomVideoSDK.getInstance().getSession().getMySelf().isHost()) {
                    FeedbackResultDialog.show(BaseMeetingActivity.this);
                } else {
                    FeedbackSubmitDialog.show(BaseMeetingActivity.this);
                }
                builder.dismiss();
            }
        });

        if (showCameraControl) {
            builder.findViewById(R.id.camera_control).setVisibility(View.VISIBLE);
           /* builder.findViewById(R.id.btn_request).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_give_up).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_left).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_right).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_up).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_down).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_zoom_in).setOnClickListener(cameraControlListener);
            builder.findViewById(R.id.btn_zoom_out).setOnClickListener(cameraControlListener);*/


            builder.findViewById(R.id.btn_request).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;



                    if (null == user) {
                        user = users.get(0);
                    }
                    ret=user.getRemoteCameraControlHelper().requestControlRemoteCamera();

                }
            });
            builder.findViewById(R.id.btn_give_up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().giveUpControlRemoteCamera();
                    if (ret == 0) {
                        feccUser = null;
                    }
                }
            });
            builder.findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().turnLeft(range);
                }
            });
            builder.findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().turnRight(range);
                }
            });
            builder.findViewById(R.id.btn_up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().turnUp(range);
                }
            });
            builder.findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret= user.getRemoteCameraControlHelper().turnDown(range);
                }
            });
            builder.findViewById(R.id.btn_zoom_in).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().zoomIn(range);
                }
            });
            builder.findViewById(R.id.btn_zoom_out).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ZoomVideoSDKUser> users = session.getRemoteUsers();
                    if (null == users || users.isEmpty()) {
                        return;
                    }
                    ZoomVideoSDKUser user = feccUser;
                    int ret=0;
                    if (null == user && view.getId() != R.id.btn_request) {
                        Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int range = 100;


                    ret=user.getRemoteCameraControlHelper().zoomOut(range);
                }
            });
        }



        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.show();
    }

   /* private View.OnClickListener cameraControlListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            List<ZoomVideoSDKUser> users = session.getRemoteUsers();
            if (null == users || users.isEmpty()) {
                return;
            }
            ZoomVideoSDKUser user = feccUser;
            int ret=0;
            if (null == user && v.getId() != R.id.btn_request) {
                Toast.makeText(BaseMeetingActivity.this, "need request and approve ", Toast.LENGTH_SHORT).show();
                return;
            }
            int range = 100;
            switch (v.getId()) {
                case R.id.btn_request: {
                    if (null == user) {
                        user = users.get(0);
                    }
                    ret=user.getRemoteCameraControlHelper().requestControlRemoteCamera();
                    break;
                }
                case R.id.btn_give_up: {
                    ret=user.getRemoteCameraControlHelper().giveUpControlRemoteCamera();
                    if (ret == 0) {
                        feccUser = null;
                    }
                    break;
                }
                case R.id.btn_left: {
                    ret=user.getRemoteCameraControlHelper().turnLeft(range);
                    break;
                }
                case R.id.btn_right: {
                    ret=user.getRemoteCameraControlHelper().turnRight(range);
                    break;
                }
                case R.id.btn_up: {
                    ret=user.getRemoteCameraControlHelper().turnUp(range);
                    break;
                }
                case R.id.btn_down: {
                    ret= user.getRemoteCameraControlHelper().turnDown(range);
                    break;
                }
                case R.id.btn_zoom_in: {
                    ret=user.getRemoteCameraControlHelper().zoomIn(range);
                    break;
                }
                case R.id.btn_zoom_out: {
                    ret=user.getRemoteCameraControlHelper().zoomOut(range);
                    break;
                }
            }
            Toast.makeText(BaseMeetingActivity.this,"ret:"+ret,Toast.LENGTH_SHORT).show();
        }
    };
*/

    private void checkMoreAction() {
        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        // SET THIS TO VISIBLE FOR SHOWING MORE BUTTON IN SIDE RIBBON
        iconMore.setVisibility(View.VISIBLE);
    }


    private boolean canSwitchAudioSource() {
        return ZoomVideoSDK.getInstance().getAudioHelper().canSwitchSpeaker();

    }


    private boolean canStartRecord() {
        return ZoomVideoSDK.getInstance().getRecordingHelper().canStartRecording() == ZoomVideoSDKErrors.Errors_Success;
    }

    private void showRaiseHand(final Dialog dialog) {
        final LinearLayout llRaiseHand = dialog.findViewById(R.id.llRaiseHand);
        final TextView tvRaiseHand = dialog.findViewById(R.id.tvRaiseHand);
        final ImageView ivRaiseHand = dialog.findViewById(R.id.ivRaiseHand);
        if (adapter.isHandRaised()) {
            tvRaiseHand.setText("Low Hand");
            ivRaiseHand.setImageResource(R.drawable.low_hand);
        } else {
            tvRaiseHand.setText("Raise Hand");
            ivRaiseHand.setImageResource(R.drawable.raise_hand);
        }
        llRaiseHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CmdReactionRequest request = new CmdReactionRequest();
                request.user = null;
                request.reactionType = adapter.isHandRaised() ? EmojiReactionType.LowHand : EmojiReactionType.RaisedHand;
                CmdHelper.getInstance().sendCommand(request);
                dialog.dismiss();
            }
        });
    }

    private void showEmojiPanel(@NonNull final Dialog dialog) {
        /*View.OnClickListener listener = new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                EmojiReactionType type = EmojiReactionType.None;
                switch (v.getId()) {
                    case R.id.btnClap:
                        type = EmojiReactionType.Clap;
                        break;
                    case R.id.btnThumbup:
                        type = EmojiReactionType.Thumbsup;
                        break;
                    case R.id.btnHeart:
                        type = EmojiReactionType.Heart;
                        break;
                    case R.id.btnJoy:
                        type = EmojiReactionType.Joy;
                        break;
                    case R.id.btnOpenMouth:
                        type = EmojiReactionType.Openmouth;
                        break;
                    case R.id.btnTada:
                        type = EmojiReactionType.Tada;
                        break;
                }
                CmdReactionRequest cmdReactionRequest = new CmdReactionRequest();
                cmdReactionRequest.user = null;
                cmdReactionRequest.reactionType = type;
                CmdHelper.getInstance().sendCommand(cmdReactionRequest);
                dialog.dismiss();
            }
        };*/
        /*dialog.findViewById(R.id.btnClap).setOnClickListener(listener);
        dialog.findViewById(R.id.btnThumbup).setOnClickListener(listener);
        dialog.findViewById(R.id.btnHeart).setOnClickListener(listener);
        dialog.findViewById(R.id.btnJoy).setOnClickListener(listener);
        dialog.findViewById(R.id.btnOpenMouth).setOnClickListener(listener);
        dialog.findViewById(R.id.btnTada).setOnClickListener(listener);*/
        dialog.findViewById(R.id.llEmojis).setBackground(getDrawable(R.drawable.more_action_last_bg));
    }

    private void showLowerThirdBtn(@NonNull final Dialog dialog) {
        final View llLowerThird = dialog.findViewById(R.id.llLowerThird);
        llLowerThird.setVisibility(View.GONE);
        llLowerThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LowerThirdSettingBottomFragment fragment = LowerThirdSettingBottomFragment.newInstance();
                fragment.lowerThirdDisableListener = new LowerThirdSettingBottomFragment.LowerThirdDisableListener() {
                    @Override
                    public void onLowerThirdDisabled() {
                        showLowerThird(null);
                    }

                    @Override
                    public void onLowerThirdEnabled() {
                        onUserActive(mActiveUser);
                    }
                };
                fragment.show(BaseMeetingActivity.this.getSupportFragmentManager(), "LowerThirdSettingBottomFragment");
                dialog.dismiss();
            }
        });
    }

    @SuppressLint("NewApi")
    protected void askScreenSharePermission() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            return;
        }
        MediaProjectionManager mgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mgr != null) {
            Intent intent = mgr.createScreenCaptureIntent();
            try {
                startActivityForResult(intent, REQUEST_SHARE_SCREEN_PERMISSION);
            } catch (Exception e) {
                Log.e(TAG, "askScreenSharePermission failed");
            }
        }
    }

    protected void updateSessionInfo() {
        ZoomVideoSDKSession sessionInfo = ZoomVideoSDK.getInstance().getSession();
        if (ZoomVideoSDK.getInstance().isInSession()) {
            int size = UserHelper.getAllUsers().size();
            if (size <= 0) {
                size = 1;
            }
            practiceText.setText("Participants:" + size);
            if (sessionInfo != null) meetingPwd = sessionInfo.getSessionPassword();
            // mtvInput.setVisibility(View.VISIBLE);
            text_fps.setVisibility(View.GONE); // Was Visible Here Change to GOne as per requirement on 08/07/23 @arul
        } else {
            if (keyBoardLayout.isKeyBoardShow()) {
                keyBoardLayout.dismissChat(true);
                return;
            }
            actionBar.setVisibility(View.GONE);
            // mtvInput.setVisibility(View.GONE);
            text_fps.setVisibility(View.GONE);
            practiceText.setText("Connecting ...");
        }
        if (sessionInfo != null) sessionNameText.setText(sessionInfo.getSessionName());
        if (TextUtils.isEmpty(meetingPwd)) {
            iconLock.setImageResource(R.drawable.unlock);
        } else {
            iconLock.setImageResource(R.drawable.small_lock);
        }
    }


    protected void unSubscribe() {

    }

    @Override
    public void onSessionJoin() {
        Log.d(TAG, "onSessionJoin ");
        updateSessionInfo();
        updateFpsOrientation();
        actionBar.setVisibility(View.VISIBLE);
        if (ZoomVideoSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomVideoSDK.getInstance().getShareHelper().stopShare();
        }

        adapter.onUserJoin(UserHelper.getAllUsers());
        refreshUserListAdapter();
        // mtvInput.setVisibility(View.VISIBLE);
    }


    @Override
    public void onSessionLeave() {
        Log.d(TAG, "onSessionLeave");
        finish();
    }

    @Override
    public void onError(int errorcode) {
        Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(errorcode) + ". Error code: "+errorcode, Toast.LENGTH_LONG).show();
        if (errorcode == ZoomVideoSDKErrors.Errors_Session_Disconnect) {
            unSubscribe();
            adapter.clear(true);
            updateSessionInfo();
            currentShareUser = null;
            mActiveUser=null;
            chatMsgAdapter.clear();
            chatListView.setVisibility(View.GONE);
            btnViewShare.setVisibility(View.GONE);
        } else if (errorcode == ZoomVideoSDKErrors.Errors_Session_Reconncting) {
            //start preview
            subscribeVideoByUser(session.getMySelf());
        } else {
            ZoomVideoSDK.getInstance().leaveSession(false);
            finish();
        }

    }

    protected void subscribeVideoByUser(ZoomVideoSDKUser user) {

    }

    protected void subscribeShareByUser(ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserJoin(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {

        Log.d(TAG, "onUserJoin " + userList.size());
        updateVideoListLayout();
        if (!isActivityPaused) {
            adapter.onUserJoin(userList);
        }
        refreshUserListAdapter();
        updateSessionInfo();
    }

    protected void selectAndScrollToUser(ZoomVideoSDKUser user) {
        if (null == user) {
            return;
        }
        adapter.updateSelectedVideoUser(user);
        int index = adapter.getIndexByUser(user);
        if (index >= 0) {
            LinearLayoutManager manager = (LinearLayoutManager) userVideoList.getLayoutManager();
            int first = manager.findFirstVisibleItemPosition();
            int last = manager.findLastVisibleItemPosition();
            if (index > last || index < first) {
                userVideoList.scrollToPosition(index);
                adapter.notifyDataSetChanged();
            }
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
        View view = linearLayoutManager.getChildAt(0);
        if (null != view) {
            scrollVideoViewForMargin(view);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    scrollVideoViewForMargin(view);
                }
            }, 50);
        }
    }

    private void scrollVideoViewForMargin(View view) {
        if (null == view) {
            return;
        }
        int left = view.getLeft();
        int margin = 5;
        if (left > margin || left <= 0) {
            userVideoList.scrollBy(left - margin, 0);
        }
    }

    private void refreshUserListAdapter() {
        if (adapter.getItemCount() > 0) {
            videoListContain.setVisibility(View.VISIBLE);
            if (adapter.getSelectedVideoUser() == null) {
                ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
                if (null != zoomSDKUserInfo) {
                    selectAndScrollToUser(zoomSDKUserInfo);
                }
            }
        }
    }

    private void updateVideoListLayout() {
        int size = UserHelper.getAllUsers().size();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userVideoList.getLayoutParams();
        int preWidth = params.width;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (size - 1 >= 3) {
            int maxWidth = (int) (325 * displayMetrics.scaledDensity);
            width = maxWidth;
        }
        if (width != preWidth) {
            params.width = width;
            userVideoList.setLayoutParams(params);
        }
    }

    @Override
    public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {
        updateVideoListLayout();
        Log.d(TAG, "onUserLeave " + userList.size());
        adapter.onUserLeave(userList);
        if (adapter.getItemCount() == 0) {
            videoListContain.setVisibility(View.INVISIBLE);
        }
        updateSessionInfo();
    }

    @Override
    public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {
        Log.d(TAG, "onUserVideoStatusChanged ");
        if (null == iconVideo) {
            return;
        }

        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null != zoomSDKUserInfo) {
            iconVideo.setImageResource(zoomSDKUserInfo.getVideoStatus().isOn() ? R.drawable.icon_video_off : R.drawable.icon_video_on);
            if (userList.contains(zoomSDKUserInfo)) {
                checkMoreAction();
            }
        }
        adapter.onUserVideoStatusChanged(userList);
    }

    @Override
    public void onUserAudioStatusChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> userList) {
        // Toast.makeText(this, "" + userList.size(), Toast.LENGTH_SHORT).show();

        ZoomVideoSDKUser zoomSDKUserInfo = session.getMySelf();
        if (zoomSDKUserInfo != null && userList.contains(zoomSDKUserInfo)) {
            if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomVideoSDKAudioStatus.ZoomVideoSDKAudioType.ZoomVideoSDKAudioType_None) {
                iconAudio.setImageResource(R.drawable.icon_join_audio);
            } else {
                if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                    iconAudio.setImageResource(R.drawable.icon_unmute);
                } else {
                    iconAudio.setImageResource(R.drawable.icon_mute);
                }
            }
            checkMoreAction();
        }

        if (isVisitFirstTime)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isVisitFirstTime = false;
                    adapter.onUserMuteUnmuteChanged(userList,userVideoList);
                }
            }, 3000);
        }
        else
        {
            adapter.onUserMuteUnmuteChanged(userList,userVideoList);
        }





    }

    @Override
    public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {
        if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Start) {
            currentShareUser = userInfo;
            if (userInfo== session.getMySelf()) {
                iconShare.setImageResource(R.drawable.icon_stop_share);
            }
        } else if (status == ZoomVideoSDKShareStatus.ZoomVideoSDKShareStatus_Stop) {
            currentShareUser = null;
            iconShare.setImageResource(R.drawable.icon_share);
            shareViewGroup.setVisibility(View.GONE);
            if (null != shareToolbar) {
                shareToolbar.destroy();
            }
        }
    }


    @Override
    public void onLiveStreamStatusChanged(ZoomVideoSDKLiveStreamHelper liveStreamHelper, ZoomVideoSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatMessage messageItem) {
        Log.d(TAG, "onChatNewMessageNotify msgId: " + messageItem.getMessageId());
        chatMsgAdapter.onReceive(messageItem);

        updateChatLayoutParams();
    }

    @Override
    public void onChatDeleteMessageNotify(ZoomVideoSDKChatHelper chatHelper, String msgID, ZoomVideoSDKChatMessageDeleteType deleteBy) {
        Log.d(TAG, "onChatDeleteMessageNotify msgID: " + msgID + ",deleteBy: " + deleteBy);
    }

    @Override
    public void onUserHostChanged(ZoomVideoSDKUserHelper userHelper, ZoomVideoSDKUser userInfo) {
        if (userInfo != null) {
            Log.d(TAG, "onUserHostChanged userInfo: " + userInfo.getUserName());
        }
    }


    @Override
    public void onSessionNeedPassword(ZoomVideoSDKPasswordHandler handler) {
        Log.d(TAG, "onSessionNeedPassword ");
        showInputPwdDialog(handler);
    }

    private void showInputPwdDialog(final ZoomVideoSDKPasswordHandler handler) {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_session_input_pwd);
        builder.setCancelable(false);
        builder.setCanceledOnTouchOutside(false);
        final EditText editText = builder.findViewById(R.id.edit_pwd);
        builder.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = editText.getText().toString();
                if (!TextUtils.isEmpty(pwd)) {
                    handler.inputSessionPassword(pwd);
                    builder.dismiss();
                }
            }
        });

        builder.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.leaveSessionIgnorePassword();
                builder.dismiss();
            }
        });

        builder.show();
    }


    @Override
    public void onSessionPasswordWrong(ZoomVideoSDKPasswordHandler handler) {
        Log.d(TAG, "onSessionPasswordWrong ");
        Toast.makeText(this, "Password wrong", Toast.LENGTH_LONG).show();
        showInputPwdDialog(handler);
    }

    @Override
    public void onUserActiveAudioChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> list) {
//        Log.d(TAG, "onUserActiveAudioChanged " + list);
        adapter.onUserActiveAudioChanged(list, userVideoList);
    }

    @Override
    public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {

    }

    @Override
    public void onUserManagerChanged(ZoomVideoSDKUser user) {
        Log.d(TAG,"onUserManagerChanged:"+user);
    }

    @Override
    public void onUserNameChanged(ZoomVideoSDKUser user) {
        Log.d(TAG,"onUserNameChanged:"+user);

    }

    @Override
    public void onShareAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

    }

    @Override
    public void onCommandReceived(ZoomVideoSDKUser sender, String strCmd) {
        Log.d(TAG, "onCommandReceived sender userName: " + sender.getUserName() + ", cmd: " + strCmd);
    }

    @Override
    public void onCommandChannelConnectResult(boolean isSuccess) {
        Log.d(TAG, "onCommandChannelConnectResult: " + isSuccess);
    }

    @Override
    public void onCloudRecordingStatus(ZoomVideoSDKRecordingStatus status) {

    }

    @Override
    public void onHostAskUnmute() {
        Log.d(TAG, "onHostAskUnmute ");
        Toast.makeText(this, "The host would like you to unmute", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInviteByPhoneStatus(ZoomVideoSDKPhoneStatus status, ZoomVideoSDKPhoneFailedReason reason) {
        Log.d(TAG, "onInviteByPhoneStatus: " + "status: " + status + ", reason: " + reason);
    }

    @Override
    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKRawDataPipe videoPipe) {
        Log.d(TAG, "onMultiCameraStreamStatusChanged: " + "status: " + status + ", user: " + user.getUserName()+" videoPipe:"+videoPipe);
        if (null != getMultiStreamDelegate()&&renderType==RENDER_TYPE_OPENGLES&&null!=videoPipe) {
            Log.d(TAG, "onMultiCameraStreamStatusChanged: subscribe pipe");
            if (status == ZoomVideoSDKMultiCameraStreamStatus.Status_Joined) {
                videoPipe.subscribe(ZoomVideoSDKVideoResolution.VideoResolution_720P, getMultiStreamDelegate());
            } else {
                videoPipe.unSubscribe(getMultiStreamDelegate());
            }
        }
    }

    public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKVideoCanvas canvas) {
        Log.d(TAG, "onMultiCameraStreamStatusChanged: " + "status: " + status + ", user: " + user.getUserName()+" videoPipe:"+canvas);
        if(null!=getMultiStreamVideoView()&&renderType==RENDER_TYPE_ZOOMRENDERER&&null!=canvas){
            Log.d(TAG, "onMultiCameraStreamStatusChanged: subscribe canvas");
            if (status == ZoomVideoSDKMultiCameraStreamStatus.Status_Joined) {
                canvas.subscribe(getMultiStreamVideoView(), ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_PanAndScan);
            } else {
                canvas.unSubscribe(getMultiStreamVideoView());
            }
        }
    }

    @Override
    public void onLiveTranscriptionStatus(ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus status) {

    }

    @Override
    public void onLiveTranscriptionMsgReceived(String ltMsg, ZoomVideoSDKUser pUser, ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionOperationType type) {

    }

    @Override
    public void onLiveTranscriptionMsgError(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage spokenLanguage, ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage transcriptLanguage) {

    }

    @Override
    public void onSSLCertVerifiedFailNotification(ZoomVideoSDKSSLCertificateInfo info) {

    }

    @Override
    public void onProxySettingNotification(ZoomVideoSDKProxySettingHandler handler) {
    }

    protected  ZoomVideoSDKUser feccUser;
    public void onCameraControlRequestResult(ZoomVideoSDKUser user, boolean isApproved) {
        Log.d(TAG,"onCameraControlRequestResult:"+user+":"+isApproved);
        if (isApproved) {
            feccUser = user;
        } else {
            feccUser = null;
        }
    }

    protected ZoomVideoSDKRawDataPipeDelegate getMultiStreamDelegate(){
        return null;
    }

    protected ZoomVideoSDKVideoView getMultiStreamVideoView(){
        return null;
    }

    @Override
    public void onUserVideoNetworkStatusChanged(ZoomVideoSDKNetworkStatus status, ZoomVideoSDKUser user) {
        Log.d(TAG, "onUserVideoNetworkStatusChanged:" + user.getUserName() + ":" + status);
    }

    public void showOfflineDialog()
    {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(BaseMeetingActivity.this,R.style.CustomAlertDialog);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(iconMore.getContext()).inflate(R.layout.offline_layout, viewGroup, false);

        TextView heading = dialogView.findViewById(R.id.heading);

        heading.setText("You Are Offline!!!");

        TextView pay = dialogView.findViewById(R.id.pay);

        builder.setView(dialogView);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);


        pay.setText("Try Again");

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isonline = isOnline(BaseMeetingActivity.this);
                if (isonline)
                {
                    alertDialog.dismiss();
                }
                else
                {
                    Toast.makeText(BaseMeetingActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
                //callDeleteInMailAPI();
            }
        });


        alertDialog.show();
    }


    @Subscribe
    public void onInternetChange(InternetEvent internetEvent){
        /*cartEventList.add(cartEvent);
        String cartTotalItems = "Cart Items: "+cartEventList.size();
        cartTextView.setText(cartTotalItems);*/
        //  Toast.makeText(this, "Item added to cart.", Toast.LENGTH_SHORT).show();

        if (internetEvent.isOnline)
        {
            if (alertDialog!=null)
            {
                if (alertDialog.isShowing())
                {
                    alertDialog.dismiss();
                }
            }

        }
        else
        {
            if (alertDialog!=null)
            {
                if (!alertDialog.isShowing())
                {
                    showOfflineDialog();
                }
            }
            else
            {
                showOfflineDialog();
            }
        }

    }


    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }



}

