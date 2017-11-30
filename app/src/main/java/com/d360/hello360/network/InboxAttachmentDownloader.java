/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.d360.hello360.ui.inbox.InboxActivity;
import com.threesixtydialog.sdk.D360InboxAttachment;
import com.threesixtydialog.sdk.D360InboxMessage;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.d360.hello360.Hello360.TAG;

public class InboxAttachmentDownloader extends IntentService {

    public static final String ACTION = "download";

    private static final String sServiceName = "com.d360.hello360.network.InboxAttachmentDownloader";

    public InboxAttachmentDownloader() {
        super(sServiceName);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public InboxAttachmentDownloader(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        if (!ACTION.equals(intent.getAction())) return;

        D360InboxMessage message = intent.getParcelableExtra("message");

        // Check if there is anything to download for the message
        if (message == null || message.getAttachment() == null || message.getAttachment().getUrl() == null) {
            return;
        }

        Log.d(TAG, "Downloading attachment for: " + message.getTitle());
        String viewHolderId = intent.getStringExtra(InboxActivity.INBOX_EXTRA_VIEW_HOLDER_ID);

        D360InboxAttachment attachment = message.getAttachment();
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(attachment.getUrl()).build();

        Response response;
        byte[] rawBitmap = null;

        try {
            response = client.newCall(request).execute();
            if (null != response.body()) {
                //noinspection ConstantConditions
                rawBitmap = response.body().bytes();
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't download the attachment for the inbox message: \""
                    + message.getTitle() + "\". Message: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Intent bitmapIntent = new Intent(InboxActivity.INBOX_ACTION_UPDATE_IMAGE);
        bitmapIntent.putExtra(InboxActivity.INBOX_EXTRA_BITMAP, rawBitmap);
        bitmapIntent.putExtra(InboxActivity.INBOX_EXTRA_VIEW_HOLDER_ID, viewHolderId);
        bitmapIntent.putExtra(InboxActivity.INBOX_EXTRA_MESSAGE, message);

        sendBroadcast(bitmapIntent);
    }
}
