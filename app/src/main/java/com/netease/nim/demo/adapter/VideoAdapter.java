package com.netease.nim.demo.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.demo.R;
import com.netease.nim.demo.modle.UploadState;
import com.netease.nim.demo.modle.VideoInfoEntity;
import com.netease.nim.demo.modle.VideoItem;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseFetchLoadAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2017/4/11.
 */

public class VideoAdapter extends BaseFetchLoadAdapter<VideoItem, BaseViewHolder> {
    // view
    private ImageView videoImage;
    private TextView videoNameText;
    private TextView videoTimeText;
    private ImageView arrowImage;
    private TextView videoProgressTip;
    private ProgressBar progressBar;
    private TextView videoPathText;
    private TextView playBtn;
    private TextView copyBtn;
    private TextView deleteBtn;
    private RelativeLayout progressLayout;
    private RelativeLayout playLayout;
    private RelativeLayout uploadFailedLayout;
    private TextView retryBtn;
    // data
    private VideoItem videoItem;
    private Map<Long, Boolean> playLayoutMap;
    private Map<String, Integer> progressMap;
    private EventListener eventListener;

    public interface EventListener {
        void onRetryUpload(VideoItem videoItem);

        void onVideoDeleted(int position, VideoItem videoItem);
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param recyclerView
     * @param layoutResId  The layout resource id of each item.
     * @param data         A new list is created out of this one to avoid mutable list
     */
    public VideoAdapter(RecyclerView recyclerView, int layoutResId, List<VideoItem> data) {
        super(recyclerView, layoutResId, data);
        playLayoutMap = new HashMap<>();
        progressMap = new HashMap<>();
    }

    @Override
    protected void convert(BaseViewHolder holder, VideoItem item, int position, boolean isScrolling) {
        this.videoItem = item;

        videoImage = holder.getView(R.id.video_thumb);
        videoNameText = holder.getView(R.id.video_name);
        videoTimeText = holder.getView(R.id.video_time);
        arrowImage = holder.getView(R.id.video_detail_arrow);
        videoProgressTip = holder.getView(R.id.video_upload_progress);
        progressBar = holder.getView(R.id.video_progress_bar);
        videoPathText = holder.getView(R.id.video_path);
        playBtn = holder.getView(R.id.play_btn);
        copyBtn = holder.getView(R.id.copy_btn);
        deleteBtn = holder.getView(R.id.delete_btn);
        progressLayout = holder.getView(R.id.progress_layout);
        playLayout = holder.getView(R.id.video_play_layout);
        uploadFailedLayout = holder.getView(R.id.upload_failed_layout);
        retryBtn = holder.getView(R.id.retry_btn);

        refreshViewholderType(item);

        if (item.getEntity() == null) {
            // 本地视频，未上传 或者 上传失败
            videoNameText.setText(item.getDisplayName());
            videoTimeText.setText(item.getDateTaken());
            Glide.with(mContext).load(item.getUriString()).into(videoImage);
            videoTimeText.setVisibility(View.GONE);
            arrowImage.setVisibility(View.GONE);
            if (item.getState() == UploadState.STATE_UPLOAD_FAIL) {
                uploadFailedLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            } else {
                uploadFailedLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
                updateProgress(videoItem);
            }
        } else {
            // 已上传的视频
            VideoInfoEntity videoInfoEntity = item.getEntity();
            videoTimeText.setVisibility(View.VISIBLE);
            arrowImage.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            videoNameText.setText(TextUtils.isEmpty(videoInfoEntity.getVideoName()) ? item.getDisplayName() : videoInfoEntity.getVideoName());
            videoTimeText.setText(TimeUtil.getTimeShowString(videoInfoEntity.getCreateTime(), false));

            if (!TextUtils.isEmpty(item.getUriString())) {
                Glide.with(mContext).load(item.getUriString()).apply(RequestOptions.errorOf(R.drawable.video_thumb)).into(videoImage);
            } else if (!TextUtils.isEmpty(videoInfoEntity.getSnapshotUrl())) {
                Glide.with(mContext).load(item.getUriString()).apply(RequestOptions.errorOf(R.drawable.video_thumb)).into(videoImage);
            } else {
                Glide.with(mContext).load(R.drawable.video_thumb).into(videoImage);
            }
        }

        setListener(position);
    }

    public void updateProgress(VideoItem videoItem) {
        if (progressMap == null || progressMap.size() <= 0
                || videoItem == null || TextUtils.isEmpty(videoItem.getId())) {
            return;
        }
        if (!progressMap.containsKey(videoItem.getId())) {
            return;
        }
        int progress = progressMap.get(videoItem.getId());
        videoProgressTip.setText("保存完成，正在上传(" + progress + "%)");
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(progress);
    }

    public void putProgress(String id, int progress) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        progressMap.put(id, progress);
    }

    public boolean isPlayLayoutShow(VideoItem videoItem) {
        if (playLayoutMap == null || playLayoutMap.size() <= 0 || videoItem == null) {
            return false;
        }
        if (videoItem.getEntity() == null) {
            return false;
        }
        long vid = videoItem.getEntity().getVid();
        return playLayoutMap.get(vid) == null ? false : playLayoutMap.get(vid);
    }

    public void setPlayLayoutMap(VideoItem videoItem, boolean isShow) {
        if (videoItem.getEntity() == null) {
            return;
        }
        playLayoutMap.put(videoItem.getEntity().getVid(), isShow);
    }

    private void refreshViewholderType(VideoItem item) {
        if (item.getEntity() == null) {
            return;
        }
        if (isPlayLayoutShow(item)) {
            playLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            videoPathText.setText(item.getEntity().getOrigUrl());
        } else {
            playLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setListener(final int position) {
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventListener.onVideoDeleted(position, videoItem);
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                VideoPlayerActivity.startActivity(mContext, videoItem.getEntity().getOrigUrl());
            }
        });
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", videoItem.getEntity().getOrigUrl());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                Toast.makeText(mContext, "复制成功，请到播放器观看", Toast.LENGTH_SHORT).show();
            }
        });
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventListener.onRetryUpload(videoItem);
            }
        });
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
}

