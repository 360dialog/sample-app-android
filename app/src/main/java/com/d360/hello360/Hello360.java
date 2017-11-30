/*
 * Created by Wojciech Kukiełczak
 * Copyright (c) 2017 360dialog. All rights reserved.
 *
 * Last modified 11/30/17 11:22 AM
 */

package com.d360.hello360;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.threesixtydialog.fcmcompat.D360FcmPushProvider;
import com.threesixtydialog.sdk.D360;
import com.threesixtydialog.sdk.D360ActivityLifecycleCallbacks;
import com.threesixtydialog.sdk.D360Options;

public class Hello360 extends Application {

    public static final String TAG = "com.d360.hello360";
    public static final String NOTIFICATION_CHANNEL_DEFAULT = "com.d360.default";

    @Override
    public void onCreate() {
        super.onCreate();

        init360dialogSdk();
    }

    /**
     * This method shows how to initialize the 360dialog SDK
     */
    private void init360dialogSdk() {
        String appId = "213";
        String apiKey = "5df4c4b26263bb7a2462fb476d0d50076da07a4d7f15ba991f39977d2ac4e1fe";

        D360Options options = new D360Options(appId, apiKey);

        // Push setup (using Firebase Cloud Messaging and 360dialog's fcmcompat helper)
        D360FcmPushProvider fcmPushProvider = new D360FcmPushProvider(this);
        options.setPushProvider(fcmPushProvider);
        // END: Push setup

        D360.init(options, this);

        registerActivityLifecycleCallbacks(new D360ActivityLifecycleCallbacks());

        // Android O: Register notification channels to the SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel();
    }

    /**
     * Starting from Android 8.0 you need to create at least one notification channel
     * in order to be able to show any notification.
     *
     * This method creates the notification channel and registers it in 360dialog SDK
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_DEFAULT,
                getString(R.string.app_notification_channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(getString(R.string.app_notification_channel_default_description));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);

            // Register just created notification channel to the 360dialog SDK
            D360.push().registerDefaultNotificationChannel(channel);
        }
    }

}
