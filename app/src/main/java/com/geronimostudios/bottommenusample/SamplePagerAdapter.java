package com.geronimostudios.bottommenusample;

import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geronimostudios.ui.BottomMenuView;

/**
 * Created by jerome on 14/03/17.
 */

public class SamplePagerAdapter extends PagerAdapter implements BottomMenuView.Adapter {

    private static final int PAGE_INFORMATION = 0;
    private static final int PAGE_CAMERA = 1;
    private static final int PAGE_GALLERY = 2;

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public @DrawableRes int getMenuIcon(int position) {
        switch (position) {
            case PAGE_INFORMATION:
                return R.drawable.ic_info;
            case PAGE_CAMERA:
                return R.drawable.ic_camera;
            case PAGE_GALLERY:
                return R.drawable.ic_gallery;
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        TextView view = new TextView(collection.getContext());
        view.setGravity(Gravity.CENTER);
        view.setText("Position : " + position);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        collection.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }
}
