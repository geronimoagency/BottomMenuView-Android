package com.geronimostudios.bottommenusample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.geronimostudios.ui.BottomMenuView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BottomMenuView.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        SamplePagerAdapter adapter = new SamplePagerAdapter();
        viewPager.setAdapter(adapter);

        BottomMenuView menuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
        menuView.setListener(this);
        menuView.setupWith(viewPager);

        ArrayList<BottomMenuView.Tab> tabs = new ArrayList<>();
        tabs.add(new BottomMenuView.Tab(
                this,
                android.R.drawable.ic_menu_preferences,
                ContextCompat.getDrawable(this, R.drawable.bg_ripple_grey_over_white)
        ));
        tabs.add(new BottomMenuView.Tab(
                this,
                android.R.drawable.ic_menu_camera,
                ContextCompat.getDrawable(this, R.drawable.bg_ripple_grey_over_white)
        ));
        tabs.add(new BottomMenuView.Tab(
                this,
                android.R.drawable.ic_menu_gallery,
                ContextCompat.getDrawable(this, R.drawable.bg_ripple_grey_over_white)
        ));
        menuView.setTabs(tabs);
    }

    @Override
    public void onMenuPageChanged(int page) {
        Log.d("debug", "page changed : " + page);
    }
}
