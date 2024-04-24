package com.example.contextawaremusicapp;

import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.example.contextawaremusicapp.model.Song;
import com.example.contextawaremusicapp.repository.SpotifyRepository;
import com.example.contextawaremusicapp.ui.SongsAdapter;
import com.google.android.material.navigation.NavigationView;

import com.spotify.android.appremote.api.*;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "cd8e973d123a4fed9669e5f9bd9fd0d7";
    private static final String REDIRECT_URI = "auratunes://callback";
    private static final int AUTH_TOKEN_REQUEST_CODE = 1337;
    private SpotifyAppRemote mSpotifyAppRemote;
    private ImageButton playPauseButton;
    private SongsAdapter adapter;
    private List<Song> songs;
    private RecyclerView songListRecyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;


    // Fields for managing playback and the buffer queue
    private List<Song> bufferList = new ArrayList<>();
    private static final int BUFFER_SIZE = 3; // Including the currently playing song
    private Song currentSong = null; // Track the currently playing song

    private List<Song> currentContextSongList = new ArrayList<>();


    private boolean isPlaying = false;


    // Instantiate SpotifyRepository
    private SpotifyRepository spotifyRepository;

    public interface CustomCallback<T> {
        void onSuccess(T result);

        void onFailure(Throwable throwable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"playlist-read-private","app-remote-control","user-library-read", "streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);

        // Initialize the play/pause button and set its click listener
        playPauseButton = findViewById(R.id.btnPlayPause);
        playPauseButton.setOnClickListener(this::onPlayPauseButtonClicked);

        // Initialize the RecyclerView for the list of songs
        songListRecyclerView = findViewById(R.id.rvSongList);
        songListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize your songs list
        spotifyRepository = new SpotifyRepository(this);

        // Initialize the DrawerLayout and NavigationView for the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            if (id == R.id.nav_options) {
                fragment = new OptionsFragment();
            }


//            if (fragment != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, fragment)
//                        .commit();
//            }

            // Close the drawer
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        initializeBufferQueueUI();
        setupPlaybackProgressListener();

        // TODO: Set the navigation item selected listener

        // Initialize other UI components like next, previous, and close buttons
        findViewById(R.id.btnNext).setOnClickListener(view -> skipNext());
        findViewById(R.id.btnBack).setOnClickListener(view -> skipPrevious());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Log.d("SpotifyConnect", "connected to Spotify");
                    final String accessToken = response.getAccessToken();
                    saveAccessToken(accessToken);
                    connectToSpotify();

                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e("SpotifyConnect", "Failed to connect to Spotify");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void saveAccessToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("token", token).apply();
    }
    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSpotifyAppRemote == null || !mSpotifyAppRemote.isConnected()) {
            connectToSpotify();
        } else {
            // Set up the progress listener each time the activity resumes and is connected
            setupPlaybackProgressListener();
        }
    }

    private void initializeBufferQueueUI() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                // Disable scrolling
                return false;
            }
        };
        songListRecyclerView.setLayoutManager(layoutManager);
        // Initialize the adapter with an empty list or however you prefer to start
        adapter = new SongsAdapter(new ArrayList<>(), this::onSongClicked);
        songListRecyclerView.setAdapter(adapter);
    }

    private void connectToSpotify() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote; // Assign the connected App Remote to your field
                Log.d("SpotifyConnect", "Connected to Spotify App Remote");

                // Subscribe to the player state to get updates about the playback status
                mSpotifyAppRemote.getPlayerApi()
                        .subscribeToPlayerState()
                        .setEventCallback(playerState -> {
                            // Update your UI to reflect the current playback state
                            isPlaying = !playerState.isPaused;
                            playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                        });

                // Now you can start interacting with App Remote
                getMySongsList(mSpotifyAppRemote);
            }

            @Override
            public void onFailure(Throwable error) {
                // Log the error and inform the user that the connection failed
                Log.e("SpotifyConnect", "Could not connect to Spotify App Remote", error);
                Toast.makeText(MainActivity.this, "Failed to connect to Spotify", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupPlaybackProgressListener() {
        // Assume a playback progress check interval (in milliseconds)
        final int progressCheckInterval = 1000;

        final Handler handler = new Handler();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mSpotifyAppRemote != null) {
                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                        // Example logic to check if the song is at its end and transition to the next song
                        // This is a simplification. A more robust implementation would consider song duration.
                        if (!playerState.isPaused && playerState.playbackPosition >= playerState.track.duration - progressCheckInterval) {
                            skipNext();
                        }
                        handler.postDelayed(this, progressCheckInterval);
                    });
                }
            }
        };

        // Start the periodic check
        handler.postDelayed(progressRunnable, progressCheckInterval);
    }

    private void playTrack(SpotifyAppRemote spotifyAppRemote, Song song) {
        // Set the current song
        currentSong = song;

        // Play the track
        spotifyAppRemote.getPlayerApi().play("spotify:track:" + song.getId()).setResultCallback(empty -> {
            // Track play initiated
            updateCurrentSongUI(currentSong); // Update the UI with the current song's details
        }).setErrorCallback(error -> {
            //Log.e("SpotifyPlayTrack", "Could not play track", error);
        });
    }

    // Method to get a list of songs (from your data source)
    private void getMySongsList(SpotifyAppRemote spotifyAppRemote) {
        spotifyRepository.generateUserPlaylist(new SpotifyRepository.CustomCallback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songList) {
                // Here, instead of updating the whole song list UI, we update the context list and manage the buffer.
                onNewContextSongListFetched(songList);

                // Play the first song from the buffer if the player is not already playing
                if (!isPlaying && !bufferList.isEmpty()) {
                    Song firstSongToPlay = bufferList.get(0); // Get the first song from the buffer without removing it
                    if (firstSongToPlay != null) {
                        playTrack(spotifyAppRemote, firstSongToPlay);
                    }
                }

                // Even if songs are already playing, this method ensures the buffer queue is always filled.
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("MainActivity", "Failed to fetch songs: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error fetching songs", Toast.LENGTH_LONG).show();
            }
        });
    }
    // Method called when new context song list is fetched
    private void onNewContextSongListFetched(List<Song> songList) {
        currentContextSongList.clear();
        currentContextSongList.addAll(songList);
        manageBufferList();
    }

    private void manageBufferList() {
        while (bufferList.size() < BUFFER_SIZE && !currentContextSongList.isEmpty()) {
            Song songToAdd = currentContextSongList.remove(0); // Remove the first song from the context list to add to the buffer
            if (!bufferList.contains(songToAdd)) {
                bufferList.add(songToAdd); // Add to the buffer if it's not already present
            }
        }
        updateBufferQueueUI(); // Refresh the UI to reflect the new buffer state

        // Automatically start playback if not already playing
        if (!isPlaying && !bufferList.isEmpty()) {
            playNextFromBuffer();
        }
    }


    // Skip to the next track
    private void skipNext() {
        // Simulate skipping to the next track in the buffer
        if (!bufferList.isEmpty()) {
            bufferList.remove(0); // Remove the current song from the queue
            playNextFromBuffer();
            updateBufferQueueUI();

        }
    }

    // Skip to the previous track
    void skipPrevious() {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    private void playNextFromBuffer() {
        if (!bufferList.isEmpty()) {
            Song nextSong = bufferList.get(0); // Look at the next song without removing it
            playTrack(mSpotifyAppRemote, nextSong);
            isPlaying = true;
            manageBufferList();
        }
    }

    private void updateBufferQueueUI() {
        List<Song> bufferedSongs = new ArrayList<>(bufferList);
        adapter.updateSongs(bufferedSongs); // Assuming SongsAdapter has an updateSongs method to refresh its data
        songListRecyclerView.setAdapter(adapter); // Refresh the adapter on the RecyclerView
    }

    private void onSongClicked(Song song) {
        int index = bufferList.indexOf(song);
        if (index != -1) {
            // Move the selected song to the start of the buffer (after the currently playing song)
            bufferList.remove(index);
            bufferList.add(1, song); // Assuming index 0 is the currently playing song
            playTrack(mSpotifyAppRemote, song);
            manageBufferList();
        }
    }

    private void updateCurrentSongUI(Song song) {
        ImageView albumCoverView = findViewById(R.id.currentSongAlbumCover);
        TextView titleView = findViewById(R.id.currentSongTitle);
        TextView artistView = findViewById(R.id.currentSongArtist);
        TextView durationView = findViewById(R.id.currentSongDuration);

        titleView.setText(song.getName());
        artistView.setText(song.getArtist());
        Picasso.get().load(song.getAlbumCoverUri()).into(albumCoverView);
        durationView.setText(song.getDuration());

    }

    private void onPlayPauseButtonClicked(View view) {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            if (isPlaying) {
                mSpotifyAppRemote.getPlayerApi().pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mSpotifyAppRemote.getPlayerApi().resume();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
            isPlaying = !isPlaying; // Toggle the state
        }
    }


}