
package com.aokp.romcontrol.github;

/*
 * Copyright (C) 2012 The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.aokp.romcontrol.R;

/**
 * Shows extended infomation about a selected commit
 * in an AlertDialog
 */
public class CommitViewerDialog extends AlertDialog.Builder {
    private View mRoot;
    private Context mContext;
    private static Config mConfig;
    private static CommitObject mCommit;

    /**
     * Constructor using a context for this builder and the {@link android.app.AlertDialog} it creates.
     */
    public CommitViewerDialog(Context context, CommitObject commit) {
        super(context);
        mConfig = new Config();
        mCommit = commit;
        mContext = context;
    }

    public CommitViewerDialog(Context context) {
        super(context);
        mContext = context;
    }

    public void setCommitAndShow(CommitObject commit) {
        mCommit = commit;
        showDialog();
    }

    public void showDialog() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflater.inflate(R.layout.extended_commit_info_layout, null);
        setup();

        this.setTitle(R.string.commit_extended_info_title);
        this.setView(mRoot);
        this.setNegativeButton(R.string.button_close, null);
        this.setPositiveButton(R.string.button_webview,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String webviewUrl = "https://github.com/" + Config.StaticVars.ORGANIZATION
                        + mCommit.getPath() + "/commit/"
                        + mCommit.getCommitHash();
                    i.setData(Uri.parse(webviewUrl));
                    mContext.startActivity(i);
                }
            });
        this.create().show();
    }

    private void setup() {
        // references for our objects
        ScrollView scroller = (ScrollView) mRoot.findViewById
            (R.id.extended_commit_info_layout_scrollview);
        // so we scroll smoothly if commit message is large
        scroller.setSmoothScrollingEnabled(true);
        scroller.fling(Config.StaticVars.DEFAULT_FLING_SPEED);

        // gain object references
        LinearLayout authorContainer = (LinearLayout) mRoot.findViewById
            (R.id.author_container);
        LinearLayout committerContainer = (LinearLayout) mRoot.findViewById
            (R.id.committer_container);
        ImageView authorAvatar = (ImageView) mRoot.findViewById
            (R.id.author_avatar);
        ImageView committerAvatar = (ImageView) mRoot.findViewById
            (R.id.committer_avatar);
        TextView author_header = (TextView) mRoot.findViewById
            (R.id.author_header);
        TextView committer_header = (TextView) mRoot.findViewById
            (R.id.committer_header);
        TextView author_tv = (TextView) mRoot.findViewById
            (R.id.commit_author);
        TextView committer_tv = (TextView) mRoot.findViewById
            (R.id.commit_committer);
        TextView message_tv = (TextView) mRoot.findViewById
            (R.id.commit_message);
        TextView date_tv = (TextView) mRoot.findViewById
            (R.id.commit_date);
        TextView sha_tv = (TextView) mRoot.findViewById
            (R.id.commit_sha);

        // remove any LinearLayouts we don't have values for
        if (mCommit.getAuthorName() == null) {
            authorContainer.setVisibility(View.GONE);
            author_header.setVisibility(View.GONE);
            // reset our watcher
            Config.StaticVars.REMOVE_AUTHOR_LAYOUT = false;
        } else {
            // since we have this value we don't hide and we load our images
            // this way we don't waste bandwidth loading legacy values
            if (mCommit.getAuthorGravatar() != null)
                new DownloadImageTask(authorAvatar).execute(mCommit.getAuthorGravatar());
            else
                // this is important because if we have null value
                // it won't fail till too late for us to handle
                Config.StaticVars.REMOVE_AUTHOR_LAYOUT = true;
        }

        if (Config.StaticVars.REMOVE_COMMITTER_LAYOUT) {
            committerContainer.setVisibility(View.GONE);
            committer_header.setVisibility(View.GONE);
            // reset our watcher
            Config.StaticVars.REMOVE_COMMITTER_LAYOUT = false;
        } else {
            // try to populate the image from gravatar
            // @link http://stackoverflow.com/a/9288544
            if (mCommit.getCommitterGravatar() != null)
                new DownloadImageTask(committerAvatar).execute(mCommit.getCommitterGravatar());
            else
                Config.StaticVars.REMOVE_COMMITTER_LAYOUT = true;
        }

        // setText for TextViews
        author_tv.setText(mCommit.getAuthorName());
        committer_tv.setText(mCommit.getCommitterName());
        message_tv.setText(mCommit.getBody());
        date_tv.setText(mCommit.getCommitterDate());

        // we split the sha-1 hash into two strings because
        // it looks horrible by default display and smaller
        // size text is hard to read
        int halfHashLength = mCommit.getCommitHash().length() / 2;
        StringBuilder splitHash = new StringBuilder();
        splitHash.append(mCommit.getCommitHash().substring(0, halfHashLength));
        splitHash.append("-\n"); // to seperate the strings
        splitHash.append(mCommit.getCommitHash().substring(halfHashLength));
        splitHash.trimToSize();
        // set the text from our StringBuilder
        sha_tv.setText(splitHash.toString());
    }
}
