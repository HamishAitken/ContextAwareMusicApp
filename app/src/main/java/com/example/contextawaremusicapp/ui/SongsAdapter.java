package com.example.contextawaremusicapp.ui;

import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contextawaremusicapp.R;
import com.example.contextawaremusicapp.model.Song;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {
    private List<Song> songs;
    private final OnItemClickListener onItemClickListener;
    private int currentlyPlayingIndex = -1;

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public SongsAdapter(List<Song> songs, OnItemClickListener onItemClickListener) {
        this.songs = songs;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position == currentlyPlayingIndex, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setCurrentlyPlayingIndex(int index) {
        this.currentlyPlayingIndex = index;
        notifyDataSetChanged(); // Refresh the adapter to update the highlighted item
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged(); // Refresh the whole dataset
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCover;
        TextView songTitle;
        TextView artistName;
        TextView duration;

        SongViewHolder(View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.ivAlbumCover);
            songTitle = itemView.findViewById(R.id.tvSongTitle);
            artistName = itemView.findViewById(R.id.tvArtistName);
            duration = itemView.findViewById(R.id.tvDuration);
        }

        void bind(Song song, boolean isPlaying, OnItemClickListener listener) {
            songTitle.setText(song.getName());
            artistName.setText(song.getArtist());
            duration.setText(formatDuration(song.getDuration()));
            Picasso.get().load(song.getAlbumCoverUri()).into(albumCover);

            // Use ContextCompat.getColor() for compatibility with newer API levels
            itemView.setBackgroundColor(isPlaying ?
                    ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_light) :
                    ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));

            itemView.setOnClickListener(v -> listener.onItemClick(song));
        }

        private String formatDuration(int duration) {
            long minutes = duration / 60000;
            long seconds = (duration % 60000) / 1000;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}