package com.motorola.livestream.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.motorola.livestream.model.fb.User;

public class Util {

    private static final int HOUSAND = 1000;
    private static final int MEGA = 1000000;

    private static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";

    public static String getFormattedNumber(int number) {
        if (number <= 0) {
            // TBD with CXD, shall we display 0 or empty when the number is 0
            return String.valueOf(0);
            //return null;
        } else if (number < HOUSAND) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / (HOUSAND * 1.0f));
        } else {
            return String.format("%.1fM", number / (MEGA * 1.0f));
        }
    }

    public static void jumpToFacebook(Context context, User currentUser) {
        if (context == null || currentUser == null) {
            return;
        }

        Intent intent = null;
        try {
            PackageInfo pkgInfo =
                    context.getPackageManager().getPackageInfo(FACEBOOK_PACKAGE_NAME, 0);
            if (pkgInfo.applicationInfo.enabled) {
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("facebook://profile/" + currentUser.getId()));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.facebook.com/" + currentUser.getName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
