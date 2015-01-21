package com.share.ylh.mediaplayer.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.share.ylh.mediaplayer.base.BaseApp;
import com.share.ylh.mediaplayer.service.MyService;
import com.share.ylh.mediaplayer.ui.adapter.IshowShareView;
import com.share.ylh.mediaplayer.ui.adapter.MyAdapter;
import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.domain.FileInfo;
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
public class PlayActivity extends ActionBarActivity implements IshowShareView {

    //2activity=>myservice 播放音乐
    private static final String ACTION_PLAY = "com.share.ylh.mediaplayer.PLAY";
    //myservice=>2activity 保存状态
    private static final String ACTION_SAVESTATE = "com.share.ylh.mediaplayer.SAVESTATE";
    //myservice->MyActivity 更新进度条
    private static final String ACTION_progressbar = "com.share.ylh.mediaplayer.progressbar";

    private static final int DONE = 1;

    private static int id = 1;  //文件id
    private int STATE;//1:play,2:psuse,3:stop;//7：上一首 8：下一首
    private int playstate = 4;//  4:顺序播放;5：单曲循环播放；6：随机；
    private static int ListViewpostion;

    private IntentFilter intentFilter = null;
    private ProgressDialog mProgressDialog;
    private ListView mListView;
    private MyAdapter adapter;
    private List<HashMap<String, String>> filelists;
    private DbUtils db;
    private LinearLayout share, buttonmenu, playbar, toMyActivity;
    private TextView cancel;
    private ImageView playerprebtn, playerplaybtn, playerpausebtn, playernextbtn;


    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == DONE) {

                //保存数据库
                FileInfo fileInfo;
                for (int i = 0; i < filelists.size(); i++) {

                    try {
                        FileInfo f = db.findFirst(Selector.from(FileInfo.class).where("id",
                                "=", i));
                        //先将
                        if (null == f) {
                            fileInfo = new FileInfo();
//                            fileInfo.setId(i);
                            fileInfo.setFilePath(filelists.get(i).get("filePath"));
                            db.save(fileInfo);
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
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
        //playStateChange();
        initEvent();
        initService();
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
                filelists = new ArrayList<HashMap<String, String>>();
                List<FileInfo> list = db.findAll(FileInfo.class);//通过类型查找
                HashMap<String, String> map;
                for (int i = 0; i < list.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("fileName", list.get(i).getFilePath().substring(list.get(i)
                            .getFilePath().lastIndexOf("/") + 1, list.get(i)
                            .getFilePath().lastIndexOf(".") - 1));

                    map.put("id", list.get(i).getId() + "");

                    map.put("fileType", list.get(i).getFilePath().substring(list.get(i)
                            .getFilePath().lastIndexOf(".") + 1));

                    filelists.add(map);
                }
                adaptNodify();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化service
     */
    private void initService() {
        Intent intent = new Intent();
        intent.setClass(PlayActivity.this, MyService.class);
        intent.setAction(ACTION_PLAY);
        startService(intent);
    }

    private void playStateChange() {

        if ("1".equals(STATE + "")) {
            playingView();
        } else if ("2".equals(STATE + "")) {
            pausingView();
        }
    }


    private void adaptNodify() {
//        adapter = new SimpleAdapter(getApplicationContext(), filelists, R.layout.player, new String[]{"fileName",
//                "filePath","fileType"}, new int[]{R.id.fileName, R.id.filePath,R.id.fileType});
//        mListView.setAdapter(adapter);
//        mProgressDialog.dismiss();

//        adapter = new MyAdapter(getApplicationContext(), filelists, R.layout.player);
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

//                filelists = ViewUtil.getMusicListOnSys(ViewUtil.getExternalStoragePath(), "mp3");

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
                HashMap<String, String> music = (HashMap<String, String>) lv.getItemAtPosition
                        (position);
                STATE = 9;
                id=Integer.parseInt(music.get("id"));
                sendOperateBroadCast();//发送广播 to service

                Intent intent = new Intent();
                intent.setClass(PlayActivity.this, MyActivity.class);
                startActivity(intent);//页面跳转

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "音乐同享";
                String content = "我正在听" + filelists.get(ListViewpostion).get("fileName");
                String url = "";
                //分享到
                //ViewUtil.showShareDialog(PlayActivity.this, title,content, url);
                ViewUtil.showShareMore(PlayActivity.this, title, content, url);
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
                if ((id - 1) >= 0) {
                    id = id - 1;
                } else {
                    id = 0;
                    Toast.makeText(getApplicationContext(), "已经是第一首歌", Toast.LENGTH_SHORT).show();
                }
                Log.e("playerprebtn====>id", "" + id);
                STATE=9;
                sendOperateBroadCast();
                playingView();//页面的变化

            }
        });

        playernextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((id + 1) <= filelists.size()) {
                    id = (id + 1);
                } else {
                    id = filelists.size();
                    Toast.makeText(getApplicationContext(), "已经是最后一首歌", Toast.LENGTH_SHORT).show();
                }
                Log.e("playernextbtn====>id", "" + id);
                STATE=9;
                sendOperateBroadCast();
                playingView();
            }
        });
        playerplaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                STATE=1;
                sendOperateBroadCast();
                playingView();
            }
        });
        playerpausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                STATE=2;
                sendOperateBroadCast();
                pausingView();
            }
        });

        toMyActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayActivity.this, MyActivity.class));
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

    private void sendOperateBroadCast() {

        Intent intent = new Intent(ACTION_PLAY);
        intent.putExtra("id", id);//文件id（1,2,3） 不是路径
        intent.putExtra("STATE", STATE);
        intent.putExtra("playstate", playstate);//需要运算的用int，另外用字符串
        ViewUtil.Loge("playActivity==="+id+"==="+STATE+"==="+playstate);

        BaseApp.AppContext.sendBroadcast(intent);
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
                id=intent.getIntExtra("id",0);
                STATE=intent.getIntExtra("STATE",1);
                playstate=intent.getIntExtra("playstate",4);
                playStateChange();
                ViewUtil.Loge("PlayActivity===" + "" + "=======" + id + "===" + STATE + "===" );
            }
        }
    };
}
