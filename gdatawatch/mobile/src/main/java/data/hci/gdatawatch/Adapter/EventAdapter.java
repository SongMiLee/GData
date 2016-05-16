package data.hci.gdatawatch.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import data.hci.gdatawatch.R;

/**
 * Created by user on 2016-05-09.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    List<EventItem> items;
    List<String> raw;

    public EventAdapter(List<String> event){
        raw = event;
        items = new ArrayList<EventItem>();
        EventItem[] item = new EventItem[raw.size()];

        for(int i=0; i<raw.size(); i++){
            item[i] = new EventItem(raw.get(i));
            items.add(item[i]);
        }
    }

    public EventAdapter(String eventName, String eventPlace, String eventStart, String eventEnd, String eventPerson){
        items = new ArrayList<EventItem>();
        EventItem item = new EventItem(eventName, eventPlace, eventStart, eventEnd, eventPerson);
        items.add(item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {      holder.eventName.setText(items.get(position).getEventName());   }

    @Override
    public int getItemCount() {     return items.size();   }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView eventName, eventPlace, eventStart, eventEnd, eventPerson;

        public ViewHolder(View itemView) {
            super(itemView);
            eventName = (TextView)itemView.findViewById(R.id.calendar_event_text);
            eventPlace = (TextView)itemView.findViewById(R.id.calendar_event_location);
            eventStart = (TextView)itemView.findViewById(R.id.calendar_event_start);
            eventEnd = (TextView)itemView.findViewById(R.id.calendar_event_end);
            eventPerson = (TextView)itemView.findViewById(R.id.calendar_event_person);
        }
    }
}
