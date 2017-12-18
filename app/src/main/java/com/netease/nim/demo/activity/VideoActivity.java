package com.netease.nim.demo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.faceunity.FaceU;
import com.faceunity.utils.VersionUtil;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.chatvideo.AVChatExitCode;
import com.netease.nim.demo.chatvideo.AVChatNotification;
import com.netease.nim.demo.chatvideo.AVChatSoundPlayer;
import com.netease.nim.demo.chatvideo.AVChatSurface;
import com.netease.nim.demo.chatvideo.AVChatTimeoutObserver;
import com.netease.nim.demo.chatvideo.CallStateEnum;
import com.netease.nim.demo.chatvideo.PhoneCallStateObserver;
import com.netease.nim.demo.utils.SharePreUtils;
import com.netease.nim.uikit.UI;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.support.permission.BaseMPermission;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatImageFormat;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;

import java.util.List;
import java.util.Map;

import static com.netease.nim.demo.modle.Extras.EXTRA_ACCOUNT;

public class VideoActivity extends UI
        implements AVChatUI.AVChatListener, AVChatStateObserver, AVChatSurface.TouchZoneCallback {
    // constant
    private static final String TAG = "AVChatActivity";
    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    public static final String INTENT_ACTION_AVCHAT = "INTENT_ACTION_AVCHAT";

    /**
     * 来自广播
     */
    public static final int FROM_BROADCASTRECEIVER = 0;
    /**
     * 来自发起方
     */
    public static final int FROM_INTERNAL = 1;
    /**
     * 来自通知栏
     */
    public static final int FROM_NOTIFICATION = 2;
    /**
     * 未知的入口
     */
    public static final int FROM_UNKNOWN = -1;

    // data
    private AVChatUI avChatUI; // 音视频总管理器
    private AVChatData avChatData; // config for connect video server
    private int state; // calltype 音频或视频
    private String receiverId; // 对方的account

    // state
    private boolean isUserFinish = false;
    private boolean mIsInComingCall = false;// is incoming call or outgoing call
    private boolean isCallEstablished = false; // 电话是否接通
    private static boolean needFinish = true; // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
    private boolean hasOnPause = false; // 是否暂停音视频

    // face unity
    private FaceU faceU;
    // notification
    private AVChatNotification notifier;
    //自己写的实时视频通话
    private AVChatCameraCapturer mVideoCapturer;
    private AVChatParameters avChatParameters;
    private AVChatSurface avChatSurface;
    private AVChatData avChatData1;
    private final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.CAMERA,};
    public static void launch(Context context, String account, int callType, int source) {
        needFinish = false;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, VideoActivity.class);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_IN_CALLING, false);
        intent.putExtra(KEY_CALL_TYPE, callType);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    /**
     * incoming call
     *
     * @param context
     */
    public static void launch(Context context, AVChatData config, int source) {
        needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, VideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (needFinish || !checkSource()) {
            finish();
            return;
        }
*/
        //得到去电账号
        receiverId = getIntent().getStringExtra(EXTRA_ACCOUNT);
        // 锁屏唤醒
        dismissKeyguard();
        View root = LayoutInflater.from(this).inflate(R.layout.activity_video, null);
        setContentView(root);
        avChatSurface = new AVChatSurface(this, findViewById(R.id.avchat_surface_layout), this);
        this.avChatParameters = new AVChatParameters();
        registerAVChatIncomingCallObserver(true);
        initLinstener();
    }


    private void initLinstener() {
//        接口 回调
        AVChatManager.getInstance().observeAVChatState(this, true);
//        拨打挂断通知
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver , true);

//        接听
        receiveInComingCall(avChatData1);
//        拨打
//        outGoingCalling(receiverId, AVChatType.VIDEO);
        //挂断
