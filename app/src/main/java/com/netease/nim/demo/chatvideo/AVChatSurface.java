package com.netease.nim.demo.chatvideo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.NimApplication;
import com.netease.nim.demo.R;
import com.netease.nim.demo.others.HB;
import com.netease.nim.demo.others.ScreenUtil;
import com.netease.nim.demo.utils.SharePreUtils;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;

/**
 * 视频绘制管理
 * Created by hzxuwen on 2015/5/6.
 */
public class AVChatSurface {

    private Context context;

    private View surfaceRoot;
    private Handler uiHandler;

    // constant
    private static final int PEER_CLOSE_CAMERA = 0;
    private static final int LOCAL_CLOSE_CAMERA = 1;
    private static final int AUDIO_TO_VIDEO_WAIT = 2;
    private static final int TOUCH_SLOP = 10;

    // view
    private LinearLayout largeSizePreviewLayout;
    private FrameLayout smallSizePreviewFrameLayout;
    private LinearLayout smallSizePreviewLayout;
    private ImageView smallSizePreviewCoverImg;//stands for peer or local close camera
    private View largeSizePreviewCoverLayout;//stands for peer or local close camera
    private View touchLayout;

    //render
    private AVChatSurfaceViewRenderer smallRender;
    private AVChatSurfaceViewRenderer largeRender;

    // state
    private boolean init = false;
    private boolean localPreviewInSmallSize = true;
    private boolean isPeerVideoOff = false;
    private boolean isLocalVideoOff = false;

    // move
    private int lastX, lastY;
    private int inX, inY;
    private Rect paddingRect;

    // data
    private String largeAccount; // 显示在大图像的用户id
    private String smallAccount; // 显示在小图像的用户id

    // touch zone
    public interface TouchZoneCallback {
        void onTouch();
    }

    private TouchZoneCallback touchZoneCallback;

    public AVChatSurface(Context context, View surfaceRoot, TouchZoneCallback cb) {
        this.context = context;
        this.surfaceRoot = surfaceRoot;
        this.uiHandler = new Handler(context.getMainLooper());
        this.smallRender = new AVChatSurfaceViewRenderer(context);
        this.largeRender = new AVChatSurfaceViewRenderer(context);
        this.touchZoneCallback = cb;
    }

