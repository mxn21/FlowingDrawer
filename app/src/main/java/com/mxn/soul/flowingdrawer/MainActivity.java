package com.mxn.soul.flowingdrawer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mxn.soul.flowingdrawer_core.FluidView;
import com.mxn.soul.flowingdrawer_core.LeftDrawerLayout;


public class MainActivity extends AppCompatActivity {

    private MenuFragment mMenuFragment;
    private RecyclerView rvFeed;
    private LeftDrawerLayout mLeftDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        rvFeed = (RecyclerView) findViewById(R.id.rvFeed);

        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.id_container_menu);
        FluidView mFluidView = (FluidView) findViewById(R.id.sv);
        mFluidView.setAnimationListener(new AnimationImp());
        mLeftDrawerLayout.setFluidView(mFluidView);

        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new MenuFragment()).commit();
        }
        setupFeed();

    }


    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLeftDrawerLayout.toggle();
            }
        });
    }


    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvFeed.setLayoutManager(linearLayoutManager);

        FeedAdapter feedAdapter = new FeedAdapter(this);
        rvFeed.setAdapter(feedAdapter);
        feedAdapter.updateItems();
    }


    class AnimationImp implements FluidView.AnimationListener {

        @Override
        public void onStart() {
        }

        @Override
        public void onEnd(int y) {
        }

        @Override
        public void onContentShow(int y) {
            mMenuFragment.show(y);
        }

        @Override
        public void onReSet() {
            mMenuFragment.hideView();
        }
    }

    @Override
    public void onBackPressed() {
        if (mLeftDrawerLayout.isShownMenu()) {
            mLeftDrawerLayout.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
