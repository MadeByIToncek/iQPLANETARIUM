package cz.iqlandia.iqplanetarium.scanner.planetarium;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cz.iqlandia.iqplanetarium.scanner.R;
import cz.iqlandia.iqplanetarium.scanner.api.DayShowsInfo;
import cz.iqlandia.iqplanetarium.scanner.disambiguation.MainActivity;
import de.markusfisch.android.barcodescannerview.widget.BarcodeScannerView;
import de.markusfisch.android.zxingcpp.ZxingCpp;

public class ScannerActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    private String last = "";
    private BarcodeScannerView scannerView;
    private DayShowsInfo.Event event;
    private final Timer timer = new Timer();
    private StatusColor status = StatusColor.UNSET;
    private Timer resetThread = new Timer();
    private Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Intent intent=this.getIntent();
        event = (DayShowsInfo.Event) Objects.requireNonNull(intent.getBundleExtra("bundle")).getSerializable("showID");

        setPadding();
        updateDetails();
        updateSeats();
        setupScanner();
    }

    private void updateDetails() {
        //Finding modifiable titles
//        TextView title = findViewById(R.id.show_title);
        TextView name = findViewById(R.id.show_name);
        TextView startsIn = findViewById(R.id.show_starts_in);

        //Update time until show
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Duration untilStart = Duration.between(ZonedDateTime.now(), event.start);
                runOnUiThread(()-> startsIn.setText(humanReadableFormat(untilStart)));
            }
        },0,1000);

        name.setText(event.name);
    }

    private void recolor(StatusColor c) {
        status = c;
        runOnUiThread(()->{
            //Finding all recolorable elements
            TextView title1 = findViewById(R.id.show_title);
            TextView name1 = findViewById(R.id.show_name);
            TextView name2 = findViewById(R.id.show_name_static);
            TextView start1 = findViewById(R.id.show_starts_in);
            TextView start2 = findViewById(R.id.show_starts_in_static);
            CardView top_card = findViewById(R.id.top_card);
            CardView bottom_card = findViewById(R.id.bottom_card);
            ImageButton return_button = findViewById(R.id.back);

            title1.setTextColor(ContextCompat.getColor(MainActivity.activity, status.foreground));
            title1.setText(c.textR);
            name1.setTextColor(ContextCompat.getColor(MainActivity.activity, status.foreground));
            name2.setTextColor(ContextCompat.getColor(MainActivity.activity, status.foreground));
            start1.setTextColor(ContextCompat.getColor(MainActivity.activity, status.foreground));
            start2.setTextColor(ContextCompat.getColor(MainActivity.activity, status.foreground));
            top_card.setCardBackgroundColor(ContextCompat.getColor(MainActivity.activity, status.background));
            bottom_card.setCardBackgroundColor(ContextCompat.getColor(MainActivity.activity, status.background));

            return_button.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.activity,status.background));
            return_button.setColorFilter(ContextCompat.getColor(this, status.foreground));
        });
    }

    private void updateSeats() {
        runOnUiThread(()-> event.seatmap.forEach((row, seat) -> {
            Row r = Row.getRow(row);
            seat.forEach((id, state)-> {
                ImageView img = getSeat(r, id);

                switch (state) {
                    case EMTPY:
                        img.setColorFilter(ContextCompat.getColor(this, R.color.ok_fg), android.graphics.PorterDuff.Mode.SRC_IN);break;
                    case OCCUPIED:
                        img.setColorFilter(ContextCompat.getColor(this, R.color.err_fg), android.graphics.PorterDuff.Mode.SRC_IN);break;
                    case ENTRY_ALLOWED:
                        img.setColorFilter(ContextCompat.getColor(this, R.color.warn_fg), android.graphics.PorterDuff.Mode.SRC_IN);break;
                    case POSSIBLE_DUPLICATE:
                        img.setColorFilter(ContextCompat.getColor(this, R.color.duplicate_fg), android.graphics.PorterDuff.Mode.SRC_IN);break;
                }
            });
        }));
    }

    private ImageView getSeat(Row row, Integer column) {
        switch (row) {
            case R1:
                switch (column) {
                    case 1: return findViewById(R.id.a1);
                    case 2: return findViewById(R.id.a2);
                    case 3: return findViewById(R.id.a3);
                    case 4: return findViewById(R.id.a4);
                    case 5: return findViewById(R.id.a5);
                    case 6: return findViewById(R.id.a6);
                    case 7: return findViewById(R.id.a7);
                    default: return findViewById(R.id.a8);
                }
            case R2:
                switch (column) {
                    case 1: return findViewById(R.id.b1);
                    case 2: return findViewById(R.id.b2);
                    case 3: return findViewById(R.id.b3);
                    case 4: return findViewById(R.id.b4);
                    case 5: return findViewById(R.id.b5);
                    case 6: return findViewById(R.id.b6);
                    case 7: return findViewById(R.id.b7);
                    case 8: return findViewById(R.id.b8);
                    case 9: return findViewById(R.id.b9);
                    case 10: return findViewById(R.id.b10);
                    case 11: return findViewById(R.id.b11);
                    case 12: return findViewById(R.id.b12);
                    default: return findViewById(R.id.b13);
                }
            case R3:
                switch (column) {
                    case 1: return findViewById(R.id.c1);
                    case 2: return findViewById(R.id.c2);
                    case 3: return findViewById(R.id.c3);
                    case 4: return findViewById(R.id.c4);
                    case 5: return findViewById(R.id.c5);
                    case 6: return findViewById(R.id.c6);
                    case 7: return findViewById(R.id.c7);
                    case 8: return findViewById(R.id.c8);
                    case 9: return findViewById(R.id.c9);
                    case 10: return findViewById(R.id.c10);
                    case 11: return findViewById(R.id.c11);
                    default: return findViewById(R.id.c12);
                }
            case R4:
                switch (column) {
                    case 1: return findViewById(R.id.d1);
                    case 2: return findViewById(R.id.d2);
                    case 3: return findViewById(R.id.d3);
                    case 4: return findViewById(R.id.d4);
                    case 5: return findViewById(R.id.d5);
                    case 6: return findViewById(R.id.d6);
                    case 7: return findViewById(R.id.d7);
                    case 8: return findViewById(R.id.d8);
                    case 9: return findViewById(R.id.d9);
                    case 10: return findViewById(R.id.d10);
                    default: return findViewById(R.id.d11);
                }
            case R5:
                switch (column) {
                    case 1: return findViewById(R.id.e1);
                    case 2: return findViewById(R.id.e2);
                    case 3: return findViewById(R.id.e3);
                    case 4: return findViewById(R.id.e4);
                    case 5: return findViewById(R.id.e5);
                    default: return findViewById(R.id.e6);
                }
        }
        return findViewById(R.id.e6);
    }

    public static String humanReadableFormat(Duration duration) {
        if(duration.isNegative()) {
            return "Already started";
        } else if(duration.toDays() > 0) {
            return duration.toDays() + " d";
        } else if(duration.toHours() > 0) {
            return duration.toHours() + " h";
        } else if(duration.toMinutes() > 0) {
            return duration.toMinutes() + " m";
        } else {
            return duration.getSeconds() + " s";
        }
    }

    private void setupScanner() {
        scannerView = findViewById(R.id.barcode_scanner_view);
        scannerView.setCropRatio(.75f);
        scannerView.setOnBarcodeListener(result -> {
            if (!result.getText().equals(last)) {
                StatusColor color = StatusColor.READY;
                switch (result.getText()) {
                    case "VALID_TICKET":
                        color = StatusColor.ACCEPT;
                        v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case "DUPLICATE_TICKET":
                        color = StatusColor.DUPLICITY;
                        v.vibrate(VibrationEffect.createOneShot(425, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case "INVALID_TICKET":
                        color = StatusColor.ERROR;
                        v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                }

                resetThread.cancel();
                resetThread.purge();

                resetThread = new Timer();
                resetThread.schedule(generateResetThread(color), 5000);

                last = result.getText();
                recolor(color);
            }
            return true;
        });
        scannerView.formats.clear();
        scannerView.formats.add(ZxingCpp.BarcodeFormat.QR_CODE);


        recolor(StatusColor.READY);
    }

    private TimerTask generateResetThread(StatusColor color) {
        return new TimerTask() {
            @Override
            public void run() {
                if(status == color) {
                    last = "";
                }
                recolor(StatusColor.READY);
            }
        };
    }

    private void setPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            runOnUiThread(()->{
                ViewGroup.MarginLayoutParams topcard = (ViewGroup.MarginLayoutParams) findViewById(R.id.top_card).getLayoutParams();

                topcard.setMargins(topcard.leftMargin + convertPixelsToDp(systemBars.left,this),
                        topcard.topMargin + convertPixelsToDp(systemBars.top,this),
                        topcard.rightMargin + convertPixelsToDp(systemBars.right,this),
                        topcard.bottomMargin);

                findViewById(R.id.top_card).setLayoutParams(topcard);

                ViewGroup.MarginLayoutParams bottomcard = (ViewGroup.MarginLayoutParams) findViewById(R.id.bottom_card).getLayoutParams();

                bottomcard.setMargins(bottomcard.leftMargin + convertPixelsToDp(systemBars.left,this),
                        bottomcard.topMargin,
                        bottomcard.rightMargin + convertPixelsToDp(systemBars.right,this),
                        bottomcard.bottomMargin+ convertPixelsToDp(systemBars.bottom,this));

                findViewById(R.id.bottom_card).setLayoutParams(bottomcard);

                findViewById(R.id.back).setOnClickListener((b) -> super.finish());
            });

            return insets;
        });
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
    public void onResume() {
        super.onResume();
        scannerView.openAsync();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.close();
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static int convertPixelsToDp(float px, Context context){
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    private enum StatusColor {
        UNSET(R.color.unset_bg,R.color.unset_fg,R.string.scanner_undefined),
        READY(R.color.background,R.color.iqprimary, R.string.scanner_ready),
        ACCEPT(R.color.ok_bg,R.color.ok_fg,R.string.scanner_accept),
        DUPLICITY(R.color.warn_bg,R.color.warn_fg, R.string.scanner_dupl),
        ERROR(R.color.err_bg,R.color.err_fg, R.string.scanner_deny);

        public final int background;
        public final int foreground;
        public final int textR;

        StatusColor(int background, int foreground, int textR) {
            this.background = background;
            this.foreground = foreground;
            this.textR = textR;
        }
    }

    private enum Row {
        R1,
        R2,
        R3,
        R4,
        R5;
        public static Row getRow(int i) {
            switch (i) {
                case 1: return R1;
                case 2: return R2;
                case 3: return R3;
                case 4: return R4;
                default: return R5;
            }
        }
    }
}
