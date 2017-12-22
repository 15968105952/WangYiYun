package com.netease.nim.demo.controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.netease.LSMediaCapture.Statistics;
import com.netease.transcoding.record.MediaRecord;
import com.netease.transcoding.record.MessageHandler;
import com.netease.transcoding.util.LogUtil;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.render.NeteaseView;

/**
 * Created by hzxuwen on 2017/4/6.
 */

public class MediaCaptureController implements MessageHandler {
    private static final String TAG = MediaCaptureController.class.getSimpleName();
    private MediaCaptureControllerCallback captureControllerCallback;
    private MediaCaptureOptions mediaCaptureOptions;
    private MediaRecord mediaRecord;

    private Context context;

    // data
    private Statistics mStatistics = null; // 统计
    private float mCurrentDistance; // 变焦手势距离差
    private float mLastDistance = -1;
    private volatile boolean mRecording = false;
    private volatile boolean isPreviewing = false;

    public interface MediaCaptureControllerCallback {
        /**
         * 预览设置完成
         */
        void onPreviewInited();

        /**
         * 设置预览画面大小
         */
        void setPreviewSize(int videoPreviewWidth, int videoPreviewHeight);

        /**
         * 获取画布SurfaceView
         */
        SurfaceView getSurfaceView();

        /**
         * 可以开始录制了
         */
        void onStartRecording();

        /**
         * 资源释放完毕
         */
        void onRelease();

        /**
         * 异常
         */
        void onError(int code);
    }

    public MediaCaptureController(Context context, MediaCaptureControllerCallback callback, MediaCaptureOptions options) {
        this.context = context;
        this.captureControllerCallback = callback;
        this.mediaCaptureOptions = options;

        MediaRecord.MediaRecordPara mediaRecordPara = new MediaRecord.MediaRecordPara();
//        mediaRecordPara.setAppKey(DemoServerHttpClient.readAppKey());  //APPKEY
        mediaRecordPara.setContext(context.getApplicationContext()); //APP上下文
        mediaRecordPara.setLogLevel(LogUtil.LogLevel.INFO); //日志级别
        mediaRecordPara.setMessageHandler(this); //消息回调
        mediaRecord = new MediaRecord(mediaRecordPara);
    }

    public MediaRecord getMediaRecord() {
        return mediaRecord;
    }

    public void startPreview(NeteaseView videoView) {
        MediaRecord.VideoQuality videoQuality = MediaRecord.VideoQuality.SUPER_HIGH;
        if (mediaCaptureOptions.resolutionType == ResolutionType.HD) {
            videoQuality = MediaRecord.VideoQuality.SUPER_HIGH; //视频模板（SUPER_HIGH 1280*720、SUPER 960*540、HIGH 640*480、MEDIUM 480*360）
        } else if (mediaCaptureOptions.resolutionType == ResolutionType.FLUENT){
            videoQuality = MediaRecord.VideoQuality.HIGH; //视频模板（SUPER_HIGH 1280*720、SUPER 960*540、HIGH 640*480、MEDIUM 480*360）
        }
        mediaRecord.startVideoPreview(videoView, mediaCaptureOptions.cameraPosition, videoQuality, false);
        mediaRecord.setBeautyLevel(5); //磨皮强度为5,共5档，0为关闭
        mediaRecord.setFilterStrength(0.5f); //滤镜强度
        mediaRecord.setFilterType(VideoEffect.FilterType.clean);
        isPreviewing = true;
    }

    public void stopPreview() {
        if (mediaRecord != null && isPreviewing) {
            mediaRecord.stopVideoPreview();
            isPreviewing = false;
        }
    }

    /**
     * 释放录制资源
     */
    public void release() {
        if(mediaRecord != null){
            if (mRecording){
                mediaRecord.stopRecord();
            }
            if (isPreviewing) {
                mediaRecord.stopVideoPreview();
            }

            mediaRecord.destroyVideoPreview();
            mediaRecord.unInit();
        }
//        unregisterReceiver(audioMixVolumeMsgReceiver);
        // TODO: 2017/9/1 伴音注销
        captureControllerCallback.onRelease();
    }

