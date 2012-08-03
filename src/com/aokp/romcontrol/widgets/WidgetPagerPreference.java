package com.aokp.romcontrol.widgets;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
//import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout.LayoutParams;

import com.aokp.romcontrol.R;

public class WidgetPagerPreference extends Preference {
    
    private static final String TAG = "Widget";
    public static final String ACTION_ALLOCATE_ID = "com.android.systemui.ACTION_ALLOCATE_ID";
    public static final String ACTION_DEALLOCATE_ID = "com.android.systemui.ACTION_DEALLOCATE_ID";
    public static final String ACTION_SEND_ID = "com.android.systemui.ACTION_SEND_ID";
    private static final String NAVIGATION_BAR_WIDGETS = "navigation_bar_widgets";
    public int mWidgetIdQty = 0;
    int widgetIds[];
    private int mCurrentPage = 0;
    private int mPendingWidgetId = -1;
    private ViewPager mViewPager;
    WidgetPagerAdapter mAdapter;
    Context mContext;
    
    BroadcastReceiver mWidgetIdReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "widget id receiver go!");

            // Need to De-Allocate the ID that this was replacing.
            if (widgetIds[mPendingWidgetId] != -1) {
                Intent delete = new Intent();
                delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetIds[mPendingWidgetId]);
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
            widgetIds[mPendingWidgetId] = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            saveWidgets();
        };
    };
    
    public WidgetPagerPreference(Context context) {
        super(context);
        mContext = context;
    }
    public WidgetPagerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.widget_preview_preference);
        mContext = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        // Set our custom views inside the layout
         mViewPager = (ViewPager) view.findViewById(R.id.pager);
         Log.d("Widget","onBindView:" + mViewPager);
         inflateWidgetPref();
    }
    
    public void inflateWidgetPref() {
        Log.d("Widget","InflateWidgetPref");
        // calculate number of Widgets
        String settingWidgets = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDGETS);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            mWidgetIdQty = split.length;
        } else {
            mWidgetIdQty = 0;
        }
        widgetIds = new int[mWidgetIdQty+1];
        Log.i("Widget", "widgets: " + settingWidgets);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() > 0)
                    widgetIds[i] = Integer.parseInt(split[i]);
            }
        }
        // set Widget ID to -1 for 'add button'
        widgetIds[mWidgetIdQty] = -1;
        if (mViewPager != null) {
            mViewPager.setAdapter(mAdapter = new WidgetPagerAdapter(mContext, widgetIds));
            mViewPager.setOnPageChangeListener(mNewPageListener);
            int dp = mAdapter.getHeight(mViewPager.getCurrentItem());
            float px = dp * mContext.getResources().getDisplayMetrics().density;
            mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
            mViewPager.setCurrentItem(0);
        }
        Log.d("Widget"," mAdapter:" + mAdapter);
     }
     
     View.OnClickListener doWidgetPrefClick = new View.OnClickListener() {
         public void onClick(View v) {
             Log.d("Widget","DoWidgetPrefClick");
             int widgetcount = mAdapter.getCount();
             int selectedwidget = mViewPager.getCurrentItem();
             if (selectedwidget < widgetcount) { // replace the existing widget
                 mPendingWidgetId = selectedwidget -1; // adjust for array bounds.
             } else { // get/add a new widget
                 mPendingWidgetId = -1; 
             }
             Log.i(TAG, "pending widget: " + mPendingWidgetId);
             // selectWidget();
             // send intent to pick a new widget
             Intent send = new Intent();
             send.setAction(ACTION_ALLOCATE_ID);
             mContext.sendBroadcast(send);  
         };
     };
     
     private void saveWidgets() {
         StringBuilder widgetString = new StringBuilder();
         for (int i = 0; i < (mWidgetIdQty); i++) {
             widgetString.append(widgetIds[i]);
             if (i != (mWidgetIdQty - 1))
                 widgetString.append("|");
         }
         Settings.System.putString(mContext.getContentResolver(), Settings.System.NAVIGATION_BAR_WIDGETS,
                 widgetString.toString());
     }
     
     public void resetNavBarWidgets() {
         for (int i = 0; i < (mWidgetIdQty); i++) {
             if (widgetIds[i] != -1) {
                 Intent delete = new Intent();
                 delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetIds[i]);
                 delete.setAction(ACTION_DEALLOCATE_ID);
                 mContext.sendBroadcast(delete);
             }
         }
         Settings.System.putString(mContext.getContentResolver(), 
                 Settings.System.NAVIGATION_BAR_WIDGETS,"");
         inflateWidgetPref();
     }
     
     public OnPageChangeListener mNewPageListener = new OnPageChangeListener() {

         @Override
         public void onPageSelected(int page) {
             mCurrentPage = page;
             Log.d("Widget","Page Selected:" + page);
             int dp = mAdapter.getHeight(page);
             float px = dp * mContext.getResources().getDisplayMetrics().density;
             mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) px));
             int widgetcount = mAdapter.getCount() - 1;
             int widgetselected = page -1;
             setTitle(mAdapter.getLabel(page));
             setSummary(String.format(mContext.getResources().getString(R.string.navbar_widget_summary),
                       widgetselected,widgetcount));
             View wv = (View) mAdapter.getView(mCurrentPage);
             wv.setOnClickListener(doWidgetPrefClick);
             Log.d(TAG,"ImageView:" + wv);
         }

         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

         }

         @Override
         public void onPageScrollStateChanged(int arg0) {
         }
     };
}
