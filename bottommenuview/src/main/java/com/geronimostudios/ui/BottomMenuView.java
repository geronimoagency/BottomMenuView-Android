package com.geronimostudios.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerome on 03/03/17.
 */
public class BottomMenuView extends View implements ViewPager.OnPageChangeListener {

    public static final int LINE_AUTO = -1;
    public static final int LINE_FULL_WIDTH = -2;
    public static final int LINE_CUSTOM = -3;
    private float mLastTouchX;

    @IntDef({LINE_AUTO, LINE_FULL_WIDTH, LINE_CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineMode {
    }

    private List<Tab> mTabs;
    private Drawable mUnderlineDrawable;
    private @Nullable ViewPager mViewPager;
    private float mItemSize;
    private float mUnderlineHeight;
    private float mUnderlineWidth;
    private @LineMode int mUnderlineMode;
    private int mCurrentPage;
    private int mLastPage;
    private Listener mListener;

    /**
     * current page of positionOffset
     */
    private int mScrollCurrentPage;

    /**
     *  percentage scrolled from {@link BottomMenuView#mScrollCurrentPage} to the next page
     */
    private @FloatRange(from = 0f, to = 1f) float mScrollPageOffset;
    private @Nullable Drawable mDefaultTabBackground;

    public BottomMenuView(Context context) {
        super(context);
        init(context, null);
    }

    public BottomMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BottomMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BottomMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mUnderlineDrawable = new ShapeDrawable(new RectShape());
        @ColorInt int lineColor = -1;
        mUnderlineHeight = -1;
        mItemSize = -1;
        mUnderlineMode = LINE_AUTO;
        mDefaultTabBackground = null;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BottomMenuView);
            lineColor = a.getColor(R.styleable.BottomMenuView_tabLineColor, -1);
            mUnderlineHeight = a.getDimension(R.styleable.BottomMenuView_tabLineHeight, -1);
            mItemSize = a.getDimension(R.styleable.BottomMenuView_tabIconSize, -1);
            mDefaultTabBackground = a.getDrawable(R.styleable.BottomMenuView_tabBackground);

            TypedValue tv = new TypedValue();
            a.getValue(R.styleable.BottomMenuView_tabLineWidth, tv);
            if (tv.type != TypedValue.TYPE_NULL) {
                switch (tv.type) {
                    case TypedValue.TYPE_DIMENSION:
                        mUnderlineMode = LINE_CUSTOM;
                        mUnderlineWidth = a.getDimension(R.styleable.BottomMenuView_tabLineWidth, 0f);
                        break;
                    case TypedValue.TYPE_FLOAT:
                        mUnderlineMode =
                                a.getInteger(R.styleable.BottomMenuView_tabLineWidth, LINE_AUTO) == LINE_AUTO
                                        ? LINE_AUTO
                                        : LINE_FULL_WIDTH;
                        break;
                    default:
                        mUnderlineMode = LINE_AUTO;
                }
            }
            a.recycle();
        }

        Resources res = context.getResources();
        if (mItemSize == -1) {
            mItemSize = res.getDimension(R.dimen.menu_item_size);
        }

        if (mUnderlineHeight == -1) {
            mUnderlineHeight = res.getDimension(R.dimen.menu_underline_height);
        }

        if (lineColor == -1) {
            final TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
            lineColor = value.data;
        }

        mUnderlineDrawable.setColorFilter(lineColor, PorterDuff.Mode.SRC);
        setClickable(true);

