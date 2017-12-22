package com.netease.nim.demo.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.transcoding.TranscodingNative;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_FILE_NOT_EXIST;
import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_FILE_NOT_SUPPORT;
import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_PARA_NULL;
import static com.netease.transcoding.TranscodingAPI.SnapshotPara;
import static com.netease.transcoding.TranscodingAPI.TRAN_MIX_FILE_PARSE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_OUT_FILE_CREATE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_PARA_NULL;
import static com.netease.transcoding.TranscodingAPI.TRAN_PRE_IS_NOT_FINISH;
import static com.netease.transcoding.TranscodingAPI.TRAN_PROCESS_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_SOURCE_FILE_PARSE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_SOURCE_NO_VIDEO_OR_AUDIO;
import static com.netease.transcoding.TranscodingAPI.TranCrop;
import static com.netease.transcoding.TranscodingAPI.TranDynamicWater;
import static com.netease.transcoding.TranscodingAPI.TranFilter;
import static com.netease.transcoding.TranscodingAPI.TranMixAudio;
import static com.netease.transcoding.TranscodingAPI.TranOut;
import static com.netease.transcoding.TranscodingAPI.TranScale;
import static com.netease.transcoding.TranscodingAPI.TranSource;
import static com.netease.transcoding.TranscodingAPI.TranTimeCut;
import static com.netease.transcoding.TranscodingAPI.TranWaterMark;
import static com.netease.transcoding.TranscodingAPI.TranscodePara;
import static com.netease.transcoding.TranscodingAPI.VERIFY_FAILED;
import static com.netease.transcoding.TranscodingAPI.getInstance;

/**
 * 视频处理管理
 * Created by hzxuwen on 2017/4/10.
 */

public class VideoProcessController {
    private static final String TAG = VideoProcessController.class.getSimpleName();

    public interface VideoProcessCallback {
        void onVideoProcessSuccess();

        void onVideoProcessFailed(int code);

        void onVideoSnapshotSuccess(Bitmap bitmap);

        void onVideoSnapshotFailed(int code);

        void onVideoProcessUpdate(int process, int total);
    }

    private Context context;
    private AsyncTask videoProcessTask;
    private AsyncTask snapshotTask;
    private static VideoProcessCallback callback;

    public VideoProcessController(Context context, VideoProcessCallback callback) {
        this.context = context;
        this.callback = callback;
//        getInstance().init(context, DemoServerHttpClient.readAppKey());
    }

    /**
     * 视频处理
     *
     * @param videoProcessOptions 视频处理参数
     */
   /* public void startCombination(VideoProcessOptions videoProcessOptions) {
        videoProcessTask = new CombinationAsyncTask((Activity) context)
                .execute(videoProcessOptions.getSource(),
                        videoProcessOptions.getWaterMarks(),
                        videoProcessOptions.getDynamicWater(),
                        videoProcessOptions.getCrop(),
                        videoProcessOptions.getTimeCut(),
                        videoProcessOptions.getMixAudio(),
                        videoProcessOptions.getFilter(),
                        videoProcessOptions.getTranscodePara(),
                        videoProcessOptions.getOutputFilePara());

    }*/

    /**
     * 视频截图
     *
     * @param para 截图参数
     */
    public void startSnapShot(final SnapshotPara para) {
        snapshotTask = new SnapShotAsyncTask((Activity) context, para).execute(para);
    }

    private static class CombinationAsyncTask extends AsyncTask<Object, Integer, Integer> {
        private WeakReference<Activity> activityWeakReference;

