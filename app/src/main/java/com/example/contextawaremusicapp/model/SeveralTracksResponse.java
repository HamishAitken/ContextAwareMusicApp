package com.example.contextawaremusicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeveralTracksResponse {
    @SerializedName("tracks")
    private List<TrackItem> tracks;

    public List<TrackItem> getTracks() {
        return tracks;
    }

    public static class TrackItem {
        @SerializedName("album")
        private Album album;

        @SerializedName("artists")
        private List<Artist> artists;

        @SerializedName("duration_ms")
        private int durationMs;

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public Album getAlbum() {
            return album;
        }

        public List<Artist> getArtists() {
            return artists;
        }

        public int getDurationMs() {
            return durationMs;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class Album {
        @SerializedName("images")
        private List<Image> images;

        public List<Image> getImages() {
            return images;
        }
    }

    public static class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }

    public static class Artist {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }
}
