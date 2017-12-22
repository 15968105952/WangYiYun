package com.netease.nim.demo.modle;

import java.io.Serializable;

/**
 * Created by zhukkun on 3/8/17.
 */
public class VideoInfoEntity implements Serializable{

    long shdMp4Size;
    long createTime;
    String origUrl;
    long sdHlsSize;
    String downloadOrigUrl;
    long updateTime;
    int status;
    String sdHlsUrl;
    String shdMp4Url;
    String hdFlvUrl;
    String videoName;
    String downloadShdMp4Url;
    String typeName;
    long hdFlvSize;
    long duration;
    String downloadSdHlsUrl;
    String description;
    String snapshotUrl;
    String downloadHdFlvUrl;
    long initialSize;
    long vid;
    long completeTime;
    long typeId;
    long durationMsec;

    public long getShdMp4Size() {
        return shdMp4Size;
    }

    public void setShdMp4Size(long shdMp4Size) {
        this.shdMp4Size = shdMp4Size;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getOrigUrl() {
        return origUrl;
    }

    public void setOrigUrl(String origUrl) {
        this.origUrl = origUrl;
    }

    public long getSdHlsSize() {
        return sdHlsSize;
    }

    public void setSdHlsSize(long sdHlsSize) {
        this.sdHlsSize = sdHlsSize;
    }

    public String getDownloadOrigUrl() {
        return downloadOrigUrl;
    }

    public void setDownloadOrigUrl(String downloadOrigUrl) {
        this.downloadOrigUrl = downloadOrigUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSdHlsUrl() {
        return sdHlsUrl;
    }

    public void setSdHlsUrl(String sdHlsUrl) {
        this.sdHlsUrl = sdHlsUrl;
    }

    public String getShdMp4Url() {
        return shdMp4Url;
    }

    public void setShdMp4Url(String shdMp4Url) {
        this.shdMp4Url = shdMp4Url;
    }

    public String getHdFlvUrl() {
        return hdFlvUrl;
    }

    public void setHdFlvUrl(String hdFlvUrl) {
        this.hdFlvUrl = hdFlvUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getDownloadShdMp4Url() {
        return downloadShdMp4Url;
    }

    public void setDownloadShdMp4Url(String downloadShdMp4Url) {
        this.downloadShdMp4Url = downloadShdMp4Url;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public long getHdFlvSize() {
        return hdFlvSize;
    }

    public void setHdFlvSize(long hdFlvSize) {
        this.hdFlvSize = hdFlvSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDownloadSdHlsUrl() {
        return downloadSdHlsUrl;
    }

    public void setDownloadSdHlsUrl(String downloadSdHlsUrl) {
        this.downloadSdHlsUrl = downloadSdHlsUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        this.snapshotUrl = snapshotUrl;
    }

    public String getDownloadHdFlvUrl() {
        return downloadHdFlvUrl;
    }

    public void setDownloadHdFlvUrl(String downloadHdFlvUrl) {
        this.downloadHdFlvUrl = downloadHdFlvUrl;
    }

    public long getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(long initialSize) {
        this.initialSize = initialSize;
    }

    public long getVid() {
        return vid;
    }

    public void setVid(long vid) {
        this.vid = vid;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public long getDurationMsec() {
        return durationMsec;
    }

    public void setDurationMsec(long durationMsec) {
        this.durationMsec = durationMsec;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