//        hangUpMethod();
//     监听
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, true);

    }




    /**
     * 接听来电
     */
    private void receiveInComingCall(final AVChatData data) {


        //接听，告知服务器，以便通知其他端

        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        AVChatManager.getInstance().setParameters(avChatParameters);

        AVChatManager.getInstance().enableVideo();
        AVChatManager.getInstance().startVideoPreview();

        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        try {
            AVChatManager.getInstance().accept2(data.getChatId(), new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(VideoActivity.this, "接听成功", Toast.LENGTH_SHORT).show();
//               打开摄像头
//                AVChatManager.getInstance().muteLocalVideo(false);
//                avChatSurface.localVideoOn();
//                小窗口 被接这放
                    List<String> deniedPermissions = BaseMPermission.getDeniedPermissions(VideoActivity. this, BASIC_PERMISSIONS);
                    if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
                        Toast.makeText(VideoActivity.this , "avChatVideo.CameraPermissi;",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    avChatSurface.initSmallSurfaceView(DemoCache.getAccount());
//                avChatSurface.initLargeSurfaceView(aUser);

                }

                @Override
                public void onFailed(int code) {
                    if (code == -1) {
                        Toast.makeText(VideoActivity.this, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VideoActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
                    }

                    handleAcceptFailed();
                }

                @Override
                public void onException(Throwable exception) {
                    handleAcceptFailed();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

//    接听之后  关闭引擎,
//        AVChatManager.getInstance().disableRtc();
    }
    private void handleAcceptFailed() {
        AVChatManager.getInstance().stopVideoPreview();
        AVChatManager.getInstance().disableVideo();
        AVChatManager.getInstance().disableRtc();

    }


    private void registerAVChatIncomingCallObserver(boolean register) {
//        通话的基本 信息
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(final AVChatData data) {
                avChatData1 = data;
                Toast.makeText(VideoActivity.this , data.getAccount() +"来电",Toast.LENGTH_SHORT).show();
                String extra = data.getExtra();
//                拒绝请求
//                AVChatManager.getInstance().hangUp2(data.getChatId(), null);

//                这是接受 请求过来的     receiveInComingCall(data);

//                 当前来电。 如果选择继续原来的通话，挂断当前来电，最好能够先发送一个正忙的指令给对方
//                AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);

                // 有网络来电打开AVChatActivity
//                AVChatProfile.getInstance().setAVChatting(true);

            }
        }, register);
    }







  /*去电呼叫整体逻辑*/
    private void outGoingCalling(String receiverId, final AVChatType callTypeEnum) {
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        notifyOption.extendMessage = "extra_data";
        notifyOption.webRTCCompat = false;
        //        默认forceKeepCalling为true，开发者如果不需要离线持续呼叫功能可以将forceKeepCalling设为false
//        notifyOption.forceKeepCalling = false;
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        mVideoCapturer.setFocus();
        mVideoCapturer.setAutoFocus(true);
        //        mVideoCapturer.setZoom(zoomValue);
//        mVideoCapturer.switchCamera();  切换摄像头
        int currentZoom = mVideoCapturer.getCurrentZoom();
        mVideoCapturer.setFlash(false);

        avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR, AVChatAudioEffectMode.DISABLE);
        avChatParameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER, AVChatAudioEffectMode.DISABLE);
        avChatParameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_AUTO);
        avChatParameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_AUTO);
        //采用I420图像格式
        avChatParameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_FILTER_FORMAT, AVChatImageFormat.I420);
