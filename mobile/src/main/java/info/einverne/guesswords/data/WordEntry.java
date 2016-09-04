package info.einverne.guesswords.data;

import android.provider.BaseColumns;

/**
 * Created by einverne on 9/2/16.
 */
public abstract class WordEntry implements BaseColumns {
    public static final String TABLE_NAME = "words";
    public static final String COLUMN_NAME_WORD_GROUP = "groupid";
    public static final String COLUMN_NAME_WORD_CONTENT = "word";
}
