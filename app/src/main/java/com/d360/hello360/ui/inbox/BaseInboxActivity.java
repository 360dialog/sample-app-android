/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 12/8/17 11:41 AM
 */

package com.d360.hello360.ui.inbox;

import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.d360.campaigntester.CampaignException;
import com.d360.campaigntester.Tester;
import com.d360.campaigntester.campaign.InApp;
import com.d360.campaigntester.campaign.Inbox;
import com.d360.campaigntester.campaign.Notification;
import com.d360.campaigntester.campaign.action.InAppAction;
import com.d360.campaigntester.campaign.action.UrlAction;
import com.d360.hello360.Hello360;
import com.d360.hello360.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;

import static com.d360.hello360.Hello360.TAG;

/**
 * Stuff not related to the SDK integration.
 * We keep it in separate class to keep the integration code clean
 */
public abstract class BaseInboxActivity extends AppCompatActivity {

    private FloatingActionMenu mFabMenu;

    /**
     * Floating action menu and it's buttons setup
     */
    protected void setupFloatingActionButtons(@Nullable final CoordinatorLayout coordinatorLayout) {
        FloatingActionButton fabInapp = findViewById(R.id.fab_inapp);
        FloatingActionButton fabInbox = findViewById(R.id.fab_inbox);
        FloatingActionButton fabNotification = findViewById(R.id.fab_notification);

        mFabMenu = findViewById(R.id.fab_menu);

        fabInapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabMenu.close(true);
                try {
                    InApp inappCampaign = new InApp(getApplicationContext());
                    Tester.getInstance().send(getApplicationContext(), inappCampaign);
                } catch (CampaignException e) {
                    String message = getString(R.string.campaign_error, "InApp", e.getMessage());
                    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Can't send the InApp campaign. Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        fabNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabMenu.close(true);
                try {
                    UrlAction openUrl = new UrlAction("http://www.google.com/search?q=macarons");

                    Notification notification = new Notification();
                    notification
                            .setChannelId(Hello360.NOTIFICATION_CHANNEL_DEFAULT)
                            .setTitle("Hi 👋")
                            .setBody("Tap to open a URL")
                            .setLargeIconUrl("https://inapp-samples.s3.amazonaws.com/examples/JPG/desertsmall.jpg")
                            .setBigPictureUrl("http://1.bp.blogspot.com/-VWYdCzYXGjo/VazZ_-DFAGI/AAAAAAAAEx0/ZJd4csDslPQ/s1600/20150720_094211%2528edited%2529.jpg")
                            .setRichText("Tap to open a URL")
                            .allowNotificationWhenAppIsInForeground(true)
                            .setAction(openUrl)
                    ;
                    Tester.getInstance().send(getApplicationContext(), notification);
                } catch (CampaignException | JSONException e) {
                    String message = getString(R.string.campaign_error, "Notification", e.getMessage());
                    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Can't send the Notification campaign. Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        fabInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabMenu.close(true);
                try {
                    InAppAction action = new InAppAction("https://inapp-samples.s3-eu-west-1.amazonaws.com/inapp-pagination.html", InAppAction.Button.DARK);

                    Inbox inboxCampaign = new Inbox(getApplicationContext());
                    inboxCampaign
                            .setAttachmentUrl("https://inapp-samples.s3.amazonaws.com/examples/JPG/desertsmall.jpg")
                            .setInboxAction(action)
                    ;
                    Tester.getInstance().send(getApplicationContext(), inboxCampaign);
                } catch (CampaignException e) {
                    String message = getString(R.string.campaign_error, "Inbox", e.getMessage());
                    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "Can't send the Inbox campaign. Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

}
