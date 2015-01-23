package com.share.ylh.mediaplayer.domain;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.service.MyService;
import com.share.ylh.mediaplayer.ui.MyActivityOO;

import java.util.ArrayList;
import java.util.List;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-20
 * Time: 08:54
 * FIXME
 */
public class MMediaPlayer {
    //2activity=>myservice 播放音乐
    private static final String ACTION_PLAY = "com.share.ylh.mediaplayer.PLAY";

    private static int id=1;  //文件id
    private static int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private static int playstate=4;//  4:顺序播放;5：单曲循环播放；6：随机；

    private  static MMediaPlayer mMediaPlayer;
    private static List<FileInfo> lists;

    public MMediaPlayer(List<FileInfo> list) {
        this.lists = list;
    }



    public static MMediaPlayer get(List<FileInfo> l) {

        if (mMediaPlayer == null) {
            synchronized (MMediaPlayer.class){
                if(mMediaPlayer == null){
                    initService();
                    mMediaPlayer = new MMediaPlayer(l);
                }
            }
        }

        return mMediaPlayer;
    }

    public void start() {
        STATE = 1;
        sendOperateBroadCast();
    }

    public void pause() {
        STATE = 2;
        sendOperateBroadCast();
    }

    public void listViewStart(int musicid) {

        STATE = 9;
        id = musicid;
        sendOperateBroadCast();//发送广播 to service

        Intent intent = new Intent();
        intent.setClass(BaseApp.AppContext, MyActivityOO.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseApp.AppContext.startActivity(intent);//页面跳转
    }

    public void pre() {

        Log.e("playerprebtn====>id", "" + id);
        STATE = 7;
        sendOperateBroadCast();
    }


    public void next() {

        Log.e("playernextbtn====>id", "" + id);
        STATE = 8;
        sendOperateBroadCast();
    }

    //以下单击 状态变为下一级
    public void repeat() {
        STATE = 0;
        playstate = 5;
        sendOperateBroadCast();
    }

    public void shuffle() {
        STATE = 0;
        playstate = 4;
        sendOperateBroadCast();
    }


    public void replay() {
        STATE = 0;
        playstate = 6;
        sendOperateBroadCast();
    }


    private void sendOperateBroadCast() {

        Intent intent = new Intent(ACTION_PLAY);
        intent.putExtra("id", id);//文件id（1,2,3） 不是路径
        intent.putExtra("STATE", STATE);
        intent.putExtra("playstate", playstate);//需要运算的用int，另外用字符串
        //ViewUtil.Loge("playActivity===" + id + "===" + STATE + "===" + playstate);

        BaseApp.AppContext.sendBroadcast(intent);
    }


    /**
     * 初始化service
     */
    private static void initService() {
        Intent intent = new Intent();
        intent.setClass(BaseApp.AppContext.getApplicationContext(), MyService.class);
        //intent.setAction(ACTION_PLAY);
        BaseApp.AppContext.startService(intent);
    }

}
