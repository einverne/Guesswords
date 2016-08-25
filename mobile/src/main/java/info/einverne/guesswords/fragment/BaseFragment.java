package info.einverne.guesswords.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by einverne on 8/25/16.
 */
public abstract class BaseFragment extends Fragment{

    protected FirebaseDatabase database;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
    }
}
