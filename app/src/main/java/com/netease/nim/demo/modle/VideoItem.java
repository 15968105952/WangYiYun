package com.netease.nim.demo.modle;

import java.io.Serializable;

/**
 * Created by zhukkun on 2/22/17.
 */
public class VideoItem implements Serializable {
    String id; //本地视频才具有id
    String uriString;

    String displayName;
    String filePath;
    String dateTaken;//产生的时间
    String duration;
    long size;
    long width;
    long height;
    long vid; //上传至云端后的视频, 才具有vid
    int uploadProgress; //上传进度

    //nos用于断点续传
    String uploadContext;
    String uploadBucket;
    String uploadToken;
    String uploadObject;

    VideoInfoEntity entity; //上传完成后, 服务端返回的视频实体类.

    int state;
    int type; //视频类型(0:普通点播视频, 1:短视频)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public long getVid() {
        return vid;
    }

    public void setVid(long vid) {
        this.vid = vid;
    }

    public VideoInfoEntity getEntity() {
        return entity;
    }

    public void setEntity(VideoInfoEntity entity) {
        this.entity = entity;
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getUploadContext() {
        return uploadContext;
    }

    public void setUploadContext(String uploadContext) {
        this.uploadContext = uploadContext;
    }

    public String getUploadBucket() {
        return uploadBucket;
    }

    public void setUploadBucket(String uploadBucket) {
        this.uploadBucket = uploadBucket;
    }

    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public String getUploadObject() {
        return uploadObject;
    }

    public void setUploadObject(String uploadObject) {
        this.uploadObject = uploadObject;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
