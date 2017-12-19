package com.netease.nim.demo.others;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.chatvideo.AVChatSurface;
import com.netease.nim.demo.utils.SharePreUtils;
import com.netease.nim.uikit.support.permission.BaseMPermission;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatImageFormat;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;

import java.util.List;
import java.util.Map;

import static com.netease.nim.demo.modle.Extras.EXTRA_ACCOUNT;

public class MainActivity extends AppCompatActivity implements AVChatSurface.TouchZoneCallback, AVChatStateObserver {
    private final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.CAMERA,};
    private RequestCallback<LoginInfo> callback =
            new RequestCallback<LoginInfo>() {
                @Override
                public void onSuccess(LoginInfo param) {
                    SharePreUtils.setString(HB.NAME, param.getAccount());
                    Toast.makeText(MainActivity.this, "登录成功" + param.getAccount(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(int code) {
                    Toast.makeText(MainActivity.this, "登录失败" + code, Toast.LENGTH_SHORT).show();
                    Log.i("mazhuang","登录失败"+code);
                }

                @Override
                public void onException(Throwable exception) {
                    Toast.makeText(MainActivity.this, "登录Throwable" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
            };
    private Button login,goJie ,goBo ,hangUp;
    private AVChatCameraCapturer mVideoCapturer;
    private AVChatData avChatData;
    private String receiverId;
    private AVChatParameters avChatParameters;
    private String largeAccount;
    private AVChatSurface avChatSurface;
    /**
     * 来自广播
     */
    public static final int FROM_BROADCASTRECEIVER = 0;
    private static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {


        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {

//            avChatSurface.setLargeRedColor();

            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                // 对方正在忙
                Toast.makeText(MainActivity.this, "对方正在忙", Toast.LENGTH_SHORT).show();
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                // 对方拒绝接听
                Toast.makeText(MainActivity.this, "对方拒绝接听", Toast.LENGTH_SHORT).show();
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                // 对方同意接听
                Toast.makeText(MainActivity.this, "对方同意接听", Toast.LENGTH_SHORT).show();
            }
        }
    };
    /**
     * 注册/注销网络通话对方挂断的通知
     */
    private Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            Toast.makeText(MainActivity.this , "用户挂断",Toast.LENGTH_SHORT).show();
        }
    };
    private String aUser;
    private AVChatData avChatData1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //得到去电账号
        receiverId = getIntent().getStringExtra(EXTRA_ACCOUNT);
        //得到来电数据
        avChatData1= (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
//        avChatSurface = new AVChatSurface(this, findViewById(R.id.surface_layout), this);
        this.avChatParameters = new AVChatParameters();
        registerAVChatIncomingCallObserver(true);
        initLinstener();
    }

/*传递data*/
    /**
     * incoming call
     *
     * @param context
     */
    public static void launch(Context context, AVChatData config, int source) {
//        needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    private void initLinstener() {
//        接口 回调
        AVChatManager.getInstance().observeAVChatState(this, true);
//        拨打挂断通知
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver , true);
//        registerAVChatIncomingCallObserver(true);
        login = (Button) findViewById(R.id.go_login);
        goJie   = findViewById(R.id.go_jie);
        goBo = findViewById(R.id.go_bo);
        hangUp = findViewById(R.id.hang_up);
//        登录
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });
//        接听
        goJie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this , "接听",Toast.LENGTH_SHORT).show();
                receiveInComingCall(avChatData1);
                aUser = DemoCache.getAccount();
            }
        });
//        拨打
        goBo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MZ  1020025      小米  1020723
                Toast.makeText(MainActivity.this , "拨打",Toast.LENGTH_SHORT).show();
                outGoingCalling(receiverId , AVChatType.VIDEO);
            }
        });
        hangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hangUpMethod();
            }
        });
//     监听
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, true);

    }

    private void hangUpMethod() {
        AVChatManager.getInstance().stopVideoPreview();
        AVChatManager.getInstance().disableVideo();
        AVChatManager.getInstance().disableRtc();
        AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this , "挂断成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
             Toast.makeText(MainActivity.this , "挂断失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(MainActivity.this , "onException",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 拨打音视频
     */
    public void outGoingCalling(String account, final AVChatType callTypeEnum) {


        this.receiverId = account;

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
        AVChatManager.getInstance().call2(account, callTypeEnum, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;
                List<String> deniedPermissions = BaseMPermission.getDeniedPermissions(MainActivity. this, BASIC_PERMISSIONS);
                if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
                    Toast.makeText(MainActivity.this , "avChatVideo.CameraPermissi;",Toast.LENGTH_SHORT).show();
                    return;
                }

                //如果需要使用视频预览功能，在此进行设置，调用setupLocalVideoRender
                //如果不需要视频预览功能，那么删掉下面if语句代码即可
                if (callTypeEnum == AVChatType.VIDEO) {
                    Toast.makeText(MainActivity.this, "我就是init", Toast.LENGTH_SHORT).show();
                    avChatSurface.initSmallSurfaceView(SharePreUtils.getString(HB.NAME , ""));
                }
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {
            }
        });

      /*  if (callTypeEnum == AVChatType.AUDIO) {
            onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
        } else {
            onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
        }*/
    }

    private void registerAVChatIncomingCallObserver(boolean register) {
//        通话的基本 信息
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(final AVChatData data) {
                avChatData1 = data;
                Toast.makeText(MainActivity.this , data.getAccount() +"来电",Toast.LENGTH_SHORT).show();
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

    /**
     * 接听来电
     */
    private void receiveInComingCall(final AVChatData data) {
        long chatId = data.getChatId();
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
        AVChatManager.getInstance().accept2(data.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "接听成功", Toast.LENGTH_SHORT).show();
//               打开摄像头
//                AVChatManager.getInstance().muteLocalVideo(false);
//                avChatSurface.localVideoOn();
//                小窗口 被接这放
                List<String> deniedPermissions = BaseMPermission.getDeniedPermissions(MainActivity. this, BASIC_PERMISSIONS);
                if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
                    Toast.makeText(MainActivity.this , "avChatVideo.CameraPermissi;",Toast.LENGTH_SHORT).show();
                    return;
                }
                avChatSurface.initSmallSurfaceView(aUser);
//                avChatSurface.initLargeSurfaceView(aUser);

            }

            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(MainActivity.this, "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "建立连接失败", Toast.LENGTH_SHORT).show();
                }

                handleAcceptFailed();
            }

            @Override
            public void onException(Throwable exception) {
                handleAcceptFailed();
            }
        });
