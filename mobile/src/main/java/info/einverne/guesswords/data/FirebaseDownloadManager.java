package info.einverne.guesswords.data;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by einverne on 9/2/16.
 *
 * This class use to manager download from web server
 */
public class FirebaseDownloadManager {

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

    public void initGroups(final FirebaseDownloadListener listener) {
        Request request = new Request.Builder()
                .url("https://evguesswords.firebaseapp.com/groups.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
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
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String language = jsonObject.getString("language");
                        int version = jsonObject.getInt("version");
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (listener != null) {
                        listener.onFinished(null);
                    }
                } else {
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }
        });
    }

    public void initDb(final Context context, String url, final FirebaseDownloadListener listener) {
        client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("https://evguesswords.firebaseapp.com/sanguosha.txt")
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
