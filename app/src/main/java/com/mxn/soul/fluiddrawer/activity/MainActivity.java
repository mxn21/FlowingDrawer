package com.mxn.soul.fluiddrawer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.mxn.soul.fluiddrawer.R;
import com.mxn.soul.fluiddrawer.drawer.LeftDrawerLayoutActivity;

import butterknife.InjectView;


public class MainActivity extends BaseDrawerActivity {
    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";

    private static final int ANIM_DURATION_TOOLBAR = 300;
    private static final int ANIM_DURATION_FAB = 400;


    @InjectView(R.id.content)
    CoordinatorLayout clContent;

    @InjectView(R.id.test1)
    Button test1;

    @InjectView(R.id.test2)
    Button test2;

    @InjectView(R.id.test3)
    Button test3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(MainActivity.this, TestActivity.class);
                startActivity(t);
            }
        });

        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(MainActivity.this,LeftDrawerLayoutActivity.class);
                startActivity(t);
            }
        });
        test3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(MainActivity.this,Test2Activity.class);
                startActivity(t);
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }


}