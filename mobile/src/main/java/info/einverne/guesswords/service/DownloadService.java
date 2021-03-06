package info.einverne.guesswords.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.einverne.guesswords.BaseActivity;
import info.einverne.guesswords.data.FirebaseDownloadManager;
import info.einverne.guesswords.data.WordDbManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by einverne on 9/3/16.
 */

public class DownloadService extends IntentService {

    public static final String BROADCAST_FINISH_ACTION = "info.einverne.guesswords.BROADCAST";
    public static final String BROADCAST_ING_ACTION = "info.einverne.guesswords.DOWNLOADING";
    // if para is 0, there is no need to update, otherwise 1 is updated local
    public static final String BROADCAST_PARA = "BROADCAST_PARA";
    public static final String BROADCAST_CURRENT_DOWNLOADING = "BROADCAST_CURRENT_DOWNLOADING";

    public static final int BROADCAST_NO_NEED_UPDATE = 0;
    public static final String GROUP_IDS = "GROUP_IDS";
    public static final String VERSION = "VERSION";
    public static final String LANGUAGE = "LANGUAGE";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OkHttpClient client = new OkHttpClient();

        ArrayList<String> groupIDs = intent.getStringArrayListExtra(GROUP_IDS);
        int version = intent.getIntExtra(VERSION, 0);
        String language = intent.getStringExtra(LANGUAGE);

        for (String groupId : groupIDs) {
            Request request = new Request.Builder()
                    .url(FirebaseDownloadManager.BASE_URL + groupId + ".txt")
                    .build();
            try {
                Timber.d("begin download " + groupId + " " + System.currentTimeMillis());
                Intent ingLocal = new Intent(BROADCAST_ING_ACTION);
                ingLocal.putExtra(BROADCAST_CURRENT_DOWNLOADING, groupId);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(ingLocal);
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Timber.d(groupId + "download success " + System.currentTimeMillis());
                    String[] words = response.body().string().split("\n");
                    List<String> wordList = new ArrayList<String>(Arrays.asList(words));
                    WordDbManager manager = new WordDbManager(getBaseContext());
                    manager.clearWordsByGroupId(groupId);
                    Timber.d("start write db " + System.currentTimeMillis());
                    manager.addWords(groupId, wordList);
                    Timber.d("end write db " + System.currentTimeMillis());
                    manager.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = getSharedPreferences(BaseActivity.DEVICE_RELATED, MODE_PRIVATE).edit();
        editor.putInt("version", version);
        editor.putString("language", language);
        editor.apply();

        Intent local = new Intent(BROADCAST_FINISH_ACTION);
        local.putExtra(BROADCAST_PARA, 1);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(local);

    }
}
