package com.mparticle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;


import com.mparticle.messaging.AbstractCloudMessage;
import com.mparticle.messaging.CloudAction;

import com.mparticle.internal.Constants;


/**
 * Core {@code BroadcastReceiver} used to support push notification and installer referrers. Handles the following Intent actions:
 *
 * <ul>
 *     <li>{@code com.android.vending.INSTALL_REFERRER}</li>
 *     <li>{@code com.google.android.c2dm.intent.REGISTRATION}</li>
 *     <li>{@code com.google.android.c2dm.intent.RECEIVE}</li>
 *     <li>{@code com.google.android.c2dm.intent.UNREGISTER}</li>
 *     <li>{@code android.intent.action.PACKAGE_REPLACED}</li>
 *     <li>{@code android.intent.action.BOOT_COMPLETED}</li>
 * </ul>
 *
 * This {@code BroadcastReceiver} must be specified within the {@code <application>} block of your application's {@code AndroidManifest.xml} file:
 *
 *
 * <pre>
 * {@code
 *  <receiver android:name="com.mparticle.MPReceiver"
 *      android:permission="com.google.android.c2dm.permission.SEND" >
 *      <intent-filter>
 *          <action android:name="com.android.vending.INSTALL_REFERRER"/>
 *          <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
 *          <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *          <action android:name="com.google.android.c2dm.intent.UNREGISTER"/>
 *          <!-- Use your application's package name as the category -->
 *          <category android:name="<YOUR_PACKAGE_NAME_HERE>" />
 *      </intent-filter>
 *      <!-- The GCM registration ID could change on an app upgrade, this filter will allow mParticle to re-register for push notifications if necessary. -->
 *      <intent-filter>
 *          <action android:name="android.intent.action.PACKAGE_REPLACED" />
 *          <!-- Use your application's package name as the path -->
 *          <data android:path="<YOUR_PACKAGE_NAME_HERE>"
 *              android:scheme="package" />
 *      </intent-filter>
 *      <!-- The GCM registration ID could change if the device's Android version changes, this filter will allow mParticle to re-register for push notifications if necessary. -->
 *      <intent-filter>
 *          <action android:name="android.intent.action.BOOT_COMPLETED" />
 *      </intent-filter>
 * </receiver> }</pre>
 *
 *
 */
public class MPReceiver extends BroadcastReceiver {

    { // http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
        Looper looper = Looper.getMainLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            public void run() {
                try {
                    Class.forName("android.os.AsyncTask");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static final String MPARTICLE_IGNORE = "mparticle_ignore";

    public MPReceiver() {
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (!MPARTICLE_IGNORE.equals(intent.getAction()) && !intent.getBooleanExtra(MPARTICLE_IGNORE, false)) {
            if ("com.android.vending.INSTALL_REFERRER".equals(intent.getAction())) {
                String referrer = intent.getStringExtra("referrer");
                SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE);
                preferences.edit().putString(Constants.PrefKeys.INSTALL_REFERRER, referrer).commit();
            } else {
                MPService.runIntentInService(context, intent);
            }
            setResult(Activity.RESULT_OK, null, null);
        }
    }

}
