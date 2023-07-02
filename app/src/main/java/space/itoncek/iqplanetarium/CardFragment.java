/*
 * All rights reserved to IToncek
 */

package space.itoncek.iqplanetarium;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CardFragment extends Fragment {

    public CardFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardFragment.
     */
    public static CardFragment newInstance() {
        return new CardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inf = inflater.inflate(R.layout.fragment_card, container, false);

        TextView showname = inf.findViewById(R.id.showname);
        TextView start = inf.findViewById(R.id.start_time);
        TextView end = inf.findViewById(R.id.end_time);
        TextView duration = inf.findViewById(R.id.duration);
        Button details = inf.findViewById(R.id.details);

        showname.setText(requireArguments().getString("label"));
        start.setText(requireArguments().getString("start"));
        end.setText(requireArguments().getString("end"));

        duration.setText(requireArguments().getString("diff"));

        details.setOnClickListener((b) -> {
            Intent foo = new Intent(MainActivity.activity, DetailedViewActivity.class);
            foo.putExtra("id", requireArguments().getString("id"));
            MainActivity.activity.startActivity(foo);
        });

        return inf;
    }
}