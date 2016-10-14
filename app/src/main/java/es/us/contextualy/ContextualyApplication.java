package es.us.contextualy;


import android.content.Context;
import android.support.multidex.MultiDexApplication;

import es.us.contextualy.data.FirebaseRemoteConfigHelper;

public class ContextualyApplication extends MultiDexApplication {

    public ContextualyApplication(){
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseRemoteConfigHelper.getInstance().setDefaults();
    }

    public static ContextualyApplication get(Context context){
        return (ContextualyApplication) context.getApplicationContext();
    }



}
