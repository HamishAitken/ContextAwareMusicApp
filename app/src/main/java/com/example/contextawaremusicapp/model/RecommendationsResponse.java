package com.example.contextawaremusicapp.model;

import java.util.List;

public class RecommendationsResponse {
    private List<TrackItem> tracks;

    public List<TrackItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackItem> tracks) {
        this.tracks = tracks;
    }

    public static class TrackItem {
        private String id;
        private String name;
        private List<ArtistItem> artists;
        private AlbumItem album;
        private int duration_ms;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ArtistItem> getArtists() {
            return artists;
        }

        public void setArtists(List<ArtistItem> artists) {
            this.artists = artists;
        }

        public AlbumItem getAlbum() {
            return album;
        }

        public void setAlbum(AlbumItem album) {
            this.album = album;
        }

        public int getDurationMs() {
            return duration_ms;
        }

        public void setDurationMs(int duration_ms) {
            this.duration_ms = duration_ms;
        }
    }

    public static class ArtistItem {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class AlbumItem {
        private List<ImageItem> images;

        public List<ImageItem> getImages() {
            return images;
        }

        public void setImages(List<ImageItem> images) {
            this.images = images;
        }
    }

    public static class ImageItem {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
