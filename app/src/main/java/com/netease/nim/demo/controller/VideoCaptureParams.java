package com.netease.nim.demo.controller;


import java.util.List;

/**
 * 视频录制参数配置
 * Created by hzxuwen on 2017/4/7.
 */

public class VideoCaptureParams {
    private int count;
    private List<String> videoPathList;
    private long time; // ms为单位
    private ResolutionType resolutionType;

    public VideoCaptureParams(int count, long time, ResolutionType resolutionType) {
        this.count = count;
        this.time = time;
        this.resolutionType = resolutionType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getVideoPathList() {
        return videoPathList;
    }

    public void setVideoPathList(List<String> videoPathList) {
        this.videoPathList = videoPathList;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ResolutionType getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(ResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }
}
