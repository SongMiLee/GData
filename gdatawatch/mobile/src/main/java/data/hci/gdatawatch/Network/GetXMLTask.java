package data.hci.gdatawatch.Network;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import data.hci.gdatawatch.Activity.MainActivity;

/**
 * Created by user on 2016-03-18.
 */
public class GetXMLTask extends AsyncTask<String, Void, Document> {
    Document doc = null;

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
            Log.d("XML","Parsing Error");
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


        MainActivity.setXMLText(s);

        super.onPostExecute(doc);
    }
}
