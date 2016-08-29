package info.einverne.guesswords;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import info.einverne.guesswords.log.ReleaseTree;
import timber.log.Timber;

/**
 * Created by einverne on 8/21/16.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Timber settings
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        } else {
            Timber.plant(new ReleaseTree());
        }

        // Firebase settings
        if (!FirebaseApp.getApps(this).isEmpty()) {
            Timber.d("setPersistenceEnabled set to true");
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
