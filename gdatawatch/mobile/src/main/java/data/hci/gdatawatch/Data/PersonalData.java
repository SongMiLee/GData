package data.hci.gdatawatch.Data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 2016-03-29.
 */
public class PersonalData {
    SharedPreferences preferences;

    public PersonalData(Context context){
        preferences = context.getSharedPreferences("pd", Context.MODE_PRIVATE);
    }
}
