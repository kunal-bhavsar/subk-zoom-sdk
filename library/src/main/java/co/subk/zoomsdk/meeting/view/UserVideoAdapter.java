package co.subk.zoomsdk.meeting.view;

import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_ZOOMRENDERER;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.subk.zoomsdk.R;
import co.subk.zoomsdk.cmd.CmdReactionRequest;
import co.subk.zoomsdk.cmd.EmojiReactionType;
import co.subk.zoomsdk.meeting.BaseMeetingActivity;
import co.subk.zoomsdk.meeting.rawdata.RawDataRenderer;
import co.subk.zoomsdk.meeting.util.UserHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoView;

public class UserVideoAdapter extends RecyclerView.Adapter<UserVideoAdapter.BaseHolder> {

    private static final String TAG = "UserVideoAdapter";
    public interface ItemTapListener {
        void onSingleTap(ZoomVideoSDKUser user);
    }

    private ItemTapListener tapListener;

    private List<ZoomVideoSDKUser> userList = new ArrayList<>();

    private Context context;

    private int renderType;

    private ZoomVideoSDKUser selectedVideoUser;

    private List<ZoomVideoSDKUser> activeAudioList;

    @NonNull
    private List<CmdReactionRequest> emojiActiveList = new ArrayList<>();

    private Handler handler = new Handler();
    private VideoHolder viewHolder;

    private Runnable emojiTimerRunnable = new Runnable() {

        final List<CmdReactionRequest> timeOutEmoji = new ArrayList<>();
        final List<CmdReactionRequest> handRaisedList = new ArrayList<>();
        @Override
        public void run() {
            for (CmdReactionRequest request : emojiActiveList) {
                if (request.reactionType == EmojiReactionType.RaisedHand) {
                    /* raise hand only can dismiss by low hand */
                    continue;
                }
                request.dismissTimeLeftInSeconds--;
                if (request.dismissTimeLeftInSeconds <= 0) {
                    timeOutEmoji.add(request);
                }
            }
            for (CmdReactionRequest request : timeOutEmoji) {
                if (request.isHandRaised) {
                    request.reactionType = EmojiReactionType.RaisedHand;
                    request.emojiReactionResID = CmdReactionRequest.getReactionResId(request.reactionType);
                    handRaisedList.add(request);
                }
            }
            timeOutEmoji.removeAll(handRaisedList);
            emojiActiveList.removeAll(timeOutEmoji);
            if (!timeOutEmoji.isEmpty() || !handRaisedList.isEmpty()) {
                notifyItemRangeChanged(0, getItemCount(), "emoji");
            }
            timeOutEmoji.clear();
            handRaisedList.clear();
            handler.postDelayed(this, 1000);
        }
    };

    public UserVideoAdapter(Context context, ItemTapListener listener, int renderType) {
        this.context = context;
        tapListener = listener;
        this.renderType = renderType;
        handler.postDelayed(emojiTimerRunnable, 1000);
    }

    public ZoomVideoSDKUser getSelectedVideoUser() {
        return selectedVideoUser;
    }

    public void updateSelectedVideoUser(ZoomVideoSDKUser user) {
        if (null == user) {
            return;
        }
        int index = userList.indexOf(user);
        if (index >= 0) {
            selectedVideoUser = user;
            notifyItemRangeChanged(0, userList.size(), "active");
        }
    }

    public int getIndexByUser(ZoomVideoSDKUser user) {
        return userList.indexOf(user);
    }

    public void clear(boolean resetSelect) {
        userList.clear();
        if (resetSelect) {
            selectedVideoUser = null;
        }
        notifyDataSetChanged();
    }

    public void onDestroyed() {
        handler.removeCallbacks(emojiTimerRunnable);
    }

