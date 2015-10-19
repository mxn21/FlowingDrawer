package com.mxn.soul.flowingdrawer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mxn.soul.flowingdrawer_core.FluidView;
import com.mxn.soul.flowingdrawer_core.LeftDrawerLayout;


public class MainActivity extends AppCompatActivity {

    private MenuFragment mMenuFragment;
    private LeftDrawerLayout mLeftDrawerLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);

        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.id_container_menu);
        FluidView mFluidView = (FluidView) findViewById(R.id.sv);
        mFluidView.setAnimationListener(new AnimationImp());
        mLeftDrawerLayout.setFluidView(mFluidView) ;

        if (mMenuFragment == null)
        {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new MenuFragment()).commit();
        }

    }


    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }



    class AnimationImp implements FluidView.AnimationListener {

        @Override
        public void onStart() {
//            mStatus = Status.SHOWING;
//            mContentRL.setVisibility(View.GONE);

        }

        @Override
        public void onEnd(int y ) {
//            mStatus = Status.SHOW;
//            id_container_menu.setVisibility(View.VISIBLE);
//            mMenuFragment.show(y) ;

        }

        @Override
        public void onContentShow(int y ) {

//            mContentRL.setVisibility(View.VISIBLE);

//            setContentViewAnimation();
//            mContentRL.scheduleLayoutAnimation();
//            mLeftDrawerLayout.openDrawer();
            mMenuFragment.show(y) ;
        }

        @Override
        public void onReSet() {
            mMenuFragment.hideView() ;
        }
    }

}
