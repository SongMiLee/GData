package data.hci.gdata.Global;

import com.google.api.services.calendar.CalendarScopes;

/**
 * Created by user on 2016-02-01.
 */
public class StaticVariable {
    public static final int RADIUS = 100;//반경
    public static final String BROADCAST_GPS="data.hci.gdata.gps";
    public static final String BROADCAST_GYRO="data.hci.gdata.gyro";

    public static final int FINDLOCATION = 1000;
    public static final int REQUEST_PICK_ACCOUNT = 2000;
    public static final int REQUEST_AUTHORIZATION = 3000;

    public static final String[] CALENDAR_SCOPES={CalendarScopes.CALENDAR};

}
