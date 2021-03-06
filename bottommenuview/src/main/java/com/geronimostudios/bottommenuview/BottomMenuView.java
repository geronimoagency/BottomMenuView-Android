package com.geronimostudios.bottommenuview;

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
import android.support.annotation.Dimension;
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

public class BottomMenuView extends View {

    /**
     * The width of the underline will be equals to the icon size * 2.
     */
    public static final int LINE_AUTO = -1;

    /**
     * The underline will have the same width than his parent {@link Tab}.
     */
    public static final int LINE_FULL_WIDTH = -2;

    /**
     * This mode is used when a custom width is specified.
     * See {@link #setUnderlineWidth(int)}.
     */
    private static final int LINE_CUSTOM = -3;

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
    private float mLastTouchX;
    private Listener mListener;

    /**
     * Current page of positionOffset.
     */
    private int mScrollCurrentPage;

    /**
     * Percentage scrolled from {@link BottomMenuView#mScrollCurrentPage} to the next page.
     */
    private @FloatRange(from = 0f, to = 1f) float mScrollPageOffset;
    private @Nullable Drawable mDefaultTabBackground;

    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        /**
         * Callback of the viewpager used when the menu has been setup with
         * {@link BottomMenuView#setupWith(ViewPager)}.
         *
         * @param page current page of positionOffset
         * @param positionOffset percentage scrolled
         * @param positionOffsetPixels offset scrolled in pixels
         */
        @Override
        public void onPageScrolled(int page,
                                   @FloatRange(from = 0f, to = 1f) float positionOffset,
                                   int positionOffsetPixels) {
            mScrollCurrentPage = page;
            mScrollPageOffset = positionOffset;
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            internalChangePage(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // ignore
        }
    };

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
                        mUnderlineWidth
                                = a.getDimension(R.styleable.BottomMenuView_tabLineWidth, 0f);
                        break;
                    case TypedValue.TYPE_FLOAT:
                        @LineMode int mode
                                = a.getInteger(R.styleable.BottomMenuView_tabLineWidth, LINE_AUTO);
                        if (mode == LINE_AUTO) {
                            mUnderlineMode = LINE_AUTO;
                        } else {
                            mUnderlineMode = LINE_FULL_WIDTH;
                        }
                        break;
                    default:
                        mUnderlineMode = LINE_AUTO;
                }
            }
            a.recycle();
        }

        Resources res = context.getResources();
        if (mItemSize == -1) {
            mItemSize = res.getDimension(R.dimen.bottommenuview_default_menu_item_size);
        }

        if (mUnderlineHeight == -1) {
            mUnderlineHeight
                    = res.getDimension(R.dimen.bottommenuview_default_menu_underline_height);
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

    /**
     * Change the mode of the underline width.
     * The mode has to be {@link #LINE_AUTO} or {@link #LINE_FULL_WIDTH}.
     * If you want a custom width, you have to use {@link #setUnderlineWidth(int)}.
     *
     * @param mode {@link #LINE_AUTO} or {@link #LINE_FULL_WIDTH}
     */
    public void setUnderlineMode(@LineMode int mode) {
        if (mode != LINE_AUTO && mode != LINE_FULL_WIDTH) {
            throw new IllegalArgumentException("Invalid mode");
        }
        mUnderlineMode = mode;
        requestLayout();
    }

    /**
     * Change the size of the underline.
     *
     * @param width a dimension in pixel.
     */
    public void setUnderlineWidth(@Dimension int width) {
        mUnderlineMode = LINE_CUSTOM;
        mUnderlineWidth = width;
        invalidate();
    }

    /**
     * Change the color of the underline.
     *
     * @param color a rgb color.
     */
    public void setUnderlineColor(@ColorInt int color) {
        mUnderlineDrawable.setColorFilter(color, PorterDuff.Mode.SRC);
        invalidate();
    }

    /**
     * Change the height of the underline.
     *
     * @param dimension the new height of the underline in pixel.
     */
    public void setUnderlineHeight(@Dimension int dimension) {
        mUnderlineHeight = dimension;
        invalidate();
    }

    /**
     * Change the size of the icon of each {@link Tab}.
     *
     * @param dimension the new size in pixel.
     */
    public void setIconSize(@Dimension int dimension) {
        mItemSize = dimension;
        invalidate();
    }

    /**
     * Change the background drawable of each {@link Tab}.
     * The {@link android.graphics.drawable.RippleDrawable} are supported by this view.
     *
     * @param tabBackground a drawable or null.
     */
    public void setTabBackground(@Nullable Drawable tabBackground) {
        unregisterDrawableCallback();
        mDefaultTabBackground = tabBackground;
        if (mTabs != null) {
            for (Tab tab : mTabs) {
                tab.mDrawable = getCopyOfDefaultTabBackground();
            }
            invalidate();
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
                    int[] newState;
                    if (index == tabIndex) { // set current tab selected
                        newState = new int[stateSet.length + 1];
                        System.arraycopy(stateSet, 0, newState, 0, stateSet.length);
                        newState[stateSet.length] = android.R.attr.state_selected;
                    } else { // remove selected and pressed state
                        newState = new int[stateSet.length];
                        System.arraycopy(stateSet, 0, newState, 0, stateSet.length);
                        for (int i = 0; i < newState.length; ++i) {
                            int state = newState[i];
                            if (state == android.R.attr.state_selected
                                    || state == android.R.attr.state_pressed) {
                                newState[i] = 0;
                            }
                        }
                    }
                    drawable.setState(newState);
                    invalidate();
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

    /**
     * Used only when {@link #isInEditMode()} returns true.
     */
    private void fillWithFakeData() {
        mTabs = new ArrayList<>();
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_preferences,
                getCopyOfDefaultTabBackground()));
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_camera,
                getCopyOfDefaultTabBackground()));
        mTabs.add(new Tab(getContext(), android.R.drawable.ic_menu_gallery,
                getCopyOfDefaultTabBackground()));
    }

    /**
     * Setup this {@link BottomMenuView} with a {@link ViewPager}.
     * The adapter used by the view pager has to implements {@link Adapter}.
     *
     * @param viewPager to be used.
     */
    public void setupWith(ViewPager viewPager) {
        unregisterDrawableCallback();
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(mViewPagerPageChangeListener);
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
            mTabs.add(new Tab(
                    getContext(),
                    adapter.getMenuIcon(i),
                    getCopyOfDefaultTabBackground()
            ));
        }

        if (mListener != null) {
            mListener.onMenuPageChanged(mCurrentPage);
        }
        invalidate();
    }

    /**
     * Setup this {@link BottomMenuView} by providing a list of {@link Tab}.
     * You can implements {@link Listener} and use {@link #setListener(Listener)} to handle events.
     *
     * @param tabs The list of tabs
     */
    public void setupWith(@Nullable List<Tab> tabs) {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mViewPagerPageChangeListener);
            mViewPager = null;
        }
        unregisterDrawableCallback();
        mScrollCurrentPage = 0;
        mScrollPageOffset = 0f;
        mLastPage = 0;
        mCurrentPage = 0;

        mTabs = tabs;

        if (mTabs != null) {
            for (Tab tab : mTabs) {
                Drawable drawable = tab.getBackgroundDrawable();
                if (drawable != null) {
                    drawable.setCallback(this);
                }
            }
        }

        if (mListener != null) {
            mListener.onMenuPageChanged(mCurrentPage);
        }
        invalidate();
    }

    private void unregisterDrawableCallback() {
        if (mTabs != null) {
            for (Tab tab : mTabs) {
                Drawable drawable = tab.getBackgroundDrawable();
                if (drawable != null) {
                    drawable.setCallback(null);
                }
            }
        }
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
                background.setBounds(
                        i * widthPerItem, // left
                        0, // top
                        (i + 1) * widthPerItem, // right
                        getMeasuredHeight() // bottom
                );
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
        mUnderlineDrawable.setBounds(
                offsetX,
                offsetY,
                (int) (offsetX + mUnderlineWidth),
                getHeight()
        );
        mUnderlineDrawable.draw(canvas);
    }

    /**
     * Get the offset y in pixel of the icon at a specific page.
     * The default behavior is to move vertically up to 'paddingTop / 2' pixels from the center
     * position.
     *
     * @param iconPage the page of the icon
     * @return an y offset at which the icon should be draw
     */
    private int getIconOffsetY(int iconPage) {
        if ((iconPage != mScrollCurrentPage && iconPage != mScrollCurrentPage + 1)
                || (iconPage != mCurrentPage && iconPage != mLastPage)) {
            return getPaddingTop();
        }
        @FloatRange(from = 0f, to = 1f) float percent
                = (iconPage == mScrollCurrentPage)
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

    /**
     * Callback of {@link #setCurrentPage(int)}.
     */
    @SuppressWarnings("unused")
    private void setInternalPageScrolled(float page) {
        mScrollCurrentPage = (int) Math.floor(page);
        mScrollPageOffset = page - mScrollCurrentPage;
        invalidate();
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
                    setCurrentPage(page);
                }
                return true;
            default:
                return handled;
        }
    }

    /**
     * Change the current selected page with an animation.
     *
     * @param position the new page position
     */
    public void setCurrentPage(final int position) {
        setCurrentPage(position, true);
    }

    /**
     * Change the current selected page with or without animation.
     *
     * @param position the new page position
     */
    public void setCurrentPage(final int position, boolean animate) {
        if (animate) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    this,
                    "internalPageScrolled",
                    mCurrentPage,
                    position
            );
            animator.setDuration(150);
            animator.setInterpolator(new DecelerateInterpolator());

            internalChangePage(position);
            animator.start();
        } else {
            mScrollCurrentPage = position;
            mScrollPageOffset = 0;
            internalChangePage(position);
        }
    }

    private void internalChangePage(int position) {
        mLastPage = mCurrentPage;
        mCurrentPage = position;

        if (mListener != null) {
            mListener.onMenuPageChanged(position);
        }
        invalidate();
    }

    /**
     * Set a listener to his {@link BottomMenuView} to handle events.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Model a of Tab used by {@link BottomMenuView}.
     */
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
