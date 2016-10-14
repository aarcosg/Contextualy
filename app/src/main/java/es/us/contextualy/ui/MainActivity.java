package es.us.contextualy.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

import es.us.contextualy.R;
import es.us.contextualy.data.AwarenessApiHelper;
import es.us.contextualy.data.FirebaseRemoteConfigHelper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private Context mContext;
    private Snackbar mPermissionsSnackbar;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setupFirebase();
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text1);
        MainActivityPermissionsDispatcher.setupAwarenessFencesWithCheck(this);
    }

    private void setupFirebase() {
        FirebaseApp.initializeApp(this);
        //Fetch remote config
        FirebaseRemoteConfigHelper.getInstance().fetchRemoteConfig();
    }

    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION})
    public void setupAwarenessFences() {
        Log.i(TAG,"@setupAwarenessFences");
        AwarenessApiHelper.getInstance(mContext).updateMainAwarenessFence(null);
    }

    @OnShowRationale({android.Manifest.permission.ACCESS_FINE_LOCATION})
    protected void showRationaleForAppPermissions(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_permissions_required)
                .setMessage(R.string.app_permissions_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> request.proceed())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> request.cancel())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({android.Manifest.permission.ACCESS_FINE_LOCATION})
    public void onAppPermissionsDenied() {
        mPermissionsSnackbar = Snackbar.make(mTextView.getRootView(),
                getString(R.string.app_permissions_required),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), v -> MainActivityPermissionsDispatcher.setupAwarenessFencesWithCheck(MainActivity.this));
        mPermissionsSnackbar.show();
    }

    @OnNeverAskAgain({android.Manifest.permission.ACCESS_FINE_LOCATION})
    protected void onNeverAskAppPermissions() {
        mPermissionsSnackbar = Snackbar.make(mTextView.getRootView(),
                getString(R.string.app_permissions_required),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                    }
                });
        mPermissionsSnackbar.show();
    }
}
