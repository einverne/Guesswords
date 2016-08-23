package info.einverne.guesswords.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;

import info.einverne.guesswords.GameActivity;
import info.einverne.guesswords.MainActivity;
import info.einverne.guesswords.R;
import info.einverne.guesswords.data.Group;
import info.einverne.guesswords.data.WordsManager;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {
    private static final String ARG_PARAM1 = "arg_param1";

    private Context context;
    private RecyclerView mRecyclerView;
    private FirebaseDatabase database;

    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_bar_main, container, false);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        database = FirebaseDatabase.getInstance();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        FirebaseRecyclerAdapter<Group, GroupViewHolder> adapter =
                new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                        Group.class,
                        R.layout.list_item,
                        GroupViewHolder.class,
                        database.getReference("zh").child("groups")
                ) {
                    @Override
                    protected void populateViewHolder(GroupViewHolder viewHolder, final Group oneGroup, final int position) {
                        viewHolder.mTextView.setText(oneGroup.groupName);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Timber.d("GroupViewHolder " + oneGroup.groupId + " " + oneGroup.groupName);

                                startGame(oneGroup.groupId);
                            }
                        });
                    }
                };
        mRecyclerView.setAdapter(adapter);


        return view;
    }


    private void startGame(String groupId) {
        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra(GameActivity.GROUP_ID, groupId);
        startActivity(intent);
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public GroupViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.groupName);

        }
    }
}
