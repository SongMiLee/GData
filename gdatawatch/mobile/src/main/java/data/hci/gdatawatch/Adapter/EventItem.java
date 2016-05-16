package data.hci.gdatawatch.Adapter;

/**
 * Created by user on 2016-05-06.
 */
public class EventItem {
    String eventName, eventPlace, eventStart, eventEnd, eventPerson;

    public String getEventName(){ return this.eventName; }
    public String getEventPlace(){ return eventPlace; }
    public String getEventStart(){  return eventStart; }
    public String getEventEnd(){    return  eventEnd; }
    public String getEventPerson(){     return eventPerson; }

    public EventItem(String str){
        eventName = str;
    }

    public EventItem(String name, String place, String start, String end, String person){
        eventName = name;
        eventPlace = place;
        eventStart = start;
        eventEnd = end;
        eventPerson = person;
    }
}
