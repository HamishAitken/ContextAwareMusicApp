package com.example.contextawaremusicapp.utils;

public class ContextValues {

    // Enum for different types of locations
    public enum Location {
        HOME, WORK, GYM
    }

    // Enum for different parts of the day
    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING, NIGHT
    }

    // Enum for different weather conditions
    public enum Weather {
        SUNNY, RAINY, CLOUDY, SNOWY
    }

    // Enum for different types of activities
    public enum Activity {
        WALKING, RUNNING, DRIVING
    }

    private Location location;
    private TimeOfDay timeOfDay;
    private Weather weather;
    private Activity activity;

    // Constructor
    public ContextValues(Location location, TimeOfDay timeOfDay, Weather weather, Activity activity) {
        this.location = location;
        this.timeOfDay = timeOfDay;
        this.weather = weather;
        this.activity = activity;
    }

    // Getters
    public Location getLocation() {
        return location;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public Weather getWeather() {
        return weather;
    }

    public Activity getActivity() {
        return activity;
    }

    // Setters if you need to change context values after object creation
    public void setLocation(Location location) {
        this.location = location;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}