package info.einverne.guesswords;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import info.einverne.guesswords.data.HistoryData;
import info.einverne.guesswords.data.SingleData;
import info.einverne.guesswords.data.WordDbManager;
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
    private int index = 0;

    private ArrayList<SingleData> gameRecord = new ArrayList<>();

    private WordDbManager wordDbManager;
    private ArrayList<String> randomWordsFromDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        wordDbManager = new WordDbManager(this);

        groupId = getIntent().getStringExtra(GROUP_ID);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSensor();
        initUI();
        getRandomWords();

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(STATE_INDEX);
        }
    }

    private void getRandomWords() {
        randomWordsFromDb = wordDbManager.getRandomWordsByGroupId(groupId, 100);
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
        nPrepareTime = 4;
        nLeftTime = Integer.parseInt(getDefaultSharedPreferences().getString(SettingsActivity.GAME_DURATION, "90"));
        tv_replay.setVisibility(View.GONE);
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
                            if (randomWordsFromDb.size() <= 0) return;
                            tv_guessing_word.setText(randomWordsFromDb.get(index));
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
        if (user != null && !gameRecord.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            HistoryData historyData = new HistoryData(currentTime, gameRecord);
            database.getReference("users").child(user.getUid()).child("history").child(Long.toString(currentTime)).setValue(historyData);
        } else {
            Toast.makeText(GameActivity.this, "Login to save histroy", Toast.LENGTH_SHORT).show();
        }
        getRandomWords();
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
        wordDbManager.close();
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
        Timber.d("FaceUp " + index);
        if (!isReady || isGameOver) return;
        index++;
        if (index >= randomWordsFromDb.size()){
            tv_guessing_word.setText(getString(R.string.game_no_more_words));
            return;
        }
        gameRecord.add(new SingleData(randomWordsFromDb.get(index), false));
        tv_guessing_word.setText(randomWordsFromDb.get(index));
    }

    @Override
    public void FaceDown() {
        Timber.d("FaceDown " + index);
        if (!isReady || isGameOver) return;
        index++;
        if (index >= randomWordsFromDb.size()){
            tv_guessing_word.setText(getString(R.string.game_no_more_words));
            return;
        }
        gameRecord.add(new SingleData(randomWordsFromDb.get(index), true));
        tv_guessing_word.setText(randomWordsFromDb.get(index));
    }

    private void initSensor() {
        if (screenFaceDetector == null) {
            screenFaceDetector = new ScreenFaceDetector(this);
        }
        screenFaceDetector.start(sensorManager);
    }
}
