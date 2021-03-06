package data.hci.gdatawatch.Global;

import com.google.api.services.calendar.CalendarScopes;

public class StaticVariable {

    public static final int RADIUS = 100;//반경
    public static final String BROADCAST_GPS ="data.hci.gdata.gps";
    public static final String BROADCAST_GYRO ="data.hci.gdata.gyro";
    public static final String BROADCAST_ACCEL ="data.hci.gdata.accel";
    public static final String BROADCAST_TIME ="data.hci.gdata.time";

    public static final int FINDLOCATION = 1000;
    public static final int REQUEST_PICK_ACCOUNT = 2000;
    public static final int REQUEST_AUTHORIZATION = 3000;

    public static final String[] CALENDAR_SCOPES={CalendarScopes.CALENDAR};
}
