package info.einverne.guesswords.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.einverne.guesswords.BaseActivity;
import info.einverne.guesswords.service.DownloadService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by einverne on 9/2/16.
 * <p>
 * This class use to manager download from web server
 */
public class FirebaseDownloadManager {

    public static final String BASE_URL = "https://evguesswords.firebaseapp.com/";

    private OkHttpClient client;
    private Context context;

    public FirebaseDownloadManager(Context c) {
        client = new OkHttpClient();
        context = c;
    }

    public interface FirebaseDownloadListener {
        void onFinished(Object object);

        void onFailed();
    }

    /**
     * download groups json file and update all groups of words
     *
     * @param listener call back when down load groups json file success or failed
     */
    public void initAll(final FirebaseDownloadListener listener) {
        Request request = new Request.Builder()
                .url(BASE_URL + "groups.json")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailed();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    ArrayList<GroupItem> groupItemsList = new ArrayList<GroupItem>();
                    String language = "";
                    int version = 0;
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        SharedPreferences sharedPreferences = context.getSharedPreferences(BaseActivity.DEVICE_RELATED, Context.MODE_PRIVATE);
                        language = jsonObject.getString("language");
                        version = jsonObject.getInt("version");
                        if (sharedPreferences.getInt("version", 0) >= version) {
                            if (listener != null) {
                                listener.onFinished(null);
                            }
                            Intent local = new Intent(DownloadService.BROADCAST_FINISH_ACTION);
                            local.putExtra(DownloadService.BROADCAST_PARA, DownloadService.BROADCAST_NO_NEED_UPDATE);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(local);
                            return;
                        }

                        JSONArray groups = jsonObject.getJSONArray("groups");
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject oneGroup = groups.getJSONObject(i);
                            String groupId = oneGroup.getString("groupId");
                            String groupName = oneGroup.getString("groupName");
                            GroupItem groupItem = new GroupItem(groupId, groupName);
                            groupItemsList.add(groupItem);
                        }
                        WordDbManager manager = new WordDbManager(context);
                        manager.clearGroups();
                        manager.saveGroups(groupItemsList);
                        manager.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ArrayList<String> groupIds = new ArrayList<>();
                    for (GroupItem item : groupItemsList) {

                        groupIds.add(item.groupId);
                    }
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.putStringArrayListExtra(DownloadService.GROUP_IDS, groupIds);
                    intent.putExtra(DownloadService.VERSION, version);
                    intent.putExtra(DownloadService.LANGUAGE, language);
                    context.startService(intent);
                } else {
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }
        };

        client.newCall(request).enqueue(callback);
    }

    public void initDb(final Context context, String url, final FirebaseDownloadListener listener) {
        final Request request = new Request.Builder()
                .url(BASE_URL + "sanguosha.txt")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onFailed();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String[] words = response.body().string().split("\n");
                    List<String> wordList = new ArrayList<String>(Arrays.asList(words));
                    WordDbManager manager = new WordDbManager(context);
                    manager.addWords("sanguosha", wordList);
                    if (listener != null) {
                        listener.onFinished(null);
                    }
                } else {
                    Toast.makeText(context, "Error Response code " + response.code(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }
        });
    }
}
