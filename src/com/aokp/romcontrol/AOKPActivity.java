
package com.aokp.romcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class AOKPActivity extends Activity {

    protected boolean mStartedFromShortcut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            if (getIntent().hasExtra("started_from_shortcut"))
                mStartedFromShortcut = getIntent().getBooleanExtra("started_from_shortcut", false);
        }

        getActionBar().setDisplayHomeAsUpEnabled(!mStartedFromShortcut);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
