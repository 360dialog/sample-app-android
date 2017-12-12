/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 12/8/17 11:41 AM
 */

package com.d360.hello360.ui.inbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.threesixtydialog.sdk.D360;

import org.json.JSONException;

import static com.d360.hello360.Hello360.TAG;

/**
 * Stuff not related to the SDK integration.
 * We keep it in separate class to keep the integration code clean
 */
@SuppressLint("Registered")
public class BaseInboxActivity extends AppCompatActivity {

    public static final String DEEPLINK_SCHEME = "360sampleapp";

    private FloatingActionMenu mFabMenu;

    @Override
    protected void onResume() {
        super.onResume();
        handleDeeplink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Notify user that the app was opened by a deeplink
        handleDeeplink(intent);

        /*
        In case of Activity having `android:launchMode="singleTask"`, it is required to add deeplink
        tracking in `onNewIntent()` method to make sure that deeplinks will be tracked correctly
         */
        if (isDeeplink(intent)) {
            //noinspection ConstantConditions
            D360.urls().reportDeepLinkOpened(intent.getDataString());
        }
    }

    /**
     * Handle a deeplink: show alert with current deeplink
     *
     * @param intent An intent
     */
    private void handleDeeplink(Intent intent) {
        if (isDeeplink(intent)) {
            //noinspection ConstantConditions
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_link_dialog_title)
                    .setMessage(getString(R.string.app_link_dialog_message, intent.getData().toString()))
                    .setPositiveButton(R.string.general_ok, null)
                    .create();

            dialog.show();
        }
    }

    private boolean isDeeplink(Intent intent) {
        return intent != null &&
                intent.getData() != null &&
                intent.getData().getScheme().equals(DEEPLINK_SCHEME);
    }

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
                    InApp inappCampaign = new InApp("https://inapp-samples.s3-eu-west-1.amazonaws.com/sample-inapp-pagination.html");
                    Tester.send(getApplicationContext(), inappCampaign);
                } catch (CampaignException e) {
                    handleCampaignTesterError(coordinatorLayout, e, "InApp");
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
                    Tester.send(getApplicationContext(), notification);
                } catch (CampaignException | JSONException e) {
                    handleCampaignTesterError(coordinatorLayout, e, "Notification");
                }
            }
        });

        fabInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabMenu.close(true);
                try {
                    InAppAction action = new InAppAction("https://inapp-samples.s3-eu-west-1.amazonaws.com/sample-inapp-pagination.html", InAppAction.Button.DARK);

                    Inbox inboxCampaign = new Inbox(getApplicationContext());
                    inboxCampaign
                            .setAttachmentUrl("https://inapp-samples.s3.amazonaws.com/examples/JPG/desertsmall.jpg")
                            .setInboxAction(action)
                    ;
                    Tester.send(getApplicationContext(), inboxCampaign);
                } catch (CampaignException e) {
                    handleCampaignTesterError(coordinatorLayout, e, "Inbox");
                }
            }
        });
    }

    private void handleCampaignTesterError(@Nullable CoordinatorLayout coordinatorLayout, Exception e, String campaignType) {
        String message = getString(R.string.campaign_error, campaignType, e.getMessage());
        Log.d(TAG, "Can't send the " + campaignType + " campaign. Message: " + e.getMessage());
        e.printStackTrace();

        if (coordinatorLayout != null) {
            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
        }
    }

}
