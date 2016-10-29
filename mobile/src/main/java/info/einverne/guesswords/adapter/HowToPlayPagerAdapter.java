package info.einverne.guesswords.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import info.einverne.guesswords.fragment.HowToPlayFragment;

/**
 * Created by einverne on 9/2/16.
 */
public class HowToPlayPagerAdapter extends FragmentStatePagerAdapter {

    public HowToPlayPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return HowToPlayFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
