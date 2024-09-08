package cz.iqlandia.iqplanetarium.scanner.planetarium;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import cz.iqlandia.iqplanetarium.scanner.R;
import cz.iqlandia.iqplanetarium.scanner.api.DayShowsInfo;
import cz.iqlandia.iqplanetarium.scanner.disambiguation.MainActivity;

public class MainCardFragment extends Fragment {

    private DayShowsInfo.Event event;

    public MainCardFragment() {
        super(R.layout.fragment_main_card);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Check if the fragment has arguments
        if (getArguments() == null) {
            throw new IllegalStateException("Fragment does not have any arguments.");
        }

        event = (DayShowsInfo.Event) getArguments().getSerializable("showID");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_card, container, false);

        //Log.i("DEBUGR", String.valueOf(getArguments().getSerializable("showID").hashCode()));
        System.out.println(event == null);

        assert event != null;
        ((TextView) v.findViewById(R.id.showTitle)).setText(event.name);
        ((TextView) v.findViewById(R.id.showDate)).setText(generateShowSpan(event.start, event.end));
        ((TextView) v.findViewById(R.id.showDuration)).setText(generateShowDuration(event.start, event.end));
        ((TextView) v.findViewById(R.id.occupancy)).setText(String.format(Locale.ENGLISH, "%d/%d", event.CurrentCapacity, event.MaxCapacity));

        v.findViewById(R.id.enter_scan).setOnClickListener((b) -> {
            // setContentView(R.layout.next_page); this will modify the current activity view
            // if you want to start a new activity:
            Intent i = new Intent(super.getActivity(), ScannerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("showID", event);
            i.putExtra("bundle", bundle);
            MainActivity.activity.startActivity(i);
        });

        return v;
    }

    private String generateShowSpan(ZonedDateTime start, ZonedDateTime end) {
        return String.format(Locale.ENGLISH, "%02d:%02d - %02d:%02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute());
    }

    private String generateShowDuration(ZonedDateTime start, ZonedDateTime end) {
        long totalSecs = ChronoUnit.SECONDS.between(start, end);

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;

        if (minutes != 0) {
            return String.format(Locale.ENGLISH, "(%2dh %02dm)", hours, minutes);
        } else {
            return String.format(Locale.ENGLISH, "(%dh)", hours);
        }
    }
}