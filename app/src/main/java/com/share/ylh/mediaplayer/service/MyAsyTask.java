package com.share.ylh.mediaplayer.service;

import android.os.AsyncTask;

import java.util.List;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-24
 * Time: 12:39
 * FIXME
 */
public class MyAsyTask<T> extends AsyncTask<String, Void, List<T>> {

    public MyAsyTask() {
        super();
    }

    @Override
    protected List doInBackground(String... params) {
        return null;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<T> ts) {
        super.onPostExecute(ts);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
