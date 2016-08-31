package info.einverne.guesswords;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import info.einverne.guesswords.fragment.GameHistoryFragment;
import info.einverne.guesswords.fragment.GroupFragment;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // UI
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private View view_before_login;
    private View view_after_login;
    private ImageView imageViewAvatar;

    // fragment
    private FragmentManager fragmentManager;
    private GroupFragment groupFragment;
    private Fragment historyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();

        groupFragment = GroupFragment.newInstance();

        fragmentManager.beginTransaction()
                .add(R.id.frame_content, groupFragment)
                .commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra(GameActivity.GROUP_ID, "random");
                startActivity(intent);
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);

        view_before_login = navHeader.findViewById(R.id.nav_header_before_login);
        view_after_login = navHeader.findViewById(R.id.nav_header_after_login);

        final ProgressDialog loading = ProgressDialog.show(this, "",
                getResources().getString(R.string.data_progress_dialog_message));
        database.getReference("zh").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                loading.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loading.dismiss();
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    Timber.d("logout");
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
                }
            });
        }
        navigationView.setCheckedItem(R.id.nav_game_groups);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume");
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
        int id = item.getItemId();

        if (id == R.id.nav_game_groups) {
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_content, groupFragment)
                    .commit();

        } else if (id == R.id.nav_game_history) {
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                historyFragment = GameHistoryFragment.newInstance(mAuth.getCurrentUser().getUid());
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_content, historyFragment)
                        .commit();
            }
        } else if (id == R.id.nav_my_words) {
            Timber.d("nav my words clicked");
            Toast.makeText(MainActivity.this, "Add your own words will be available next version", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_setting) {
            Timber.d("nav setting clicked");
            startSetting();
        } else if (id == R.id.nav_about) {
            Timber.d("nav about clicked");
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSetting() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
