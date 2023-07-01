package com.subk.zoomsdk.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
    public static void showShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
