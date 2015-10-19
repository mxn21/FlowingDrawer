package com.mxn.soul.fluiddrawer.drawer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mxn.soul.fluiddrawer.R;


/**
 * Created by zhy on 15/4/26.
 */
public class LeftMenuAdapter extends ArrayAdapter<MenuItem> {


    private LayoutInflater mInflater;

    private int mSelected;


    public LeftMenuAdapter(Context context, MenuItem[] objects) {
        super(context, -1, objects);

        mInflater = LayoutInflater.from(context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_left_menu, parent, false);
        }

        ImageView iv = (ImageView) convertView.findViewById(R.id.id_item_icon);
        TextView title = (TextView) convertView.findViewById(R.id.id_item_title);
        title.setText(getItem(position).text);
        iv.setImageResource(getItem(position).icon);
        convertView.setBackgroundColor(Color.TRANSPARENT);

        if (position == mSelected) {
            iv.setImageResource(getItem(position).iconSelected);
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.state_menu_item_selected));
        }

        return convertView;
    }

    public void setSelected(int position) {
        this.mSelected = position;
        notifyDataSetChanged();
    }


}

