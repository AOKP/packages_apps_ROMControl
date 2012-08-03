
package com.aokp.romcontrol.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aokp.romcontrol.R;

public class WidgetPager extends ViewPager {

    private static final String TAG = "Widget";
    public static final String ACTION_ALLOCATE_ID = "com.android.systemui.ACTION_ALLOCATE_ID";
    public static final String ACTION_DEALLOCATE_ID = "com.android.systemui.ACTION_DEALLOCATE_ID";
    public static final String ACTION_SEND_ID = "com.android.systemui.ACTION_SEND_ID";
    public static final String ACTION_GET_WIDGET_DATA = "com.android.systemui.ACTION_GET_WIDGET_DATA";
    public static final String ACTION_SEND_WIDGET_DATA = "com.android.systemui.ACTION_SEND_WIDGET_DATA";
    public int mWidgetIdQty = 0;
    int mWidgetIds[];
    private int mCurrentPage = 0;
    private int mPendingWidgetId = -1;
    private int[] mWidgetHeight;
    private int[] mWidgetWidth;
    private String[] mTitles;
    WidgetPagerAdapter mAdapter;
    Context mContext;
    ImageView mWidgetView;
    int[] mWidgetResId;
    String[] mProvider;
    AppWidgetManager mAppWidgetManager;

