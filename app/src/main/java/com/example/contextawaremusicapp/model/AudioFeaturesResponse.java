package com.example.contextawaremusicapp.model;

import java.util.List;

// Define a class to encapsulate the overall audio features response
public class AudioFeaturesResponse {
    private List<AudioFeature> audio_features;

    // Constructor
    public AudioFeaturesResponse(List<AudioFeature> audio_features) {
        this.audio_features = audio_features;
    }

    // Getter
    public List<AudioFeature> getAudioFeatures() {
        return audio_features;
    }

    // Define a class to encapsulate the audio features of a single track
    public static class AudioFeature {
        private double acousticness;
        private String analysis_url;
        private double danceability;
        private int duration_ms;
        private double energy;
        private String id;
        private double instrumentalness;
        private int key;
        private double liveness;
        private double loudness;
        private int mode;
        private double speechiness;
        private double tempo;
        private int time_signature;
        private String track_href;
        private String type;
        private String uri;
        private double valence;

        public double getAcousticness() {
            return acousticness;
        }

        public String getAnalysisUrl() {
            return analysis_url;
        }

        public double getDanceability() {
            return danceability;
        }

        public int getDurationMs() {
            return duration_ms;
        }

        public double getEnergy() {
            return energy;
        }

        public String getId() {
            return id;
        }

        public double getInstrumentalness() {
            return instrumentalness;
        }

        // Continue for other fields...
        public double getTempo() {
            return tempo;
        }
        public double getValence() {
            return valence;
        }

        // toString method for debugging
        @Override
        public String toString() {
            return "AudioFeature{" +
                    "acousticness=" + acousticness +
                    ", danceability=" + danceability +
                    ", id='" + id + '\'' +
                    ", energy=" + energy +
                    ", tempo=" + tempo +
                    ", valence=" + valence +
                    '}';
        }
    }
}


