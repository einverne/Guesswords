package info.einverne.guesswords;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import info.einverne.guesswords.detector.ScreenFaceDetector;

public class GameActivity extends AppCompatActivity implements ScreenFaceDetector.Listener {
    private SensorManager sensorManager;
    private ScreenFaceDetector screenFaceDetector;

    private boolean isReady = false;
    private TextView tv_guessing_word;
    private TextView tv_game_left_time;

    private Timer timerPrepare;
    private TimerTask timerTaskPrepare;
    private int nPrepareTime = 5;

    private Timer timerCountDown;
    private TimerTask timerTaskCountDown;
    private int nLeftTime = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSensor();
        initUI();
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
                            tv_guessing_word.setText("Word");
                            startCountDown();
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
                            
                        }
                    }
                });
            }
        };
        timerCountDown.schedule(timerTaskCountDown, 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        screenFaceDetector.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerPrepare != null) {
            timerPrepare.cancel();
        }
        if (timerCountDown != null) {
            timerCountDown.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
    }

    @Override
    public void FaceUp() {

    }

    @Override
    public void FaceDown() {

    }


    private void initSensor() {
        if (screenFaceDetector == null) {
            screenFaceDetector = new ScreenFaceDetector(this);
        }
        screenFaceDetector.start(sensorManager);
    }
}
