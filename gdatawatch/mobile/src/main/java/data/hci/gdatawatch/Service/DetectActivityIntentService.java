package data.hci.gdatawatch.Service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import data.hci.gdatawatch.Global.StaticVariable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DetectActivityIntentService extends IntentService {


    public DetectActivityIntentService() {
        super("DetectActivityIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(StaticVariable.BROADCAST_ACTION);

        DetectedActivity detectedActivities = result.getMostProbableActivity();
        Log.d("da", StaticVariable.getActivityString(detectedActivities.getType())+" "+detectedActivities.getConfidence());

        localIntent.setAction(StaticVariable.BROADCAST_ACTION);
        localIntent.putExtra("type", detectedActivities.getType());
        localIntent.putExtra("confidence", detectedActivities.getConfidence());
        sendBroadcast(localIntent);
    }

}
