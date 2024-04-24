package com.example.contextawaremusicapp.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class PlaylistTracksResponse {
    @SerializedName("items")
    private List<PlaylistTrackItem> items;

    @SerializedName("next")
    private String next;

    public String getNext() {
        return next;
    }

    public List<PlaylistTrackItem> getItems() {
        return items;
    }

    public static class PlaylistTrackItem {
        @SerializedName("track")
        private Track track;

        public Track getTrack() {
            return track;
        }
    }

    public static class Track {
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }
    }
}
