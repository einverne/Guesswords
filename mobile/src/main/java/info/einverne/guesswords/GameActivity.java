package info.einverne.guesswords;

import android.app.ProgressDialog;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import info.einverne.guesswords.data.HistoryData;
import info.einverne.guesswords.data.SingleData;
import info.einverne.guesswords.data.SingleWord;
import info.einverne.guesswords.detector.ScreenFaceDetector;
import timber.log.Timber;

public class GameActivity extends BaseActivity implements ScreenFaceDetector.Listener {
    public static final String GROUP_ID = "GROUP_ID";
    private static final String STATE_INDEX = "STATE_INDEX";
    private SensorManager sensorManager;
    private ScreenFaceDetector screenFaceDetector;

    private boolean isReady = false;
    private boolean isGameOver = false;
    private TextView tv_guessing_word;
    private TextView tv_game_left_time;
    private TextView tv_replay;

    private Timer timerPrepare;
    private int nPrepareTime;

    private Timer timerCountDown;
    private int nLeftTime;

    private String groupId;
    private FirebaseDatabase database;
    List<SingleWord> words = new ArrayList<>();
    List<SingleWord> randomWords = new ArrayList<>();
    private int index = 0;

    private ArrayList<SingleData> gameRecord = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        database = FirebaseDatabase.getInstance();

        groupId = getIntent().getStringExtra(GROUP_ID);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSensor();
        initUI();

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(STATE_INDEX);
        }
    }

    public void getWords() {
        Timber.d("groupId " + groupId);
        final ProgressDialog loading = ProgressDialog.show(this, "", "loading");
        final DatabaseReference databaseReference = database.getReference("zh").child(groupId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("GameActivity data change");
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    words.add(shot.getValue(SingleWord.class));
                }
                for (int i = 0; i < 90; i++) {
                    randomWords.add(words.get(new Random().nextInt(words.size())));
                }
                loading.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.w(databaseError.toException(), "getWords onCancelled");
            }
        });
    }

    private void initUI() {
        tv_guessing_word = (TextView) findViewById(R.id.tv_guessing_word);
        tv_game_left_time = (TextView) findViewById(R.id.tv_game_left_time);
        tv_game_left_time.setText("");
        tv_replay = (TextView) findViewById(R.id.tv_replay);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startGame();
    }

    private void startGame() {
        isReady = false;
        isGameOver = false;
        index = 0;
        nPrepareTime = 2;
        nLeftTime = 10;
        tv_replay.setVisibility(View.GONE);
        getWords();
        startTimerPrepare();
    }

    private void startTimerPrepare() {
        timerPrepare = new Timer();
        TimerTask timerTaskPrepare = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_guessing_word.setText(nPrepareTime + "");
                        nPrepareTime--;
                        if (nPrepareTime < 0) {
                            timerPrepare.cancel();
                            isReady = true;
                            startCountDown();
                            if (randomWords.size() <= 0) return;
                            tv_guessing_word.setText(randomWords.get(index).wordString);
                        }
                    }
                });
            }
        };
        timerPrepare.schedule(timerTaskPrepare, 0, 1000);
    }

    private void startCountDown() {
        timerCountDown = new Timer();
        TimerTask timerTaskCountDown = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_game_left_time.setText(getResources().getString(R.string.game_left_time).replace("%1", Integer.toString(nLeftTime)));
                        nLeftTime--;
                        if (nLeftTime < 0) {
                            timerCountDown.cancel();
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            gameOver();
                        }
                    }
                });
            }
        };
        timerCountDown.schedule(timerTaskCountDown, 0, 1000);
    }

    private void gameOver() {
        isGameOver = true;
        tv_guessing_word.setText(getString(R.string.game_over));
        tv_replay.setVisibility(View.VISIBLE);
        tv_replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null ){
            long currentTime = System.currentTimeMillis();
            HistoryData historyData = new HistoryData(currentTime, gameRecord);
            database.getReference("users").child(user.getUid()).child("history").child(Long.toString(currentTime)).setValue(historyData);
        } else {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenFaceDetector.stop();
        if (timerPrepare != null) {
            timerPrepare.cancel();
        }
        if (timerCountDown != null) {
            timerCountDown.cancel();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_INDEX, index);

    }

    @Override
    public void FaceUp() {
        Timber.d("FaceUp");
        if (!isReady || isGameOver) return;
        if (index >= randomWords.size()) return;
        gameRecord.add(new SingleData(randomWords.get(index).wordString, false));
        index++;
        tv_guessing_word.setText(randomWords.get(index).wordString);
    }

    @Override
    public void FaceDown() {
        Timber.d("FaceDown");
        if (!isReady || isGameOver) return;
        if (index >= randomWords.size()) return;
        gameRecord.add(new SingleData(randomWords.get(index).wordString, true));
        index++;
        tv_guessing_word.setText(randomWords.get(index).wordString);
    }

    private void initSensor() {
        if (screenFaceDetector == null) {
            screenFaceDetector = new ScreenFaceDetector(this);
        }
        screenFaceDetector.start(sensorManager);
    }
}
