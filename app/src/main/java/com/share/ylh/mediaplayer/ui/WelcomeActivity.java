package com.share.ylh.mediaplayer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.share.ylh.mediaplayer.R;
import com.share.ylh.mediaplayer.utils.ViewUtil;


/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-15
 * Time: 11:10
 * FIXME
 */
public class WelcomeActivity extends Activity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        //initSP();



        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                    startActivity(new Intent(WelcomeActivity.this, PlayActivityOO.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    private void initSP() {
        ViewUtil.setShardPString("id", "1");
        ViewUtil.setShardPString("STATE", "0");
        ViewUtil.setShardPString("playstate", "4");
    }

}
