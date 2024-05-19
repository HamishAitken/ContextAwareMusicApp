package com.example.contextawaremusicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;

public class LocationsFragment extends Fragment {
    private EditText editTextHome, editTextWork, editTextGym;
    private ImageView checkHome, checkWork, checkGym;
    private MapFragment mapFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        editTextHome = view.findViewById(R.id.editTextHome);
        editTextWork = view.findViewById(R.id.editTextWork);
        editTextGym = view.findViewById(R.id.editTextGym);

        checkHome = view.findViewById(R.id.checkHome);
        checkWork = view.findViewById(R.id.checkWork);
        checkGym = view.findViewById(R.id.checkGym);

        // Load the map fragment
        mapFragment = new MapFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.map_container, mapFragment).commit();

        checkHome.setOnClickListener(v -> updateLocation(editTextHome, "Home"));
        checkWork.setOnClickListener(v -> updateLocation(editTextWork, "Work"));
        checkGym.setOnClickListener(v -> updateLocation(editTextGym, "Gym"));

        return view;
    }

    private void updateLocation(EditText editText, String title) {
        String[] coordinates = editText.getText().toString().split(",");
        if (coordinates.length == 2) {
            try {
                double lat = Double.parseDouble(coordinates[0].trim());
                double lng = Double.parseDouble(coordinates[1].trim());
                LatLng latLng = new LatLng(lat, lng);
                mapFragment.addMarker(latLng, title);
                Toast.makeText(getContext(), title + " location updated", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid coordinates", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Please enter valid coordinates", Toast.LENGTH_SHORT).show();
        }
    }
}
