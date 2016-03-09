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

    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Gyro
            if(intent.getAction().equals((StaticVariable.BROADCAST_GYRO))){
                float x = Math.round(intent.getFloatExtra("x", 0));
                float y = Math.round(intent.getFloatExtra("y", 0));
                float z = Math.round(intent.getFloatExtra("z", 0));

                String gyroString = "자이로스코프값 : " + "x : " + x + ", y : " + y + ", z : " +z;
                gyroTextView.setText(gyroString);
            }
            //Accel
            else if(intent.getAction().equals(StaticVariable.BROADCAST_ACCEL)){
                float x = (int)intent.getFloatExtra("x", 0);
                float y = (int)intent.getFloatExtra("y", 0);
                float z = (int)intent.getFloatExtra("z", 0);

                accelTextView.setText("가속도 값 : "+"x : "+x+" y : "+y+" z : "+z);
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

        tempview = (TextView) findViewById(R.id.tv_temp); // 기상청
        gyroTextView = (TextView)findViewById(R.id.tv_gyro);        //자이로 텍스트뷰 지정
        accelTextView = (TextView)findViewById(R.id.tv_accel); //엑셀


        startService((new Intent(getApplicationContext(), GyroService.class)));
        startService((new Intent(getApplicationContext(), AccelService.class)));
        registerReceiver(broadcastReceiver, intentFilter);

        GetXMLTask task = new GetXMLTask();
        task.execute("http://www.kma.go.kr/wid/queryDFS.jsp?gridx=59&gridy=125");

    }

    //리스너 등록
    protected void onResume(){
        super.onResume();
    }
    //리스너 해제
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
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {

            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("data");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            int i = 0 ;
            //날씨 데이터를 추출
            s += "현 위치의 날씨 정보: ";
            Node node = nodeList.item(i);
            Element fstElmnt = (Element) node;
            NodeList nameList  = fstElmnt.getElementsByTagName("temp");
            Element nameElement = (Element) nameList.item(0);
            nameList = nameElement.getChildNodes();
            s += "온도 = "+ ((Node) nameList.item(0)).getNodeValue() +",";

            NodeList websiteList = fstElmnt.getElementsByTagName("reh");
            s += "습도 = "+  websiteList.item(0).getChildNodes().item(0).getNodeValue() +",";

            NodeList rainList = fstElmnt.getElementsByTagName("r06");
            s += "강우량 = "+  rainList.item(0).getChildNodes().item(0).getNodeValue() +"\n";

            tempview.setText(s);

            super.onPostExecute(doc);
        }


    }//end inner class - GetXMLTask

}