        if (isInEditMode()) {
            fillWithFakeData();
        }
    }

    @Override
    protected void drawableStateChanged() {
        int[] stateSet = getDrawableState();
        if (mTabs != null) {
            int widthPerItem = getWidth() / mTabs.size();
            int index = (int) (mLastTouchX / widthPerItem);
            for (int tabIndex = 0; tabIndex < mTabs.size(); ++tabIndex) {
                Tab tab = mTabs.get(tabIndex);
                Drawable drawable = tab.getBackgroundDrawable();
                if (drawable != null && drawable.isStateful()) {
                    if (index == tabIndex) { // set current tab selected
                        if (mCurrentPage == mTabs.indexOf(tab)) {
                            int[] newState = new int[stateSet.length + 1];
                            System.arraycopy(stateSet, 0, newState, 0, stateSet.length);
                            newState[stateSet.length] = android.R.attr.state_selected;
                            stateSet = newState;
                        }
                        drawable.setState(stateSet);
                        invalidate();
                    } else { // remove selected state
                        for (int i = 0; i < stateSet.length; ++i) {
                            int state = stateSet[i];
                            if (state == android.R.attr.state_selected) {
                                stateSet[i] = 0;
                            }
                        }
                        drawable.setState(stateSet);
                        invalidate();
                    }
                }
            }
        }
        super.drawableStateChanged();
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mTabs != null) {
            int widthPerItem = getWidth() / mTabs.size();
            int index = (int) (x / widthPerItem);
            Tab tab = mTabs.get(index);
            Drawable drawable = tab.getBackgroundDrawable();
            if (drawable != null) {
                drawable.setHotspot(x, y);
            }
        }
        super.drawableHotspotChanged(x, y);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        if (mTabs != null) {
            for (Tab tab : mTabs) {
                Drawable drawable = tab.getBackgroundDrawable();
                if (drawable != null) {
                    drawable.jumpToCurrentState();
                }
            }
        }
        super.jumpDrawablesToCurrentState();
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        boolean verified = false;
        if (mTabs != null) {
            for (Tab tab : mTabs) {
                Drawable drawable = tab.getBackgroundDrawable();
                if (drawable != null && who == drawable) {
                    verified = true;
                }
            }
        }
        return super.verifyDrawable(who) || verified;
    }

    private void fillWithFakeData() {
        mTabs = new ArrayList<>();
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_preferences, getCopyOfDefaultTabBackground()));
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_camera, getCopyOfDefaultTabBackground()));
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_gallery, getCopyOfDefaultTabBackground()));
    }

    public void setupWith(ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);
        mScrollCurrentPage = mViewPager.getCurrentItem();
        mScrollPageOffset = 0f;
        mLastPage = mViewPager.getCurrentItem();
        mCurrentPage = mViewPager.getCurrentItem();

        PagerAdapter vpAdapter = mViewPager.getAdapter();
        if (vpAdapter == null || !(mViewPager.getAdapter() instanceof Adapter)) {
            throw new IllegalArgumentException("Adapter not implemented");
        }
        Adapter adapter = (Adapter) vpAdapter;
        mTabs = new ArrayList<>();
        for (int i = 0; i < vpAdapter.getCount(); ++i) {
            mTabs.add(new Tab(getContext(), adapter.getMenuIcon(i), getCopyOfDefaultTabBackground()));
        }
        invalidate();
    }

    public void setTabs(@Nullable List<Tab> tabs) {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
            mViewPager = null;
        }
        mScrollCurrentPage = 0;
        mScrollPageOffset = 0f;
        mLastPage = 0;
        mCurrentPage = 0;

        mTabs = tabs;
        invalidate();
    }

    private Drawable getCopyOfDefaultTabBackground() {
        if (mDefaultTabBackground != null) {
            Drawable.ConstantState state = mDefaultTabBackground.getConstantState();
            if (state != null) {
                Drawable drawable = state.newDrawable();
                drawable.setCallback(this);
                return drawable;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTabs == null) {
            return;
        }

        int itemCount = mTabs.size();
        int widthPerItem = getWidth() / itemCount;

        // Draw icons and backgrounds
        for (int i = 0; i < itemCount; ++i) {
            Tab tab = mTabs.get(i);

            float offsetX = (widthPerItem * i) + (widthPerItem / 2) - mItemSize / 2;
            float offsetY = getIconOffsetY(i);

            Drawable background = tab.getBackgroundDrawable();
            if (background != null) {
                background.setBounds(i * widthPerItem, 0, (i + 1) * widthPerItem, getMeasuredHeight());
                background.draw(canvas);
            }

            Drawable iconDrawable = tab.getIconDrawable();
            iconDrawable.setBounds(
                    (int) offsetX,
                    (int) offsetY,
                    (int) (offsetX + mItemSize),
                    (int) (offsetY + mItemSize)
            );
            iconDrawable.draw(canvas);
        }

        // Draw underline
        int offsetX = (int) ((widthPerItem * mScrollCurrentPage)
                + (widthPerItem / 2f)
                - (mUnderlineWidth / 2f)
                + (widthPerItem * mScrollPageOffset));
        int offsetY = (int) (getHeight() - mUnderlineHeight);
        mUnderlineDrawable.setBounds(offsetX, offsetY, (int) (offsetX + mUnderlineWidth), getHeight());
        mUnderlineDrawable.draw(canvas);
    }

    /**
     * Get the offset y in pixel of the icon at a specific page.
     * The default behavior is to move vertically up to 'paddingTop / 2' pixels from the center position.
     *
     * @param iconPage the page of the icon
     * @return an y offset at which the icon should be draw
     */
    private int getIconOffsetY(int iconPage) {
        if ((iconPage != mScrollCurrentPage && iconPage != mScrollCurrentPage + 1)
                || (iconPage != mCurrentPage && iconPage != mLastPage)) {
            return getPaddingTop();
        }
        @FloatRange(from = 0f, to = 1f) float percent =
                (iconPage == mScrollCurrentPage)
                        ? 1f - mScrollPageOffset
                        : mScrollPageOffset;
        return (int) (getPaddingTop() - (getPaddingTop() / 4f) * percent);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                (int) (getPaddingTop() + mItemSize + getPaddingBottom())
        );

        // Measure the underline width
        switch (mUnderlineMode) {
            case LINE_AUTO:
                mUnderlineWidth = mItemSize * 2;
                break;
            case LINE_FULL_WIDTH:
                if (mTabs == null) {
                    break;
                }
                int itemCount = mTabs.size();
                mUnderlineWidth = getMeasuredWidth() / itemCount;
                break;
            case LINE_CUSTOM:
                // nothing to do here
                break;
            default:
                throw new IllegalArgumentException("Invalid mode");
        }
    }

    @Override
    public void onPageScrolled(int page, // current page of positionOffset
                               @FloatRange(from = 0f, to = 1f) float positionOffset, // percentage scrolled
                               int positionOffsetPixels) { // offset scrolled in pixels
        mScrollCurrentPage = page;
        mScrollPageOffset = positionOffset;
        invalidate();
    }

    @SuppressWarnings("unused")
    private void setInternalPageScrolled(float page) {
        mScrollCurrentPage = (int) Math.floor(page);
        mScrollPageOffset = page - mScrollCurrentPage;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        mLastPage = mCurrentPage;
        mCurrentPage = position;

        if (mListener != null) {
            mListener.onMenuPageChanged(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // ignore
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mLastTouchX = event.getX();
        boolean handled = super.dispatchTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // true if we have at least one item
                return (mViewPager != null && mTabs != null && !mTabs.isEmpty())
                        || handled;
            case MotionEvent.ACTION_UP:
                int widthPerItem = getWidth() / mTabs.size();
                int page = (int) (event.getX() / widthPerItem);

                if (mViewPager != null) {
                    mViewPager.setCurrentItem(page);
                } else {
                    doChangePageAnimation(page);
                }
                return true;
            default:
                return handled;
        }
    }

    private void doChangePageAnimation(final int page) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "internalPageScrolled", mCurrentPage * 1f, page * 1f);
        animator.setAutoCancel(true);
        animator.setDuration(150);
        animator.setInterpolator(new DecelerateInterpolator());

        onPageSelected(page);
        animator.start();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static class Tab {
        private @Nullable Drawable mDrawable;
        private @NonNull Drawable mIconDrawable;

        public Tab(Context context, @DrawableRes int icon) {
            mIconDrawable = ContextCompat.getDrawable(context, icon);
        }

        public Tab(Context context, @DrawableRes int icon, @Nullable Drawable drawable) {
            mDrawable = drawable;
            mIconDrawable = ContextCompat.getDrawable(context, icon);
        }

        @Nullable
        Drawable getBackgroundDrawable() {
            return mDrawable;
        }

        @NonNull
        Drawable getIconDrawable() {
            return mIconDrawable;
        }
    }

    public interface Adapter {
        @DrawableRes int getMenuIcon(int position);
    }

    public interface Listener {
        void onMenuPageChanged(int page);
    }
}
