package space.itoncek.iqplanetarium;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static MainActivity activity;
    public iQAPIAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        adapter = new iQAPIAdapter(this);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cal).setVisibility(View.GONE);

        findViewById(R.id.calendar).setOnClickListener((v) -> {
            findViewById(R.id.cal).setVisibility(View.VISIBLE);
        });

        ((CalendarView) findViewById(R.id.cal)).setOnDateChangeListener((calendarView, year, month, day) -> {
            ((LinearLayout) findViewById(R.id.lineal)).removeAllViews();
            updateShows(LocalDate.of(year, month+1, day));
            calendarView.setVisibility(View.GONE);
        });
        updateShows(LocalDate.now());
    }

    private void updateShows(LocalDate date) {
        ((LinearLayout) findViewById(R.id.lineal)).removeAllViews();

        List<Show> shows = null;
        try {
            System.out.println("Requesting");
            shows = adapter.getDayShows(date);
            System.out.println("Recived " + shows.size() + " shows");
        } catch (JSONException | IOException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        assert shows != null;
        for (Show show : shows) {
            Bundle bundle = new Bundle();
            bundle.putString("label", show.label());
            bundle.putString("start", show.start().format(DateTimeFormatter.ISO_TIME));
            bundle.putString("end", show.end().format(DateTimeFormatter.ISO_TIME));
            bundle.putString("diff", TimeUnit.MILLISECONDS.toMinutes(Duration.between(show.start(), show.end()).toMillis()) + " min");
            bundle.putString("id", show.id());
            bundle.putInt("aval", show.avaliable());

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.lineal, CardFragment.class, bundle)
                    .commit();

            Space space = new Space(this);
            space.setMinimumHeight(8);

        }
        TextView v = findViewById(R.id.date);
        v.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    }
}