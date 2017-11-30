/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360.ui.inbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.d360.campaigntester.CampaignException;
import com.d360.campaigntester.Tester;
import com.d360.campaigntester.campaign.Inbox;
import com.d360.hello360.R;
import com.d360.hello360.network.InboxAttachmentDownloader;
import com.threesixtydialog.sdk.D360;
import com.threesixtydialog.sdk.D360InboxFetchRequest;
import com.threesixtydialog.sdk.D360InboxMessage;
import com.threesixtydialog.sdk.D360InboxService;

import java.util.ArrayList;
import java.util.List;

import static com.d360.hello360.Hello360.TAG;

public class InboxActivity extends AppCompatActivity implements
        D360InboxService.OnMessagesFetchListener,
        D360InboxService.OnMessageCompletionListener,
        AdapterView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener {

    public static final String INBOX_ACTION_UPDATE = "update";
    public static final String INBOX_ACTION_UPDATE_IMAGE = "update.image";
    public static final String INBOX_ACTION_REMOVE = "remove";
    public static final String INBOX_EXTRA_MESSAGE = "message";
    public static final String INBOX_EXTRA_MESSAGES = "messages";
    public static final String INBOX_EXTRA_BITMAP = "bitmap";
    public static final String INBOX_EXTRA_VIEW_HOLDER_ID = "viewholder.id";

    private ListView mListView;
    private InboxArrayAdapter mInboxArrayAdapter;
    private List<InboxMessageViewHolder> mMessages = new ArrayList<>();
    private ActionMode mActionMode = null;

    private RadioGroup mFilterReadGroup;
    private RadioGroup mFilterDeletedGroup;

    /**
     * Start with default filter set
     */
    private D360InboxFetchRequest mInboxFilter = D360InboxFetchRequest.inboxRequest();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFloatingActionButton();

        mFilterReadGroup = findViewById(R.id.filter_read_group);
        mFilterDeletedGroup = findViewById(R.id.filter_deleted_group);

        mInboxArrayAdapter = new InboxArrayAdapter(this, R.layout.inbox_item, mMessages);

        mListView = findViewById(R.id.inbox_messages);
        mListView.setAdapter(mInboxArrayAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        mListView.setOnItemClickListener(this);

        registerForContextMenu(mListView);

        setListenersForFilters();
        D360.inbox().setDelegate(new InboxServiceDelegate(getApplicationContext()));
        fetchInbox();
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INBOX_ACTION_UPDATE);
        intentFilter.addAction(INBOX_ACTION_REMOVE);
        intentFilter.addAction(INBOX_ACTION_UPDATE_IMAGE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inbox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_select_all:
                for (int i = 0; i < mMessages.size(); i++) {
                    mListView.setItemChecked(i, true);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        final CoordinatorLayout layout = findViewById(R.id.coordinator_layout);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Inbox inboxCampaign = new Inbox(getApplicationContext());
                    Tester.getInstance().send(getApplicationContext(), inboxCampaign);
                } catch (CampaignException e) {
                    String message = getString(R.string.inbox_send_sample_error);
                    Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Can't send the InboxCampaign. Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    protected void fetchInbox() {
        Log.d(TAG, "FetchInbox called");
        D360.inbox().fetch(mInboxFilter, InboxActivity.this);
    }

    public void clearList() {
        Log.d(TAG, "Clear the messages list");
        mMessages.clear();
        mInboxArrayAdapter.updateMessagesList();
    }

    /**
     * Broadcast Receiver to handle delegate's calls. This receiver works when the inbox message is
     * received while the InboxActivity is active
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            Log.d(TAG, "New inbox message received by BroadcastReceiver");
            String action = intent.getAction();

            if (action.equals(INBOX_ACTION_UPDATE) || action.equals(INBOX_ACTION_REMOVE)) {
                fetchInbox();
            } else if (action.equals(INBOX_ACTION_UPDATE_IMAGE)) {
                setBitmap(intent);
            }
        }

        /**
         * Set just downloaded bitmap attachment for InboxMessage
         *
         * @param intent Intent
         */
        public void setBitmap(Intent intent) {
            byte[] rawBitmap = intent.getByteArrayExtra(INBOX_EXTRA_BITMAP);
            String viewHolderId = intent.getStringExtra(INBOX_EXTRA_VIEW_HOLDER_ID);

            InboxMessageViewHolder viewHolder = null;
            for (InboxMessageViewHolder vh : mMessages) {
                if (vh.getId().equals(viewHolderId)) {
                    viewHolder = vh;
                    break;
                }
            }

            if (viewHolder == null) {
                Log.d(TAG, "No view holder has been found");
                return;
            }

            if (rawBitmap != null) {
                Log.d(TAG, "Attachment for \"" +
                        viewHolder.getInboxMessage().getTitle() +
                        "\" downloaded. Applying!"
                );
                int offset = 0;
                int length = rawBitmap.length;

                viewHolder.setAttachmentBitmap(BitmapFactory.decodeByteArray(rawBitmap, offset, length));
                mInboxArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    /*

        ============================================================================================
        D360InboxService.OnMessagesFetchListener implementation
        ============================================================================================

     */

    @Override
    public void onSuccess(@NonNull List<D360InboxMessage> list) {
        clearList();

        for (D360InboxMessage inboxMessage : list) {
            InboxMessageViewHolder message = new InboxMessageViewHolder(inboxMessage);
            mMessages.add(message);
            // new DownloadImageTask(message, mInboxArrayAdapter).execute();

            Intent intent = new Intent(this, InboxAttachmentDownloader.class);
            intent.setAction(InboxAttachmentDownloader.ACTION);
            intent.putExtra(INBOX_EXTRA_VIEW_HOLDER_ID, message.getId());
            intent.putExtra(INBOX_EXTRA_MESSAGE, inboxMessage);

            startService(intent);
        }

        // Reset view if there was selection made on the list
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }

        Log.d(TAG, "Inbox messages received!");

        mInboxArrayAdapter.updateMessagesList();
    }

    @Override
    public void onError() {
        Toast.makeText(
                this,
                "Can't fetch the inbox now. Try again later",
                Toast.LENGTH_SHORT
        ).show();
    }

    /*

        ============================================================================================
        D360InboxService.OnMessageFetchListener implementation
        (be aware: onError is shared with the D360InboxService.OnMessageFetchListener in this sample!)
        ============================================================================================

     */

    @Override
    public void onSuccess() {
        fetchInbox();
    }

    /*

        ============================================================================================
        AdapterView.OnItemClickListener implementation
        ============================================================================================

     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        InboxMessageViewHolder inboxMessageViewHolder = mMessages.get(position);
        D360InboxMessage message = inboxMessageViewHolder.getInboxMessage();

        if (message == null) return;

        if (message.isExecutable()) {
            D360.inbox().executeMessage(message, this);
        } else {
            Toast.makeText(
                    this,
                    "This message is not executable",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*

        ============================================================================================
        AbsListView.MultiChoiceModeListener implementation
        ============================================================================================

     */

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (mode == null) return;

        int checkedCount = mListView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.inbox_list_messages_selected, checkedCount, checkedCount));
        mInboxArrayAdapter.toggleSelection(position);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (mode == null) return false;

        mode.getMenuInflater().inflate(R.menu.menu_inbox_list, menu);
        mActionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        SparseBooleanArray selected = mInboxArrayAdapter.getSelectedItemIds();
        final List<D360InboxMessage> messages = new ArrayList<>();
        if (selected != null) {
            int i = 0;
            while (i < selected.size()) {
                int position = selected.keyAt(i);
                InboxMessageViewHolder messageViewHolder = mInboxArrayAdapter.getItem(position);
                if (messageViewHolder != null && messageViewHolder.getInboxMessage() != null) {
                    messages.add(messageViewHolder.getInboxMessage());
                }

                i++;
            }
        }

        clearList();

        switch (item.getItemId()) {
            case R.id.inbox_item_menu_read:
                D360.inbox().updateMessagesAsRead(messages, true, this);
                break;

            case R.id.inbox_item_menu_unread:
                D360.inbox().updateMessagesAsRead(messages, false, this);
                break;

            case R.id.inbox_item_menu_delete:
                D360.inbox().updateMessagesAsDeleted(messages, true, this);
                break;

            case R.id.inbox_item_menu_undelete:
                D360.inbox().updateMessagesAsDeleted(messages, false, this);
                break;

            case R.id.inbox_item_menu_delete_forever:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.inbox_item_dialog_delete_title))
                        .setMessage(getString(R.string.inbox_item_dialog_delete_message))
                        .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                D360.inbox().removeMessages(messages, InboxActivity.this);
                            }
                        })
                        .setNegativeButton(R.string.general_cancel, null)
                        .show();
                break;

            case R.id.inbox_item_menu_deselect:
                mInboxArrayAdapter.removeSelection();
                break;
        }

        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mInboxArrayAdapter.removeSelection();
    }

    /*

        Handle inbox filters

     */

    /**
     * Select filters on the view depending on the state of D360InboxFetchRequest state
     */
    private void selectFilters() {
        int readFilterId;
        switch (mInboxFilter.getReadFilter()) {
            case READ:
                readFilterId = R.id.filter_read_read;
                break;

            case NOT_READ:
                readFilterId = R.id.filter_read_unread;
                break;

            default:
            case ANY:
                readFilterId = R.id.filter_read_any;
                break;
        }

        int deletedFilterId;
        switch (mInboxFilter.getDeleteFilter()) {
            case DELETED:
                deletedFilterId = R.id.filter_deleted_deleted;
                break;

            case NOT_DELETED:
                deletedFilterId = R.id.filter_deleted_inbox;
                break;

            default:
            case ANY:
                deletedFilterId = R.id.filter_deleted_any;
                break;
        }

        mFilterReadGroup.check(readFilterId);
        mFilterDeletedGroup.check(deletedFilterId);
    }

    /**
     * React on filters change
     */
    private void setListenersForFilters() {
        selectFilters(); // firs, select the filters

        mFilterReadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                D360InboxFetchRequest.DeleteFilter deleteFilter = mInboxFilter.getDeleteFilter();
                D360InboxFetchRequest.ReadFilter readFilter;
                switch (checkedId) {
                    case R.id.filter_read_read:
                        readFilter = D360InboxFetchRequest.ReadFilter.READ;
                        break;
                    case R.id.filter_read_unread:
                        readFilter = D360InboxFetchRequest.ReadFilter.NOT_READ;
                        break;
                    case R.id.filter_read_any:
                    default:
                        readFilter = D360InboxFetchRequest.ReadFilter.ANY;
                        break;
                }

                clearList();

                mInboxFilter = new D360InboxFetchRequest(readFilter, deleteFilter);
                fetchInbox();
            }
        });

        mFilterDeletedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                D360InboxFetchRequest.ReadFilter readFilter = mInboxFilter.getReadFilter();
                D360InboxFetchRequest.DeleteFilter deleteFilter;
                switch (checkedId) {
                    case R.id.filter_deleted_deleted:
                        deleteFilter = D360InboxFetchRequest.DeleteFilter.DELETED;
                        break;
                    case R.id.filter_deleted_inbox:
                        deleteFilter = D360InboxFetchRequest.DeleteFilter.NOT_DELETED;
                        break;
                    case R.id.filter_deleted_any:
                    default:
                        deleteFilter = D360InboxFetchRequest.DeleteFilter.ANY;
                        break;
                }

                clearList();

                mInboxFilter = new D360InboxFetchRequest(readFilter, deleteFilter);
                fetchInbox();
            }
        });
    }
}
