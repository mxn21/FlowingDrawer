package com.mxn.soul.flowingdrawer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mxn.soul.flowingdrawer_core.RevealLayout;
import com.squareup.picasso.Picasso;



public class MenuFragment extends Fragment {


    private ImageView ivMenuUserProfilePhoto;


    private RevealLayout mRevealLayout;

    private boolean isShown;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container,
                false);
        ivMenuUserProfilePhoto = (ImageView) view.findViewById(R.id.ivMenuUserProfilePhoto);
        mRevealLayout = (RevealLayout) view.findViewById(R.id.reveal_layout);
        setupHeader();
        hideView() ;
        return view;
    }

    private void setupHeader() {
        int avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        String profilePhoto = getResources().getString(R.string.user_profile_photo);
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
