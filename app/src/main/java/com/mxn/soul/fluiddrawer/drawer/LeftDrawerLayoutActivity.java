package com.mxn.soul.fluiddrawer.drawer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mxn.soul.fluiddrawer.R;


public class LeftDrawerLayoutActivity extends ActionBarActivity
{

    private MenuFragment mMenuFragment;
    private LeftDrawerLayout mLeftDrawerLayout ;
    private TextView mContentTv ;
    private FrameLayout id_container_menu ;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left_drawer_layout);

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

//        mMenuFragment.setOnMenuItemSelectedListener(new LeftMenuFragment.OnMenuItemSelectedListener()
//        {
//            @Override
//            public void menuItemSelected(String title)
//            {
//                mLeftDrawerLayout.closeDrawer();
//                mContentTv.setText(title);
//            }
//        });
//        mContentTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLeftDrawerLayout.openDrawer();
//            }
//        });
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

    protected enum Status {

        SHOW, SHOWING,
        DISMISS, DISMISSING
    }
}
