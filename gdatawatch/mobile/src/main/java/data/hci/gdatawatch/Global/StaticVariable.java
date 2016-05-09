package data.hci.gdatawatch.Global;

import com.google.android.gms.location.DetectedActivity;
import com.google.api.services.calendar.CalendarScopes;

public class StaticVariable {
    public static final int RADIUS = 100;//반경
    public static final String BROADCAST_GPS ="data.hci.gdata.gps";
    public static final String BROADCAST_GYRO ="data.hci.gdata.gyro";
    public static final String BROADCAST_ACCEL ="data.hci.gdata.accel";
    public static final String BROADCAST_ACTION = "data.hci.action";
    public static final String BROADCAST_TIME = "data.hci.time";

    public static final int REQUEST_PICK_ACCOUNT = 1000;
    public static final int REQUEST_AUTHORIZATION = 2000;
    public static final int ACCOUNT_PERMISSION = 3000;
    public static final int GPS_PERMISSION = 4000;
    public static final int CALENDAR_ACCEPT = 5000;

    //permission variable
    public static final int REQ_PERMISSION_GPS = 200;

    public static final String[] CALENDAR_SCOPES={CalendarScopes.CALENDAR};

    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on bicycle";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "undefine";
        }
    }

}
