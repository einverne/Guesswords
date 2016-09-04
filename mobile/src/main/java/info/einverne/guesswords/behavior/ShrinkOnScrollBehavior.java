package info.einverne.guesswords.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by einverne on 9/4/16.
 */

public class ShrinkOnScrollBehavior extends FloatingActionButton.Behavior {
    private int mTotalDyConsumed;

    public ShrinkOnScrollBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild,
                                       View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild,
                        target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout,
                               FloatingActionButton child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        mTotalDyConsumed += dyConsumed;

        if (mTotalDyConsumed > 5) {
            mTotalDyConsumed = 0;
            if (child.getVisibility() == View.VISIBLE)
                child.hide();
        } else if (mTotalDyConsumed < -5) {
            mTotalDyConsumed = 0;
            if (child.getVisibility() != View.VISIBLE)
                child.show();
        }
    }
}
