
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.aokp.romcontrol.NavBarWidget;
import com.aokp.romcontrol.R;

import java.util.ArrayList;

public class WidgetConfigurationFragment extends DialogFragment {

    private static final String TAG = "Widget";
    public static final String ACTION_ALLOCATE_ID = "com.android.systemui.ACTION_ALLOCATE_ID";
    public static final String ACTION_DEALLOCATE_ID = "com.android.systemui.ACTION_DEALLOCATE_ID";
    public static final String ACTION_SEND_ID = "com.android.systemui.ACTION_SEND_ID";
    public static final String ACTION_GET_WIDGET_DATA = "com.android.systemui.ACTION_GET_WIDGET_DATA";
    public static final String ACTION_SEND_WIDGET_DATA = "com.android.systemui.ACTION_SEND_WIDGET_DATA";
    private ViewPager mViewPager;
    WidgetPagerAdapter mAdapter;
    Context mContext;
    ImageView mWidgetView;
    int[] mWidgetResId;
    String[] mProvider;
    // TextView mTitle;
    // TextView mSummary;
    AppWidgetManager mAppWidgetManager;
    View mView;
    ArrayList<NavBarWidget> mWidgets = new ArrayList<NavBarWidget>();
    protected int mPendingWidgetIndex;

    BroadcastReceiver mWidgetIdReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "widget id receiver go!");

            // Need to De-Allocate the ID that this was replacing.
            if (mWidgets.get(mPendingWidgetIndex).getWidgetId() != -1) {
                Intent delete = new Intent();
                delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        mWidgets.get(mPendingWidgetIndex).getWidgetId());
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
                mWidgets.remove(mPendingWidgetIndex);
                mPendingWidgetIndex = -1;
            }
            // 
            mWidgets.get(index)intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (mPendingWidgetIndex == mWidgetIdQty) { // we put a widget in the
                // last spot
                mWidgetIdQty++;
            }
            saveWidgets();
            refreshParams();
            mViewPager.setCurrentItem(mPendingWidgetIndex);
            mAdapter.notifyDataSetChanged();
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.navbar_widgets, container, false);
        mViewPager = (ViewPager) mView.findViewById(R.id.pager);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshParams();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity().getApplicationContext();
        // IntentFilter filter = new IntentFilter(ACTION_SEND_ID);
        // mContext.registerReceiver(mWidgetIdReceiver, filter);
        // filter = new IntentFilter(ACTION_SEND_WIDGET_DATA);
        // mContext.registerReceiver(mWidgetDataReceiver, filter);
        // mAppWidgetManager = AppWidgetManager.getInstance(mContext);
    }
    
    private void removeWidget(int whichIndex) {
        NavBarWidget removedWidget =  mWidgets.remove(whichIndex);
        saveWidgets();
        refreshParams();
        // remove in system ui
        Intent delete = new Intent();
        delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                removedWidget.getWidgetId());
        delete.setAction(ACTION_DEALLOCATE_ID);
        mContext.sendBroadcast(delete);
        
    }

    private ArrayList<NavBarWidget> inflateWidgets() {
        String settingWidgets = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            ArrayList<NavBarWidget> widgets = new ArrayList<NavBarWidget>(split.length);
            mAppWidgetManager = AppWidgetManager.getInstance(mContext);

            for (int i = 0; i < split.length; i++) {
                int appWidgetId = Integer.parseInt(split[i]);
                AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
                        .getAppWidgetInfo(appWidgetId);
                if (appWidgetInfo == null) {
                    // we don't have access to this widget id that we think we
                    // have! it might not exist anymore, ignore it. later we'll
                    // save the widget ids to Settings.System
                } else {
                    widgets.add(new NavBarWidget(mContext, appWidgetInfo, appWidgetId));
                }
            }
            widgets.add(new NavBarWidget(mContext)); // add +1 button!
            return widgets;
        } else {
            return null;
        }
    }

    private void refreshParams() {
        if (mViewPager != null) {
            if (mAdapter == null) {
                mViewPager
                        .setAdapter(mAdapter = new WidgetPagerAdapter(mWidgets = inflateWidgets()));
                mViewPager.setOnPageChangeListener(mNewPageListener);
            }
            int dp = mAdapter.getHeight(mViewPager.getCurrentItem());
            float px = dp * mContext.getResources().getDisplayMetrics().density;
            mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
        }
    }

    View.OnClickListener mDoPrefClick = new View.OnClickListener() {
        public void onClick(View v) {
            requestNewWidget();
        };
    };

    private void requestNewWidget() {
        // mPendingWidgetIndex = mCurrentPage;
        // selectWidget();
        // send intent to pick a new widget
        Intent send = new Intent();
        send.setAction(ACTION_ALLOCATE_ID);
        mContext.sendBroadcast(send);
    }

    private void saveWidgets() {
        StringBuilder widgetString = new StringBuilder();
        for (int i = 0; i < (mWidgets.size()); i++) {
            widgetString.append(mWidgets.get(i).getWidgetId());
            if (i != (mWidgets.size() - 1))
                widgetString.append("|");
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS,
                widgetString.toString());
        Log.d(TAG, "Saved:" + widgetString.toString());
    }

    public void resetNavBarWidgets() {
        for (int i = 0; i < (mWidgets.size()); i++) {
            if (mWidgets.get(i).getWidgetId() != -1) {
                Intent delete = new Intent();
                delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgets.get(i).getWidgetId());
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS, "");
        refreshParams();
        mAdapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        // if (mCurrentPage < mWidgetIdQty) {
        // mSummary.setText(String.format(mContext.getResources().getString(R.string.navbar_widget_summary),
        // (mCurrentPage + 1),mWidgetIdQty));
        // mTitle.setText(mTitles[mCurrentPage]);
        // } else {
        // mSummary.setText(mContext.getResources().getString(R.string.navbar_widget_summary_add));
        // mTitle.setText("");
        // }
    }

    public SimpleOnPageChangeListener mNewPageListener = new SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int page) {
            // mCurrentPage = page;
            Log.d(TAG, "Page Selected:" + page);
            int dp = mAdapter.getHeight(page);
            float px = dp * mContext.getResources().getDisplayMetrics().density;
            mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
            updateSummary();
            // requestWidgetInfo(page);
        }

    };

    public class WidgetPagerAdapter extends PagerAdapter {

        ArrayList<NavBarWidget> mWidgets;
        ImageView addPageImaveView;

        public WidgetPagerAdapter(ArrayList<NavBarWidget> w) {
            mWidgets = w;
        }

        @Override
        public int getCount() {
            return mWidgets.size();
        }

        public int getHeight(int pos) {
            if (pos < mWidgets.size()) {
                if (mWidgets != null && mWidgets.get(pos) != null) {
                    int validHeight = mWidgets.get(pos).getHeight();
                    setSavedHeight(pos, validHeight);
                    return validHeight;
                } else {
                    return getSavedHeight(pos);
                }
            } else {
                return -1;
            }
        }

        private void setSavedHeight(int pos, int height) {
            SharedPreferences prefs = mContext.getSharedPreferences("widget_adapter",
                    Context.MODE_WORLD_WRITEABLE);
            prefs.edit().putInt("widget_pos_" + pos, height).commit();
        }

        private int getSavedHeight(int pos) {
            SharedPreferences prefs = mContext.getSharedPreferences("widget_adapter",
                    Context.MODE_WORLD_WRITEABLE);
            return prefs.getInt("widget_pos_" + pos, 100);
        }

        /**
         * Create the page for the given position. The adapter is responsible for
         * adding the view to the container given here, although it only must ensure
         * this is done by the time it returns from {@link #finishUpdate()}.
         * 
         * @param container The containing View in which the page will be shown.
         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page. This does not need
         *         to be a View, but can be some other container of the page.
         */
        @Override
        public Object instantiateItem(View collection, int position) {
            // if (position == getCount() || mWidgets.isEmpty()) {
            // if (addPageImaveView == null) {
            // addPageImaveView = new ImageView(getActivity());
            // addPageImaveView.setImageResource(R.drawable.widget_add);
            // ((ViewGroup) collection).addView(addPageImaveView);
            // }
            // return addPageImaveView;
            // } else {

            if (position > (mWidgets.size() - 1))
                return null;

            View v = mWidgets.get(position).getView();
            if (mWidgets.get(position).isAddWidget()) {
                v.setOnClickListener(mDoPrefClick);
            } else {
                // click on active widget
            }
            ((ViewGroup) collection).addView(v);
            return mWidgets.get(position).getView();
            // }

            // mWidgetView.setTag("preview_" + position);
            // if (widgetId == -1) {
            // mWidgetView.setImageResource(R.drawable.widget_add);
            // } else {
            // PackageManager pm = mContext.getPackageManager();
            // Drawable d = null;
            // try {
            // ComponentName cn =
            // ComponentName.unflattenFromString(mProvider[position]);
            // d = pm.getDrawable(cn.getPackageName(),
            // mWidgetResId[position], null);
            // } catch (NullPointerException e) {
            // }
            // if (d == null) {
            // mWidgetView.setImageResource(R.drawable.widget_na);
            // } else {
            // mWidgetView.setImageDrawable(d);
            // }
            // }
            // mWidgetView.setOnClickListener(mDoPrefClick);
            // return mWidgetView;
            // View returnView = null;
            // if (widgetViews != null && position < widgetViews.length) {
            // widgetViews[position] = vg;
            // ((ViewPager) collection).addView(widgetViews[position], 0);
            // returnView = widgetViews[position];
            // } else {
            // Log.d(TAG, "widgetViews null!!");
            // }
            // return returnView;
        }

        /**
         * Remove a page for the given position. The adapter is responsible for
         * removing the view from its container, although it only must ensure this
         * is done by the time it returns from {@link #finishUpdate()}.
         * 
         * @param container The containing View from which the page will be removed.
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
            if (object instanceof NavBarWidget) {
                ((NavBarWidget) object).getView().equals(view);
            }
            return view == ((View) object);
        }

        /**
         * Called when the a change in the shown pages has been completed. At this
         * point you must ensure that all of the pages have actually been added or
         * removed from the container as appropriate.
         * 
         * @param container The containing View which is displaying this adapter's
         *            page views.
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
