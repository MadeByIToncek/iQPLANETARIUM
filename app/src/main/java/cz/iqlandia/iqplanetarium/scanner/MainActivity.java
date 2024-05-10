package cz.iqlandia.iqplanetarium.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import cz.iqlandia.iqplanetarium.api.DayShowsInfo;
import cz.iqlandia.iqplanetarium.api.IQApi;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    public static MainActivity activity;
    private static LocalDate date = LocalDate.now();

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

        generateButtons();
        checkPermissions();
        updateList();
    }

    private void generateButtons() {
        ImageButton back = findViewById(R.id.bck);
        back.setOnClickListener((c)->{
            date = date.minusDays(1);
            updateList();
        });

        ImageButton fwd = findViewById(R.id.fwd);
        fwd.setOnClickListener((c)->{
            date = date.plusDays(1);
            updateList();
        });
    }

    private void updateList() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return IQApi.getShowInfoForDateCached(date);
            } catch (IOException e) {
                runOnUiThread(()-> {
                    Intent offline = new Intent(activity, Offline.class);
                    this.startActivity(offline);
                });
                return null;
            }
        }).thenAccept((info) -> {
            Log.d("updateList()", "Initializing linear layout update");
            LinearLayout scroll = findViewById(R.id.scroll);
            Log.d("updateList()", "Clearing linear layout");
            runOnUiThread(() -> {
                scroll.removeAllViews();
                Log.d("updateList()", "Linear layout cleared");

                if (info == null) {
                    Log.d("updateList()", "Info is null, throwing");
                    showError();
                } else {
                    Log.d("updateList()", "Info is not null, generating new view");
                    generateList(info);
                }

                ((TextView)findViewById(R.id.date)).setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));

            });
        });

    }

    private void generateList(DayShowsInfo info) {
        for (DayShowsInfo.Event event : info.events) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("showID", event);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.scroll, MainCardFragment.class, bundle)
                    .commit();

        }
    }

    private void showError() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.scroll, ErrorCardFragment.class,new Bundle())
                .commit();
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
}