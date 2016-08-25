package info.einverne.guesswords.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by einverne on 8/25/16.
 */
public class HistoryData {
    public long time;
    public ArrayList<SingleData> histroyData;

    public HistoryData() {
    }

    public HistoryData(long time, ArrayList<SingleData> histroyData) {
        this.time = time;
        this.histroyData = histroyData;
    }
}
