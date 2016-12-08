# FlowingDrawer 

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-FlowingDrawer-green.svg?style=flat)](https://android-arsenal.com/details/1/2658)
[![travis-ic](https://travis-ci.org/mxn21/FlowingDrawer.svg?branch=master)](https://travis-ci.org/mxn21/FlowingDrawer)

![Showcase](http://baobaoloveyou.com/flowingdrawer.gif)

swipe right to display drawer with flowing effects.


## Download

Include the following dependency in your build.gradle file.

Gradle:

```Gradle
    repositories {
        jcenter()
    }

    dependencies {
        compile 'com.mxn.soul:flowingdrawer-core:1.2.5'
        compile 'com.nineoldandroids:library:2.4.0'
    }
```


## V1.2.2

add onOpenMenu and onCloseMenu event Listener to catch open and close event . Add these two 
method in MyMenuFragment extends MenuFragment.

## Usage

*For a working implementation of this project see the `app/` folder.*

MainActivity:

```java
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
```

activity_main.xml:

```xml
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
```

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

```java
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_menu, container,
                    false);
            .......
            return  setupReveal(view) ;
        }
```

* in MenuFragment'xml ,add 'android:background="@android:color/transparent" '

* in MainActivity,use

```java
mMenuFragment = new MyMenuFragment();
mLeftDrawerLayout.setFluidView(mFlowingView);
mLeftDrawerLayout.setMenuFragment(mMenuFragment);
```

in order .

* you can call mLeftDrawerLayout.closeDrawer()  and  mLeftDrawerLayout.openDrawer() to close or
open drawer automatically.

* change background color for drawer by "paint_color" in colors.xml.This is not a good way,and 
will be improved in the next version.

## TODO

* use spring dynamics models to make more bouncing effects.
* close by touch effect need to enhance.
* improve the lines to be smoother when open and close drawer.
* some part of the code need to simplify.


## Contribution

First of all, thank you ! As you see ,the project is not as good as the original design
sketch and thank you for watch and star. At present we still have a lot of things to do .
I would love to get some help on the TODO list .So if you find a bug in the library or want a feature
and think you can fix it yourself,fork + pull request and i will greatly appreciate it!



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

