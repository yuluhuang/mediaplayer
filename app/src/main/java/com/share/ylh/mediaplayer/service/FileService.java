package com.share.ylh.mediaplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.share.ylh.mediaplayer.utils.ViewUtil;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-13
 * Time: 18:03
 * FIXME
 */
public class FileService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ViewUtil.getMusicListOnSys(ViewUtil.getExternalStoragePath(), "mp3");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
