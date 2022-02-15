package com.importusername.musicplayer.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class AppToast {
    public static void showToast(String text, Activity activity) {
        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
        });
    }
}
