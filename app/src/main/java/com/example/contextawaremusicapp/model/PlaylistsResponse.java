package com.example.contextawaremusicapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaylistsResponse {
    @SerializedName("items")
    private List<PlaylistItem> items;

    @SerializedName("next")
    private String next;

    public String getNext() {
        return next;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }

    public static class PlaylistItem {
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }
    }
}
