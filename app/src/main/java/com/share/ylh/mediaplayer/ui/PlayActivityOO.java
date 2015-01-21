package com.share.ylh.mediaplayer.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.domain.FileInfo;
import com.share.ylh.mediaplayer.domain.MMediaPlayer;
import com.share.ylh.mediaplayer.service.MyService;
import com.share.ylh.mediaplayer.ui.adapter.IshowShareView;
import com.share.ylh.mediaplayer.ui.adapter.MyAdapter;
import com.share.ylh.mediaplayer.utils.ViewUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-13
 * Time: 16:08
 * <p/>
 * 存在本地的有id==>歌曲id
 * 存在本地的有STATE==>播放，暂停，
 * 存在本地的有playstate==>循环，重复，随机
 * FIXME
 */
public class PlayActivityOO extends ActionBarActivity implements IshowShareView {

    //myservice=>2activity 保存状态
    private static final String ACTION_SAVESTATE = "com.share.ylh.mediaplayer.SAVESTATE";
    private static int id;  //文件id
    private int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private int playstate;//  4:顺序播放;5：单曲循环播放；6：随机；
    private static final int DONE = 1;
    private static int ListViewpostion;

    private MMediaPlayer mMediaPlayer;
    private IntentFilter intentFilter = null;
    private ProgressDialog mProgressDialog;
    private ListView mListView;
    private MyAdapter adapter;
    private List<FileInfo> filelists;
    private DbUtils db;
    private LinearLayout share, buttonmenu, playbar, toMyActivity;
    private TextView cancel;
    private ImageView playerprebtn, playerplaybtn, playerpausebtn, playernextbtn;


    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == DONE) {

                //首次保存数据库
                FileInfo fileInfo;
                for (int i = 0; i < filelists.size(); i++) {

                    try {
                        FileInfo f = db.findFirst(Selector.from(FileInfo.class).where("filePath",
                                "=", filelists.get(i).getFilePath()));
                        //先将
                        if (null == f) {
                            fileInfo = new FileInfo();
                            fileInfo.setId(i);
                            fileInfo.setFilePath(filelists.get(i).getFilePath());
                            db.save(fileInfo);

                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    db.findAll(Selector.from(FileInfo.class));
                } catch (DbException e) {
                    e.printStackTrace();
                }
                adaptNodify();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlist);

        initView();
        playStateChange();
        initEvent();
        try {

            String count = ViewUtil.getShardPStringByKey("count");
            /*
            * 如果数据库没数据，初始化
            * */
            if ("".equals(count)) {
                mProgressDialog.show();
                searchMusic();
            } else {
                /*
                * 从数据库取数据
                * */

                filelists = db.findAll(FileInfo.class);//通过类型查找

                adaptNodify();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        mMediaPlayer = MMediaPlayer.get(filelists);//初始化 MMediaPlayer
    }


    private void playStateChange() {

        if ("1".equals(STATE + "")) {
            playingView();
        } else if ("2".equals(STATE + "")) {
            pausingView();
        }
    }


    private void adaptNodify() {

        adapter = new MyAdapter(getApplicationContext(), filelists, R.layout.player);
        mListView.setAdapter(adapter);

        adapter.setIshowview(this);
        mProgressDialog.dismiss();
    }

    /**
     * 重新扫描文件
     */
    private void searchMusic() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                filelists = ViewUtil.getMusicListOnSys(ViewUtil.getExternalStoragePath(), "mp3");

                //保存歌曲数量
                ViewUtil.setShardPString("count", filelists.size() + "");
                Message message = Message.obtain();
                message.what = DONE;
                mhandler.sendMessage(message);
            }
        }).start();
    }


    private void initView() {
        db = DbUtils.create(this);
        filelists = new ArrayList<FileInfo>();
        mListView = (ListView) findViewById(R.id.playerlists);
        share = (LinearLayout) findViewById(R.id.share);
        playbar = (LinearLayout) findViewById(R.id.playbar);
        toMyActivity = (LinearLayout) findViewById(R.id.toMyActivity);
        buttonmenu = (LinearLayout) findViewById(R.id.buttonmenu); //点击分享，弹出

        playerprebtn = (ImageView) findViewById(R.id.playerprebtn);
        playerplaybtn = (ImageView) findViewById(R.id.playerplaybtn);
        playerpausebtn = (ImageView) findViewById(R.id.playerpausebtn);
        playernextbtn = (ImageView) findViewById(R.id.playernextbtn);

        cancel = (TextView) findViewById(R.id.cancel);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("正在搜索mp3文件，请稍后...");
        mProgressDialog.setCancelable(false);
    }


    private void initEvent() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long idd) {
                ListView lv = (ListView) parent;
                //Cursor cursor = (Cursor)lv.getItemAtPosition(position);
                FileInfo music = (FileInfo) lv.getItemAtPosition(position);

                mMediaPlayer.listViewStart(music.getId());

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "音乐同享";
                String content = "我正在听" + ViewUtil.getFileName(filelists.get(ListViewpostion).getFilePath());
                String url = "";
                //分享到
                //ViewUtil.showShareDialog(PlayActivity.this, title,content, url);
                ViewUtil.showShareMore(PlayActivityOO.this, title, content, url);
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonmenu.setVisibility(View.GONE);
            }
        });


        playerprebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pre();
                playingView();//页面的变化

            }
        });

        playernextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMediaPlayer.next();
                playingView();
            }
        });
        playerplaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMediaPlayer.start();
                playingView();
            }
        });
        playerpausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMediaPlayer.pause();
                pausingView();
            }
        });

        toMyActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayActivityOO.this, MyActivity.class));
            }
        });
    }

    /**
     * 播放时界面变化
     */
    private void playingView() {
        playerplaybtn.setVisibility(View.GONE);
        playerpausebtn.setVisibility(View.VISIBLE);
    }

    /**
     * 暂停时界面变化
     */
    private void pausingView() {
        playerpausebtn.setVisibility(View.GONE);
        playerplaybtn.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reflash:
                mProgressDialog.show();
                searchMusic();
                break;
            case R.id.exit:
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册 保存状态的广播
        registerReceiver(receiver, init());
        Log.e("PlayActivity onResume", "PlayActivity onResume");
        playStateChange();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //退出程序
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void showView(int pos) {
        ListViewpostion = pos;
        buttonmenu.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // 解除注册
        unregisterReceiver(receiver);
    }

    private IntentFilter init() {
        if (intentFilter == null) {
            intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_SAVESTATE);
        }
        return intentFilter;
    }

    //广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SAVESTATE)) {
                id = intent.getIntExtra("id", 0);
                STATE = intent.getIntExtra("STATE", 1);
                playstate = intent.getIntExtra("playstate", 4);
                playStateChange();
                //ViewUtil.Loge("PlayActivity===" + "" + "=======" + id + "===" + STATE + "===");
            }
        }
    };
}
