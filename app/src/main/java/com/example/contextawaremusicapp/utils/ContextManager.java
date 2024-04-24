package com.example.contextawaremusicapp.utils;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

public class ContextManager {

    private ContextValues currentContext;
    private ActivityRecognitionClient activityRecognitionClient;
    private Context appContext; // Context of your application
    public ContextManager(Context context) {
        // Initialize with default values
        this.appContext = context;
        this.activityRecognitionClient = ActivityRecognition.getClient(appContext);
        currentContext = new ContextValues(Location.HOME, TimeOfDay.MORNING, Weather.SUNNY, Activity.WALKING);
        requestActivityUpdates();
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
        return PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                    // Handle activity transition event
                    String activityType = getActivityString(event.getActivityType());
                    String transitionType = getTransitionType(event.getTransitionType());
                    Log.d("ActivityTransition", "Activity: " + activityType + " Transition: " + transitionType);

                    // You can use this to update your current context based on activity
                }
            }
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
            // Add other cases as necessary
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


    public void updateContext(Location location, TimeOfDay timeOfDay, Weather weather, Activity activity) {
        // Update the current context
        currentContext = new ContextValues(location, timeOfDay, weather, activity);

        // Notify listeners about the context change
        notifyContextChange();
    }

    private void notifyContextChange() {
        // This method would notify any registered listeners that the context has changed.
        // Those listeners would then update their behavior based on the new context.
    }

    public ContextValues getCurrentContext() {
        return currentContext;
    }

    // Add methods to detect context changes, such as polling sensors, listening for system intents, etc.
}