    /**************
     * 视频操作
     *********/

    /**
     * 开始录制
     */
    public void startRecording() {
        if (!mRecording) {
            mediaRecord.startRecord(mediaCaptureOptions.mFilePath);
            mRecording = true;
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mRecording) {
            mediaRecord.stopRecord();
            mRecording = false;
        }
    }

    public void switchCamera() {
        if (mediaRecord != null) {
            mediaRecord.switchCamera();
        }
    }

    /**
     * ***************** 摄像头变焦 ****************
     */

    // 对焦
    public void setCameraFocus(MotionEvent event) {
        if (mediaRecord != null) {
            mediaRecord.setCameraFocus();
        }
    }

    private int maxZoomValue = 0;
    private int currenZoomValue = 0;

    // 变焦
    public void setCameraZoomParam(MotionEvent event) {
        /**
         * 首先判断按下手指的个数是不是大于两个。
         * 如果大于两个则执行以下操作（即图片的缩放操作）。
         */
        if (event.getPointerCount() >= 2) {

            float offsetX = event.getX(0) - event.getX(1);
            float offsetY = event.getY(0) - event.getY(1);
            /**
             * 原点和滑动后点的距离差
             */
            mCurrentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
            if (mLastDistance < 0) {
                mLastDistance = mCurrentDistance;
            } else {
                if (mediaRecord != null) {
                    maxZoomValue = mediaRecord.getCameraMaxZoomValue();
                    currenZoomValue = mediaRecord.getCameraZoomValue();
                }
                /**
                 * 如果当前滑动的距离（currentDistance）比最后一次记录的距离（lastDistance）相比大于5英寸（也可以为其他尺寸），
                 * 那么现实图片放大
                 */
                if (mCurrentDistance - mLastDistance > 5) {
                    //Log.i(TAG, "test: 放大！！！");
                    currenZoomValue++;
                    if (currenZoomValue > maxZoomValue) {
                        currenZoomValue = maxZoomValue;
                    }
                    if (mediaRecord != null) {
                        mediaRecord.setCameraZoomPara(currenZoomValue);
                    }

                    mLastDistance = mCurrentDistance;
                    /**
                     * 如果最后的一次记录的距离（lastDistance）与当前的滑动距离（currentDistance）相比小于5英寸，
                     * 那么图片缩小。
                     */
                } else if (mLastDistance - mCurrentDistance > 5) {
                    currenZoomValue--;
                    if (currenZoomValue < 0) {
                        currenZoomValue = 0;
                    }
                    //Log.i(TAG, "test: 缩小！！！");
                    if (mediaRecord != null) {
                        mediaRecord.setCameraZoomPara(currenZoomValue);
                    }

                    mLastDistance = mCurrentDistance;
                }
            }
        }
    }

    /**
     * ******************** 滤镜选择 *****************
     */
    // 设置滤镜模式
    public void setFilterType(VideoEffect.FilterType type) {
        if (mediaRecord != null) {
            mediaRecord.setFilterType(type);
        }
    }

    /*****************************
     * 视频录制相关通知
     ***********************/

    @Override
    public void handleMessage(int i, Object o) {
        switch (i) {
            case MSG_INIT_RECORD_VERIFY_ERROR:
            case MSG_START_RECORD_ERROR:
            case MSG_START_CAMERA_ERROR:
            case MSG_START_AUDIO_ERROR:
                captureControllerCallback.onError(i);
                break;
            case MSG_START_PREVIEW_FINISHED:
                // camera采集预览完成，才能开始录制
                captureControllerCallback.onPreviewInited();
                break;
            case MSG_START_RECORD_FINISHED:
                // 真正可以录制了
                captureControllerCallback.onStartRecording();
                break;
            case MSG_SWITCH_CAMERA_FINISHED:
                break;
            case MSG_CAMERA_NOT_SUPPORT_FLASH:
                break;
        }
    }
}
