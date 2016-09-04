package info.einverne.guesswords.data;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by einverne on 8/21/16.
 */
public class WordsManager {

    public interface QueryFinishedListener {
        void onSuccess(Object object);

        void onFailed(DatabaseError error);
    }

    public void init(final FirebaseDatabase database) {
        Timber.d("Words Manager init() ");
        final DatabaseReference databaseReference = database.getReference("zh").child("characters");

        final ArrayList<SingleWord> wordsList = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                wordsList.addAll((ArrayList<SingleWord>) dataSnapshot.getValue());
//                addWordsToRandom(wordsList);
                database.getReference("zh").child("random").setValue(wordsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Timber.w(databaseError.toException(), "Failed to read value.");
            }
        });

//
//        writeNewWord("character", "1", "oneword", "word description");
//
//        List<GroupItem> list = getAllGroup();
//        for (GroupItem oneGroup : list) {
//            Timber.d("map key: " + oneGroup.groupId + "value: " + oneGroup.groupName);
//        }

        final HashMap<String, SingleWord> mapWords = new HashMap<>();
        database.getReference("zh").child("newsongs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    SingleWord word = data.getValue(SingleWord.class);
                    mapWords.put(word.wordString, word);
                }

                for (String word : mapWords.keySet()) {
                    String key = database.getReference("zh").child("random").push().getKey();
                    database.getReference("zh").child("random").child(key).setValue(mapWords.get(word));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private ArrayList<SingleWord> randomList;

    private void addWordsToRandom(FirebaseDatabase database, final ArrayList<SingleWord> toAddWords) {
        final DatabaseReference databaseReference = database.getReference("zh").child("random");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                randomList = ((ArrayList<SingleWord>) dataSnapshot.getValue());
                for (int i = 0; i < toAddWords.size(); i++) {
                    if (!randomList.contains(toAddWords.get(i))) {
                        randomList.add(toAddWords.get(i));
                    }
                }
                databaseReference.setValue(randomList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void getWordsByGroupId(FirebaseDatabase database, String groupId, final QueryFinishedListener listener) {
        final ArrayList<SingleWord> words = new ArrayList<SingleWord>();
        final DatabaseReference databaseReference = database.getReference("zh").child(groupId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("GameActivity data change");
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    words.add(shot.getValue(SingleWord.class));
                }
                listener.onSuccess(words);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.w(databaseError.toException(), "getWords onCancelled");
                listener.onFailed(databaseError);
            }
        });

    }

    public static void readAssets(Context context, FirebaseDatabase database) {
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        try {
            inputStream = context.getResources().getAssets().open("computer.txt", Context.MODE_WORLD_READABLE);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                writeNewWord(database, "computer", line, "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String ReadFromfile(Context context, String fileName) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }

    /**
     * create one group, if not exist
     *
     * @param groupId     key to group, use this key to retrieve group words
     * @param groupDetail the name of group, may be different according to language, show in UI
     */
    public static void createGroup(FirebaseDatabase database,
                                   final String groupId,
                                   final String groupDetail,
                                   final QueryFinishedListener listener) {
        final DatabaseReference groupsRef = database.getReference("zh").child("groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> listGroup = new ArrayList<String>();
                GroupItem group = new GroupItem(groupId, groupDetail);
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    GroupItem oneGroup = data.getValue(GroupItem.class);
                    listGroup.add(oneGroup.groupId);
                }
                if (!listGroup.contains(groupId)) {
                    Timber.d("group not exsit");
                    String groupKey = groupsRef.push().getKey();
                    groupsRef.child(groupKey).setValue(group);
                } else {
                    Timber.d("group exist");
                }
                listener.onSuccess(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }

    /**
     * add new words to group
     *
     * @param database Firebase instance
     * @param groupId  groupId
     * @param word     word content
     * @param wordDesc word desc,
     */
    public static void writeNewWord(FirebaseDatabase database, String groupId, String word, String wordDesc) {
        SingleWord singleWord = new SingleWord(word, wordDesc);
        String wordKey = database.getReference("zh").child(groupId).push().getKey();
        database.getReference("zh").child(groupId).child(wordKey).setValue(singleWord);
    }

}
