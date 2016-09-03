package info.einverne.guesswords.data;

import android.provider.BaseColumns;

/**
 * Created by einverne on 9/3/16.
 */

public abstract class GroupEntry implements BaseColumns {
    public static final String TABLE_NAME = "groups";
    public static final String COLUMN_NAME_GROUP_ID = "groupId";
    public static final String COLUMN_NAME_GROUP_CONTENT = "groupName";

}