    private void findViews() {

//        TODO 6666 显示布局
        setSurfaceRoot(true);


        if (init)
            return;
        if (surfaceRoot != null) {

            smallSizePreviewFrameLayout = (FrameLayout) surfaceRoot.findViewById(R.id.small_size_preview_layout);
            smallSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.small_size_preview);
            smallSizePreviewCoverImg = (ImageView) surfaceRoot.findViewById(R.id.smallSizePreviewCoverImg);
            smallSizePreviewFrameLayout.setOnTouchListener(smallPreviewTouchListener);

            largeSizePreviewLayout = (LinearLayout) surfaceRoot.findViewById(R.id.large_size_preview);
            largeSizePreviewCoverLayout = surfaceRoot.findViewById(R.id.notificationLayout);

            init = true;
        }
    }


    public void setLargeRedColor(){
        findViews();
        largeSizePreviewLayout.setBackgroundColor(Color.RED);
    }


    private View.OnTouchListener smallPreviewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    int[] p = new int[2];
                    smallSizePreviewFrameLayout.getLocationOnScreen(p);
                    inX = x - p[0];
                    inY = y - p[1];

                    break;
                case MotionEvent.ACTION_MOVE:
                    final int diff = Math.max(Math.abs(lastX - x), Math.abs(lastY - y));
                    if (diff < TOUCH_SLOP)
                        break;

                    if (paddingRect == null) {
                        paddingRect = new Rect(ScreenUtil.dip2px(10), ScreenUtil.dip2px(20), ScreenUtil.dip2px(10),
                                ScreenUtil.dip2px(70));
                    }

                    int destX, destY;
                    if (x - inX <= paddingRect.left) {
                        destX = paddingRect.left;
                    } else {
                        destX = x - inX;
                    }

                    if (y - inY <= paddingRect.top) {
                        destY = paddingRect.top;
                    }  else {
                        destY = y - inY;
                    }

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    params.gravity = Gravity.NO_GRAVITY;
                    params.leftMargin = destX;
                    params.topMargin = destY;
                    v.setLayoutParams(params);

                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5) {
                        if (largeAccount == null || smallAccount == null) {
                            return true;
                        }
                        String temp;
                        switchRender(smallAccount, largeAccount);
                        temp = largeAccount;
                        largeAccount = smallAccount;
                        smallAccount = temp;
                        switchAndSetLayout();
                    }

                    break;
            }

            return true;
        }
    };



    /**
     * 大图像surfaceview 初始化
     *
     * @param account 显示视频的用户id
     */
    public void initLargeSurfaceView(String account) {
        largeAccount = account;
        findViews();
        /**
         * 设置画布，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        if (SharePreUtils.getString(HB.NAME , "").equals(account)) {
            boolean b = AVChatManager.getInstance().setupLocalVideoRender(largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            Toast.makeText(NimApplication.context,"w我家在的都不就setupLocalVideoRender"+ b , Toast.LENGTH_SHORT).show();
        } else {
            boolean b = AVChatManager.getInstance().setupRemoteVideoRender(account, largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            Toast.makeText(NimApplication.context,"w我家在的都不就setupRemoteVideoRender"+ b , Toast.LENGTH_SHORT).show();
        }
        addIntoLargeSizePreviewLayout(largeRender);

    }

    /**
     * 小图像surfaceview 初始化
     *
     * @param account
     * @return
     */
    public void initSmallSurfaceView(String account) {
        smallAccount = account;

        findViews();
        smallSizePreviewFrameLayout.setVisibility(View.VISIBLE);
        /**
         * 设置画布，加入到自己的布局中，用于呈现视频图像
         * account 要显示视频的用户帐号
         */
        if (DemoCache.getAccount().equals(account)) {
            AVChatManager.getInstance().setupLocalVideoRender(smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(account, smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        addIntoSmallSizePreviewLayout(smallRender);


    }


    /**
     * 添加surfaceview到largeSizePreviewLayout
     *
     * @param surfaceView
     */
    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        largeSizePreviewLayout.addView(surfaceView);
//        surfaceView.setZOrderMediaOverlay(false);
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
        smallSizePreviewFrameLayout.bringToFront();
    }
    /**
     * 添加surfaceview到smallSizePreviewLayout
     */
    private void addIntoSmallSizePreviewLayout(SurfaceView surfaceView) {
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        smallSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        smallSizePreviewLayout.setVisibility(View.VISIBLE);

    }

    /**
     * 关闭小窗口
     */
    private void closeSmallSizePreview() {
        smallSizePreviewCoverImg.setVisibility(View.VISIBLE);
    }

    /**
     * 对方打开了摄像头
     */
    public void peerVideoOn() {
        isPeerVideoOff = false;
        if (localPreviewInSmallSize) {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        } else {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        }
    }

    public void isNO(){
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
    }

    /**
     * 对方关闭了摄像头
     */
    public void peerVideoOff() {
        isPeerVideoOff = true;
        if (localPreviewInSmallSize) { //local preview in small size layout, then peer preview should in large size layout
            showNotificationLayout(PEER_CLOSE_CAMERA);
        } else {  // peer preview in small size layout
            closeSmallSizePreview();
        }
    }

    /**
     * 对方打开了摄像头
     */
    public void localVideoOn() {
        isLocalVideoOff = false;

        largeSizePreviewCoverLayout.setVisibility(View.GONE);

    }

    /**
     * 本地关闭了摄像头
     */
    public void localVideoOff() {
        isLocalVideoOff = true;
        if (localPreviewInSmallSize)
            closeSmallSizePreview();
        else
            showNotificationLayout(LOCAL_CLOSE_CAMERA);
    }

    /**
     * 摄像头切换时，布局显隐
     */
    private void switchAndSetLayout() {
        localPreviewInSmallSize = !localPreviewInSmallSize;
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (isPeerVideoOff) {
            peerVideoOff();
        }
        if (isLocalVideoOff) {
            localVideoOff();
        }
    }

    /**
     * 界面提示
     *
     * @param closeType
     */
    private void showNotificationLayout(int closeType) {
        if (largeSizePreviewCoverLayout == null) {
            return;
        }
        TextView textView = (TextView) largeSizePreviewCoverLayout;
        switch (closeType) {
            case PEER_CLOSE_CAMERA:
                textView.setText("PEER_CLOSE_CAMERA");
                break;
            case LOCAL_CLOSE_CAMERA:
                textView.setText("LOCAL_CLOSE_CAMERA");
                break;
            case AUDIO_TO_VIDEO_WAIT:
                textView.setText("AUDIO_TO_VIDEO_WAIT");
                break;
            default:
                return;
        }
        largeSizePreviewCoverLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 布局是否可见
     *
     * @param visible
     */
    private void setSurfaceRoot(boolean visible) {
        surfaceRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 大小图像显示切换
     *
     * @param user1 用户1的account
     * @param user2 用户2的account
     */
    private void switchRender(String user1, String user2) {

        //先取消用户的画布
        if (SharePreUtils.getString(HB.NAME,"").equals(user1)) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user1, null, false, 0);
        }
        if (SharePreUtils.getString(HB.NAME,"").equals(user2)) {
            AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user2, null, false, 0);
        }
        //交换画布
        //如果存在多个用户,建议用Map维护account,render关系.
        //目前只有两个用户,并且认为这两个account肯定是对的
        AVChatSurfaceViewRenderer render1;
        AVChatSurfaceViewRenderer render2;
        if (user1.equals(smallAccount)) {
            render1 = largeRender;
            render2 = smallRender;
        } else {
            render1 = smallRender;
            render2 = largeRender;
        }

        //重新设置上画布
        if (user1 == SharePreUtils.getString(HB.NAME,"")) {
            AVChatManager.getInstance().setupLocalVideoRender(render1, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user1, render1, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        if (user2 == SharePreUtils.getString(HB.NAME,"")) {
            AVChatManager.getInstance().setupLocalVideoRender(render2, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(user2, render2, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
    }

    /**
     * 是否本地预览图像在小图像（UI上层）
     * @return
     */
    public boolean isLocalPreviewInSmallSize() {
        return localPreviewInSmallSize;
    }

    public void closeSession() {

        if (init) {
            if (largeRender.getParent() != null) {
                ((ViewGroup) largeRender.getParent()).removeView(largeRender);
            }
            if (smallRender.getParent() != null) {
                ((ViewGroup) smallRender.getParent()).removeView(smallRender);
            }
            largeRender = null;
            smallRender = null;
        }
    }

}
