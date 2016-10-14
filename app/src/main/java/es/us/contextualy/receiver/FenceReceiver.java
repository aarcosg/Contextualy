package es.us.contextualy.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.cantrowitz.rxbroadcast.RxBroadcast;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.util.ArrayList;
import java.util.List;

import es.us.contextualy.R;
import es.us.contextualy.data.AwarenessApiHelper;
import es.us.contextualy.data.FirebaseDatabaseHelper;
import es.us.contextualy.data.FirebaseRemoteConfigHelper;
import es.us.contextualy.data.SharedPreferencesHelper;
import es.us.contextualy.model.AwarenessContext;
import es.us.contextualy.model.BatteryStatus;
import es.us.contextualy.observable.google.awareness.DetectedActivityObservable;
import es.us.contextualy.observable.google.awareness.HeadphoneStateObservable;
import es.us.contextualy.observable.google.awareness.LocationObservable;
import es.us.contextualy.observable.google.awareness.PlacesObservable;
import es.us.contextualy.observable.google.awareness.WeatherObservable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class FenceReceiver extends BroadcastReceiver {

    private static final String TAG = FenceReceiver.class.getCanonicalName();

    private Context mContext;
    private AwarenessApiHelper mAwarenessApiHelper;
    private FirebaseRemoteConfigHelper mFirebaseRemoteConfigHelper;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private DetectedActivity mCurrentActivity;
    private String mCurrentActivityName;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "@onReceive");
        mContext = context.getApplicationContext();
        mSharedPreferencesHelper = SharedPreferencesHelper.getInstance(mContext);
        mAwarenessApiHelper = AwarenessApiHelper.getInstance(mContext);
        mFirebaseRemoteConfigHelper = FirebaseRemoteConfigHelper.getInstance();
        FenceState fenceState = FenceState.extract(intent);
        if (TextUtils.equals(fenceState.getFenceKey(), AwarenessApiHelper.MAIN_FENCE_KEY)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.e(TAG, "Fence triggered");
                    mAwarenessApiHelper.updateMainAwarenessFence(
                            TimeFence.inInterval(System.currentTimeMillis() +
                                            /*mFirebaseRemoteConfigHelper.getDetectionIntervalInMillis()*/ 1000,
                                    Long.MAX_VALUE));
                    mSubscriptions.add(mAwarenessApiHelper.getGoogleApiClientAwarenessObservable()
                            .flatMap(googleApiClient -> Observable.create(new DetectedActivityObservable(googleApiClient)))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleDetectedActivity));
                    break;
                case FenceState.FALSE:
                    Log.e(TAG, "Fence not triggered");
                    break;
                case FenceState.UNKNOWN:
                    Log.e(TAG, "UNKNOWN");
                    break;
            }
        }
    }

    private void handleDetectedActivity(DetectedActivity detectedActivity) {
        Log.d(TAG, detectedActivity.toString());
        if (detectedActivity.getConfidence() >= mFirebaseRemoteConfigHelper.getMinDetectedActivityConfidence()) {
            mCurrentActivity = detectedActivity;
            mCurrentActivityName = mAwarenessApiHelper.getActivityString(detectedActivity.getType());
            String lastActivity = mSharedPreferencesHelper.getLastActivity();
            // Reset properties saved in preferences if the user has been still for a long time
            long lastNotificationTime = mSharedPreferencesHelper.getLastNotificationTime();
            fetchCurrentContext();
            /*if(mFirebaseRemoteConfigHelper.isContextResetIfStillEnabled()
                    && TextUtils.equals(mCurrentActivityName, mContext.getString(R.string.still))
                    && TextUtils.equals(mCurrentActivityName, lastActivity)
                    && (new Date().getTime() - lastNotificationTime)
                        > mFirebaseRemoteConfigHelper.getStillResetIntervalInMillis()){
                mSharedPreferencesHelper.resetActivityRecognitionProperties();
                lastActivity = "";
            }
            if (TextUtils.isEmpty(lastActivity) || !TextUtils.equals(mCurrentActivityName, lastActivity)) {
                String candidateActivity = mSharedPreferencesHelper.getLastCandidateActivity();
                if (TextUtils.isEmpty(candidateActivity) || !TextUtils.equals(mCurrentActivityName, candidateActivity)) {
                    mSharedPreferencesHelper.saveLastCandidateActivity(mCurrentActivityName);
                } else if (TextUtils.equals(mCurrentActivityName, candidateActivity)) {
                    fetchCurrentContext();
                }
            }*/
        }
    }

    private void fetchCurrentContext(){
        Log.d(TAG, "@fetchCurrentContext");
        mSubscriptions.add(mAwarenessApiHelper.getGoogleApiClientAwarenessObservable()
                .flatMap(googleApiClient -> Observable.zip(
                        Observable.create(new LocationObservable(mContext, googleApiClient)),
                        Observable.create(new HeadphoneStateObservable(googleApiClient)),
                        Observable.create(new WeatherObservable(mContext, googleApiClient)),
                        Observable.create(new PlacesObservable(mContext, googleApiClient)),
                        RxBroadcast.fromBroadcast(mContext, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)),
                        (location, headphoneState, weather, placeLikelihoods, batteryStatusIntent)
                                -> new AwarenessContext(mCurrentActivity, location, headphoneState, weather,
                                new BatteryStatus(batteryStatusIntent), placeLikelihoods))
                )
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::sendContextToFirebase)
        );
    }

    private void sendContextToFirebase(AwarenessContext awarenessContext){
        Log.d(TAG, "@sendContextToBackend");
        String userId = mSharedPreferencesHelper.getUserId();
        if(!TextUtils.isEmpty(userId)){

            List<String> weatherConditions = new ArrayList<>();
            for(int i = 0; i < awarenessContext.getWeather().getConditions().length; i++){
                weatherConditions.add(mContext.getResources().getStringArray(R.array.weather_conditions)[i]);
            }

            List<String> places = new ArrayList<>();
            for(PlaceLikelihood placeLikelihood : awarenessContext.getPlaces()){
                places.add(placeLikelihood.getPlace().getName().toString());
            }

            FirebaseDatabaseHelper.getInstance().writeNewContext(
                    userId,
                    (long)awarenessContext.getDetectedActivity().getType(),
                    mAwarenessApiHelper.getActivityString(awarenessContext.getDetectedActivity().getType()),
                    new GeoLocation(awarenessContext.getLocation().getLatitude(), awarenessContext.getLocation().getLongitude()),
                    awarenessContext.getBatteryStatus().getBatteryPct(),
                    awarenessContext.getHeadphoneState().getState() == HeadphoneState.PLUGGED_IN,
                    weatherConditions,
                    places
            );

            mSharedPreferencesHelper.saveNewActivityRecognized(mCurrentActivityName);
        }
    }
}
