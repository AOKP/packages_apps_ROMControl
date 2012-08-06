package com.aokp.romcontrol;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.aokp.romcontrol.R;

public class ShareToClipboard extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the clipboard system service
        ClipboardManager mClipboardManager = (ClipboardManager) this
                .getSystemService(CLIPBOARD_SERVICE);

        // get the intent
        Intent intent = getIntent();

        // get the text
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);

        // put the text on the clipboard
        mClipboardManager.setPrimaryClip(ClipData.newPlainText("Shared to RC", text));

        // alert the user
        Toast.makeText(this, R.string.clipboard_notification, Toast.LENGTH_SHORT).show();
        finish();
    }
}
