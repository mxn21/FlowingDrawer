# FlowingDrawer [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlowingDrawer-green.svg?style=flat)](https://android-arsenal.com/details/1/2658)


![Showcase](screen.gif)

swipe right to display drawer with flowing effects.


# Download

Include the following dependency in your build.gradle file.

Gradle:

    repositories {
        jcenter()
    }

    dependencies {
        compile 'com.mxn.soul:flowingdrawer-core:1.1.0'
    }



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

* use LeftDrawerLayout as the root of xml.

* The root has two child, first for content,second for menu.

* menu'root alse has two child , first for FlowingView to display flowing effects,second for
fragment .

* fragment need to has a 'marginRight', for example 25dp.
marginRight here is important .
'marginRight'  have effect on drawer's elasticity. The more  'marginRight' is  the more elastic.
Try to set '10dp', '25dp' ,'50dp' to see the difference.

* make the fragment of menu extends MenuFragment.

* pay attention to MenuFragment's onCreateView: return setupReveal(root) ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_menu, container,
                  false);
          .......
          return  setupReveal(view) ;
      }

* in MenuFragment'xml ,add 'android:background="@android:color/transparent" '

* in MainActivity,call
mMenuFragment = new MyMenuFragment();
mLeftDrawerLayout.setFluidView(mFlowingView);
mLeftDrawerLayout.setMenuFragment(mMenuFragment);

in order .

* you can call mLeftDrawerLayout.closeDrawer()  and  mLeftDrawerLayout.openDrawer() to close or
open drawer automatically.

### V1.0




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

