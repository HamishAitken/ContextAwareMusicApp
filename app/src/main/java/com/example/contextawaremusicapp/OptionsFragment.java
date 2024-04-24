package com.example.contextawaremusicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.contextawaremusicapp.databinding.FragmentOptionsBinding;

public class OptionsFragment extends Fragment {

    private FragmentOptionsBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        Button btnRefresh = view.findViewById(R.id.btnRefreshSpotifyData);
        btnRefresh.setOnClickListener(v -> {
            // Call the method to refresh Spotify data.
            refreshSpotifyData();
        });

        // You can add more controls like a SeekBar for recommendation ratio here.

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnRefreshSpotifyData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void refreshSpotifyData() {
        // Implement the refresh functionality.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}