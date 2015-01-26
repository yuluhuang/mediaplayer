package com.share.ylh.mediaplayer.domain;

/**
 * Created by ylh on 2015/1/24.
 */
public enum ACTION {
    //2activity=>myservice 播放音乐
    ACTION_PLAY("com.share.ylh.mediaplayer.PLAY"),
    //myservice=>2activity 保存状态
    ACTION_SAVESTATE("com.share.ylh.mediaplayer.SAVESTATE"),
    //myservice->MyActivity 更新进度条
    ACTION_progressbar("com.share.ylh.mediaplayer.progressbar"),
    //歌词下载后发送广播
    ACTION_DOWNLOADLYRIC("com.share.ylh.mediaplayer.DOWNLOADLYRIC");

    private String action;

    ACTION(String action) {
        this.action=action;

    }
}
