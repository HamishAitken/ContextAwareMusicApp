package com.example.contextawaremusicapp;

import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contextawaremusicapp.model.Song;
import com.example.contextawaremusicapp.repository.SpotifyRepository;
import com.example.contextawaremusicapp.ui.SongsAdapter;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
        songListRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize your songs list
        spotifyRepository = new SpotifyRepository(this);

        // Initialize the DrawerLayout and NavigationView for the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_options) {
                fragment = new OptionsFragment();
            } else if (id == R.id.nav_locations) {
                fragment = new LocationsFragment();
            } else if (id == R.id.nav_rules) {
                fragment = new RulesFragment();
            } else if (id == R.id.nav_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // This flag returns to the existing MainActivity, clearing all on top
                startActivity(intent);
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set up click listener for imageView2 to open the navigation drawer
        ImageView imageView2 = findViewById(R.id.imageView2);
        imageView2.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeBufferQueueUI();
        setupPlaybackProgressListener();

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
                            playPauseButton.setImageResource(isPlaying ? R.drawable._665737_pause_icon : R.drawable._695059_music_play_play_button_player_icon);
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
        final int progressCheckInterval = 1000;
        final Handler handler = new Handler();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mSpotifyAppRemote != null) {
                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
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
            // Log.e("SpotifyPlayTrack", "Could not play track", error);
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
        // Check if the buffer list is empty and current song is not playing
        if (bufferList.isEmpty() && !isPlaying) {
            // Automatically start playback if not already playing
            skipNext();
        }

        while (bufferList.size() < BUFFER_SIZE && !currentContextSongList.isEmpty()) {
            Song songToAdd = currentContextSongList.remove(0);
            if (!bufferList.contains(songToAdd) && !songToAdd.equals(currentSong)) {
                bufferList.add(songToAdd);
            }
        }
        songListRecyclerView.smoothScrollToPosition(0);
        updateBufferQueueUI();
    }

    // Skip to the next track
    private void skipNext() {
        if (!bufferList.isEmpty()) {
            Song nextSong = bufferList.remove(0); // Remove the next song from the buffer to make it current
            playTrack(mSpotifyAppRemote, nextSong); // Play the new current song
            manageBufferList(); // Ensure the buffer is refilled correctly
            updateBufferQueueUI(); // Update the UI to reflect changes
        }
    }

    // Skip to the previous track
    void skipPrevious() {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    private void updateBufferQueueUI() {
        List<Song> bufferedSongs = new ArrayList<>(bufferList);
        adapter.updateSongs(bufferedSongs);
        songListRecyclerView.setAdapter(adapter); // Refresh the adapter on the RecyclerView
    }

    private void onSongClicked(Song song) {
        int index = bufferList.indexOf(song);
        if (index != -1) {
            bufferList.remove(index); // Remove the song from the buffer list
            playTrack(mSpotifyAppRemote, song); // Play the song immediately
            manageBufferList(); // Update and refill the buffer list as needed
            updateBufferQueueUI(); // Update the UI with the new state of the buffer
        }
    }

    private void updateCurrentSongUI(Song song) {
        currentSong = song;
        ImageView albumCoverView = findViewById(R.id.currentSongAlbumCover);
        TextView titleView = findViewById(R.id.currentSongTitle);
        TextView artistView = findViewById(R.id.currentSongArtist);
        TextView durationView = findViewById(R.id.currentSongDuration);

        titleView.setText(song.getName());
        artistView.setText(song.getArtist());
        Picasso.get().load(song.getAlbumCoverUri()).into(albumCoverView);
        durationView.setText(formatDuration(song.getDuration()));
    }

    private String formatDuration(int duration) {
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void onPlayPauseButtonClicked(View view) {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            if (isPlaying) {
                mSpotifyAppRemote.getPlayerApi().pause();
                playPauseButton.setImageResource(R.drawable._695059_music_play_play_button_player_icon);
            } else {
                mSpotifyAppRemote.getPlayerApi().resume();
                playPauseButton.setImageResource(R.drawable._665737_pause_icon);
            }
            isPlaying = !isPlaying; // Toggle the state
        }
    }
}
