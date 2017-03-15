package com.geronimostudios.bottommenusample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.geronimostudios.ui.BottomMenuView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BottomMenuView mMenuView;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mCustomTabsPagerChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mMenuView.setCurrentPage(position); // with animation
            //mMenuView.setCurrentPage(position, false); // without animation
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        SamplePagerAdapter adapter = new SamplePagerAdapter();
        mViewPager.setAdapter(adapter);

        mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);

        onSetupMenuWithViewPagerRequested();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_with_view_pager:
                onSetupMenuWithViewPagerRequested();
                return true;
            case R.id.menu_item_with_custom_tabs:
                onSetupMenuWithCustomTabsRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSetupMenuWithViewPagerRequested() {
        mMenuView.setupWith(mViewPager);

        mViewPager.removeOnPageChangeListener(mCustomTabsPagerChangeListener);
        mMenuView.setListener(new BottomMenuView.Listener() {
            @Override
            public void onMenuPageChanged(int page) {
                Log.d("debug", "page changed with viewpager : " + page);
            }
        });
    }

    private void onSetupMenuWithCustomTabsRequested() {
        mMenuView.setupWith(createDummyTabs());

        mViewPager.addOnPageChangeListener(mCustomTabsPagerChangeListener);
        mMenuView.setListener(new BottomMenuView.Listener() {
            @Override
            public void onMenuPageChanged(int page) {
                Log.d("debug", "page changed with custom tabs : " + page);
                mViewPager.setCurrentItem(page);
            }
        });
    }

    private ArrayList<BottomMenuView.Tab> createDummyTabs() {
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
        return tabs;
    }
}
