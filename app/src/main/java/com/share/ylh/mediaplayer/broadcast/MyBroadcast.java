package com.share.ylh.mediaplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-15
 * Time: 13:47
 * FIXME
 */
public class MyBroadcast extends BroadcastReceiver{
    private static  final  String ACTION_BROADCASR="com.share.ylh.mediaplayer.broatcast";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.share.ylh.mediaplayer.broatcast")){
            String msg = intent.getStringExtra("time");
            Log.e("AAAAAAAA", msg);
            //setProgress(400);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }
}
