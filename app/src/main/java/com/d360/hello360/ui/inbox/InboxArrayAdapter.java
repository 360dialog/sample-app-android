/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.ui.inbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.d360.hello360.R;
import com.squareup.picasso.Picasso;
import com.threesixtydialog.sdk.D360InboxMessage;

import java.util.List;

public class InboxArrayAdapter extends ArrayAdapter<InboxMessageViewHolder> {

    private SparseBooleanArray mSelectedItemIds = new SparseBooleanArray();

    public InboxArrayAdapter(@NonNull Context context, int resource, @NonNull List<InboxMessageViewHolder> objects) {
        super(context, resource, objects);
        setNotifyOnChange(true);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView != null ?
                convertView :
                LayoutInflater
                        .from(getContext())
                        .inflate(R.layout.inbox_item, parent, false);

        InboxMessageViewHolder viewHolder = getItem(position);
        if (viewHolder == null) return view;

        D360InboxMessage message = viewHolder.getInboxMessage();

        setRead(view, message);
        setTitle(view, message.getTitle());
        setBody(view, message.getBody());
        setAttachment(view, message);

        return view;
    }

    public void updateMessagesList() {
        notifyDataSetInvalidated();
        notifyDataSetChanged();
    }

    /*

        ============================================================================================
        Local helpers to populate the contents to the views
        ============================================================================================

     */

    private void setRead(View view, D360InboxMessage message) {
        if (view == null || message == null) return;

        View indicator = view.findViewById(R.id.unread_indicator);

        if (message.getState().isRead()) {
            indicator.setVisibility(View.GONE);
        } else {
            indicator.setVisibility(View.VISIBLE);
        }
    }

    private void setTitle(View view, String title) {
        if (title == null || view == null) {
            return;
        }

        TextView titleTv = view.findViewById(R.id.title);
        if (titleTv != null) {
            titleTv.setText(title);
        }
    }

    private void setBody(View view, String body) {
        if (view == null || body == null) {
            return;
        }

        TextView bodyTv = view.findViewById(R.id.body);
        if (bodyTv != null) {
            bodyTv.setText(body);
        }
    }

    private void setAttachment(View view, D360InboxMessage message) {
        if (message.getAttachment() == null) return;
        ImageView attachmentImageView = view.findViewById(R.id.attachment);

        if (attachmentImageView != null) {
            Picasso
                    .with(getContext())
                    .load(message.getAttachment().getUrl().toString())
                    .into(attachmentImageView);
        }
    }

    /*

        ============================================================================================
        Selection handling
        ============================================================================================

     */

    public SparseBooleanArray getSelectedItemIds() {
        return mSelectedItemIds;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemIds.put(position, true);
        } else {
            mSelectedItemIds.delete(position);
        }

        notifyDataSetChanged();
    }
}
