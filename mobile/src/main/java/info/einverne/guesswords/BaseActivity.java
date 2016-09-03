package info.einverne.guesswords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

/**
 * Created by einverne on 8/23/16.
 */
public class BaseActivity extends AppCompatActivity {
    public static final String DEVICE_RELATED = "device_related";

    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthListener;

    protected FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    Timber.d("onAuthStateChanged " + user.getUid());
                } else {
                    // user is signed out
                    Timber.d("onAuthStateChanged signed out");
                }
            }
        };

        database = FirebaseDatabase.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * 获取默认 SharedPreferences
     *
     * @return 默认
     */
    public SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * 获取设备相关
     *
     * @return 设备相关
     */
    public SharedPreferences getDeviceSharedPreferences() {
        return getSharedPreferences(DEVICE_RELATED, MODE_PRIVATE);
    }

    public void setDeviceSharedPreferences(String key, boolean value) {
        SharedPreferences.Editor editor = getDeviceSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
