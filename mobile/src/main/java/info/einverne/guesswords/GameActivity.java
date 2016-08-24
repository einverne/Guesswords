package info.einverne.guesswords;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import info.einverne.guesswords.data.SingleWord;
import info.einverne.guesswords.detector.ScreenFaceDetector;
import timber.log.Timber;

public class GameActivity extends BaseActivity implements ScreenFaceDetector.Listener {
    public static final String GROUP_ID = "GROUP_ID";
    private static final String STATE_INDEX = "STATE_INDEX";
    private SensorManager sensorManager;
    private ScreenFaceDetector screenFaceDetector;

    private boolean isSync = false;
    private boolean isReady = false;
    private boolean isGameOver = false;
    private TextView tv_guessing_word;
    private TextView tv_game_left_time;

    private Timer timerPrepare;
    private TimerTask timerTaskPrepare;
    private int nPrepareTime = 2;

    private Timer timerCountDown;
    private TimerTask timerTaskCountDown;
    private int nLeftTime = 10;

    private String groupId;
    private FirebaseDatabase database;
    List<SingleWord> randomWords = new ArrayList<>();
    private int index = 0;

    private Map<String, Boolean> gameRecord = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        database = FirebaseDatabase.getInstance();

        groupId = getIntent().getStringExtra(GROUP_ID);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSensor();
        initUI();
        getWords();

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(STATE_INDEX);
        }
    }

    public void getWords() {
        Timber.d("groupId " + groupId);
        final DatabaseReference databaseReference = database.getReference("zh").child(groupId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("GameActivity data change");
                List<SingleWord> words = new ArrayList<>();
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    words.add(shot.getValue(SingleWord.class));
                }
                for (int i = 0; i < 90; i++) {
                    randomWords.add(words.get(new Random().nextInt(words.size())));
                }
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimerPrepare();
    }

    private void startTimerPrepare() {
        timerPrepare = new Timer();
        timerTaskPrepare = new TimerTask() {
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
        timerTaskCountDown = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_game_left_time.setText(nLeftTime + "");
                        nLeftTime--;
                        if (nLeftTime < 0) {
                            timerCountDown.cancel();
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
        tv_guessing_word.setText("Game Over");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null ){
            String key = database.getReference("users").child(user.getUid()).child("histroy").push().getKey();
            database.getReference("users").child(user.getUid()).child("histroy").child(key).setValue(gameRecord);
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
        gameRecord.put(randomWords.get(index).wordString, false);
        index++;
        tv_guessing_word.setText(randomWords.get(index).wordString);
    }

    @Override
    public void FaceDown() {
        if (!isReady || isGameOver) return;
        if (index >= randomWords.size()) return;
        gameRecord.put(randomWords.get(index).wordString, true);
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
