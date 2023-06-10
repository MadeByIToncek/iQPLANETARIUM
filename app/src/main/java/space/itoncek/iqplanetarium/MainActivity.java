package space.itoncek.iqplanetarium;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        iQAPIAdapter adapter = new iQAPIAdapter(this);

        List<Show> shows = null;
        try {
            shows = adapter.getDayShows(LocalDate.now());
        } catch (JSONException | IOException | InterruptedException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.cardLayout);

        assert shows != null;
        for (Show show : shows) {
            CardView cardview = new CardView(this);

            CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                    CardView.LayoutParams.WRAP_CONTENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );

            cardview.setLayoutParams(layoutparams);
            cardview.setRadius(15);
            cardview.setPadding(25, 25, 25, 25);
            cardview.setCardBackgroundColor(Color.MAGENTA);
            cardview.setMaxCardElevation(30);
            cardview.setMaxCardElevation(6);

            TextView title = new TextView(this);
            title.setLayoutParams(layoutparams);
            title.setText(show.getLabel());

            cardview.addView(title);

            layout.addView(cardview);
        }
        TextView v = findViewById(R.id.date);
        v.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    }
}