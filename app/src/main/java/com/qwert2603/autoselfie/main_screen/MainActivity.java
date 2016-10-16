package com.qwert2603.autoselfie.main_screen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qwert2603.autoselfie.R;
import com.qwert2603.autoselfie.utils.LogUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("MainActivity#onCreate");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new MainFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d("MainActivity#onNewIntent");
    }
}
