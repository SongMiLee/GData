package data.hci.gdatawatch.Network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2016-03-18.
 */
public class EnvironmentPostTask extends AsyncTask<String, Integer ,String> {

    HttpURLConnection conn;

    @Override
    protected String doInBackground(String... params) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL("http://203.252.195.131:80/environments");
            String data = params[0];	// AsyncTask 실행 시 인자로 넘겼던 data

            Log.d("Server send data", data);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");	// Create는 POST 메소드 사용
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(data.getBytes().length);

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(data);
            out.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        Log.d("Server Response", result.toString());
        return result.toString();
    }
}
