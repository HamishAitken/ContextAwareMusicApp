package com.example.contextawaremusicapp.model;

public class UserOptions {
    // Define user option properties here
    // For example, the preferred number of tracks in a playlist
    private int preferredPlaylistSize = 30;

    public UserOptions(int preferredPlaylistSize) {
        this.preferredPlaylistSize = preferredPlaylistSize;
    }

    // Getter and Setter
    public int getPreferredPlaylistSize() {
        return preferredPlaylistSize;
    }

    public void setPreferredPlaylistSize(int preferredPlaylistSize) {
        this.preferredPlaylistSize = preferredPlaylistSize;
    }
}
