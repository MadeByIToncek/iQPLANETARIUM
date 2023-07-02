package space.itoncek.iqplanetarium;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static MainActivity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        iQAPIAdapter adapter = new iQAPIAdapter(this);

        List<Show> shows = null;
        try {
            System.out.println("Requesting");
            shows = adapter.getDayShows(LocalDate.now().plus(2, ChronoUnit.DAYS));
            System.out.println("Recived " + shows.size() + " shows");
        } catch (JSONException | IOException | InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.lineal);

        assert shows != null;
        for (Show show : shows) {
            if (savedInstanceState == null) {
                Bundle bundle = new Bundle();
                bundle.putString("label", show.label());
                bundle.putString("start", show.start().format(DateTimeFormatter.ISO_TIME));
                bundle.putString("end", show.end().format(DateTimeFormatter.ISO_TIME));
                bundle.putString("diff", TimeUnit.MILLISECONDS.toMinutes(Duration.between(show.start(), show.end()).toMillis()) + " min");
                bundle.putString("id", show.id());

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.lineal, CardFragment.class, bundle)
                        .commit();

                Space space = new Space(this);
                space.setMinimumHeight(8);
            }
        }
        TextView v = findViewById(R.id.date);
        v.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    }
}