package co.subk.zoomsdk;

import android.app.Application;
import android.content.Context;



public class ZoomApplication extends Application {
    static ZoomApplication application;

    @Override
    public Context getApplicationContext() {
        application=this;


     //   PreferenceHandler.initialize(this);
        return super.getApplicationContext();
    }

    public static Context getInstance(){
        return application;
    }

}
