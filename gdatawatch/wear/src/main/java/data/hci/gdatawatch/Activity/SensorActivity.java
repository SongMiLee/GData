package data.hci.gdatawatch.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import data.hci.gdatawatch.Global.StaticVariable;
import data.hci.gdatawatch.R;
import data.hci.gdatawatch.Service.AccelService;
import data.hci.gdatawatch.Service.GyroService;

public class SensorActivity extends Activity {

    TextView tempview;
    TextView gyroTextView;
    TextView accelTextView;
    Document doc = null;

    //?úÍ∞Ñ Í¥Ä??Î≥Ä??
    Calendar calendar = Calendar.getInstance();
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int season;
    String[] strSeason = {"Î¥?,"?¨Î¶Ñ","Í∞Ä??,"Í≤®Ïö∏"};
    String nowDate;
    TextView dateTextView;

    //?§Î†à??
    timeRefresh update;
    Thread Update;

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gyro
            if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                double x = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;
                double y = Math.round(intent.getFloatExtra("y", 0)*10d) / 10d;
                double z = Math.round(intent.getFloatExtra("z", 0)*10d) / 10d;

                String gyroString = "?êÏù¥Î°úÏä§ÏΩîÌîÑÍ∞?: " + "x : " + x + ", y : " + y + ", z : " +z;
                gyroTextView.setText(gyroString);
            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                double x = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;
                double y = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;
                double z = Math.round(intent.getFloatExtra("x", 0)*10d) / 10d;

                accelTextView.setText("Í∞Ä?çÎèÑ Í∞?: "+"x : "+x+" y : "+y+" z : "+z);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        intentFilter = new IntentFilter();
        intentFilter.addAction(StaticVariable.BROADCAST_GYRO);
        intentFilter.addAction(StaticVariable.BROADCAST_ACCEL);

        tempview = (TextView) findViewById(R.id.tv_temp); // Í∏∞ÏÉÅÏ≤?
        gyroTextView = (TextView)findViewById(R.id.tv_gyro);        //?êÏù¥Î°??çÏä§?∏Î∑∞ ÏßÄ??
        accelTextView = (TextView)findViewById(R.id.tv_accel); //?ëÏ?

        //?úÍ∞Ñ?çÏä§??ÏßÄ??
        dateTextView = (TextView) findViewById(R.id.tv_date);

        //?úÍ∞Ñ ?ÖÎç∞?¥Ìä∏
        Update = new Thread(new timeRefresh());
        Update.start();

        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));
        registerReceiver(broadcastReceiver, intentFilter);

        GetXMLTask task = new GetXMLTask();
        task.execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=59&gridy=125");

    }

    //Î¶¨Ïä§???±Î°ù
    protected void onResume(){
        super.onResume();
    }
    //Î¶¨Ïä§???¥Ï†ú
    protected void onPause(){
        super.onPause();
    }



    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XMLÎ¨∏ÏÑú ÎπåÎçî Í∞ùÏ≤¥Î•??ùÏÑ±
                doc = db.parse(new InputSource(url.openStream())); //XMLÎ¨∏ÏÑúÎ•??åÏã±?úÎã§.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {

            String s = "";
            //data?úÍ∑∏Í∞Ä ?àÎäî ?∏ÎìúÎ•?Ï∞æÏïÑ??Î¶¨Ïä§???ïÌÉúÎ°?ÎßåÎì§?¥ÏÑú Î∞òÌôò
            NodeList nodeList = doc.getElementsByTagName("data");
            //data ?úÍ∑∏Î•?Í∞ÄÏßÄ???∏ÎìúÎ•?Ï∞æÏùå, Í≥ÑÏ∏µ?ÅÏù∏ ?∏Îìú Íµ¨Ï°∞Î•?Î∞òÌôò

            int i = 0 ;
            //?†Ïî® ?∞Ïù¥?∞Î? Ï∂îÏ∂ú
            s += "???ÑÏπò???†Ïî® ?ïÎ≥¥: ";
            Node node = nodeList.item(i);
            Element fstElmnt = (Element) node;
            NodeList nameList  = fstElmnt.getElementsByTagName("temp");
            Element nameElement = (Element) nameList.item(0);
            nameList = nameElement.getChildNodes();
            s += "?®ÎèÑ = "+ ((Node) nameList.item(0)).getNodeValue() +",";

            NodeList websiteList = fstElmnt.getElementsByTagName("reh");
            s += "?µÎèÑ = "+  websiteList.item(0).getChildNodes().item(0).getNodeValue() +",";

            NodeList rainList = fstElmnt.getElementsByTagName("r06");
            s += "Í∞ïÏö∞??= "+  rainList.item(0).getChildNodes().item(0).getNodeValue() +"\n";

            tempview.setText(s);

            super.onPostExecute(doc);
        }


    }//end inner class - GetXMLTask

    //?úÍ∞Ñ Í∞±Ïã†???ÑÌïú ?§Î†à??
    public class timeRefresh implements Runnable {
        @Override
        public void run() {
            while(true) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR);
                minute = calendar.get(Calendar.MINUTE);

                nowDate = year + "/";
                if (month < 10) {
                    nowDate += "0";
                }
                nowDate += month + "/";
                if (day < 10) {
                    nowDate += "0";
                }
                nowDate += day + " ";
                if (hour < 10) {
                    nowDate += "0";
                }
                nowDate += hour + ":";
                if (minute < 10) {
                    nowDate += "0";
                }
                nowDate += minute;

                switch (month){
                    case 3:case 4:case 5:
                        season = 0;
                        break;
                    case 6:case 7:case 8:
                        season = 1;
                        break;
                    case 9:case 10:case 11:
                        season = 2;
                        break;
                    default:
                        season = 3;
                }
                nowDate += strSeason[season];

                //     Log.d(nowDate,nowDate);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dateTextView.setText(nowDate);
                    }
                });


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
