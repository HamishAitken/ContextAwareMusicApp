package com.example.contextawaremusicapp.model;

public class Song {
    private String id;
    private String name;
    private String artist;
    private String albumCoverUri;
    private int durationMs;

    public Song(String id, String name, String artist,String albumCoverUri ,int durationMs) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.albumCoverUri = albumCoverUri;
        this.durationMs = durationMs;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumCoverUri() {
        return albumCoverUri;
    }

    public int getDuration(){return durationMs;}

    // You can also include setter methods if needed.
}
