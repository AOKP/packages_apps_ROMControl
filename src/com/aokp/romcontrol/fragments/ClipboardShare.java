package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.widget.Toast;

public class SendToClipboard extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the clipboard system service
        ClipboardManager mClipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);

        // get the intent
        Intent intent = getIntent();

        // get the text
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);

        // put the text on the clipboard
        mClipboardManager.setText(text);
        
        // alert the user
        Toast.makeText(this, R.string.clipboard_notifcation, Toast.LENGTH_SHORT).show();
        
        finish();
    }
}
