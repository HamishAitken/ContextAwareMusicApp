package com.example.contextawaremusicapp.utils;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.contextawaremusicapp.utils.ContextValues;
import com.example.contextawaremusicapp.utils.ContextValues.Activity;
import com.example.contextawaremusicapp.utils.ContextValues.Location;
import com.example.contextawaremusicapp.utils.ContextValues.TimeOfDay;
import com.example.contextawaremusicapp.utils.ContextValues.Weather;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;

public class ContextManager {

    private ContextValues currentContext;
    private ActivityRecognitionClient activityRecognitionClient;
    private Context appContext;
    private LocationManager locationManager;

    private static final double HOME_LAT = 56.45806050801304;  // Example latitude for home
    private static final double HOME_LNG = -2.976413328769624;  // Example longitude for home
    private static final double WORK_LAT = 56.46006050801304;  // Example latitude for work
    private static final double WORK_LNG = -2.978413328769624;  // Example longitude for work
    private static final double GYM_LAT = 56.46206050801304;   // Example latitude for gym
    private static final double GYM_LNG = -2.980413328769624;   // Example longitude for gym
    private static final float LOCATION_RADIUS = 100; // Radius in meters to consider as "at location"

    public ContextManager(Context context) {
        this.appContext = context;
        this.activityRecognitionClient = ActivityRecognition.getClient(appContext);
        this.locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        currentContext = new ContextValues(Location.HOME, TimeOfDay.MORNING, Weather.SUNNY, Activity.STILL);
        requestActivityUpdates();
        updateTimeOfDay();
        updateWeather();
        requestLocationUpdates();
    }

    private void requestActivityUpdates() {
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                0,  // Detection interval (0 for best-effort detection)
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(result -> Log.d("ActivityRecognition", "Successfully requested activity updates"));
        task.addOnFailureListener(e -> Log.w("ActivityRecognition", "Requesting activity updates failed to start", e));
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(appContext, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private class DetectedActivitiesIntentService extends IntentService {
        public DetectedActivitiesIntentService() {
            super("DetectedActivitiesIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    handleActivityTransitionEvent(event);
                }
            }
        }
    }

    private void handleActivityTransitionEvent(ActivityTransitionEvent event) {
        String activityType = getActivityString(event.getActivityType());
        String transitionType = getTransitionType(event.getTransitionType());
        Log.d("ActivityTransition", "Activity: " + activityType + " Transition: " + transitionType);

        switch (event.getActivityType()) {
            case DetectedActivity.STILL:
                updateContext(currentContext.getLocation(), currentContext.getTimeOfDay(), currentContext.getWeather(), Activity.STILL);
                break;
            case DetectedActivity.WALKING:
                updateContext(currentContext.getLocation(), currentContext.getTimeOfDay(), currentContext.getWeather(), Activity.WALKING);
                break;
            case DetectedActivity.RUNNING:
                updateContext(currentContext.getLocation(), currentContext.getTimeOfDay(), currentContext.getWeather(), Activity.RUNNING);
                break;
            case DetectedActivity.IN_VEHICLE:
                updateContext(currentContext.getLocation(), currentContext.getTimeOfDay(), currentContext.getWeather(), Activity.DRIVING);
                break;
            // Add more cases for other activities as needed
        }
    }

    private String getActivityString(int activityType) {
        switch (activityType) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.IN_VEHICLE:
                return "DRIVING";
            default:
                return "UNKNOWN";
        }
    }

    private String getTransitionType(int transitionType) {
        switch (transitionType) {
            case 0:
                return "ENTER";
            case 1:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    private void requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.e("ContextManager", "Location permission not granted.", e);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            updateLocationContext(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void updateLocationContext(android.location.Location location) {
        Location currentLocation = currentContext.getLocation();
        if (isWithinRadius(location, HOME_LAT, HOME_LNG)) {
            currentLocation = Location.HOME;
        } else if (isWithinRadius(location, WORK_LAT, WORK_LNG)) {
            currentLocation = Location.WORK;
        } else if (isWithinRadius(location, GYM_LAT, GYM_LNG)) {
            currentLocation = Location.GYM;
        }
        updateContext(currentLocation, currentContext.getTimeOfDay(), currentContext.getWeather(), currentContext.getActivity());
    }

    private boolean isWithinRadius(android.location.Location location, double lat, double lng) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lng, results);
        return results[0] < LOCATION_RADIUS;
    }

    private void updateTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        TimeOfDay timeOfDay;
        if (hour >= 6 && hour < 12) {
            timeOfDay = TimeOfDay.MORNING;
        } else if (hour >= 12 && hour < 18) {
            timeOfDay = TimeOfDay.AFTERNOON;
        } else if (hour >= 18 && hour < 21) {
            timeOfDay = TimeOfDay.EVENING;
        } else {
            timeOfDay = TimeOfDay.NIGHT;
        }
        updateContext(currentContext.getLocation(), timeOfDay, currentContext.getWeather(), currentContext.getActivity());
    }

    private void updateWeather() {
        new Thread(() -> {
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + HOME_LAT + "&lon=" + HOME_LNG + "&appid=fb7796c1d4141471f9889ef935a6ac77");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    Scanner scanner = new Scanner(urlConnection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    Weather weather = Weather.SUNNY; // Default weather

                    switch (weatherDescription) {
                        case "Rain":
                            weather = Weather.RAINY;
                            break;
                        case "Clouds":
                            weather = Weather.CLOUDY;
                            break;
                        case "Snow":
                            weather = Weather.SNOWY;
                            break;
                        case "Clear":
                            weather = Weather.SUNNY;
                            break;
                    }

                    updateContext(currentContext.getLocation(), currentContext.getTimeOfDay(), weather, currentContext.getActivity());
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException | JSONException e) {
                Log.e("ContextManager", "Failed to get weather information", e);
            }
        }).start();
    }

    public void updateContext(Location location, TimeOfDay timeOfDay, Weather weather, Activity activity) {
        currentContext = new ContextValues(location, timeOfDay, weather, activity);
        notifyContextChange();
    }

    private void notifyContextChange() {
        // This method would notify any registered listeners that the context has changed.
        // Those listeners would then update their behavior based on the new context.
        Log.d("ContextManager", "Context updated to: " + currentContext.toString());
    }

    public ContextValues getCurrentContext() {
        return currentContext;
    }
}
