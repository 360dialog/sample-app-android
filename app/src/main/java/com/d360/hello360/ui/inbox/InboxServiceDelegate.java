/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.ui.inbox;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.threesixtydialog.sdk.D360InboxMessage;
import com.threesixtydialog.sdk.D360InboxServiceDelegate;

import java.util.List;

public class InboxServiceDelegate implements D360InboxServiceDelegate {

    private Context mContext;

    public InboxServiceDelegate(Context context) {
        mContext = context;
    }

    @Override
    public void didReceiveInboxMessage(@NonNull D360InboxMessage d360InboxMessage) {
        broadcastUpdated(d360InboxMessage);
    }

    @Override
    public void didUpdateInboxMessage(@NonNull D360InboxMessage d360InboxMessage) {
        broadcastUpdated(d360InboxMessage);
    }

    @Override
    public void didRemoveInboxMessages(@NonNull List<D360InboxMessage> list) {
        if (mContext == null) return;

        if (!list.isEmpty()) {
            Intent intent = new Intent(InboxActivity.INBOX_ACTION_REMOVE);
            intent.putExtra(InboxActivity.INBOX_EXTRA_MESSAGES, list.toArray());

            mContext.sendBroadcast(intent);
        }
    }

    private void broadcastUpdated(D360InboxMessage message) {
        if (mContext == null) return;

        Intent intent = new Intent(InboxActivity.INBOX_ACTION_UPDATE);
        intent.putExtra(InboxActivity.INBOX_EXTRA_MESSAGE, message);

        mContext.sendBroadcast(intent);
    }
}
