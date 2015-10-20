# FlowingDrawer

![Showcase](screen.gif)

swipe right to display drawer with flowing effects.

# Usage

*For a working implementation of this project see the `app/` folder.*

MainActivity:

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
           ....
        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        FragmentManager fm = getSupportFragmentManager();
        MyMenuFragment mMenuFragment = (MyMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        FlowingView mFlowingView = (FlowingView) findViewById(R.id.sv);
        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new MyMenuFragment()).commit();
        }
        mLeftDrawerLayout.setFluidView(mFlowingView);
        mLeftDrawerLayout.setMenuFragment(mMenuFragment);
        ...
    }

activity_main.xml:

    <com.mxn.soul.flowingdrawer_core.LeftDrawerLayout
        android:id="@+id/id_drawerlayout"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipChildren="false"
        >

        <!--content-->
        <android.support.design.widget.CoordinatorLayout
            android:id="@+id/content"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical">
        </android.support.design.widget.CoordinatorLayout>

        <!--menu-->
        <RelativeLayout
            android:layout_width="280dp"
            android:layout_height="match_parent"
            android:layout_gravity="start"
            android:clipChildren="false"
            >
            <com.mxn.soul.flowingdrawer_core.FlowingView
                android:id="@+id/sv"
                android:layout_width="match_parent"
                android:layout_height="match_parent"/>
            <FrameLayout
                android:id="@+id/id_container_menu"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_alignParentLeft="true"
                android:layout_marginRight="25dp"
                android:paddingRight="10dp"
                />
        </RelativeLayout>

    </com.mxn.soul.flowingdrawer_core.LeftDrawerLayout>

1.use LeftDrawerLayout as the root of xml.

2.The root has two child, first for content,second for menu.

3.menu'root alse has two child , first for FlowingView to display flowing effects,second for
fragment .

4.fragment need to has a 'marginRight', for example 25dp.

5.make the fragment of menu extends MenuFragment.

6.you can call mLeftDrawerLayout.closeDrawer()  and  mLeftDrawerLayout.openDrawer() to close or
open drawer automatically.

### V1.0

Submit to jcenter Just now . use build.gradle  compile  soon.


# TODO



License
=======

    Copyright 2015 soul.mxn

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

