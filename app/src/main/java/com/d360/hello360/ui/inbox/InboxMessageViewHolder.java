/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.ui.inbox;

import com.threesixtydialog.sdk.D360InboxMessage;

public class InboxMessageViewHolder {

    private D360InboxMessage mInboxMessage;

    public InboxMessageViewHolder(D360InboxMessage message) {
        mInboxMessage = message;
    }

    public D360InboxMessage getInboxMessage() {
        return mInboxMessage;
    }

}
