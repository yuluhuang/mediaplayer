package com.share.ylh.mediaplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.domain.LyricObject;
import com.share.ylh.mediaplayer.utils.GetGeCi;
import com.share.ylh.mediaplayer.utils.ViewUtil;
import java.util.List;


/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-13
 * Time: 12:30
 * FIXME
 */
public class MyService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    //2activity=>myservice 播放音乐
    private static final String ACTION_PLAY = "com.share.ylh.mediaplayer.PLAY";
    //myservice=>2activity 保存状态
    private static final String ACTION_SAVESTATE = "com.share.ylh.mediaplayer.SAVESTATE";
    //myservice->MyActivity 更新进度条
    private static final String ACTION_progressbar = "com.share.ylh.mediaplayer.progressbar";
    //歌词下载后发送广播
    private static final String ACTION_DOWNLOADLYRIC = "com.share.ylh.mediaplayer.DOWNLOADLYRIC";
    private static  final int DOWNLOAD=2;
    private static int id = 1;  //文件id
    private int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private int playstate = 4;//  4:顺序播放;5：单曲循环播放；6：随机；
    private DbUtils db;
    private static List<FileInfo> list;
    private static FileInfo fileInfo;
    MediaPlayer mp = null;
    private boolean isprepare;

    private IntentFilter intentFilter = null;

    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==DOWNLOAD){
                //TODO 发送广播 activity好像没接到 歌词显示也有问题
                Intent intent2 =new Intent(ACTION_DOWNLOADLYRIC);
                BaseApp.AppContext.sendBroadcast(intent2);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (mp == null) {
            synchronized (MyService.class) {
                if (mp == null) {

                    initDatas();//实例化数据库
                    registerReceiver(receiver, init());
                    Log.e("service BroadcastReceiver ", "注册广播");
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void startPlay() {

        try {
            if (mp != null && STATE == 2) {
                Log.e("startPlay pause", "" + STATE);
                mp.pause();
            }
        } catch (Exception e) {
            Log.e("Service pause Exception", "Service pause Exception");
        }

        try {
            if (mp != null && STATE == 1) {
                mp.start();
            }
        } catch (Exception e) {
            Log.e("Service start Exception", "Service start Exception");
        }
    }


    private void initDatas() {
        try {
            db = DbUtils.create(this);
            list = db.findAll(FileInfo.class);//通过类型查找
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private void initMediaPlayer() {
        try {
            if (mp != null) {
                mp.release();
                mp = null;
            }
            mp = new MediaPlayer();

            //mp=MediaPlayer.create(MyActivity.this,Uri.parse(list.get(id).getFilePath()));
            // 设置指定的流媒体地址
            mp.setDataSource(fileInfo.getFilePath());

            // 设置音频流的类型
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setOnPreparedListener(this);
            mp.setOnErrorListener(this);
            mp.setOnCompletionListener(this);
            //Using wake locks  http://blog.csdn.net/shichaosong/article/details/8125343
            //mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            isprepare = true;
            // 通过异步的方式装载媒体资源
            mp.prepareAsync();

        } catch (Exception e) {
            STATE = 2;
        }

    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        try {
              /*发生错误时也解除资源与MediaPlayer的赋值*/
            mp.release();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //一首歌播放完后执行
        try {
            /*解除资源与MediaPlayer的赋值关系让资源可以为其它程序利用*/
            String ps = playstate + "";
            if ("4".equals(ps)) {
                //顺序播放
                repeatPlayer();

            } else if ("5".equals(ps)) {
                //重复
                mediaPlayer.start();

            } else if ("6".equals(ps)) {
                int ramdom = ViewUtil.getRandam(0, list.size());
                id = ramdom;
                //随机
                initMediaPlayer();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        mediaPlayer.start();
        isprepare = false;
        new Thread(mRunnable).start();

    }

    //更新ui进度条
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                if (isprepare) {
                    //正在准备播放资源
                    return;
                }

                //发送广播，更新进度条
                Intent intent = new Intent(ACTION_progressbar);
                try {
                    //暂停后切换 异常 进行捕获
                    intent.putExtra("Current", mp.getCurrentPosition());

                } catch (Exception e) {
                    intent.putExtra("Current", 0);
                }
                try {
                    //重新选歌 异常 进行捕获
                    intent.putExtra("Duration", mp.getDuration());

                } catch (Exception e) {
                    intent.putExtra("Duration", 0);
                }

                BaseApp.AppContext.sendBroadcast(intent);


                //发送广播，保持状态
                Intent intent1 = new Intent(ACTION_SAVESTATE);
                intent1.putExtra("id", id);//文件id（1,2,3） 不是路径
                intent1.putExtra("STATE", STATE);//文件id（1,2,3） 不是路径
                intent1.putExtra("playstate", playstate);//需要运算的用int，另外用字符串

                BaseApp.AppContext.sendBroadcast(intent1);


                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 顺序播放
     */
    private void repeatPlayer() {
        id = id + 1;
        if (id <= list.size()) {
            initMediaPlayer();
        } else {
            id = 0;
            initMediaPlayer();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 解除注册
        Toast.makeText(BaseApp.AppContext, "解除注册", Toast.LENGTH_SHORT).show();
        unregisterReceiver(receiver);
    }

    private IntentFilter init() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_PLAY);

        }
        return intentFilter;
    }

    private void getFileInfo() {
        try {
            fileInfo = db.findFirst(Selector.from(FileInfo.class).where("id",
                    "=", id));

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            id = intent.getIntExtra("id", 0);
            STATE = intent.getIntExtra("STATE", 1);
            playstate = intent.getIntExtra("playstate", 4);

            if (intent.getAction().equals(ACTION_PLAY)) {
                Log.e("service BroadcastReceiver ", "接收广播");
                ViewUtil.Loge("MyService===" + "" + "=======" + id + "===" + STATE + "===" +
                        playstate);

                //上。下一首 。listview 进来
                if (STATE == 7 || STATE == 8 || STATE == 9) {

                    if (STATE == 7) {
                        if ((id - 1) >= 0) {
                            id = id - 1;
                        } else {
                            id = 0;
                            //Toast.makeText(BaseApp.AppContext, "已经是第一首歌",
                                   // Toast.LENGTH_SHORT).show();
                        }
                    } else if (STATE == 8) {
                        if ((id + 1) <= list.size()) {
                            id = (id + 1);
                        } else {
                            id = list.size();
                            //Toast.makeText(BaseApp.AppContext, "已经是最后一首歌",
                                  //  Toast.LENGTH_SHORT).show();
                        }
                    }
                    STATE = 1;
                    getFileInfo();

                    initMediaPlayer();//直接播放
                    downGeCi(fileInfo.getFilePath(),id);


                } else if (STATE == 1 || STATE == 2) {
                    startPlay();//判断状态 后决定是否播放
                }
            }
        }
    };


    /**
     * 通过thread 下载歌词
     * 通过handler发送广播显示歌词
     */
    private void downGeCi(final String filepath, final int musicId){

        try {
            if(db.findAll(Selector.from(LyricObject.class).where
                    ("musciId", "=", id))==null){
                return;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetGeCi.getGeCi(filepath,musicId);

                Message message=Message.obtain();
                message.what=DOWNLOAD;
                handler.sendMessage(message);

            }
        }).start();
    }
}
