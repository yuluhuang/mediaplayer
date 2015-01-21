package com.share.ylh.mediaplayer.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.domain.MMediaPlayer;
import com.share.ylh.mediaplayer.utils.ViewUtil;

import java.util.List;


public class MyActivityOO extends Activity {

    //2activity=>myservice 播放音乐
    private static final String ACTION_PLAY = "com.share.ylh.mediaplayer.PLAY";
    //myservice=>2activity 保存状态
    private static final String ACTION_SAVESTATE = "com.share.ylh.mediaplayer.SAVESTATE";
    //myservice->MyActivity 更新进度条
    private static final String ACTION_progressbar = "com.share.ylh.mediaplayer.progressbar";

    private DbUtils db;
    List<FileInfo> list;
    private ImageView play, pause, pre, next, repeat, shuffle, replay;
    private ProgressBar progressBar;
    private IntentFilter intentFilter = null;

    private static int id = 1;  //文件id
    private int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private int playstate = 4;//  4:顺序播放;5：单曲循环播放；6：随机；

    MMediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initView();
        initEvent();
        mMediaPlayer = MMediaPlayer.get(list);//初始化 MMediaPlayer
    }


    private void initView() {

        try {
            db = DbUtils.create(this);
            list = db.findAll(FileInfo.class);//通过类型查找
            Log.e("AAAAAAAA", list.size() + "");
        } catch (DbException e) {
            e.printStackTrace();
        }

        play = (ImageView) findViewById(R.id.playerplaybtn);
        pause = (ImageView) findViewById(R.id.playerpausebtn);
        pre = (ImageView) findViewById(R.id.playerprebtn);
        next = (ImageView) findViewById(R.id.playernextbtn);
        repeat = (ImageView) findViewById(R.id.repeat);
        shuffle = (ImageView) findViewById(R.id.shuffle);
        replay = (ImageView) findViewById(R.id.replay);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    /**
     * 退出进来后保持播放类型，顺序，随机，重复
     * 根据state判断
     */
    private void stayPlayType() {
        if (STATE == 1) {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
        } else if (STATE == 2) {
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
        }

        if (playstate == 4) {
            //顺序
            shuffle.setVisibility(View.GONE);
            replay.setVisibility(View.GONE);
            repeat.setVisibility(View.VISIBLE);
        } else if (playstate == 5) {
            //重复
            repeat.setVisibility(View.GONE);
            shuffle.setVisibility(View.GONE);
            replay.setVisibility(View.VISIBLE);
        } else if (playstate == 6) {
            //6
            replay.setVisibility(View.GONE);
            repeat.setVisibility(View.GONE);
            shuffle.setVisibility(View.VISIBLE);
        } else {
            //默认顺序播放
            shuffle.setVisibility(View.GONE);
            replay.setVisibility(View.GONE);
            repeat.setVisibility(View.VISIBLE);
        }
    }


    private void initEvent() {

        //播放
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
        });

        //暂停
        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mMediaPlayer.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });

        //上一首
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pre();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
        });

        //下一首
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.next();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
        });

        //顺序->重复
        repeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mMediaPlayer.repeat();
                repeat.setVisibility(View.GONE);
                replay.setVisibility(View.VISIBLE);
            }


        });

        //重复->随机
        replay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.replay();
                replay.setVisibility(View.GONE);
                shuffle.setVisibility(View.VISIBLE);
            }
        });

        //随机->顺序
        shuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.shuffle();
                shuffle.setVisibility(View.GONE);
                repeat.setVisibility(View.VISIBLE);
            }
        });

        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgress(100);
            }
        });
    }


    private void sendOperateBroadCast() {
        Log.i("MYACT sendOperateBroadCast", STATE + "===" + id);
        stayPlayType();
        Intent intent = new Intent(ACTION_PLAY);
        intent.putExtra("id", id);//文件id（1,2,3） 不是路径
        intent.putExtra("STATE", STATE);//文件id（1,2,3） 不是路径
        intent.putExtra("playstate", playstate);//需要运算的用int，另外用字符串
        BaseApp.AppContext.sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 解除注册
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        IntentFilter dynamic_filter = new IntentFilter();
//        dynamic_filter.addAction("com.share.ylh.mediaplayer.broatcast");//添加动态广播的Action
//        registerReceiver(receiver, dynamic_filter);  // 注册自定义动态广播消息
        // 接收广播
        registerReceiver(receiver, init());
    }

    private IntentFilter init() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_progressbar);
            intentFilter.addAction(ACTION_SAVESTATE);
        }
        return intentFilter;
    }

    //接收跟新进度条的广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_progressbar)) {
                int Current = intent.getIntExtra("Current", 0);
                int Duration = intent.getIntExtra("Duration", 0);
                //ViewUtil.Loge(Current+"==="+Duration);
                progressBar.setMax(Duration);
                progressBar.setProgress(Current);
            }

            if (intent.getAction().equals(ACTION_SAVESTATE)) {
                id = intent.getIntExtra("id", 0);
                STATE = intent.getIntExtra("STATE", 1);
                playstate = intent.getIntExtra("playstate", 4);
                stayPlayType();
                //ViewUtil.Loge("MyActivity===" + "" + "=======" + id + "===" + STATE + "===");
            }
        }
    };
}
