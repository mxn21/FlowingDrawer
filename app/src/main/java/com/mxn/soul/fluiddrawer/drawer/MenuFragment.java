package com.mxn.soul.fluiddrawer.drawer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mxn.soul.fluiddrawer.R;
import com.mxn.soul.fluiddrawer.util.CircleTransformation;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by zhy on 15/4/26.
 */
public class MenuFragment extends Fragment {


    @InjectView(R.id.ivMenuUserProfilePhoto)
    ImageView ivMenuUserProfilePhoto;


    @InjectView(R.id.reveal_layout)
    RevealLayout mRevealLayout;

    private int avatarSize;
    private String profilePhoto;

    private boolean isShown;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_view, container,
                false);
        ButterKnife.inject(this, view);
        setupHeader();
        hideView() ;
        return view;
    }

    private void setupHeader() {
        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        this.profilePhoto = getResources().getString(R.string.user_profile_photo);
        Picasso.with(getActivity())
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivMenuUserProfilePhoto);
    }


    public void show(int y) {
        if (!isShown) {
            isShown = true;
            mRevealLayout.show(100, y, 1000);
        }
    }

    public void reset() {
        isShown = false ;
    }

    public void hideView(){
        mRevealLayout.hide();
        isShown = false;
    }

}
