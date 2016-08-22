package info.einverne.guesswords.data;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by einverne on 8/21/16.
 */
public class WordsManager {

    private FirebaseDatabase database;
    private Context context;

    public WordsManager(Context c) {
        database = FirebaseDatabase.getInstance();
        context = c;
    }

    public void init() {
        database.setPersistenceEnabled(true);           // Disk Persistence enabled
        DatabaseReference databaseReference = database.getReference("message");

        databaseReference.setValue("Hello，, World");

        List<String> characters = new ArrayList<>();
        characters.add("Angel");
        characters.add("Beel");
        database.getReference("zh").child("1").setValue(characters);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Timber.d("Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Timber.w(databaseError.toException(), "Failed to read value.");
            }
        });
        createGroup("Movies", "Movie To Display");

        writeNewWord("character", "1", "oneword", "word description");
        getWords("1");
        HashMap<String, String> map = getAllGroup();
        for (String key : map.keySet()) {
            Timber.d("map key: " + key + "value: " + map.get(key));
        }

    }

    public String ReadFromfile(String fileName) {
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


    public HashMap<String, String> getAllGroup() {
        final HashMap<String, String> ret = new HashMap<>();
        DatabaseReference groupRef = database.getReference("zh").child("groups");
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    ret.put(shot.getKey(), (String) shot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return ret;
    }

    /**
     * create one group, if not exist
     *
     * @param groupId     key to group
     * @param groupDetail the name of group, may be different according to language
     */
    public void createGroup(final String groupId, final String groupDetail) {
        final DatabaseReference groupsRef = database.getReference("zh").child("groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(groupId)) {
                    Timber.d("group not exsit");
                    groupsRef.child(groupId).setValue(groupDetail);
                } else {
                    Timber.d("group exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void writeNewWord(String groupId, String wordId, String word, String wordDesc) {
        SingleWord singleWord = new SingleWord(word, wordDesc);
        database.getReference("zh").child(groupId).child(wordId).setValue(singleWord);
    }

    public List<String> getWords(String groupId) {
        final List<String> words = new ArrayList<>();
        final DatabaseReference databaseReference = database.getReference("zh").child(groupId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    words.add((String) shot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.w(databaseError.toException(), "getWords onCancelled");
            }
        });
        return words;
    }

    public void addNewItem(String groupId, String guessWord) {
    }
}
