/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.ui.inbox;

import android.graphics.Bitmap;

import com.threesixtydialog.sdk.D360InboxMessage;

import java.util.UUID;

public class InboxMessageViewHolder {

    private D360InboxMessage mInboxMessage;
    private Bitmap mAttachmentBitmap = null;

    /**
     * Unique view ID so it can be properly identified
     */
    private String mId = UUID.randomUUID().toString();

    public InboxMessageViewHolder(D360InboxMessage message) {
        mInboxMessage = message;
    }

    public D360InboxMessage getInboxMessage() {
        return mInboxMessage;
    }

    public Bitmap getAttachmentBitmap() {
        return mAttachmentBitmap;
    }

    public void setAttachmentBitmap(Bitmap attachmentBitmap) {
        mAttachmentBitmap = attachmentBitmap;
    }

    public String getId() {
        return mId;
    }

}
