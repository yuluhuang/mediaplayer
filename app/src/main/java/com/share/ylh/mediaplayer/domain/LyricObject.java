package com.share.ylh.mediaplayer.domain;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-23
 * Time: 16:18
 * FIXME
 */
public class LyricObject {

    private int id;
    private int musciId;//对应歌曲
    private int begintime; // 开始时间
    private int endtime; // 结束时间
    private int timeline; // 单句歌词用时
    private String lrc; // 单句歌词

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getMusciId() {
        return musciId;
    }

    public void setMusciId(int musciId) {
        this.musciId = musciId;
    }

    public int getBegintime() {
        return begintime;
    }

    public void setBegintime(int begintime) {
        this.begintime = begintime;
    }

    public int getEndtime() {
        return endtime;
    }

    public void setEndtime(int endtime) {
        this.endtime = endtime;
    }

    public int getTimeline() {
        return timeline;
    }

    public void setTimeline(int timeline) {
        this.timeline = timeline;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

}
