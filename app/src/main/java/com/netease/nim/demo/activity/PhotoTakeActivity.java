package com.netease.nim.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.netease.nim.demo.R;
import com.netease.nim.demo.adapter.VideoAdapter;
import com.netease.nim.demo.modle.VideoItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import view.RecyclerViewEmptySupport;

public class PhotoTakeActivity extends AppCompatActivity implements View.OnClickListener, VideoAdapter.EventListener{

    @InjectView(R.id.take_video_image)
    ImageView takeVideoImage;
    @InjectView(R.id.list_empty)
    LinearLayout listEmpty;
    @InjectView(R.id.video_list)
    RecyclerViewEmptySupport videoListView;
    // data
    private int videoCount; // 已经上传服务器的视频数量
    private List<VideoItem> items; // 视频item列表
    VideoAdapter videoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_take);
        ButterKnife.inject(this);
//        findViews();
    }

  /*  private void findViews() {
        // adapter
        items = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoListView, R.layout.video_item_layout, items);
        videoAdapter.setEventListener(PhotoTakeActivity.this);
        videoAdapter.setFetchMoreView(new MsgListFetchLoadMoreView());
        videoAdapter.setOnFetchMoreListener(new BaseFetchLoadAdapter.RequestFetchMoreListener() {
            @Override
            public void onFetchMoreRequested() {
                getVideoList(null);
            }
        });
        videoListView.addOnItemTouchListener(new OnItemClickListener<VideoAdapter>() {
            @Override
            public void onItemClick(VideoAdapter adapter, View view, int position) {
                VideoInfoEntity entity = adapter.getItem(position).getEntity();
                if (entity != null) {
                    if (TextUtils.isEmpty(entity.getSnapshotUrl())) {
                        entity.setSnapshotUrl(adapter.getItem(position).getUriString());
                    }
                    VideoDetailInfoActivity.startActivity(getActivity(), adapter.getItem(position).getEntity(),
                            adapter.getItem(position).getState(), false);
                }
            }

            @Override
            public void onItemLongClick(VideoAdapter adapter, View view, int position) {
                onNormalLongClick(position);
            }
        });
        videoListView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoListView.setEmptyView(findView(R.id.list_empty));
        videoListView.setAdapter(videoAdapter);
    }*/



   /* private void getVideoList(final FetchVideoListener fetchVideoListener) {
        DemoServerHttpClient.getInstance().videoInfoGet(null, UploadType.SHORT_VIDEO, new DemoServerHttpClient.DemoServerHttpCallback<List<VideoInfoEntity>>() {
            @Override
            public void onSuccess(List<VideoInfoEntity> entities) {
                List<VideoInfoEntity> videoInfoEntities = new ArrayList<>();
                List<VideoItem> videoItems = new ArrayList<>();
                for (VideoInfoEntity videoInfoEntity : entities) {
                    if (videosFromServer.containsKey(videoInfoEntity.getVid())) {
                        continue;
                    }
                    videoInfoEntities.add(videoInfoEntity);
                    VideoItem videoItem = new VideoItem();
                    videoItem.setEntity(videoInfoEntity);
                    videoItems.add(videoItem);
                    videosFromServer.put(videoInfoEntity.getVid(), videoInfoEntity);
                }

                // 顶部加载
                if (videoInfoEntities.size() <= 0) {
                    videoAdapter.fetchMoreEnd(true);
                } else {
                    videoCount = videoInfoEntities.size();
                    videoAdapter.fetchMoreComplete(videoListView, videoItems);
                }
                if (fetchVideoListener != null) {
                    fetchVideoListener.onFetchVideoDone();
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
            }
        });
    }*/



    @OnClick({R.id.take_video_image, R.id.list_empty})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_video_image:
                checkTakeVideo();
                break;
            case R.id.video_list:
                break;
            case R.id.list_empty:
                break;
        }
    }
    /**
     * 确认是否可以开始拍摄视频
     */
    private void checkTakeVideo() {
        /*if (videoCount >= VIDEO_LIMIT) {
            showVideoCountDialog();
        } else {
            VideoShootActivity.startActivityForResult(getActivity());
        }*/
        //进行视频录制
        VideoShootActivity.startActivityForResult(PhotoTakeActivity.this);
    }

    @Override
    public void onRetryUpload(VideoItem videoItem) {

    }

    @Override
    public void onVideoDeleted(int position, VideoItem videoItem) {

    }
}
