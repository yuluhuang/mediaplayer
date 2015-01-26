package com.share.ylh.mediaplayer.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.domain.LyricObject;
import com.share.ylh.mediaplayer.domain.MMediaPlayer;
import com.share.ylh.mediaplayer.utils.ViewUtil;

import java.util.List;


public class MyFragment extends Fragment {

    //2activity=>myservice 播放音乐
    private static final String ACTION_PLAY = "com.share.ylh.mediaplayer.PLAY";
    //myservice=>2activity 保存状态
    private static final String ACTION_SAVESTATE = "com.share.ylh.mediaplayer.SAVESTATE";
    //myservice->MyActivity 更新进度条
    private static final String ACTION_progressbar = "com.share.ylh.mediaplayer.progressbar";
    //歌词下载后发送广播
    private static final String ACTION_DOWNLOADLYRIC = "com.share.ylh.mediaplayer.DOWNLOADLYRIC";
    private DbUtils db;
    List<FileInfo> list;
    private ImageView play, pause, pre, next, repeat, shuffle, replay;
    private ProgressBar progressBar;
    private IntentFilter intentFilter = null;

    private static int id = 1;  //文件id
    private static int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private static int playstate = 4;//  4:顺序播放;5：单曲循环播放；6：随机；

    private static MMediaPlayer mMediaPlayer;
    private static boolean isDownLoad;
    LocalBroadcastManager broadcastManager;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.activity_my,container,false);
        initView(v);
        initEvent();
        mMediaPlayer = MMediaPlayer.get(list);//初始化 MMediaPlayer
        return v;
    }




    private void initView(View v) {

        play = (ImageView) v.findViewById(R.id.playerplaybtn);
        pause = (ImageView) v.findViewById(R.id.playerpausebtn);
        pre = (ImageView) v.findViewById(R.id.playerprebtn);
        next = (ImageView) v.findViewById(R.id.playernextbtn);
        repeat = (ImageView) v.findViewById(R.id.repeat);
        shuffle = (ImageView) v.findViewById(R.id.shuffle);
        replay = (ImageView) v.findViewById(R.id.replay);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

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

        try {
            db = DbUtils.create(getActivity());
            list = db.findAll(FileInfo.class);//通过类型查找
            Log.e("AAAAAAAA", list.size() + "");
        } catch (DbException e) {
            e.printStackTrace();
        }
        broadcastManager=LocalBroadcastManager.getInstance(getActivity());

        //播放
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
//                play.setVisibility(View.GONE);
//                pause.setVisibility(View.VISIBLE);
            }
        });

        //暂停
        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mMediaPlayer.pause();
//                pause.setVisibility(View.GONE);
//                play.setVisibility(View.VISIBLE);
            }
        });

        //上一首
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pre();
//                play.setVisibility(View.GONE);
//                pause.setVisibility(View.VISIBLE);
            }
        });

        //下一首
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.next();
//                play.setVisibility(View.GONE);
//                pause.setVisibility(View.VISIBLE);
            }
        });

        //顺序->重复
        repeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mMediaPlayer.repeat();

            }


        });

        //重复->随机
        replay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.replay();

            }
        });

        //随机->顺序
        shuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMediaPlayer.shuffle();

            }
        });

        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().setProgress(100);
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
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 解除注册
        broadcastManager.unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 接收广播
        broadcastManager.registerReceiver(receiver, init());
    }

    private IntentFilter init() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_progressbar);
            intentFilter.addAction(ACTION_SAVESTATE);
        }
        return intentFilter;
    }

    private static Integer temptime = 0;
    private List<LyricObject> lyricObjects;
    //接收跟新进度条的广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (ACTION_DOWNLOADLYRIC.equals(intent.getAction())) {
                isDownLoad = true;
            }

            if (intent.getAction().equals(ACTION_progressbar)) {
                int Current = intent.getIntExtra("Current", 0);
                int Duration = intent.getIntExtra("Duration", 0);

                //ViewUtil.Loge(Current+"==="+Duration);

                progressBar.setMax(Duration);
                progressBar.setProgress(Current);


               // if (isDownLoad) {
                    try {
                        lyricObjects = db.findAll(Selector.from(LyricObject.class).where
                                ("musicId", "=", id));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }

                    if (lyricObjects != null) {
                        for (int i = 0; i < lyricObjects.size(); i++) {
                            Integer key = (Integer) lyricObjects.get(i).getBegintime();
                            if (temptime < Current && key > Current) {
                                ViewUtil.Loge(Current + "===" + key);

                                ViewUtil.ToastText(lyricObjects.get(i).getLrc(), true);
                                try {
                                    temptime = key; //记录上一句歌词的时间
                                } catch (Exception e) {
                                    temptime = 0;
                                }
                                break;
                            }
                        }
                  //  }
                }

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
