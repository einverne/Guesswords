package info.einverne.guesswords.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import info.einverne.guesswords.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HowToPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HowToPlayFragment extends Fragment {

    private static final String ARG_PAGER_INDEX = "ARG_PAGER_INDEX";

    private int mPagerIndex;
    private ImageView mImage;

    public HowToPlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param index index of fragment in pager
     * @return A new instance of fragment HowToPlayFragment.
     */
    public static HowToPlayFragment newInstance(int index) {
        HowToPlayFragment fragment = new HowToPlayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGER_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPagerIndex = getArguments().getInt(ARG_PAGER_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_how_to_play, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setUpContent();
    }

    private void setUpContent() {
        switch (mPagerIndex) {
            case 0:
                setImage(R.drawable.ic_media_play);
                break;
            case 1:
                setImage(R.drawable.ic_media_play);
                break;
            case 2:
                setImage(R.drawable.ic_media_play);
                break;
            case 3:
                setImage(R.drawable.ic_media_play);
                break;
        }
    }

    private void setImage(int drawableId) {
        mImage.setImageResource(drawableId);
    }

    private void initView(View view) {
        mImage = (ImageView) view.findViewById(R.id.how_to_play_fragment_iv);
    }


}
