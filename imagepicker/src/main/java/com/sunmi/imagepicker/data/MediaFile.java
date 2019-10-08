package com.sunmi.imagepicker.data;

/**
 * 媒体实体类
 * Create by: chenWei.li
 * Date: 2018/8/22
 * Time: 上午12:36
 * Email: lichenwei.me@foxmail.com
 */
public class MediaFile {

    /**
     * 路径
     */
    private String path;
    private String mime;
    /**
     * 文件夹Id
     */
    private Integer folderId;
    /**
     * 文件夹名
     */
    private String folderName;
    /**
     * 媒体类型
     */
    private long duration;
    /**
     * 创建的时间
     */
    private long dateToken;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDateToken() {
        return dateToken;
    }

    public void setDateToken(long dateToken) {
        this.dateToken = dateToken;
    }
}