    public void onUserVideoStatusChanged(List<ZoomVideoSDKUser> changeList) {

        for (ZoomVideoSDKUser user : changeList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                notifyItemChanged(index, "avar");
            }
        }
    }

    public void addAll() {
        // Get all users without clearing the list first
        List<ZoomVideoSDKUser> all = UserHelper.getAllUsers();

        // Only update the list if there are changes
        if (!userList.equals(all)) {
            userList.clear();
            userList.addAll(all);
            notifyDataSetChanged();
        }
    }

    public void onUserJoin(List<ZoomVideoSDKUser> joinList) {
        int originalSize = userList.size();
        ZoomVideoSDKUser self = ZoomVideoSDK.getInstance().getSession().getMySelf();

        joinList.remove(self);
        for (ZoomVideoSDKUser user : joinList) {
                        if (!userList.contains(user) && !userList.contains(self)) {
                userList.add(user);
                notifyDataSetChanged();
               // notifyItemInserted(userList.size() - 1);  // Notify new position
            }
        }

        // Double check that the "self" user is not duplicated

        /*if (!userList.contains(self)) {
            userList.add(self);
            notifyItemInserted(userList.size() - 1);
        }*/
      //  notifyDataSetChanged();
       /* if (originalSize != userList.size()) {
            checkUserList();
        }*/
    }

    private void checkUserList() {
        List<ZoomVideoSDKUser> all = UserHelper.getAllUsers();

        // Remove duplicates, ensure "self" is only present once
        ZoomVideoSDKUser self = ZoomVideoSDK.getInstance().getSession().getMySelf();
        if (userList.contains(self) && Collections.frequency(userList, self) > 1) {
            userList.remove(self);  // Remove duplicate "self"
        }

        // Update the list if necessary
        if (!userList.equals(all)) {
            userList.clear();
            userList.addAll(all);
            notifyDataSetChanged();
        }
    }

    public void onUserLeave(List<ZoomVideoSDKUser> leaveList) {
        boolean refreshActive = false;

        // If the active user leaves, reset selectedVideoUser
        if (selectedVideoUser != null && leaveList.contains(selectedVideoUser)) {
            selectedVideoUser = ZoomVideoSDK.getInstance().getSession().getMySelf();
            refreshActive = true;
        }

        for (ZoomVideoSDKUser user : leaveList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                userList.remove(index);
                notifyItemRemoved(index);  // Notify removal at the correct position
            }
        }

        // Refresh active video user if necessary
        if (refreshActive) {
            notifyItemRangeChanged(0, userList.size(), "active");
        }
        //notifyDataSetChanged();
       // checkUserList();
    }



    public void onUserActiveAudioChanged(List<ZoomVideoSDKUser> list, RecyclerView userVideoList) {
        activeAudioList = list;
        int childCount = userVideoList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = userVideoList.getChildAt(i);
            int position = userVideoList.getChildAdapterPosition(view);
            if (position >= 0 && position < userList.size()) {
                ZoomVideoSDKUser userId = userList.get(position);
                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(position);
                if (null != holder) {
                    if (null != activeAudioList && activeAudioList.contains(userId)) {
                        holder.audioStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.audioStatus.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void onUserMuteUnmuteChanged(List<ZoomVideoSDKUser> list, RecyclerView userVideoList) {
        activeAudioList = list;

        for (int i=0;i<activeAudioList.size();i++)
        {
            if (userList.contains(activeAudioList.get(i)))
            {
                int indexxOfMatched = userList.indexOf(activeAudioList.get(i));

                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(indexxOfMatched);

                if (null != holder) {
                    if (activeAudioList.get(i).getAudioStatus().isMuted()) {
                        holder.muted.setVisibility(View.VISIBLE);
                    } else {
                        holder.muted.setVisibility(View.GONE);
                    }
                }
            }
        }

       /* int childCount = userVideoList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = userVideoList.getChildAt(i);
            int position = userVideoList.getChildAdapterPosition(view);
            if (position >= 0 && position < userList.size()) {
                ZoomVideoSDKUser userId = userList.get(position);
                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(position);
                if (null != holder) {
                    if (null != activeAudioList && activeAudioList.contains(userId) && activeAudioList.get(0).getAudioStatus().isMuted()) {
                        holder.muted.setVisibility(View.VISIBLE);
                    } else {
                        holder.muted.setVisibility(View.GONE);
                    }
                }
            }
        }*/
    }

    public void onEmojiReceived(@NonNull CmdReactionRequest newRequest, RecyclerView userVideoList) {
        boolean existRequest = false;
        for (CmdReactionRequest request : emojiActiveList) {
            if (request.user.equals(newRequest.user)) {
                if (newRequest.reactionType == EmojiReactionType.LowHand || newRequest.reactionType == EmojiReactionType.RaisedHand) {
                    /* only raise hand & low hand need modify hand raised status */
                    request.isHandRaised = newRequest.isHandRaised;
                    if (newRequest.reactionType == EmojiReactionType.LowHand && request.reactionType == EmojiReactionType.RaisedHand) {
                        /* lowHand should remove the origin raiseHand from the list in the next loop */
                        request.dismissTimeLeftInSeconds = -1;
                        request.reactionType = EmojiReactionType.LowHand;
                        request.emojiReactionResID = 0;
                    }
                }
                if (newRequest.reactionType != EmojiReactionType.LowHand) {
                    /* low hand should not effect emoji */
                    request.reactionType = newRequest.reactionType;
                    request.dismissTimeLeftInSeconds = CmdReactionRequest.EMOJI_DISMISS_TIME_OUT;
                    request.emojiReactionResID = newRequest.emojiReactionResID;
                }
                existRequest = true;
                break;
            }
        }
        if (!existRequest) {
            emojiActiveList.add(newRequest);
        }

        int childCount = userVideoList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = userVideoList.getChildAt(i);
            int position = userVideoList.getChildAdapterPosition(view);
            if (position >= 0 && position < userList.size()) {
                ZoomVideoSDKUser userId = userList.get(position);
                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(position);
                if (null != holder) {
                    holder.emojiView.setVisibility(View.GONE);
                    for (CmdReactionRequest request : emojiActiveList) {
                        if (request.user.equals(userId)) {
                            int resID = request.emojiReactionResID;
                            if (resID != 0) {
                                holder.emojiView.setVisibility(View.VISIBLE);
                                holder.emojiView.setImageResource(resID);
                            } else {
                                if (request.reactionType == EmojiReactionType.LowHand) {
                                    /* low hand will remove the status */
                                    holder.emojiView.setVisibility(View.GONE);
                                } else {
                                    Log.e(TAG, "wrong reaction resID");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isHandRaised() {
        ZoomVideoSDKUser user = ZoomVideoSDK.getInstance().getSession().getMySelf();
        for (CmdReactionRequest request : emojiActiveList) {
            if (request.user.equals(user)) {
                return request.isHandRaised;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_video, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {

        onBindViewHolder(holder, position, null);
    }


    @Override
    public void onViewRecycled(@NonNull BaseHolder holder) {
        super.onViewRecycled(holder);
        viewHolder = (VideoHolder) holder;
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            viewHolder.user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);
        } else {
            viewHolder.user.getVideoPipe().unSubscribe(viewHolder.rawDataRenderer);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position, @NonNull List<Object> payloads) {

        ZoomVideoSDKUser user = userList.get(position);
        VideoHolder viewHolder = (VideoHolder) holder;

        if (payloads != null && payloads.contains("emoji")) {
            /* emoji playload only need update emoji */
            showEmojiView(viewHolder, user);
            return;
        }

        if (payloads == null || payloads.isEmpty() || payloads.contains("video")) {
            subscribeVideo(user, viewHolder);
        }
        viewHolder.user = user;

        if (user != null) {
            if (!user.getVideoStatus().isOn()) {
                viewHolder.video_off_contain.setVisibility(View.VISIBLE);
                viewHolder.video_off_tips.setImageResource(R.drawable.zm_conf_no_avatar);
            } else {
                viewHolder.video_off_contain.setVisibility(View.INVISIBLE);
            }
            viewHolder.userNameText.setText(user.getUserName());
        }

        if (selectedVideoUser == user) {
            new Handler().postDelayed(() -> {
                if (tapListener != null && viewHolder.itemView != null) {
                    tapListener.onSingleTap(user);
                    viewHolder.itemView.setBackgroundResource(R.drawable.video_active_item_bg);
                    viewHolder.itemView.setVisibility(View.GONE);
                }
            }, 1000);
        } else {
            new Handler().postDelayed(() -> {
                if (viewHolder.itemView != null) {
                    viewHolder.itemView.setBackgroundResource(R.drawable.video_item_bg);
                    viewHolder.itemView.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }

        if (null != activeAudioList && activeAudioList.contains(user)) {
            viewHolder.audioStatus.setVisibility(View.VISIBLE);
        } else {
            viewHolder.audioStatus.setVisibility(View.GONE);
        }

        showEmojiView(viewHolder, user);
    }

    private void showEmojiView(VideoHolder viewHolder, ZoomVideoSDKUser user) {
        viewHolder.emojiView.setVisibility(View.GONE);
        for (CmdReactionRequest request : emojiActiveList) {
            if (request.user.equals(user)) {
                int resID = request.emojiReactionResID;
                if (resID != 0) {
                    viewHolder.emojiView.setVisibility(View.VISIBLE);
                    viewHolder.emojiView.setImageResource(resID);
                } else {
                    Log.e(TAG, "wrong reaction resID");
                }
                break;
            }
        }
    }

    public void RefreshMyList(List<ZoomVideoSDKUser> all)
    {
        userList.clear();
        userList.addAll(all);
        notifyDataSetChanged();
    }

    public void isOrientationChanges()
    {

        for (int i=0;i<userList.size();i++)
        {
            ZoomVideoSDKUser user = userList.get(i);

            if (selectedVideoUser == user) {
                //Toast.makeText(context, "aayo", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.itemView.setBackgroundResource(R.drawable.video_active_item_bg);
                        viewHolder.itemView.setVisibility(View.GONE);
                    }
                }, 2000);

            }
            else {
                //Toast.makeText(context, "nhi aayo", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.itemView.setBackgroundResource(R.drawable.video_item_bg);
                        viewHolder.itemView.setVisibility(View.VISIBLE);
                    }
                }, 2000);

            }
        }


    }

    private void subscribeVideo(ZoomVideoSDKUser user, VideoHolder viewHolder) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);
            user.getVideoCanvas().subscribe(viewHolder.videoRenderer, ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_PanAndScan);
        } else {
            viewHolder.rawDataRenderer.unSubscribe();
            user.getVideoPipe().subscribe(ZoomVideoSDKVideoResolution.VideoResolution_90P, viewHolder.rawDataRenderer);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        protected View view;

        BaseHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    class VideoHolder extends BaseHolder {

        ZoomVideoSDKVideoView videoRenderer;

        RawDataRenderer rawDataRenderer;

        ImageView emojiView;

        ImageView audioStatus;

        View itemView;

        TextView userNameText;

        ImageView video_off_tips;

        ImageView muted;

        View video_off_contain;

        ZoomVideoSDKUser user;

        VideoHolder(View view) {
            super(view);
            itemView = view;
            video_off_tips = view.findViewById(R.id.video_off_tips);
            emojiView = view.findViewById(R.id.emojiIv);
            audioStatus = view.findViewById(R.id.item_audio_status);
            muted = view.findViewById(R.id.muted);
            userNameText = view.findViewById(R.id.item_user_name);
            video_off_contain = view.findViewById(R.id.video_off_contain);

            videoRenderer = view.findViewById(R.id.videoRenderer);
            rawDataRenderer = view.findViewById(R.id.videoRawDataRenderer);


            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                videoRenderer.setVisibility(View.VISIBLE);
                videoRenderer.setZOrderMediaOverlay(true);
            } else {
                ((ViewGroup) rawDataRenderer.getParent()).setVisibility(View.VISIBLE);
                rawDataRenderer.setVisibility(View.VISIBLE);
                //open when user ZoomSurfaceViewRender
                rawDataRenderer.setZOrderMediaOverlay(true);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != tapListener) {
                        if (selectedVideoUser == user) {
                            return;
                        }
                        tapListener.onSingleTap(user);
                        selectedVideoUser = user;
                        notifyItemRangeChanged(0, getItemCount(), "active");
                    }
                }
            });
        }
    }
}
