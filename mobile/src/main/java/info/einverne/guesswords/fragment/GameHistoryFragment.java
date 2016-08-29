package info.einverne.guesswords.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import info.einverne.guesswords.HistoryDataActivity;
import info.einverne.guesswords.R;
import info.einverne.guesswords.data.FirebaseDatabaseConstant;
import info.einverne.guesswords.data.HistoryData;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link GameHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameHistoryFragment extends BaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String UID = "UID";

    // TODO: Rename and change types of parameters
    private String uid;
    private RecyclerView mRecyclerView;

    public GameHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uid Parameter 1.
     * @return A new instance of fragment GameHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameHistoryFragment newInstance(String uid) {
        GameHistoryFragment fragment = new GameHistoryFragment();
        Bundle args = new Bundle();
        args.putString(UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString(UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_histroy, container, false);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewHistory);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference historyRef = database
                .getReference(FirebaseDatabaseConstant.USERS)
                .child(uid)
                .child(FirebaseDatabaseConstant.HISTORY);

        FirebaseRecyclerAdapter<HistoryData, HistoryViewHolder> adapter =
                new FirebaseRecyclerAdapter<HistoryData, HistoryViewHolder>(
                        HistoryData.class,
                        R.layout.history_list_item,
                        HistoryViewHolder.class,
                        historyRef
                ) {
                    @Override
                    protected void populateViewHolder(HistoryViewHolder viewHolder, final HistoryData data, final int position) {
                        long time = data.time;
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                        viewHolder.mTextView.setText(timeStamp);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Timber.d("Game History ");
                                Intent intent = new Intent(getContext(), HistoryDataActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelableArrayList(HistoryDataActivity.DATA_SET, data.histroyData);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                    }
                };
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.history_time);

        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
}
