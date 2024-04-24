package com.example.contextawaremusicapp.utils;
import com.example.contextawaremusicapp.model.AudioFeaturesResponse.AudioFeature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextAlgorithm {

    // Method to filter songs by dynamic context
    public static List<AudioFeature> filterSongsByContext(
            List<AudioFeature> audioFeatures,
            ContextValues contextValues) {  // Accept ContextValues as a parameter

        // Define preferences based on current context provided by ContextManager
        Map<String, Double> contextPreferences = new HashMap<>();
        setupContextPreferences(contextPreferences, contextValues);

        // Score the songs based on how their features match the preferences
        List<SongScore> scoredSongs = scoreAudioFeatures(audioFeatures, contextPreferences);

        // Sort and convert to list
        return scoredSongs.stream()
                .sorted(Comparator.comparingDouble(SongScore::getScore).reversed())
                .map(SongScore::getAudioFeature)
                .collect(Collectors.toList());
    }

    private static void setupContextPreferences(Map<String, Double> contextPreferences, ContextValues contextValues) {
        // Example: Adjust preferences based on weather
        switch (contextValues.getWeather()) {
            case SUNNY:
                contextPreferences.put("tempo", 150.0);
                contextPreferences.put("valence", 0.75);
                break;
            case RAINY:
                contextPreferences.put("acousticness", 0.5);
                break;
            // Additional context handling...
        }
        // Repeat for other context factors (timeOfDay, location, activity)
    }

    private static List<SongScore> scoreAudioFeatures(List<AudioFeature> features, Map<String, Double> preferences) {
        List<SongScore> scores = new ArrayList<>();
        for (AudioFeature feature : features) {
            double score = calculateScore(feature, preferences);
            scores.add(new SongScore(feature, score));
        }
        return scores;
    }

    private static double calculateScore(AudioFeature feature, Map<String, Double> preferences) {
        double score = 0;
        for (Map.Entry<String, Double> entry : preferences.entrySet()) {
            double featureValue = getFeatureValue(feature, entry.getKey());
            score += 1.0 - Math.abs(featureValue - entry.getValue());
        }
        return score;
    }

    private static double getFeatureValue(AudioFeature feature, String key) {
        // Implement retrieval of feature values based on key
        switch (key) {
            case "tempo":
                return feature.getTempo();
            case "energy":
                return feature.getEnergy();
            // Add more cases as needed
        }
        return 0;
    }

    static class SongScore {
        private final AudioFeature audioFeature;
        private final double score;

        public SongScore(AudioFeature feature, double score) {
            this.audioFeature = feature;
            this.score = score;
        }

        public AudioFeature getAudioFeature() {
            return audioFeature;
        }

        public double getScore() {
            return score;
        }
    }
}
