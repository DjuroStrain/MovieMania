package com.example.durobelacic.moviemania.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.durobelacic.moviemania.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDialogFragment extends BottomSheetDialogFragment {


    public MovieDialogFragment() {
        // Required empty public constructor
    }

    public static MovieDialogFragment newInstance(String param1, String param2) {
        MovieDialogFragment fragment = new MovieDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_dialog, container, false);
    }
}