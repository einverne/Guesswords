package info.einverne.guesswords.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.einverne.guesswords.R;
import info.einverne.guesswords.adapter.GroupRecyclerAdapter;
import info.einverne.guesswords.data.WordDbManager;

/**
 * Main Group choose Fragment, need wait data load in database.
 */
public class GroupFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "arg_param1";

    private WordDbManager mDbManager;
    private RecyclerView mRecyclerView;

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
        mDbManager.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_main, container, false);

        mDbManager = new WordDbManager(getContext());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        GroupRecyclerAdapter adapter = new GroupRecyclerAdapter(getContext(), mDbManager.getGroups());
        mRecyclerView.setAdapter(adapter);

        return view;
    }
}
