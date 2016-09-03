package info.einverne.guesswords.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by einverne on 9/2/16.
 */
public class WordDbManager {
    private WordDbHelper dbHelper;
    private SQLiteDatabase db;

    public WordDbManager(Context context) {
        dbHelper = new WordDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Add words to db
     *
     * @param words words list
     */
    public void addSingleWords(String groupName, List<SingleWord> words) {
        db.beginTransaction();
        for (SingleWord word : words) {
            ContentValues values = new ContentValues();
            values.put(GroupEntry.COLUMN_NAME_WORD_GROUP, groupName);
            values.put(GroupEntry.COLUMN_NAME_WORD_CONTENT, word.wordString);
            db.insert(GroupEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addWords(String groupName, List<String> words) {
        db.beginTransaction();
        for (String word : words) {
            ContentValues values = new ContentValues();
            values.put(GroupEntry.COLUMN_NAME_WORD_GROUP, groupName);
            values.put(GroupEntry.COLUMN_NAME_WORD_CONTENT, word);
            db.insert(GroupEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public ArrayList<String> getRandomWordsByGroupName(String groupName, int count) {
        if (groupName == null || count <= 0) return null;
        ArrayList<String> wordsList = new ArrayList<>();
        String orderBy = "RANDOM()";
        Cursor cursor = db.query(GroupEntry.TABLE_NAME,
                new String[]{GroupEntry.COLUMN_NAME_WORD_CONTENT},
                GroupEntry.COLUMN_NAME_WORD_GROUP + " = ?",
                new String[]{groupName},
                null,
                null,
                orderBy,
                Integer.toString(count));
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndexOrThrow(GroupEntry.COLUMN_NAME_WORD_CONTENT);
            String word = cursor.getString(columnIndex);
            wordsList.add(word);
        }
        return wordsList;
    }

    public ArrayList<String> getRandomWords(int count) {
        if (count <= 0) return null;
        ArrayList<String> wordsList = new ArrayList<>();
        String orderBy = "RANDOM()";
        Cursor cursor = db.query(GroupEntry.TABLE_NAME,
                new String[]{GroupEntry.COLUMN_NAME_WORD_CONTENT},
                null,
                null,
                null,
                null,
                orderBy,
                Integer.toString(count));
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndexOrThrow(GroupEntry.COLUMN_NAME_WORD_CONTENT);
            String word = cursor.getString(columnIndex);
            wordsList.add(word);
        }
        return wordsList;
    }

    public void close() {
        db.close();
    }
}
