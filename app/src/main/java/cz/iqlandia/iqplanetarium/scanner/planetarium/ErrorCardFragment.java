package cz.iqlandia.iqplanetarium.scanner.planetarium;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cz.iqlandia.iqplanetarium.scanner.R;

public class ErrorCardFragment extends Fragment {

    public ErrorCardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_error_card, container, false);
    }
}