package info.einverne.guesswords;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Random;

import info.einverne.guesswords.analytics.FAEvent;
import info.einverne.guesswords.analytics.FAParam;
import info.einverne.guesswords.data.FirebaseDownloadManager;
import info.einverne.guesswords.fragment.GameHistoryFragment;
import info.einverne.guesswords.fragment.GroupFragment;
import info.einverne.guesswords.service.DownloadService;
import info.einverne.guesswords.utils.Utils;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String IS_FIRST_TIME_INIT = "isFirstTimeInit";
    // UI
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private View view_before_login;
    private View view_after_login;
    private ImageView imageViewAvatar;
    private ProgressDialog progressDialog;

    private AppBarLayout appBarLayout;
    private TextView title;
    private FrameLayout frameLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView app_bar_main_image;
    private LinearLayout nav_header;

    // fragment
    private FragmentManager fragmentManager;
    private GroupFragment groupFragment;
    private Fragment historyFragment;

    //data
    private FirebaseDownloadManager firebaseDownloadManager;
    private DownloadFinishReceiver downloadReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        initViews();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra(GameActivity.GROUP_ID, "random");
                startActivity(intent);
                firebaseAnalytics.logEvent(FAEvent.RANDOM_PRESSED, null);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);

        view_before_login = navHeader.findViewById(R.id.nav_header_before_login);
        view_after_login = navHeader.findViewById(R.id.nav_header_after_login);


        // first time download all words from server
        boolean isFirstTimeInit = getDeviceSharedPreferences().getBoolean(IS_FIRST_TIME_INIT, true);
        if (isFirstTimeInit) {
            updateDatabase();
            setDeviceSharedPreferences(IS_FIRST_TIME_INIT, false);
        } else {
            groupFragment = GroupFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.frame_content, groupFragment)
                    .commit();
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            public static final int ALPHA_ANIMATIONS_DURATION = 250;
            public boolean mIsTheTitleContainerVisible = false;
            public boolean mIsTheTitleVisible = false;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
                // 完全展开为0.0 收拢时为 1.0
                Timber.d("percentage " + percentage);

                handleAlphaonTitle(percentage);
                handleAlphaOnContainer(percentage);
            }

            private void handleAlphaonTitle(float percentage) {
                if (percentage >= 0.7f) {
                    if (!mIsTheTitleVisible) {
                        startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
//                        startBackgroundAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                        mIsTheTitleVisible = true;
                    }
                } else {
                    if (mIsTheTitleVisible) {
                        startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
//                        startBackgroundAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                        mIsTheTitleVisible = false;
                    }
                }
            }

            private void handleAlphaOnContainer(float percentage) {
                if (percentage >= 0.3f) {
                    if (mIsTheTitleContainerVisible) {
                        startAlphaAnimation(frameLayout, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                        mIsTheTitleContainerVisible = false;
                    }
                } else {
                    if (!mIsTheTitleContainerVisible) {
                        startAlphaAnimation(frameLayout, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                        mIsTheTitleContainerVisible = true;
                    }
                }
            }

            private void startAlphaAnimation(View v, long duration, int visibility) {
                AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                        ? new AlphaAnimation(0f, 1f)
                        : new AlphaAnimation(1f, 0f);

                alphaAnimation.setDuration(duration);
                alphaAnimation.setFillAfter(true);
                v.startAnimation(alphaAnimation);
            }
        });

        collapsingToolbarLayout.setTitle(" ");
        Picasso.with(this)
                .load("https://evguesswords.firebaseapp.com/app_bar_main_image.jpg")
                .resize((int) Utils.convertDpToPixel(600f, this), (int) Utils.convertDpToPixel(500f, this))
                .centerCrop()
                .into(app_bar_main_image);
    }

    private void initViews() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbarLayout);
        title = (TextView) findViewById(R.id.title);
        frameLayout = (FrameLayout) findViewById(R.id.framelayout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        app_bar_main_image = (ImageView) findViewById(R.id.app_bar_main_image);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("MainActivity onStart");
        updateSignUI();
    }

    private void updateSignUI() {
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("Clicked on nav header");
            }
        });
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            view_before_login.setVisibility(View.GONE);
            view_after_login.setVisibility(View.VISIBLE);
            Button btnLogout = (Button) navHeader.findViewById(R.id.nv_header_log_out);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseAnalytics.logEvent(FAEvent.NAV_LOG_OUT_PRESSED, null);
                    if (user.isAnonymous()) {
                        mAuth.signOut();
                    } else {
                        FirebaseAuth.getInstance().signOut();
                    }
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });
            imageViewAvatar = (ImageView) navHeader.findViewById(R.id.imageViewAvatar);
            imageViewAvatar.setOnClickListener(null);
            if (user.getPhotoUrl() != null) {
                Picasso.with(this).load(user.getPhotoUrl()).into(imageViewAvatar);
            }
            TextView user_name = (TextView) navHeader.findViewById(R.id.user_name);
            TextView user_email = (TextView) navHeader.findViewById(R.id.user_email);
            Timber.d("user isAnonymous " + user.isAnonymous());
            if (user.isAnonymous()) {
                user_name.setText("Anonymous");
                user_email.setText("username@gmail.com");
            } else {
                user_name.setText(user.getDisplayName());
                user_email.setText(user.getEmail());
            }
        } else {
            view_before_login.setVisibility(View.VISIBLE);
            view_after_login.setVisibility(View.GONE);
            imageViewAvatar = (ImageView) navHeader.findViewById(R.id.imageViewAvatar);
            imageViewAvatar.setImageResource(R.drawable.default_avater);
            imageViewAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    firebaseAnalytics.logEvent(FAEvent.NAV_AVATAR_PRESSED, null);
                }
            });
        }
        navigationView.setCheckedItem(R.id.nav_game_groups);
        nav_header = (LinearLayout) navHeader.findViewById(R.id.nav_header);
        int bgStyle = new Random().nextInt(2);
        if (bgStyle == 0) {
            nav_header.setBackgroundResource(R.drawable.nav_header_bg1);
        } else {
            nav_header.setBackgroundResource(R.drawable.nav_header_bg2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (downloadReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startSetting();
                break;
//            case R.id.action_add_group:
//                WordsManager.createGroup(database, "groupId", "groupName", new WordsManager.QueryFinishedListener() {
//                    @Override
//                    public void onSuccess(Object object) {
//                        Timber.d("create group success");
//                    }
//
//                    @Override
//                    public void onFailed(DatabaseError error) {
//
//                    }
//                });
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_game_groups:
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_content, groupFragment)
                        .commit();
                firebaseAnalytics.logEvent(FAEvent.NAV_GROUP_PRESSED, null);
                break;
            case R.id.nav_game_history:
                if (mAuth.getCurrentUser() == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                    Bundle param = new Bundle();
                    param.putBoolean(FAParam.HISTORY_PRESSED_IS_LOGIN, false);
                    firebaseAnalytics.logEvent(FAEvent.NAV_HISTORY_PRESSED, param);
                } else {
                    historyFragment = GameHistoryFragment.newInstance(mAuth.getCurrentUser().getUid());
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_content, historyFragment)
                            .commit();
                    Bundle param = new Bundle();
                    param.putBoolean(FAParam.HISTORY_PRESSED_IS_LOGIN, true);
                    firebaseAnalytics.logEvent(FAEvent.NAV_HISTORY_PRESSED, param);
                }
                break;
            case R.id.nav_my_words:
                Timber.d("nav my words clicked");
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    Bundle param = new Bundle();
                    param.putBoolean(FAParam.MY_WORDS_PRESSED_IS_LOGIN, false);
                    firebaseAnalytics.logEvent(FAEvent.NAV_MY_WORDS_PRESSED, param);
                } else {
                    startActivity(new Intent(MainActivity.this, MyWordsActivity.class));
                    Bundle param = new Bundle();
                    param.putBoolean(FAParam.MY_WORDS_PRESSED_IS_LOGIN, true);
                    firebaseAnalytics.logEvent(FAEvent.NAV_MY_WORDS_PRESSED, param);
                }
                break;
            case R.id.nav_how_to_play:
                Timber.d("nav how to play");
                startActivity(new Intent(MainActivity.this, HowToPlayActivity.class));
                firebaseAnalytics.logEvent(FAEvent.NAV_HOW_TO_PLAY_PRESSED, null);
                break;
            case R.id.nav_update_db:
                updateDatabase();
                navigationView.setCheckedItem(R.id.group_main);
                firebaseAnalytics.logEvent(FAEvent.NAV_UPDATE_DATA_PRESSED, null);
                break;
            case R.id.nav_setting:
                Timber.d("nav setting clicked");
                startSetting();
                firebaseAnalytics.logEvent(FAEvent.NAV_SETTING_PRESSED, null);
                break;
            case R.id.nav_about:
                Timber.d("nav about clicked");
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                firebaseAnalytics.logEvent(FAEvent.NAV_ABOUT_PRESSED, null);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSetting() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void updateDatabase() {
        progressDialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.data_progress_dialog_message));
        IntentFilter intentFilter = new IntentFilter(DownloadService.BROADCAST_FINISH_ACTION);
        intentFilter.addAction(DownloadService.BROADCAST_ING_ACTION);
        downloadReceiver = new DownloadFinishReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, intentFilter);

        firebaseDownloadManager = new FirebaseDownloadManager(this);
        firebaseDownloadManager.initAll(new FirebaseDownloadManager.FirebaseDownloadListener() {
            @Override
            public void onFinished(Object object) {

            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class DownloadFinishReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DownloadService.BROADCAST_ING_ACTION:
                    Timber.d("");
                    String groupId = intent.getStringExtra(DownloadService.BROADCAST_CURRENT_DOWNLOADING);
                    progressDialog.setMessage("Downloading " + groupId);
                    break;
                case DownloadService.BROADCAST_FINISH_ACTION:
                    Timber.d("receive broadcast");
                    if (intent.getIntExtra(DownloadService.BROADCAST_PARA, 0) == DownloadService.BROADCAST_NO_NEED_UPDATE) {
                        Toast.makeText(context, "already update, no need to update", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                    groupFragment = GroupFragment.newInstance();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_content, groupFragment)
                            .commit();

                    LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(downloadReceiver);
                    break;
            }
        }
    }

}
