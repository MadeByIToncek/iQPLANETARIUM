/*
 * All rights reserved to IToncek
 */

/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

public class DetailedViewActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        String id = this.getIntent().getStringExtra("id");
        String showname = this.getIntent().getStringExtra("showname");
        int aval = this.getIntent().getIntExtra("aval", 50);

        findViewById(R.id.btn_rtn).setOnClickListener((l) -> {
            this.finish();
        });

        ((TextView) findViewById(R.id.detail_showname)).setText(showname);
        ((TextView) findViewById(R.id.detail_full)).setText((50 - aval) + " " + getResources().getString(R.string.fullness));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        iQAPIAdapter adapter = new iQAPIAdapter(this);
        Seats seatsForShow = null;
        try {
            seatsForShow = adapter.getSeatsForShow(id);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        if (seatsForShow != null) for (int r = 1; r <= 5; r++) {
            for (int s = 1; s <= seatsForShow.seatAmt(r); s++) {
                int ids = decodeSeat(r, s);
                System.out.println(r + "/" + s + " had been assigned " + ids);
                if (ids == -1) {
                    ids = R.id.a1;
                }
                ImageView view = findViewById(ids);
                if (seatsForShow.seats.containsKey(new Seat(r, s))) {
                    if (Boolean.TRUE.equals(seatsForShow.seats.get(new Seat(r, s)))) {
                        view.setImageResource(R.drawable.seat_full);
                    } else {
                        view.setImageResource(R.drawable.seat_empty);
                    }
                } else {
                    view.setImageResource(R.drawable.seat_unset);
                }
            }
        }
        //((TextView) findViewById(R.id.te)).setText(id);
    }

    private int decodeSeat(int row, int seat) {
        String r = switch (row) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            default -> throw new IllegalStateException("Unexpected value: " + row);
        };
        return switch (r+seat) {
            case "a1" -> R.id.a1;
            case "a2" -> R.id.a2;
            case "a3" -> R.id.a3;
            case "a4" -> R.id.a4;
            case "a5" -> R.id.a5;
            case "a6" -> R.id.a6;
            case "a7" -> R.id.a7;
            case "a8" -> R.id.a8;

            case "b1" -> R.id.b1;
            case "b2" -> R.id.b2;
            case "b3" -> R.id.b3;
            case "b4" -> R.id.b4;
            case "b5" -> R.id.b5;
            case "b6" -> R.id.b6;
            case "b7" -> R.id.b7;
            case "b8" -> R.id.b8;
            case "b9" -> R.id.b9;
            case "b10" -> R.id.b10;
            case "b11" -> R.id.b11;
            case "b12" -> R.id.b12;
            case "b13" -> R.id.b13;

            case "c1" -> R.id.c1;
            case "c2" -> R.id.c2;
            case "c3" -> R.id.c3;
            case "c4" -> R.id.c4;
            case "c5" -> R.id.c5;
            case "c6" -> R.id.c6;
            case "c7" -> R.id.c7;
            case "c8" -> R.id.c8;
            case "c9" -> R.id.c9;
            case "c10" -> R.id.c10;
            case "c11" -> R.id.c11;
            case "c12" -> R.id.c12;

            case "d1" -> R.id.d1;
            case "d2" -> R.id.d2;
            case "d3" -> R.id.d3;
            case "d4" -> R.id.d4;
            case "d5" -> R.id.d5;
            case "d6" -> R.id.d6;
            case "d7" -> R.id.d7;
            case "d8" -> R.id.d8;
            case "d9" -> R.id.d9;
            case "d10" -> R.id.d10;
            case "d11" -> R.id.d11;

            case "e1" -> R.id.e1;
            case "e2" -> R.id.e2;
            case "e3" -> R.id.e3;
            case "e4" -> R.id.e4;
            case "e5" -> R.id.e5;
            case "e6" -> R.id.e6;
            default -> -1;
        };
    }
}