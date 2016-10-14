package com.qwert2603.autoselfie.main_screen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qwert2603.autoselfie.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new MainFragment())
                    .commitAllowingStateLoss();
        }
    }
}
