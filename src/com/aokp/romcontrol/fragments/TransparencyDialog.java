
package com.aokp.romcontrol.fragments;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.aokp.romcontrol.R;

public class TransparencyDialog extends DialogFragment implements
        DialogInterface.OnClickListener, TabListener {

    ViewPager mPager;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ActionBar bar = getActivity().getActionBar();
        bar.addTab(bar.newTab().setText("Statusbar").setTabListener(this).setTag(0));
        bar.addTab(bar.newTab().setText("Navigation bar").setTabListener(this).setTag(1));
        
        View v = View.inflate(getActivity(), R.layout.seekbar_dialog, null);
        mPager=(ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(new PagerAdapter() {
            
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return 0;
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface unused) {
        super.onCancel(unused);
        Toast.makeText(getActivity(), R.string.back, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        Integer position = (Integer) tab.getTag();
        mPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }
}
