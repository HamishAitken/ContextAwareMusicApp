package com.example.contextawaremusicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserSavedTracksResponse {
    @SerializedName("items")
    private List<SavedTrack> items;

    @SerializedName("next")
    private String next;

    public String getNext() {
        return next;
    }

    // Getter for the items list
    public List<SavedTrack> getItems() {
        return items;
    }

    public static class SavedTrack {
        @SerializedName("track")
        private Track track;

        // Getter for the track
        public Track getTrack() {
            return track;
        }
    }

    public static class Track {
        @SerializedName("id")
        private String id;

        // Getter for the id
        public String getId() {
            return id;
        }
    }
}
