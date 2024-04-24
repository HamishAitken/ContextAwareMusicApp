package com.example.contextawaremusicapp.network;

import com.example.contextawaremusicapp.model.AlbumsResponse;
import com.example.contextawaremusicapp.model.AudioFeaturesResponse;
import com.example.contextawaremusicapp.model.PlaylistTracksResponse;
import com.example.contextawaremusicapp.model.PlaylistsResponse;
import com.example.contextawaremusicapp.model.SeveralTracksResponse;
import com.example.contextawaremusicapp.model.UserSavedTracksResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface SpotifyService {
    @GET("/v1/me/tracks")
    Call<UserSavedTracksResponse> getLikedSongs(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("/v1/me/playlists")
    Call<PlaylistsResponse> getUsersPlaylists(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("/v1/playlists/{playlist_id}/tracks")
    Call<PlaylistTracksResponse> getPlaylistTracks(
            @Path("playlist_id") String playlistId,
            @Header("Authorization") String authToken,
            @Query("fields") String fields,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("/v1/me/albums")
    Call<AlbumsResponse> getUsersAlbums(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("/v1/tracks")
    Call<SeveralTracksResponse> getSeveralTracks(
            @Header("Authorization") String authToken,
            @Query("ids") String trackIds
    );

    @GET("/v1/audio-features")
    Call<AudioFeaturesResponse> getAudioFeaturesForTracks(
            @Header("Authorization") String authToken,
            @Query("ids") String trackIds
    );
}
