package com.example.contextawaremusicapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.contextawaremusicapp.model.AudioFeaturesResponse.AudioFeature;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextAlgorithm {

    private static final String PREFS_NAME = "SpotifyPrefs";
    private static final String KEY_RULES = "rules";

    // Method to filter songs by dynamic context
    public static List<AudioFeature> filterSongsByContext(
            List<AudioFeature> audioFeatures,
            ContextValues contextValues,
            Context context) {  // Accept ContextValues as a parameter

        // Apply user-defined rules first
        List<AudioFeature> ruledSongs = applyUserRules(audioFeatures, contextValues, context);
        if (!ruledSongs.isEmpty()) {
            return ruledSongs;
        }

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

    private static List<AudioFeature> applyUserRules(List<AudioFeature> audioFeatures, ContextValues contextValues, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> rules = sharedPreferences.getStringSet(KEY_RULES, new HashSet<>());

        for (String rule : rules) {
            String[] parts = rule.split("\\|");
            String ruleContext = parts[0];
            String ruleMusic = parts[1];

            if (contextMatches(ruleContext, contextValues)) {
                return audioFeatures.stream()
                        .filter(feature -> featureMatchesMusic(feature, ruleMusic))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private static boolean contextMatches(String ruleContext, ContextValues contextValues) {
        // Logic to check if the current context matches the rule context
        String[] parts = ruleContext.split(",");
        if (parts.length != 4) return false;
        return parts[0].equals(contextValues.getLocation().name()) &&
                parts[1].equals(contextValues.getTimeOfDay().name()) &&
                parts[2].equals(contextValues.getWeather().name()) &&
                parts[3].equals(contextValues.getActivity().name());
    }

    private static boolean featureMatchesMusic(AudioFeature feature, String musicType) {
        switch (musicType.toLowerCase()) {
            case "jazz":
                return feature.getAcousticness() > 0.5 && feature.getEnergy() < 0.5;
            case "metal":
                return feature.getEnergy() > 0.8 && feature.getLoudness() > -5;
            case "pop":
                return feature.getDanceability() > 0.7 && feature.getValence() > 0.5;
            case "upbeat":
                return feature.getTempo() > 120 && feature.getValence() > 0.6;
            case "slow":
                return feature.getTempo() < 80 && feature.getEnergy() < 0.5;
            case "happy":
                return feature.getValence() > 0.7;
            case "sad":
                return feature.getValence() < 0.3;
            case "rock":
                return feature.getEnergy() > 0.6 && feature.getDanceability() < 0.6;
            case "acoustic":
                return feature.getAcousticness() > 0.7;
            default:
                return false; // Return false if the music type is unknown
        }
    }

    private static void setupContextPreferences(Map<String, Double> contextPreferences, ContextValues contextValues) {
        // Set preferences based on weather context
        switch (contextValues.getWeather()) {
            case SUNNY:
                contextPreferences.put("tempo", 150.0);
                contextPreferences.put("valence", 0.75);
                break;
            case RAINY:
                contextPreferences.put("acousticness", 0.5);
                break;
            case CLOUDY:
                contextPreferences.put("energy", 0.4);
                break;
            case SNOWY:
                contextPreferences.put("instrumentalness", 0.6);
                break;
        }

        // Set preferences based on time of day
        switch (contextValues.getTimeOfDay()) {
            case MORNING:
                contextPreferences.put("energy", 0.6);
                contextPreferences.put("tempo", 120.0);
                break;
            case AFTERNOON:
                contextPreferences.put("danceability", 0.7);
                break;
            case EVENING:
                contextPreferences.put("valence", 0.5);
                break;
            case NIGHT:
                contextPreferences.put("loudness", -10.0);
                break;
        }

        // Set preferences based on location
        switch (contextValues.getLocation()) {
            case HOME:
                contextPreferences.put("speechiness", 0.1);
                break;
            case WORK:
                contextPreferences.put("instrumentalness", 0.5);
                break;
            case GYM:
                contextPreferences.put("energy", 0.8);
                contextPreferences.put("tempo", 140.0);
                break;
        }

        // Set preferences based on activity
        switch (contextValues.getActivity()) {
            case WALKING:
                contextPreferences.put("tempo", 100.0);
                break;
            case RUNNING:
                contextPreferences.put("tempo", 160.0);
                contextPreferences.put("energy", 0.9);
                break;
            case DRIVING:
                contextPreferences.put("danceability", 0.6);
                contextPreferences.put("energy", 0.7);
                break;
        }
    }

    public static Map<String, Double> getTargetAttributes(ContextValues contextValues) {
        Map<String, Double> targetAttributes = new HashMap<>();
        switch (contextValues.getWeather()) {
            case SUNNY:
                targetAttributes.put("tempo", 150.0);
                targetAttributes.put("valence", 0.75);
                break;
            case RAINY:
                targetAttributes.put("acousticness", 0.5);
                break;
            case CLOUDY:
                targetAttributes.put("energy", 0.4);
                break;
            case SNOWY:
                targetAttributes.put("instrumentalness", 0.6);
                break;
        }
        switch (contextValues.getTimeOfDay()) {
            case MORNING:
                targetAttributes.put("energy", 0.6);
                targetAttributes.put("tempo", 120.0);
                break;
            case AFTERNOON:
                targetAttributes.put("danceability", 0.7);
                break;
            case EVENING:
                targetAttributes.put("valence", 0.5);
                break;
            case NIGHT:
                targetAttributes.put("loudness", -10.0);
                break;
        }
        switch (contextValues.getLocation()) {
            case HOME:
                targetAttributes.put("speechiness", 0.1);
                break;
            case WORK:
                targetAttributes.put("instrumentalness", 0.5);
                break;
            case GYM:
                targetAttributes.put("energy", 0.8);
                targetAttributes.put("tempo", 140.0);
                break;
        }
        switch (contextValues.getActivity()) {
            case WALKING:
                targetAttributes.put("tempo", 100.0);
                break;
            case RUNNING:
                targetAttributes.put("tempo", 160.0);
                targetAttributes.put("energy", 0.9);
                break;
            case DRIVING:
                targetAttributes.put("danceability", 0.6);
                targetAttributes.put("energy", 0.7);
                break;
        }
        return targetAttributes;
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
            case "valence":
                return feature.getValence();
            case "acousticness":
                return feature.getAcousticness();
            case "energy":
                return feature.getEnergy();
            case "instrumentalness":
                return feature.getInstrumentalness();
            case "danceability":
                return feature.getDanceability();
            case "loudness":
                return feature.getLoudness();
            case "speechiness":
                return feature.getSpeechiness();
            default:
                return 0;
        }
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
