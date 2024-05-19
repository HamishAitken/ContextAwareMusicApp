package com.example.contextawaremusicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.contextawaremusicapp.databinding.FragmentOptionsBinding;
import com.example.contextawaremusicapp.repository.SpotifyRepository;

public class OptionsFragment extends Fragment {

    private FragmentOptionsBinding binding;
    private SpotifyRepository spotifyRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);
        binding = FragmentOptionsBinding.bind(view);

        spotifyRepository = new SpotifyRepository(getContext());

        SeekBar seekBar = binding.seekBar;
        seekBar.setProgress(spotifyRepository.getRecommendationRatio()); // Set initial position

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here if you like
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                spotifyRepository.setRecommendationRatio(progressChanged);
                Toast.makeText(getContext(), "Recommendation ratio set to: " + progressChanged + "%", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
