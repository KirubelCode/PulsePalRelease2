package com.movesense.showcaseapp.section_01_movesense.tests;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.movesense.showcaseapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EcgPortraitFragment extends Fragment {


    public EcgPortraitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ecg_portrait, container, false);
    }

}