//是     否开启视频绘制帧率汇报
        avChatParameters.setBoolean(AVChatParameters.KEY_VIDEO_FPS_REPORTED, true);
        AVChatManager.getInstance().setParameters(avChatParameters);
        if (callTypeEnum == AVChatType.VIDEO) {
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().startVideoPreview();
        }

        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        AVChatManager.getInstance().call2(receiverId, callTypeEnum, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;
                List<String> deniedPermissions = BaseMPermission.getDeniedPermissions(VideoActivity.this, BASIC_PERMISSIONS);
                if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
                    Toast.makeText(VideoActivity.this , "avChatVideo.CameraPermissi;",Toast.LENGTH_SHORT).show();
                    return;
                }

                //如果需要使用视频预览功能，在此进行设置，调用setupLocalVideoRender
                //如果不需要视频预览功能，那么删掉下面if语句代码即可
                if (callTypeEnum == AVChatType.VIDEO) {
                    Toast.makeText(VideoActivity.this, "我就是init", Toast.LENGTH_SHORT).show();
                    avChatSurface.initSmallSurfaceView(SharePreUtils.getString("name" , ""));
                }
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {
            }
        });
    }









    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                AVChatSoundPlayer.instance().stop();
                finish();
            }
        }
    };

    /**
     * 接听
     */
    private void inComingCalling() {
        avChatUI.inComingCalling(avChatData);
    }

    /**
     * 拨打
     */
    private void outgoingCalling() {
        if (!NetworkUtil.isNetAvailable(VideoActivity.this)) { // 网络不可用
            Toast.makeText(this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        avChatUI.outGoingCalling(receiverId, AVChatType.typeOfValue(state));
    }

    /**
     * 注册/注销网络通话控制消息（音视频模式切换通知）
     */
    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {
            handleCallControl(netCallControlNotification);
        }
    };

    /**
     * 注册/注销网络通话对方挂断的通知
     */
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            AVChatData info = avChatUI.getAvChatData();
            if (info != null && info.getChatId() == avChatHangUpInfo.getChatId()) {
                AVChatSoundPlayer.instance().stop();
                avChatUI.closeRtc();
                avChatUI.closeSessions(AVChatExitCode.HANGUP);
                cancelCallingNotifier();
                // 如果是incoming call主叫方挂断，那么通知栏有通知
                if (mIsInComingCall && !isCallEstablished) {
                    activeMissCallNotifier();
                }
            }

        }
    };

    /**
     * 通知栏
     */
    private void activeCallingNotifier() {
        if (notifier != null && !isUserFinish) {
            notifier.activeCallingNotification(true);
        }
    }

    private void cancelCallingNotifier() {
        if (notifier != null) {
            notifier.activeCallingNotification(false);
        }
    }

    private void activeMissCallNotifier() {
        if (notifier != null) {
            notifier.activeMissCallNotification(true);
        }
    }

    /**
     * 注册/注销网络通话被叫方的响应（接听、拒绝、忙）
     */
    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {
            AVChatData info = avChatUI.getAvChatData();
            if (info != null && info.getChatId() == ackInfo.getChatId()) {
                AVChatSoundPlayer.instance().stop();

                if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {

                    AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);

                    avChatUI.closeSessions(AVChatExitCode.PEER_BUSY);
                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                    avChatUI.closeRtc();
                    avChatUI.closeSessions(AVChatExitCode.REJECT);
                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                    avChatUI.isCallEstablish.set(true);
                    avChatUI.canSwitchCamera = true;
                }
            }
        }
    };
    /**
     * 注册/注销同时在线的其他端对主叫方的响应
     */
    Observer<AVChatOnlineAckEvent> onlineAckObserver = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent ackInfo) {
            AVChatData info = avChatUI.getAvChatData();
            if (info != null && info.getChatId() == ackInfo.getChatId()) {
                AVChatSoundPlayer.instance().stop();

                String client = null;
                switch (ackInfo.getClientType()) {
                    case ClientType.Web:
                        client = "Web";
                        break;
                    case ClientType.Windows:
                        client = "Windows";
                        break;
                    case ClientType.Android:
                        client = "Android";
                        break;
                    case ClientType.iOS:
                        client = "iOS";
                        break;
                    case ClientType.MAC:
                        client = "Mac";
                        break;
                    default:
                        break;
                }
                if (client != null) {
                    String option = ackInfo.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
                    Toast.makeText(VideoActivity.this, "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
                }
                avChatUI.closeSessions(-1);
            }
        }
    };

    /**
     * 注册监听
     *
     * @param register
     */
    private void registerNetCallObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(this, register);
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
//        超时挂断请在demo上层实现，sdk未来会移除超时相关接口
//        AVChatManager.getInstance().observeTimeoutNotification(timeoutObserver, register);
//        demo上层实现超时挂断示例
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, register, mIsInComingCall);
        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register);
    }

    Observer<Integer> autoHangUpForLocalPhoneObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {

            AVChatSoundPlayer.instance().stop();

            avChatUI.closeSessions(AVChatExitCode.PEER_BUSY);
        }
    };
    Observer<Integer> timeoutObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {

            avChatUI.onHangUp();

            // 来电超时，自己未接听
            if (mIsInComingCall) {
                activeMissCallNotifier();
            }

            AVChatSoundPlayer.instance().stop();
        }
    };

    /**
     * 处理音视频切换请求
     *
     * @param notification
     */
    private void handleCallControl(AVChatControlEvent notification) {
        if (AVChatManager.getInstance().getCurrentChatId() != notification.getChatId()) {
            return;
        }
        switch (notification.getControlCommand()) {
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO:
                avChatUI.incomingAudioToVideo();
                break;
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_AGREE:
                onAudioToVideo();
                break;
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_REJECT:
                avChatUI.onCallStateChange(CallStateEnum.AUDIO);
                Toast.makeText(VideoActivity.this, R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
                break;
            case AVChatControlCommand.SWITCH_VIDEO_TO_AUDIO:
                onVideoToAudio();
                break;
            case AVChatControlCommand.NOTIFY_VIDEO_OFF:
                avChatUI.peerVideoOff();
                break;
            case AVChatControlCommand.NOTIFY_VIDEO_ON:
                avChatUI.peerVideoOn();
                break;
            default:
                Toast.makeText(this, "对方发来指令值：" + notification.getControlCommand(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 音频切换为视频
     */
    private void onAudioToVideo() {
        avChatUI.onAudioToVideo();
        avChatUI.initAllSurfaceView(avChatUI.getVideoAccount());
    }

    /**
     * 视频切换为音频
     */
    private void onVideoToAudio() {
        avChatUI.onCallStateChange(CallStateEnum.AUDIO);
        avChatUI.onVideoToAudio();
    }

    /**
     * ******************************** face unity 接入 ********************************
     */

    private void initFaceU() {
        showOrHideFaceULayout(false); // hide default

        if (VersionUtil.isCompatible(Build.VERSION_CODES.JELLY_BEAN_MR2) && FaceU.hasAuthorized()) {
            // async load FaceU
            FaceU.createAndAttach(VideoActivity.this, findView(R.id.avchat_video_face_unity), new FaceU.Response<FaceU>() {
                @Override
                public void onResult(FaceU faceU) {
                    VideoActivity.this.faceU = faceU;
                    showOrHideFaceULayout(true); // show
                }
            });
        }
    }

    private void showOrHideFaceULayout(boolean show) {
        ViewGroup vp = findView(R.id.avchat_video_face_unity);
        for (int i = 0; i < vp.getChildCount(); i++) {
            vp.getChildAt(i).setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    // 设置窗口flag，亮屏并且解锁/覆盖在锁屏界面上
    private void dismissKeyguard() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }


    /**
     * 判断来电还是去电
     *
     * @return
     */
    private boolean checkSource() {
        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER: // incoming call
                parseIncomingIntent();
                return true;
            case FROM_INTERNAL: // outgoing call
                parseOutgoingIntent();
                if (state == AVChatType.VIDEO.getValue() || state == AVChatType.AUDIO.getValue()) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * 来电参数解析
     */
    private void parseIncomingIntent() {
        avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
        state = avChatData.getChatType().getValue();
    }

    /**
     * 去电参数解析
     */
    private void parseOutgoingIntent() {
        receiverId = getIntent().getStringExtra(EXTRA_ACCOUNT);
        state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
    }


    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, VideoActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    public void onTouch() {

    }

    @Override
    public void uiExit() {
        finish();
    }

    @Override
    public void onTakeSnapshotResult(String s, boolean b, String s1) {

    }

    @Override
    public void onAVRecordingCompletion(String account, String filePath) {
        if (account != null && filePath != null && filePath.length() > 0) {
            String msg = "音视频录制已结束, " + "账号：" + account + " 录制文件已保存至：" + filePath;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "录制已结束.", Toast.LENGTH_SHORT).show();
        }
        if (avChatUI != null) {
            avChatUI.resetRecordTip();
        }
    }

    @Override
    public void onAudioRecordingCompletion(String filePath) {
        if (filePath != null && filePath.length() > 0) {
            String msg = "音频录制已结束, 录制文件已保存至：" + filePath;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "录制已结束.", Toast.LENGTH_SHORT).show();
        }
        if (avChatUI != null) {
            avChatUI.resetRecordTip();
        }
    }

    @Override
    public void onLowStorageSpaceWarning(long l) {
        if (avChatUI != null) {
            avChatUI.showRecordWarning();
        }
    }

    @Override
    public void onAudioMixingEvent(int i) {

    }

    @Override
    public void onJoinedChannel(int code, String audioFile, String videoFile, int i) {
        LogUtil.d(TAG, "audioFile -> " + audioFile+" videoFile -> " + videoFile);
        handleWithConnectServerResult(code);
    }
    /****************************** 连接建立处理 ********************/

    /**
     * 处理连接服务器的返回值
     *
     * @param auth_result
     */
    protected void handleWithConnectServerResult(int auth_result) {
        LogUtil.i(TAG, "result code->" + auth_result);
        if (auth_result == 200) {
            LogUtil.d(TAG, "onConnectServer success");
        } else if (auth_result == 101) { // 连接超时
            avChatUI.closeSessions(AVChatExitCode.PEER_NO_RESPONSE);
        } else if (auth_result == 401) { // 验证失败
            avChatUI.closeSessions(AVChatExitCode.CONFIG_ERROR);
        } else if (auth_result == 417) { // 无效的channelId
            avChatUI.closeSessions(AVChatExitCode.INVALIDE_CHANNELID);
        } else { // 连接服务器错误，直接退出
            avChatUI.closeSessions(AVChatExitCode.CONFIG_ERROR);
        }
    }
    @Override
    public void onUserJoined(String account) {
        LogUtil.d(TAG, "onUserJoin -> " + account);
        avChatUI.setVideoAccount(account);
        avChatUI.initLargeSurfaceView(avChatUI.getVideoAccount());
    }

    @Override
    public void onUserLeave(String account, int i) {
        LogUtil.d(TAG, "onUserLeave -> " + account);
        avChatUI.onHangUp();
        avChatUI.closeSessions(AVChatExitCode.HANGUP);
    }

    @Override
    public void onLeaveChannel() {

    }

    @Override
    public void onProtocolIncompatible(int i) {

    }

    @Override
    public void onDisconnectServer() {

    }

    @Override
    public void onNetworkQuality(String s, int i, AVChatNetworkStats avChatNetworkStats) {

    }

    @Override
    public void onCallEstablished() {
        LogUtil.d(TAG, "onCallEstablished");
        //移除超时监听
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false, mIsInComingCall);
        if (avChatUI.getTimeBase() == 0)
            avChatUI.setTimeBase(SystemClock.elapsedRealtime());

        if (state == AVChatType.AUDIO.getValue()) {
            avChatUI.onCallStateChange(CallStateEnum.AUDIO);
        } else {
            avChatUI.initSmallSurfaceView();
            avChatUI.onCallStateChange(CallStateEnum.VIDEO);
        }
        isCallEstablished = true;
    }

    @Override
    public void onDeviceEvent(int i, String s) {

    }

    @Override
    public void onConnectionTypeChanged(int i) {

    }

    @Override
    public void onFirstVideoFrameAvailable(String s) {

    }

    @Override
    public void onFirstVideoFrameRendered(String s) {

    }

    @Override
    public void onVideoFrameResolutionChanged(String s, int i, int i1, int i2) {

    }

    @Override
    public void onVideoFpsReported(String s, int i) {

    }

    @Override
    public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean b) {
        if (faceU != null) {
            faceU.effect(frame.data, frame.width, frame.height, FaceU.VIDEO_FRAME_FORMAT.I420);
        }

        return true;

    }

    @Override
    public boolean onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
        return true;
    }

    @Override
    public void onAudioDeviceChanged(int i) {

    }

    @Override
    public void onReportSpeaker(Map<String, Integer> map, int i) {

    }

    @Override
    public void onSessionStats(AVChatSessionStats avChatSessionStats) {

    }

    @Override
    public void onLiveEvent(int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelCallingNotifier();
        if (hasOnPause) {
            avChatUI.resumeVideo();
            hasOnPause = false;
        }
    }

    @Override
    public void finish() {
        isUserFinish = true;
        super.finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(avChatUI!=null)
        avChatUI.pauseVideo(); // 暂停视频聊天（用于在视频聊天过程中，APP退到后台时必须调用）
        hasOnPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activeCallingNotifier();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, false);
        AVChatProfile.getInstance().setAVChatting(false);
        registerNetCallObserver(false);
        cancelCallingNotifier();
        destroyFaceU();
        needFinish = true;
    }

    private void destroyFaceU() {
        if (faceU == null) {
            return;
        }

        try {
            faceU.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
