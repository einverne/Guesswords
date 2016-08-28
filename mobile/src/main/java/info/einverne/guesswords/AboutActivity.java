package info.einverne.guesswords;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText(BuildConfig.VERSION_NAME);
    }
}
