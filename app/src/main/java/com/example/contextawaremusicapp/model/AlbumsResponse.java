package com.example.contextawaremusicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AlbumsResponse {
    @SerializedName("items")
    private List<AlbumItem> items;

    @SerializedName("next")
    private String next;

    public String getNext() {
        return next;
    }

    // Getter for the items list
    public List<AlbumItem> getItems() {
        return items;
    }

    public static class AlbumItem {
        @SerializedName("album")
        private Album album;

        // Getter for the album
        public Album getAlbum() {
            return album;
        }
    }

    public static class Album {
        @SerializedName("tracks")
        private Tracks tracks;

        // Getter for the tracks
        public Tracks getTracks() {
            return tracks;
        }
    }

    public static class Tracks {
        @SerializedName("items")
        private List<TrackItem> items;

        // Getter for the track items
        public List<TrackItem> getItems() {
            return items;
        }
    }

    public static class TrackItem {
        @SerializedName("id")
        private String id;

        // Getter for the id
        public String getId() {
            return id;
        }
    }
}