        public CombinationAsyncTask(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            TranscodePara transcodePara = new TranscodePara();

            final TranSource inputFilePara = (TranSource) params[0];
            final List<TranWaterMark> waterMarkPara
                    = (List)params[1];
            final TranDynamicWater chartletPara = (TranDynamicWater) params[2];
            final TranCrop cropPara = (TranCrop) params[3];
            final TranTimeCut fileCutPara = (TranTimeCut) params[4];
            final TranMixAudio mixAudioPara = (TranMixAudio) params[5];
            final TranFilter colourAdjustPara = (TranFilter) params[6];
            final TranOut outputFilePara = (TranOut) params[8];
            final TranScale scalePara = new TranScale();

            transcodePara.setSource(inputFilePara);
            if (waterMarkPara.size() > 0) {
                TranWaterMark[] waterTemps = new TranWaterMark[waterMarkPara.size()];
                transcodePara.setWaterMarks(waterMarkPara.toArray(waterTemps));
            }
            transcodePara.setDynamicWater(chartletPara);
            transcodePara.setCrop(cropPara);
            transcodePara.setTimeCut(fileCutPara);
            transcodePara.setMixAudio(mixAudioPara);
            transcodePara.setFilter(colourAdjustPara);
            transcodePara.setOut(outputFilePara);
            transcodePara.setScale(scalePara);

            transcodePara.getOut().setCallBack(new TranscodingNative.NativeCallBack() {
                @Override
                public void progress(int progress, int total) {
                    publishProgress(progress, total);
                }
            });
            return getInstance().VODProcess(transcodePara);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] <= values[1]) {
                callback.onVideoProcessUpdate(values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(Integer ret) {
            LogUtil.e(TAG, "短视频处理：" + ret);
            switch (ret) {
                case TRAN_PARA_NULL:
                    LogUtil.e(TAG, "短视频处理失败，输入文件为空");
                    break;
                case TRAN_OUT_FILE_CREATE_ERROR:
                    LogUtil.e(TAG, "短视频处理失败，无法创建目标文件，请检查目标文件地址或SD卡权限");
                    break;
                case TRAN_PRE_IS_NOT_FINISH:
                    LogUtil.e(TAG, "短视频处理失败，上一次未处理完毕");
                    break;
                case TRAN_SOURCE_FILE_PARSE_ERROR:
                    LogUtil.e(TAG, "短视频处理失败，原始文件解析失败");
                    break;
                case TRAN_SOURCE_NO_VIDEO_OR_AUDIO:
                    LogUtil.e(TAG, "短视频处理失败，原始文件没有视频或音频");
                    break;
                case TRAN_MIX_FILE_PARSE_ERROR:
                    LogUtil.e(TAG, "短视频处理失败，混音文件解析失败");
                    break;
                case TRAN_PROCESS_ERROR:
                    LogUtil.e(TAG, "短视频处理失败，媒体文件不支持，或参数设置错误");
                    break;
                default:
                    LogUtil.e(TAG, "转码已完成");
                    callback.onVideoProcessSuccess();
                    return;
            }

            callback.onVideoProcessFailed(ret);
        }
    }

    private static class SnapShotAsyncTask extends AsyncTask<SnapshotPara, Integer, Integer> {
        private WeakReference<Activity> activityWeakReference;
        private SnapshotPara para;
        private Bitmap snapShot;

        public SnapShotAsyncTask(Activity activity, SnapshotPara para) {
            activityWeakReference = new WeakReference<>(activity);
            this.para = para;
        }


        @Override
        protected Integer doInBackground(SnapshotPara... params) {
            SnapshotPara snapshotPara = params[0];
            snapshotPara.setCallBack(new SnapshotPara.SnapshotCallback() {
                @Override
                public void result(Bitmap bitmap, int i, int i1) {
                    snapShot = bitmap;
                }
            });
            return getInstance().snapShot(snapshotPara);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
        }

        @Override
        protected void onPostExecute(Integer ret) {
            switch (ret) {
                case VERIFY_FAILED:
                    LogUtil.e(TAG, "截图失败，APPkey 未授权");
                    break;
                case SNAPSHOT_PARA_NULL:
                    LogUtil.e(TAG, "截图失败，截图参数为空");
                    break;
                case SNAPSHOT_FILE_NOT_EXIST:
                    LogUtil.e(TAG, "截图失败，原始文件不存在");
                    break;
                case SNAPSHOT_FILE_NOT_SUPPORT:
                    LogUtil.e(TAG, "截图失败，原始文件不支持");
                    break;
                default:
                    LogUtil.e(TAG, "截图已结束");
                    callback.onVideoSnapshotSuccess(snapShot);
                    return;
            }
            callback.onVideoSnapshotFailed(ret);
        }
    }
}