    BroadcastReceiver mWidgetIdReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "widget id receiver go!");

            // Need to De-Allocate the ID that this was replacing.
            if (mWidgetIds[mPendingWidgetId] != -1) {
                Intent delete = new Intent();
                delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetIds[mPendingWidgetId]);
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
            mWidgetIds[mPendingWidgetId] = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (mPendingWidgetId == mWidgetIdQty) { // we put a widget in the
                                                    // last spot
                mWidgetIdQty++;
            }
            saveWidgets();
            inflateWidgetPref();
            setCurrentItem(mPendingWidgetId);
            mAdapter.notifyDataSetChanged();
        };
    };

    /*
     * BroadcastReceiver mWidgetDataReceiver = new BroadcastReceiver() { public
     * void onReceive(Context context, Intent intent) { int target = -1; int id
     * = intent.getIntExtra("widgetid",-1); for (int i = 0; i <
     * mWidgetIds.length; i++) { if (mWidgetIds[i] == id) { target = i; break; }
     * } if (target > -1){ mTitles[target]=(intent.getStringExtra("label"));
     * mWidgetResId[target]= intent.getIntExtra("imageid",0);
     * mWidgetHeight[target] = intent.getIntExtra("height",0);
     * mWidgetWidth[target] = intent.getIntExtra("width",0); mProvider[target] =
     * intent.getStringExtra("provider"); Log.d(TAG,"Rec'd:" +
     * mWidgetIds[target] +" Provider:" + mProvider[target] + " Label:"+
     * mTitles[target]); } ImageView iv = (ImageView)
     * mViewPager.findViewWithTag("preview_"+target); if (iv != null) {
     * PackageManager pm = mContext.getPackageManager(); ComponentName cn =
     * ComponentName.unflattenFromString(mProvider[target]);
     * iv.setImageDrawable(pm.getDrawable(cn.getPackageName(),
     * mWidgetResId[target], null)); } mViewPager.invalidate(); updateSummary();
     * }; };
     */

    public WidgetPager(Context context) {
        super(context);
        mContext = context;
    }

    public WidgetPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // setLayoutResource(R.layout.widget_preview_preference);
        // addView(View.inflate(mContext, R.layout.widget_preview_preference,
        // null));
        mContext = context;

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter(ACTION_SEND_ID);
        mContext.registerReceiver(mWidgetIdReceiver, filter);
        filter = new IntentFilter(ACTION_SEND_WIDGET_DATA);
        // mContext.registerReceiver(mWidgetDataReceiver, filter);
        // mAppWidgetManager = AppWidgetManager.getInstance(mContext);

        // Set our custom views inside the layout

        // inflateWidgetPref();
        int dp = mAdapter.getHeight(getCurrentItem());
        float px = dp * mContext.getResources().getDisplayMetrics().density;
        setLayoutParams(new android.view.ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
        setOnPageChangeListener(mNewPageListener);
        setFocusableInTouchMode(true);
    }

    public void inflateWidgetPref() {
        mAppWidgetManager = AppWidgetManager.getInstance(getContext());
        // calculate number of Widgets
        String settingWidgets = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            mWidgetIdQty = split.length;
        } else {
            mWidgetIdQty = 0;
        }
        mWidgetIds = new int[mWidgetIdQty + 1];
        mWidgetResId = new int[mWidgetIdQty + 1];
        mWidgetHeight = new int[mWidgetIdQty + 1];
        mWidgetWidth = new int[mWidgetIdQty + 1];
        mProvider = new String[mWidgetIdQty + 1];
        mTitles = new String[mWidgetIdQty + 1];
        mWidgetIds[mWidgetIdQty] = -1;
        Log.i(TAG, "inflatewidgets: " + settingWidgets);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            setAdapter(mAdapter = new WidgetPagerAdapter());
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() > 0) {
                    mWidgetIds[i] = Integer.parseInt(split[i]);
                    // requestWidgetInfo(mWidgetIds[i]);
                    requestWidgetInfo(i);
                }
            }
        }
        // set Widget ID to -1 for 'add button'
        // mWidgetIds[mWidgetIdQty] = -1;
        // if (mViewPager != null) {
        // if (mAdapter == null) {
        // mViewPager.setAdapter(mAdapter = new WidgetPagerAdapter());
        // mViewPager.setOnPageChangeListener(mNewPageListener);
        // }
        // }
    }

    View.OnClickListener mDoPrefClick = new View.OnClickListener() {
        public void onClick(View v) {
            doWidgetPrefClick();
        };
    };

    private void doWidgetPrefClick() {
        mPendingWidgetId = mCurrentPage;
        // selectWidget();
        // send intent to pick a new widget
        Intent send = new Intent();
        send.setAction(ACTION_ALLOCATE_ID);
        mContext.sendBroadcast(send);
    }

    private void saveWidgets() {
        StringBuilder widgetString = new StringBuilder();
        for (int i = 0; i < (mWidgetIdQty); i++) {
            widgetString.append(mWidgetIds[i]);
            if (i != (mWidgetIdQty - 1))
                widgetString.append("|");
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS,
                widgetString.toString());
        Log.d(TAG, "Saved:" + widgetString.toString());
        inflateWidgetPref();
    }

    public void resetNavBarWidgets() {
        for (int i = 0; i < (mWidgetIdQty); i++) {
            if (mWidgetIds[i] != -1) {
                Intent delete = new Intent();
                delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetIds[i]);
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS, "");
        inflateWidgetPref();
        mAdapter.notifyDataSetChanged();
    }

    private void requestWidgetInfo(int id) {
        Log.d(TAG, "Requesting Widget:" + id);
        /*
         * Intent intent = new Intent(ACTION_GET_WIDGET_DATA);
         * intent.putExtra("widgetid", id); mContext.sendBroadcast(intent);
         */
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(mWidgetIds[id]);
        if (appWidgetInfo != null) {
            mTitles[id] = appWidgetInfo.label;
            mWidgetResId[id] = appWidgetInfo.previewImage;
            mWidgetHeight[id] = appWidgetInfo.minHeight;
            mWidgetWidth[id] = appWidgetInfo.minWidth;
            mProvider[id] = appWidgetInfo.provider.flattenToString();
            PackageManager pm = mContext.getPackageManager();
            ImageView iv = (ImageView) findViewWithTag("preview_" + id);
            if (iv != null) {
                iv.setImageDrawable(pm.getDrawable(appWidgetInfo.provider.getPackageName(),
                        appWidgetInfo.previewImage, null));
                invalidate();
            }
        }

    }

    private void updateSummary() {
        // if (mCurrentPage < mWidgetIdQty) {
        // mSummary.setText(String.format(
        // mContext.getResources().getString(R.string.navbar_widget_summary),
        // (mCurrentPage + 1), mWidgetIdQty));
        // mTitle.setText(mTitles[mCurrentPage]);
        // } else {
        // mSummary.setText(mContext.getResources().getString(R.string.navbar_widget_summary_add));
        // mTitle.setText("");
        // }
    }

    public SimpleOnPageChangeListener mNewPageListener = new SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int page) {
            mCurrentPage = page;
            Log.d(TAG, "Page Selected:" + page);
            int dp = mAdapter.getHeight(page);
            float px = dp * mContext.getResources().getDisplayMetrics().density;
            // mViewPager.setLayoutParams(new
            // LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
            updateSummary();
            requestWidgetInfo(page);
        }

    };

    public class WidgetPagerAdapter extends PagerAdapter {

        View[] widgetViews = new View[1];

        public WidgetPagerAdapter() {
            setWidgetIds();
        }

        public void setWidgetIds() {
            widgetViews = new View[mWidgetIds.length];
        }

        @Override
        public int getCount() {
            return widgetViews.length;
        }

        public int getHeight(int pos) {
            if (mWidgetHeight[pos] != 0) {
                return mWidgetHeight[pos];
            } else {
                return getSavedHeight(pos);
            }
        }

        private int getSavedHeight(int pos) {
            SharedPreferences prefs = mContext.getSharedPreferences("widget_adapter",
                    Context.MODE_WORLD_WRITEABLE);
            return prefs.getInt("widget_pos_" + pos, 100);
        }

        /**
         * Create the page for the given position. The adapter is responsible
         * for adding the view to the container given here, although it only
         * must ensure this is done by the time it returns from
         * {@link #finishUpdate()}.
         * 
         * @param container The containing View in which the page will be shown.
         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page. This does not
         *         need to be a View, but can be some other container of the
         *         page.
         */
        @Override
        public Object instantiateItem(View collection, int position) {
            int widgetId = mWidgetIds[position];
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.widget_image_preview, null);
            mWidgetView = (ImageView) vg.findViewById(R.id.widget_preview);
            mWidgetView.setTag("preview_" + position);
            if (widgetId == -1) {
                mWidgetView.setImageResource(R.drawable.widget_add);
                /*
                 * } else if (mWidgetResId[position] != 0) { PackageManager pm =
                 * mContext.getPackageManager(); ComponentName cn =
                 * ComponentName.unflattenFromString(mProvider[position]);
                 * mWidgetView
                 * .setImageDrawable(pm.getDrawable(cn.getPackageName(),
                 * mWidgetResId[position], null)); }
                 */
            } else {
                mWidgetView.setImageResource(R.drawable.widget_na);
            }
            mWidgetView.setOnClickListener(mDoPrefClick);
            if (widgetViews != null && position < widgetViews.length) {
                widgetViews[position] = vg;
                ((ViewPager) collection).addView(widgetViews[position], 0);
                requestWidgetInfo(position);
                return widgetViews[position];
            } else {
                Log.d(TAG, "widgetViews null!!");
                return null;
            }
        }

        /**
         * Remove a page for the given position. The adapter is responsible for
         * removing the view from its container, although it only must ensure
         * this is done by the time it returns from {@link #finishUpdate()}.
         * 
         * @param container The containing View from which the page will be
         *            removed.
         * @param position The page position to be removed.
         * @param object The same object that was returned by
         *            {@link #instantiateItem(View, int)}.
         */
        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        /**
         * Called when the a change in the shown pages has been completed. At
         * this point you must ensure that all of the pages have actually been
         * added or removed from the container as appropriate.
         * 
         * @param container The containing View which is displaying this
         *            adapter's page views.
         */
        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }
}
