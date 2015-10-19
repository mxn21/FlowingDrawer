package com.mxn.soul.flowingdrawer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mxn.soul.flowingdrawer_core.FluidView;
import com.mxn.soul.flowingdrawer_core.LeftDrawerLayout;


public class MainActivity extends AppCompatActivity {

    private MenuFragment mMenuFragment;
    private LeftDrawerLayout mLeftDrawerLayout ;
    private TextView mContentTv ;
    private FrameLayout id_container_menu ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        mContentTv = (TextView) findViewById(R.id.id_content_tv);

        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.id_container_menu);
        id_container_menu = (FrameLayout) findViewById(R.id.id_container_menu);
        FluidView mFluidView = (FluidView) findViewById(R.id.sv);
        mFluidView.setAnimationListener(new AnimationImp());
        mLeftDrawerLayout.setFluidView(mFluidView) ;

        if (mMenuFragment == null)
        {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new MenuFragment()).commit();
        }

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
