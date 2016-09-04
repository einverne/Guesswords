package info.einverne.guesswords.analytics;

/**
 * Created by einverne on 9/4/16.
 *
 * https://developers.google.com/android/reference/com/google/firebase/analytics/FirebaseAnalytics.Event
 * Firebase Analytics 可以支持最多500种事件，每个事件可以支持25个参数
 * 每一个事件都需要有唯一的名字，事件名字可以有 32 个字符唱，只能包含字符和下划线，必须以字母开头
 * firebase_ 开头为系统保留字符不可以使用, 事件名称大小写敏感
 *
 * 以下为自定义的事件名称 Firebase Analytics Event
 */

public final class FAEvent {
    public static final String RANDOM_PRESSED = "random_pressed";
    public static final String GROUP_PRESSED = "group_pressed";
    public static final String NAV_AVATAR_PRESSED = "nav_avatar_pressed";
    public static final String NAV_LOG_OUT_PRESSED = "nav_log_out_pressed";
    public static final String NAV_GROUP_PRESSED = "nav_group_pressed";
    public static final String NAV_HISTORY_PRESSED = "nav_history_pressed";
    public static final String NAV_MY_WORDS_PRESSED = "nav_my_words_pressed";
    public static final String NAV_HOW_TO_PLAY_PRESSED = "nav_how_to_play_pressed";
    public static final String NAV_UPDATE_DATA_PRESSED = "nav_update_data_pressed";
    public static final String NAV_SETTING_PRESSED = "nav_setting_pressed";
    public static final String NAV_ABOUT_PRESSED = "nav_about_pressed";
    public static final String GAME_LAST_TIME = "game_last_time";
}
