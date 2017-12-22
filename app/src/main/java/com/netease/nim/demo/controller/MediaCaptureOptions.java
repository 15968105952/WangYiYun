package com.netease.nim.demo.controller;

import java.io.Serializable;

/**
 * Created by hzxuwen on 2017/4/6.
 */

public class MediaCaptureOptions implements Serializable {
    /**
     * 分辨率
     */
    public int mVideoPreviewWidth = 720;
    /**
     * 分辨率
     */
    public int mVideoPreviewHeight = 1280;
    /**
     * 帧率
     */
    public int mVideoPreviewFrameRate = 30;
    /**
     * 默认摄像头，后置摄像头：false，前置摄像头：true
     */
    public boolean cameraPosition = true;
    /**
     * 录制文件类型，MP4:1, FLV:0
     */
    public int mFileType = 1;
    /**
     *  录制文件路径
     */
    public String mFilePath = "/sdcard/media.mp4";
    /**
     * 码率
     */
    public int bitrate = 10000000;

    /**
     * 分辨率
     */
    public ResolutionType resolutionType = ResolutionType.HD;
}
