package cz.iqlandia.iqplanetarium.scanner.disambiguation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.appdistribution.FirebaseAppDistribution;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cz.iqlandia.iqplanetarium.scanner.planetarium.PlanetariumShowlistActivity;
import cz.iqlandia.iqplanetarium.scanner.R;

public class MainActivity extends AppCompatActivity {
    public static MainActivity activity;
    private static final int REQUEST_CAMERA = 1;
    private static Timer timer = new Timer("Time updater");
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Thread t = new Thread(() -> {
            if(!networkCheck()) {
                runOnUiThread(()-> {
                    Intent offline = new Intent(activity, Offline.class);
                    this.startActivity(offline);
                });
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        checkPermissions();
        scheduleUpdater();
        setupButtons();
    }

    private void setupButtons() {
        ImageButton feedback = findViewById(R.id.iqfeedback);
        ImageButton iqlandia = findViewById(R.id.iqlandia);
        ImageButton iqplanetarium = findViewById(R.id.iqplanetarium);
        ImageButton iqpark = findViewById(R.id.iqpark);
        ImageButton iqfablab = findViewById(R.id.iqfablab);

        iqlandia.setOnClickListener(c -> Toast.makeText(activity, "Not yet ready", Toast.LENGTH_SHORT).show());
        iqplanetarium.setOnClickListener(c ->  {
            runOnUiThread(()-> {
                Intent offline = new Intent(activity, PlanetariumShowlistActivity.class);
                this.startActivity(offline);
            });
        });
        iqpark.setOnClickListener(c -> Toast.makeText(activity, "Not yet ready", Toast.LENGTH_SHORT).show());
        iqfablab.setOnClickListener(c -> Toast.makeText(activity, "Not yet ready", Toast.LENGTH_SHORT).show());
        feedback.setOnClickListener((c) -> FirebaseAppDistribution.getInstance().startFeedback("Submit feedback :)"));
    }


    private void scheduleUpdater() {
        stopAndResetTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String time = String.format(Locale.ENGLISH,"%02d:%02d:%02d",LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(),LocalDateTime.now().getSecond());
                runOnUiThread(()-> ((TextView)findViewById(R.id.time)).setText(time));
            }
        }, 0, 1000);
    }

    private void stopAndResetTimer() {
        timer.purge();
        timer.cancel();
        timer = new Timer("Time updater");
    }

    private void checkPermissions() {
        String permission = android.Manifest.permission.CAMERA;
        if (checkSelfPermission(permission) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No camera permission",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        scheduleUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAndResetTimer();
    }

    public boolean networkCheck() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("https://iqlandia.cz").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            Log.i("Network found", urlc.getResponseCode() + "");
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            return false;
        }
    }
}