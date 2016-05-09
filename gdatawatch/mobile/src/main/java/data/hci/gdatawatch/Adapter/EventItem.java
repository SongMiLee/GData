package data.hci.gdatawatch.Adapter;

/**
 * Created by user on 2016-05-06.
 */
public class EventItem {
    String event;

    public String getEvent(){ return this.event; }
    public EventItem(String str){
        event = str;
    }
}
