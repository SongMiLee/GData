package data.hci.gdatawatch.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by user on 2016-03-30.
 */
public class PersonalPreference {
    private static SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //생성자
    public PersonalPreference(Context context){
        sharedPreferences = context.getSharedPreferences("pd", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //파일에 해당 데이터를 작성
    public void setData(int id,String name, String date, int gender, String job, int level){
        editor.putInt("id", id);
        editor.putString("name", name);
        editor.putString("date", date);
        editor.putInt("gender", gender);
        editor.putString("job", job);
        editor.putInt("level", level);

        editor.commit();
    }

    public boolean isData(){
        Log.d("id", String.valueOf(sharedPreferences.getInt("id", -1)));
        return sharedPreferences.contains("name");
    }

    public static int getId(){
        return sharedPreferences.getInt("id", -1);
    }
}
