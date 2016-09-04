package info.einverne.guesswords;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import info.einverne.guesswords.adapter.HowToPlayPagerAdapter;

public class HowToPlayActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private ViewPager mPager;
    private LinearLayout mDotsLayout;
    private PagerAdapter mPagerAdapter;
    private ImageView[] mDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        mPager = (ViewPager) findViewById(R.id.how_to_play_pager);
        mDotsLayout = (LinearLayout) findViewById(R.id.how_to_play_dots);
        mPagerAdapter = new HowToPlayPagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(this);
        setDots();
    }

    private void setDots() {
        mDots = new ImageView[mPagerAdapter.getCount()];

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new ImageView(this);
            setSelected(i, i == 0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            mDotsLayout.addView(mDots[i], params);
        }
    }

    private void setSelected(int dotIndex, boolean selected) {
        mDots[dotIndex].setImageDrawable(ContextCompat.getDrawable(this,
                selected ? R.drawable.shape_dot_selected : R.drawable.shape_dot_unselected));
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < mDots.length; ++i) {
            setSelected(i, i == position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
