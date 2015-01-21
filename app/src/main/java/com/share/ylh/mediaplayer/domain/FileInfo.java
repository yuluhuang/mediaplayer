package com.share.ylh.mediaplayer.domain;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-13
 * Time: 15:19
 * FIXME
 */
public class FileInfo {


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    private String filePath;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