//    接听之后  关闭引擎,
//        AVChatManager.getInstance().disableRtc();
    }

    private void handleAcceptFailed() {
        AVChatManager.getInstance().stopVideoPreview();
        AVChatManager.getInstance().disableVideo();
        AVChatManager.getInstance().disableRtc();

    }

    public void doLogin() {
        /**
         * 用户名 ： hk4545
         token :   bd2292f35af4efbd415173414cd24cd8
         ---------------------------------------------------------------
         用户名 : hkhk45  MZ
         token ： 430a702709934751b002d8f30388e73f
         */

//          MZ  1020025      小米  1020723
        LoginInfo info = new LoginInfo("hk4545", "bd2292f35af4efbd415173414cd24cd8"); // config...
        NIMClient
                .getService(AuthService.class)
                .login(info)
                .setCallback(callback);
    }


    @Override
    public void onTouch() {
    }

    @Override
    public void onTakeSnapshotResult(String account, boolean success, String file) {

    }

    @Override
    public void onAVRecordingCompletion(String account, String filePath) {

    }

    @Override
    public void onAudioRecordingCompletion(String filePath) {

    }

    @Override
    public void onLowStorageSpaceWarning(long availableSize) {

    }

    @Override
    public void onAudioMixingEvent(int event) {

    }

    @Override
    public void onJoinedChannel(int code, String audioFile, String videoFile, int elapsed) {
        if (code == 200) {
            Toast.makeText(MainActivity.this, "code成功", Toast.LENGTH_SHORT).show();
        } else if (code == 101) { // 连接超时
            Toast.makeText(MainActivity.this, "code连接超时", Toast.LENGTH_SHORT).show();
        } else if (code == 401) { // 验证失败
            Toast.makeText(MainActivity.this, "code验证失败", Toast.LENGTH_SHORT).show();

        } else if (code == 417) { // 无效的channelId
            Toast.makeText(MainActivity.this, "code无效的channelId", Toast.LENGTH_SHORT).show();
        } else { // 连接服务器错误，直接退出
            Toast.makeText(MainActivity.this, "code连接服务器错误,直接退出", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "onJoinedChannel------", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserJoined(String account) {
//        Toast.makeText(MainActivity.this, "onUserJoined", Toast.LENGTH_SHORT).show();
        if(!account.isEmpty()){
            avChatSurface.initLargeSurfaceView(account);
        }
        Toast.makeText(MainActivity.this, "onUserJoined", Toast.LENGTH_SHORT).show();
        Log.e(  "account", "account:" + account);

    }

    @Override
    public void onUserLeave(String account, int event) {
        Toast.makeText(MainActivity.this, "onUserLeave" + account, Toast.LENGTH_SHORT).show();
        hangUpMethod();
    }

    @Override
    public void onLeaveChannel() {

    }

    @Override
    public void onProtocolIncompatible(int status) {

    }

    @Override
    public void onDisconnectServer() {

    }

    @Override
    public void onNetworkQuality(String user, int quality, AVChatNetworkStats stats) {

    }

    @Override
    public void onCallEstablished() {


//        avChatSurface.initSmallSurfaceView(SharePreUtils.getString(HB.NAME , ""));
//        avChatSurface.initSmallSurfaceView(SharePreUtils.getString(HB.NAME, ""));
//        avChatSurface.isNO();

    }

    @Override
    public void onDeviceEvent(int code, String desc) {

    }

    @Override
    public void onConnectionTypeChanged(int netType) {

    }

    @Override
    public void onFirstVideoFrameAvailable(String account) {

    }

    @Override
    public void onFirstVideoFrameRendered(String user) {

    }

    @Override
    public void onVideoFrameResolutionChanged(String user, int width, int height, int rotate) {

    }

    @Override
    public void onVideoFpsReported(String account, int fps) {

    }

    @Override
    public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {

        return true;
    }

    @Override
    public boolean onAudioFrameFilter(AVChatAudioFrame frame) {

        return true;
    }

    @Override
    public void onAudioDeviceChanged(int device) {

    }

    @Override
    public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {

    }

    @Override
    public void onSessionStats(AVChatSessionStats sessionStats) {

    }

    @Override
    public void onLiveEvent(int event) {

    }


    /**
     * 关闭本地音视频各项功能
     *
     */
    public void closeSessions() {
        //not  user  hang up active  and warning tone is playing,so wait its end

        if (avChatSurface != null) {
//            avChatSurface.closeSession();
            avChatSurface = null;
        }

    }

}

