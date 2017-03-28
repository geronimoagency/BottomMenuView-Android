# BottomMenuView-Android

This Bottom Menu view can be used with a view pager or with custom tabs.

[ ![Download](https://api.bintray.com/packages/geronimostudios/geronimostudios/com.geronimostudios.bottommenuview/images/download.svg?version=0.0.1) ](https://bintray.com/geronimostudios/geronimostudios/com.geronimostudios.bottommenuview/0.0.1/link)

Sample app
=======
The sample app is available in this repository under sample/.

Gradle
=======
Include the dependency [Download (.aar)](https://bintray.com/geronimostudios/geronimostudios/download_file?file_path=com%2Fgeronimostudios%2Fui%2Fbottommenuview%2F0.0.1%2Fbottommenuview-0.0.1.aar) :

```groovy
dependencies {
    compile 'com.geronimostudios.bottommenuview:bottommenuview:0.0.1'
}
```

Example
=======
<img src="preview/video_sample.gif"  height="700">

How to use
=======

With a ViewPager
----------------

```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      mViewPager = (ViewPager) findViewById(R.id.view_pager);
      SamplePagerAdapter adapter = new SamplePagerAdapter();
      mViewPager.setAdapter(adapter);

      mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
      mMenuView.setupWith(mViewPager);
    }
    ...
}
```

You adapter also have to implements **BottomMenuView.Adapter** for the icons :

```java
public class SamplePagerAdapter extends PagerAdapter implements BottomMenuView.Adapter {
      ...
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
      ...
}
```

With custom tabs
----------------

```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

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
              android.R.drawable.ic_menu_gallery
      ));

      mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
      mMenuView.setupWith(tabs);
    }
    ...
}
```

Listener
--------

```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
      ...
      mMenuView.setListener(new BottomMenuView.Listener() {
            @Override
            public void onMenuPageChanged(int page) {
                Log.d("debug", "page changed : " + page);
            }
      });
    }
    ...
}
```

Customize
--------

**Layout attributs and xml:**

| Name                 | Options           | Description |
| -----                | --------          | ----------- |
| `tabBackground`      | `@drawable/image` | Background applied to the tabs |
| `tabIconSize`         | `@dimen/tabh`  | Tab icons size |
| `tabLineColor`  | `@color/blue`     | Color of the tab indicator line |
| `tabLineHeight` | `@dimen/line_height`     | Height of the tab indicator line |
| `tabLineWidth`            | `auto`, `full_width`, `@dimen/tab_width` | Indicator line width mode |

```xml
    <com.geronimostudios.bottommenuview.BottomMenuView
        app:tabIconSize="25dp"
        app:tabLineWidth="full_width"
        app:tabLineColor="@color/colorPrimary"
        app:tabLineHeight="6dp"
        app:tabBackground="@drawable/bg_ripple_grey_over_white"
        android:id="@+id/bottom_menu_view"
        android:padding="@dimen/default_padding"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
```

**Programmatically:**

```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      mMenuView = (BottomMenuView) findViewById(R.id.bottom_menu_view);
      ...
      mMenuView.setIconSize((int) getResources().getDimension(R.dimen.sample_icon_height_40)); // Change icons size
      mMenuView.setUnderlineHeight((int) getResources().getDimension(R.dimen.sample_underline_height_2)); // change line height
      mMenuView.setUnderlineColor(ContextCompat.getColor(this, R.color.colorAccent)); // change line color
      mMenuView.setTabBackground(ContextCompat.getDrawable(this, R.drawable.bg_sample)); // change tab background drawable
      
      // Change the underline width with BottomMenuView.LINE_AUTO or BottomMenuView.LINE_FULL_WIDTH
      mMenuView.setUnderlineMode(BottomMenuView.LINE_FULL_WIDTH); 
      // or use a custom underline width
      mMenuView.setUnderlineWidth(getResources().getDimension(R.dimen.sample_custim_tab_width));
    }
    ...
}
```

License
======
```
Copyright (C) 2014 Geronimo Studios

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```