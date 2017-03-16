package com.geronimostudios.bottommenusample;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.geronimostudios.ui.BottomMenuView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomMenuView mMenuView;
    private ViewPager mViewPager;
    private View mNoViewPagerContainer;
    private TextView mCustomTabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        SamplePagerAdapter adapter = new SamplePagerAdapter();
        mViewPager.setAdapter(adapter);

        mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
        mNoViewPagerContainer = findViewById(R.id.no_view_pager_container);
        mCustomTabPosition = (TextView) findViewById(R.id.no_view_pager_position);

        findViewById(R.id.goto_tab_1).setOnClickListener(this);
        findViewById(R.id.goto_tab_2).setOnClickListener(this);
        findViewById(R.id.goto_tab_3).setOnClickListener(this);

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
        mViewPager.setVisibility(View.VISIBLE);
        mNoViewPagerContainer.setVisibility(View.GONE);

        mMenuView.setListener(new BottomMenuView.Listener() {
            @Override
            public void onMenuPageChanged(int page) {
                Log.d("debug", "page changed with viewpager : " + page);
            }
        });

        mMenuView.setupWith(mViewPager);
        mMenuView.setIconSize((int) getResources().getDimension(R.dimen.sample_icon_height_25));
        mMenuView.setUnderlineHeight((int) getResources().getDimension(R.dimen.sample_underline_height_4));
        mMenuView.setUnderlineColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mMenuView.setTabBackground(ContextCompat.getDrawable(this, R.drawable.bg_ripple_grey_over_white));
        mMenuView.setUnderlineMode(BottomMenuView.LINE_AUTO);
    }

    private void onSetupMenuWithCustomTabsRequested() {
        mViewPager.setVisibility(View.GONE);
        mNoViewPagerContainer.setVisibility(View.VISIBLE);

        mMenuView.setListener(new BottomMenuView.Listener() {
            @Override
            public void onMenuPageChanged(int page) {
                Log.d("debug", "page changed with custom tabs : " + page);
                mCustomTabPosition.setText(String.format(
                        Locale.getDefault(),"Selected position: %d", page
                ));
            }
        });

        mMenuView.setupWith(createDummyTabs());
        mMenuView.setIconSize((int) getResources().getDimension(R.dimen.sample_icon_height_40));
        mMenuView.setUnderlineHeight((int) getResources().getDimension(R.dimen.sample_underline_height_2));
        mMenuView.setUnderlineColor(ContextCompat.getColor(this, R.color.colorAccent));
        mMenuView.setTabBackground(ContextCompat.getDrawable(this, R.drawable.bg_sample));
        mMenuView.setUnderlineMode(BottomMenuView.LINE_FULL_WIDTH);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goto_tab_1:
                mMenuView.setCurrentPage(0); // with animation
                // mMenuView.setCurrentPage(0, false); // without animation
                break;
            case R.id.goto_tab_2:
                mMenuView.setCurrentPage(1);
                break;
            case R.id.goto_tab_3:
                mMenuView.setCurrentPage(2);
                break;
            default:
                throw new IllegalArgumentException("Invalid view id");
        }
    }
}
