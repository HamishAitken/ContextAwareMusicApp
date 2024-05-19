package com.example.contextawaremusicapp.model;

import java.util.List;


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

        // Getters
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

        public int getKey() {
            return key;
        }

        public double getLiveness() {
            return liveness;
        }

        public double getLoudness() {
            return loudness;
        }

        public int getMode() {
            return mode;
        }

        public double getSpeechiness() {
            return speechiness;
        }

        public double getTempo() {
            return tempo;
        }

        public int getTimeSignature() {
            return time_signature;
        }

        public String getTrackHref() {
            return track_href;
        }

        public String getType() {
            return type;
        }

        public String getUri() {
            return uri;
        }

        public double getValence() {
            return valence;
        }

        // toString method for debugging
        @Override
        public String toString() {
            return "AudioFeature{" +
                    "acousticness=" + acousticness +
                    ", analysis_url='" + analysis_url + '\'' +
                    ", danceability=" + danceability +
                    ", duration_ms=" + duration_ms +
                    ", energy=" + energy +
                    ", id='" + id + '\'' +
                    ", instrumentalness=" + instrumentalness +
                    ", key=" + key +
                    ", liveness=" + liveness +
                    ", loudness=" + loudness +
                    ", mode=" + mode +
                    ", speechiness=" + speechiness +
                    ", tempo=" + tempo +
                    ", time_signature=" + time_signature +
                    ", track_href='" + track_href + '\'' +
                    ", type='" + type + '\'' +
                    ", uri='" + uri + '\'' +
                    ", valence=" + valence +
                    '}';
        }
    }
}
