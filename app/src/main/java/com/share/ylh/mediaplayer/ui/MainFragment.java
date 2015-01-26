package com.share.ylh.mediaplayer.ui;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.share.ylh.mediaplayer.R;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2015-01-26
 * Time: 15:48
 * FIXME
 */
public class MainFragment extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.playFragment);

        if (fragment == null) {
            fragment = new PlayFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.playFragment, fragment)
                    .commit();


        }
    }


}
