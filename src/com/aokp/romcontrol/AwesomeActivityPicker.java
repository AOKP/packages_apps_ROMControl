
package com.aokp.romcontrol;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.aokp.AwesomeConstants;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

import java.util.Collections;
import java.util.List;

public class AwesomeActivityPicker extends FragmentActivity {

    public static final String EXTRA_URI = "uri";
    ViewPager mPager;
    FragmentPagerAdapter mPagerAdapter;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.awesome_action_picker_activity);

        final String[] tabs = getResources().getStringArray(R.array.awesome_picker_tabs);

        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public Fragment getItem(int pos) {
                switch (pos) {
                    case 0:
                        return new AwesomeActionListFragment();
                    case 1:
                        return new InstalledAppsFragment();
                    case 2:
                        return new ShortcutListFragment();
                }
                return null;
            }
        };

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        // add tabs.
        for (String tab : tabs) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tab)
                            .setTabListener(tabListener));
        }

        // inform action bar what tab we're showing
        mPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
        setTitle(R.string.awesome_picker_title);

    }
    
    

    public static class AwesomeActionListFragment extends ListFragment {

        ArrayAdapter<String> listAdapter;
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String[] actionList = com.android.internal.util.aokp.AwesomeConstants.AwesomeActions();

            actionList = ArrayUtils.removeElement(String.class, actionList,
                    actionList[actionList.length - 1]);

            setListAdapter(listAdapter = new ArrayAdapter<String>(getActivity(), R.id.title,
                    actionList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate((Context) getActivity(),
                                R.layout.awesome_action_layout_row, null);
                    }

                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(AwesomeConstants.getProperName(getActivity(), getItem(position)));
                    title.setCompoundDrawablesWithIntrinsicBounds(
                            AwesomeConstants.getActionIcon(getActivity(), getItem(position)), null,
                            null, null);

                    return convertView;
                }
            });
        }
        
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            
            Intent data = new Intent();
            data.putExtra(EXTRA_URI, listAdapter.getItem(position));            
            getActivity().setResult(Activity.RESULT_OK, data);      
            getActivity().finish();
        }

    }

    public static class InstalledAppsFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> pkgAppsList = getActivity().getPackageManager()
                    .queryIntentActivities(mainIntent, 0);

            Collections.sort(pkgAppsList, new ResolveInfo.DisplayNameComparator(getActivity()
                    .getPackageManager()));

            setListAdapter(new ArrayAdapter<ResolveInfo>(getActivity(), R.id.title,
                    pkgAppsList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate((Context) getActivity(),
                                R.layout.awesome_action_layout_row, null);
                    }

                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position).loadLabel(getActivity().getPackageManager()));
                    title.setCompoundDrawablesWithIntrinsicBounds(
                            getItem(position).loadIcon(getActivity().getPackageManager()), null,
                            null, null);
                    return convertView;
                }
            });

        }

    }

    public static class ShortcutListFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final Intent shortcuts = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            final List<ResolveInfo> pkgAppsList = getActivity().getPackageManager()
                    .queryIntentActivities(shortcuts, 0);

            setListAdapter(new ArrayAdapter<ResolveInfo>(getActivity(), R.id.title,
                    pkgAppsList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate((Context) getActivity(),
                                R.layout.awesome_action_layout_row, null);
                    }

                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position).loadLabel(getActivity().getPackageManager()));
                    title.setCompoundDrawablesWithIntrinsicBounds(
                            getItem(position).loadIcon(getActivity().getPackageManager()), null,
                            null, null);
                    return convertView;
                }
            });

        }

    }

}
