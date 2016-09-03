package info.einverne.guesswords.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by einverne on 9/2/16.
 */
public class WordDbHelper extends SQLiteOpenHelper {


    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_WORDS_ENTRIES =
            "CREATE TABLE " + WordEntry.TABLE_NAME + " (" +
                    WordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    WordEntry.COLUMN_NAME_WORD_GROUP + TEXT_TYPE + COMMA_SEP +
                    WordEntry.COLUMN_NAME_WORD_CONTENT + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_GROUPS_ENTRIES =
            "CREATE TABLE " + GroupEntry.TABLE_NAME + " (" +
                    GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GroupEntry.COLUMN_NAME_GROUP_ID + TEXT_TYPE + COMMA_SEP +
                    GroupEntry.COLUMN_NAME_GROUP_CONTENT + TEXT_TYPE + " )";

    private static final String SQL_DELETE_WORDS_ENTRIES =
            "DROP TABLE IF EXISTS " + WordEntry.TABLE_NAME;

    private static final String SQL_DELETE_GROUPS_ENTRIES =
            "DROP TABLE IF EXISTS " + GroupEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "words.db";

    public WordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_WORDS_ENTRIES);
        db.execSQL(SQL_CREATE_GROUPS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // if DATABASE_VERSION changed onUpgrade will be called
        db.execSQL(SQL_DELETE_WORDS_ENTRIES);
        db.execSQL(SQL_DELETE_GROUPS_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
