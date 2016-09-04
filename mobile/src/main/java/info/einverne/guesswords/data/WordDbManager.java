package info.einverne.guesswords.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
     * Save groups into database
     *
     * @param groupItems groupItems
     */
    public void saveGroups(ArrayList<GroupItem> groupItems) {
        db.beginTransaction();
        for (GroupItem groupItem : groupItems) {
            ContentValues values = new ContentValues();
            values.put(GroupEntry.COLUMN_NAME_GROUP_ID, groupItem.groupId);
            values.put(GroupEntry.COLUMN_NAME_GROUP_CONTENT, groupItem.groupName);
            db.insert(GroupEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * delete all rows in groups table
     */
    public void clearGroups() {
        db.delete(GroupEntry.TABLE_NAME, null, null);
    }

    /**
     * read all groups from database
     *
     * @return all groupItems
     */
    public ArrayList<GroupItem> getGroups() {
        ArrayList<GroupItem> groupItems = new ArrayList<>();
        Cursor cursor = db.query(GroupEntry.TABLE_NAME,
                new String[]{GroupEntry.COLUMN_NAME_GROUP_ID, GroupEntry.COLUMN_NAME_GROUP_CONTENT},
                null,
                null,
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndexOrThrow(GroupEntry.COLUMN_NAME_GROUP_ID);
            String groupID = cursor.getString(columnIndex);
            columnIndex = cursor.getColumnIndexOrThrow(GroupEntry.COLUMN_NAME_GROUP_CONTENT);
            String groupName = cursor.getString(columnIndex);
            groupItems.add(new GroupItem(groupID, groupName));
        }
        return groupItems;
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
            values.put(WordEntry.COLUMN_NAME_WORD_GROUP, groupName);
            values.put(WordEntry.COLUMN_NAME_WORD_CONTENT, word.wordString);
            db.insert(WordEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * add data to database
     *
     * @param groupId groupId
     * @param words   words list
     */
    public void addWords(String groupId, List<String> words) {
        Timber.d("add words to db with groupName " + groupId);
        db.beginTransaction();
        for (String word : words) {
            ContentValues values = new ContentValues();
            values.put(WordEntry.COLUMN_NAME_WORD_GROUP, groupId);
            values.put(WordEntry.COLUMN_NAME_WORD_CONTENT, word);
            db.insert(WordEntry.TABLE_NAME, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * clear words table
     */
    public void clearWords() {
        db.delete(WordEntry.TABLE_NAME, null, null);
    }

    /**
     * delete select group of words
     *
     * @param groupId groupId to delete
     */
    public void clearWordsByGroupId(String groupId) {
        db.delete(WordEntry.TABLE_NAME, WordEntry.COLUMN_NAME_WORD_GROUP + " =? ",
                new String[]{groupId});
    }

    public ArrayList<String> getRandomWordsByGroupId(String groupId, int count) {
        if (groupId == null || count <= 0) return null;
        if (groupId.equals("random")) {
            return getRandomWords(100);
        }
        ArrayList<String> wordsList = new ArrayList<>();
        String orderBy = "RANDOM()";
        Cursor cursor = db.query(WordEntry.TABLE_NAME,
                new String[]{WordEntry.COLUMN_NAME_WORD_CONTENT},
                WordEntry.COLUMN_NAME_WORD_GROUP + " = ?",
                new String[]{groupId},
                null,
                null,
                orderBy,
                Integer.toString(count));
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD_CONTENT);
            String word = cursor.getString(columnIndex);
            wordsList.add(word);
        }
        return wordsList;
    }

    public ArrayList<String> getRandomWords(int count) {
        if (count <= 0) return null;
        ArrayList<String> wordsList = new ArrayList<>();
        String orderBy = "RANDOM()";
        Cursor cursor = db.query(WordEntry.TABLE_NAME,
                new String[]{WordEntry.COLUMN_NAME_WORD_CONTENT},
                null,
                null,
                null,
                null,
                orderBy,
                Integer.toString(count));
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndexOrThrow(WordEntry.COLUMN_NAME_WORD_CONTENT);
            String word = cursor.getString(columnIndex);
            wordsList.add(word);
        }
        return wordsList;
    }

    public void close() {
        db.close();
    }
}
