package info.einverne.guesswords.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by einverne on 8/25/16.
 */
public class SingleData implements Parcelable{
    public String word;
    public boolean isRight;

    public SingleData() {
    }

    public SingleData(String word, boolean isRight) {
        this.word = word;
        this.isRight = isRight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(word);
        parcel.writeByte((byte) (isRight ? 1 : 0));
    }

    public static final Parcelable.Creator<SingleData> CREATOR
            = new Parcelable.Creator<SingleData>() {

        @Override
        public SingleData createFromParcel(Parcel parcel) {
            return new SingleData(parcel);
        }

        @Override
        public SingleData[] newArray(int i) {
            return new SingleData[0];
        }
    };

    private SingleData(Parcel in) {
        word = in.readString();
        isRight = in.readByte() != 0;
    }
}
