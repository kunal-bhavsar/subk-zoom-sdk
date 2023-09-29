package com.subk.testing;

import android.app.Application;

import co.subk.zoomsdk.ZoomSdkInitializer;
import co.subk.zoomsdk.meeting.exceptions.ZoomInitializationException;

public class TestingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ZoomSdkInitializer.initialize(getApplicationContext(), true);
        } catch (ZoomInitializationException e) {
            throw new RuntimeException(e);
        }

    }
}
