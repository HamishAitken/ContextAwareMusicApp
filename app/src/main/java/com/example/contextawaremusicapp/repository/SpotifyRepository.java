package com.example.contextawaremusicapp.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.contextawaremusicapp.model.AlbumsResponse;
import com.example.contextawaremusicapp.model.AudioFeaturesResponse;
import com.example.contextawaremusicapp.model.PlaylistTracksResponse;
import com.example.contextawaremusicapp.model.PlaylistsResponse;
import com.example.contextawaremusicapp.model.RecommendationsResponse;
import com.example.contextawaremusicapp.model.SeveralTracksResponse;
import com.example.contextawaremusicapp.model.Song;
import com.example.contextawaremusicapp.model.UserSavedTracksResponse;
import com.example.contextawaremusicapp.network.SpotifyService;
import com.example.contextawaremusicapp.utils.ContextAlgorithm;
import com.example.contextawaremusicapp.utils.ContextManager;
import com.example.contextawaremusicapp.utils.ContextValues;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotifyRepository {
    private SpotifyService spotifyService;
    private ContextManager contextManager;
    private Context context;
    private String accessToken;
    private static final String PREFS_NAME = "SpotifyPrefs";
    private static final String KEY_AUDIO_FEATURES = "audio_features";
    private static final String KEY_RECOMMENDATION_RATIO = "recommendation_ratio";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public SpotifyRepository(Context context) {
        this.spotifyService = retrofit.create(SpotifyService.class);
        this.context = context;
        this.accessToken = getAccessToken();
        this.contextManager = new ContextManager(context); // Initialize the context manager
    }

    public interface CustomCallback<T> {
        void onSuccess(T response);
        void onFailure(Throwable t);
    }

    // Method to set the recommendation ratio
    public void setRecommendationRatio(int ratio) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(KEY_RECOMMENDATION_RATIO, ratio).apply();
    }

    // Method to get the recommendation ratio
    public int getRecommendationRatio() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_RECOMMENDATION_RATIO, 50); // Default to 50 if not set
    }

    // Utility method to convert List<AudioFeaturesResponse.AudioFeature> to JSON String
    private String audioFeaturesListToJson(List<AudioFeaturesResponse.AudioFeature> audioFeatures) {
        Gson gson = new Gson();
        return gson.toJson(audioFeatures);
    }

    // Utility method to convert JSON String back to List<AudioFeaturesResponse.AudioFeature>
    private List<AudioFeaturesResponse.AudioFeature> jsonToAudioFeaturesList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AudioFeaturesResponse.AudioFeature>>(){}.getType();
        List<AudioFeaturesResponse.AudioFeature> audioFeatures = gson.fromJson(json, type);
        return audioFeatures.stream().filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    // Call this method to save the fetched library data
    private void saveLibraryData(List<AudioFeaturesResponse.AudioFeature> audioFeatures) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = audioFeaturesListToJson(audioFeatures);
        sharedPreferences.edit().putString(KEY_AUDIO_FEATURES, json).apply();
    }

    // Call this method to load the saved library data
    private List<AudioFeaturesResponse.AudioFeature> loadLibraryData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_AUDIO_FEATURES, null);
        if (json == null) return new ArrayList<>(); // Return empty list if no data is found
        return jsonToAudioFeaturesList(json);
    }

    // Method to refresh the library data from the API and save it
    public void refreshLibraryData(CustomCallback<List<AudioFeaturesResponse.AudioFeature>> callback) {
        fetchUserLibraryData(new CustomCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> uniqueTrackIds) {
                fetchAudioFeaturesForTracks(uniqueTrackIds, new CustomCallback<List<AudioFeaturesResponse.AudioFeature>>() {
                    @Override
                    public void onSuccess(List<AudioFeaturesResponse.AudioFeature> audioFeatures) {
                        saveLibraryData(audioFeatures); // Save fetched audio features
                        callback.onSuccess(audioFeatures);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onFailure(t);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    // Method to check if data is saved and load it or fetch it if not
    public void ensureLibraryDataIsFetched(CustomCallback<List<AudioFeaturesResponse.AudioFeature>> callback) {
        List<AudioFeaturesResponse.AudioFeature> savedAudioFeatures = loadLibraryData();
        if (savedAudioFeatures.isEmpty()) {
            // Data is not saved, need to fetch
            refreshLibraryData(callback);
        } else {
            // Data is saved, can use it directly
            callback.onSuccess(savedAudioFeatures);
        }
    }

    public void generateUserPlaylist(CustomCallback<List<Song>> callback) {
        // Ensure library data is fetched and get audio features
        ensureLibraryDataIsFetched(new CustomCallback<List<AudioFeaturesResponse.AudioFeature>>() {
            @Override
            public void onSuccess(List<AudioFeaturesResponse.AudioFeature> audioFeatures) {
                // Get the current context
                ContextValues currentContext = contextManager.getCurrentContext();

                // Filter and sort songs based on the current context
                List<AudioFeaturesResponse.AudioFeature> filteredFeatures = ContextAlgorithm.filterSongsByContext(audioFeatures, currentContext, context);

                // Use the processed audio features to generate a list of unique track IDs
                List<String> trackIds = filteredFeatures.stream()
                        .map(AudioFeaturesResponse.AudioFeature::getId)
                        .collect(Collectors.toList());

                // Use the track IDs to create the final playlist
                createFinalPlaylist(trackIds, new CustomCallback<List<Song>>() {
                    @Override
                    public void onSuccess(List<Song> finalPlaylist) {
                        // Successfully created the final playlist, pass it to the callback
                        callback.onSuccess(finalPlaylist);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // Failed to create the final playlist, pass the error to the callback
                        callback.onFailure(t);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle failure (e.g., could not fetch audio features)
                callback.onFailure(t);
            }
        });
    }


    public void fetchRecommendations(List<String> seedTracks, int limit, Map<String, Double> targetAttributes, CustomCallback<List<Song>> callback) {
        String seedTracksString = String.join(",", seedTracks);
        spotifyService.getRecommendations(
                "Bearer " + accessToken,
                seedTracksString,
                limit,
                targetAttributes.get("tempo"),
                targetAttributes.get("energy"),
                targetAttributes.get("valence")
        ).enqueue(new Callback<RecommendationsResponse>() {
            @Override
            public void onResponse(Call<RecommendationsResponse> call, Response<RecommendationsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> recommendedSongs = response.body().getTracks().stream()
                            .map(track -> new Song(track.getId(), track.getName(), track.getArtists().get(0).getName(), track.getAlbum().getImages().get(0).getUrl(), track.getDurationMs()))
                            .collect(Collectors.toList());
                    callback.onSuccess(recommendedSongs);
                } else {
                    callback.onFailure(new IOException("Failed to fetch recommendations"));
                }
            }

            @Override
            public void onFailure(Call<RecommendationsResponse> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private void fetchUserLibraryData(CustomCallback<List<String>> finalCallback) {
        Set<String> trackIds = new HashSet<>();

        // Start with fetching liked songs
        fetchLikedSongs(trackIds, 0, new Runnable() {
            @Override
            public void run() {
                // After liked songs are fetched, proceed to fetch playlists
                fetchPlaylists(trackIds, 0, new Runnable() {
                    @Override
                    public void run() {
                        // After playlists are fetched, proceed to fetch albums
                        fetchAlbums(trackIds, 0, new Runnable() {
                            @Override
                            public void run() {
                                // After albums are fetched, all track IDs are collected
                                List<String> uniqueTrackIds = new ArrayList<>(trackIds);
                                finalCallback.onSuccess(uniqueTrackIds); // Convert Set to List to remove duplicates and call final callback
                            }
                        });
                    }
                });
            }
        });
    }

    private void fetchLikedSongs(Set<String> trackIds, int offset, Runnable onComplete) {
        spotifyService.getLikedSongs("Bearer " + accessToken, 50, offset).enqueue(new Callback<UserSavedTracksResponse>() {

            @Override
            public void onResponse(Call<UserSavedTracksResponse> call, Response<UserSavedTracksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (UserSavedTracksResponse.SavedTrack savedTrack : response.body().getItems()) {
                        String trackId = savedTrack.getTrack().getId();
                        trackIds.add(trackId);
                    }
                    // Check if there's a "next" URL indicating more data to fetch
                    if (response.body().getNext() != null && !response.body().getNext().isEmpty()) {
                        // Simply add 50 to the offset for the next call
                        fetchLikedSongs(trackIds, offset + 50, onComplete);
                    } else {
                        onComplete.run(); // No more data to fetch
                    }
                } else {
                    // API call was not successful; proceed to avoid deadlock
                    Log.e("SpotifyRepository", "API call failed: " + response.message());
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<UserSavedTracksResponse> call, Throwable t) {
                // API call failed; proceed to avoid deadlock
                Log.e("SpotifyRepository", "API call failed: " + t.getMessage());
                onComplete.run();
            }
        });
    }

    private void fetchPlaylists(Set<String> trackIds, int offset, Runnable onComplete) {
        spotifyService.getUsersPlaylists("Bearer " + accessToken, 50, offset).enqueue(new Callback<PlaylistsResponse>() {
            @Override
            public void onResponse(Call<PlaylistsResponse> call, Response<PlaylistsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PlaylistsResponse.PlaylistItem> playlists = response.body().getItems();

                    AtomicInteger playlistsToProcess = new AtomicInteger(playlists.size());

                    if (playlists.isEmpty()) {
                        onComplete.run(); // If no playlists, complete the operation
                        return;
                    }

                    for (PlaylistsResponse.PlaylistItem playlist : playlists) {
                        // For each playlist, fetch its tracks
                        fetchTracksForPlaylist(playlist.getId(), trackIds, new Runnable() {
                            @Override
                            public void run() {
                                if (playlistsToProcess.decrementAndGet() == 0) {
                                    // Check if there's a "next" URL indicating more data to fetch
                                    if (response.body().getNext() != null && !response.body().getNext().isEmpty()) {
                                        // Call fetchPlaylists again with updated offset for next page
                                        fetchPlaylists(trackIds, offset + 50, onComplete);
                                    } else {
                                        onComplete.run(); // Completed all playlists
                                    }
                                }
                            }
                        });
                    }
                } else {
                    onComplete.run(); // Proceed in case of API error to avoid deadlock
                }
            }

            @Override
            public void onFailure(Call<PlaylistsResponse> call, Throwable t) {
                onComplete.run(); // Proceed in case of network failure to avoid deadlock
            }
        });
    }

    private void fetchTracksForPlaylist(String playlistId, Set<String> trackIds, Runnable onComplete) {
        fetchTracksPage(playlistId, "Bearer " + accessToken, 0, trackIds, onComplete);
    }

    private void fetchTracksPage(String playlistId, String accessToken, int offset, Set<String> trackIds, Runnable onComplete) {
        spotifyService.getPlaylistTracks(playlistId, accessToken, "items(track(id))", 50, offset).enqueue(new Callback<PlaylistTracksResponse>() {
            @Override
            public void onResponse(Call<PlaylistTracksResponse> call, Response<PlaylistTracksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Add the current page's track IDs to the set
                    for (PlaylistTracksResponse.PlaylistTrackItem trackItem : response.body().getItems()) {
                        trackIds.add(trackItem.getTrack().getId());
                    }

                    // Check if there's a "next" URL indicating more data to fetch
                    if (response.body().getNext() != null && !response.body().getNext().isEmpty()) {
                        // Fetch the next page
                        fetchTracksPage(playlistId, accessToken, offset + 50, trackIds, onComplete);
                    } else {
                        // No more pages, this playlist is complete
                        onComplete.run();
                    }
                } else {
                    // API call was not successful; proceed to avoid deadlock
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<PlaylistTracksResponse> call, Throwable t) {
                // API call failed; proceed to avoid deadlock
                onComplete.run();
            }
        });
    }

    private void fetchAlbums(Set<String> trackIds, int offset, Runnable onComplete) {
        spotifyService.getUsersAlbums("Bearer " + accessToken, 50, offset).enqueue(new Callback<AlbumsResponse>() {
            @Override
            public void onResponse(Call<AlbumsResponse> call, Response<AlbumsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (AlbumsResponse.AlbumItem albumItem : response.body().getItems()) {
                        for (AlbumsResponse.TrackItem trackItem : albumItem.getAlbum().getTracks().getItems()) {
                            trackIds.add(trackItem.getId());
                        }
                    }

                    if (response.body().getNext() != null && !response.body().getNext().isEmpty()) {
                        fetchAlbums(trackIds, offset + 50, onComplete);
                    } else {
                        onComplete.run();
                    }
                } else {
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<AlbumsResponse> call, Throwable t) {
                onComplete.run();
            }
        });
    }

    private void fetchAudioFeaturesForTracks(List<String> trackIds, CustomCallback<List<AudioFeaturesResponse.AudioFeature>> customCallback) {
        fetchAudioFeaturesBatch(trackIds, 0, new ArrayList<>(), customCallback);
    }

    private void fetchAudioFeaturesBatch(List<String> trackIds, int startIndex, List<AudioFeaturesResponse.AudioFeature> accumulatedFeatures, CustomCallback<List<AudioFeaturesResponse.AudioFeature>> customCallback) {
        // Determine the end index for the current batch (exclusive)
        int endIndex = Math.min(startIndex + 100, trackIds.size());
        List<String> currentBatch = trackIds.subList(startIndex, endIndex);
        String trackIdsString = String.join(",", currentBatch);

        spotifyService.getAudioFeaturesForTracks("Bearer " + accessToken, trackIdsString).enqueue(new Callback<AudioFeaturesResponse>() {
            @Override
            public void onResponse(Call<AudioFeaturesResponse> call, Response<AudioFeaturesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getAudioFeatures() != null) {
                    accumulatedFeatures.addAll(response.body().getAudioFeatures());

                    if (endIndex < trackIds.size()) {
                        // More batches to process
                        fetchAudioFeaturesBatch(trackIds, endIndex, accumulatedFeatures, customCallback);
                    } else {
                        // All batches processed
                        customCallback.onSuccess(accumulatedFeatures);
                    }
                } else {
                    customCallback.onFailure(new Exception("Failed to fetch audio features: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<AudioFeaturesResponse> call, Throwable t) {
                customCallback.onFailure(t);
            }
        });
    }

    public void createFinalPlaylist(List<String> filteredSongs, CustomCallback<List<Song>> callback) {
        Log.d("SpotifyRepository", getTrackIdsString(filteredSongs));
        spotifyService.getSeveralTracks("Bearer " + accessToken, getTrackIdsString(filteredSongs)).enqueue(new Callback<SeveralTracksResponse>() {
            @Override
            public void onResponse(Call<SeveralTracksResponse> call, Response<SeveralTracksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTracks() != null) {
                    List<Song> finalPlaylist = new ArrayList<>();
                    List<SeveralTracksResponse.TrackItem> tracks = response.body().getTracks();
                    for (SeveralTracksResponse.TrackItem trackItem : tracks) {
                        if (trackItem != null) {
                            String id = trackItem.getId();
                            String name = trackItem.getName();
                            // Ensure there is at least one artist before getting the name
                            String artist = (trackItem.getArtists() != null && !trackItem.getArtists().isEmpty()) ? trackItem.getArtists().get(0).getName() : "Unknown Artist";
                            String albumCoverUri = getAlbumCoverUri(trackItem.getAlbum());
                            int durationMs = trackItem.getDurationMs();
                            Song song = new Song(id, name, artist, albumCoverUri, durationMs);
                            finalPlaylist.add(song);
                        }
                    }
                    callback.onSuccess(finalPlaylist);
                } else {
                    // Log the error and handle unsuccessful response
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : null;
                    } catch (IOException e) {
                        Log.e("SpotifyRepository", "Error parsing error body", e);
                    }
                    Log.e("SpotifyRepository", "Failed to fetch tracks: " + response.code() + ", " + errorBody);
                    callback.onFailure(new Exception("Failed to fetch tracks: " + response.code() + ", " + errorBody));
                }
            }

            @Override
            public void onFailure(Call<SeveralTracksResponse> call, Throwable t) {
                // Handle network failure
                Log.e("SpotifyRepository", "Network error when fetching tracks", t);
                callback.onFailure(t);
            }
        });
    }

    private String getTrackIdsString(List<String> trackIds) {
        StringBuilder builder = new StringBuilder();

        // Shuffle the list to randomize the order
        Collections.shuffle(trackIds);

        // Limit to the first 50 IDs after shuffling
        int limit = Math.min(trackIds.size(), 50);
        for (int i = 0; i < limit; i++) {
            builder.append(trackIds.get(i)).append(",");
        }

        // Remove the last comma if the builder has content
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    private String getAlbumCoverUri(SeveralTracksResponse.Album album) {
        // Choose appropriate image URL from album images
        List<SeveralTracksResponse.Image> images = album.getImages();
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl(); // Assuming the first image is the desired album cover
        }
        return null; // No album cover available
    }

    private String getAccessToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SpotifyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", null); // 'null' is the default value if the token doesn't exist
    }
}
