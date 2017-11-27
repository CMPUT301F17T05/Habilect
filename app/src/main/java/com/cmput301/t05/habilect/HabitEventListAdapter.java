package com.cmput301.t05.habilect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This allows a habit event to be displayed in a ListView.
 * @author rarog
 */
public class HabitEventListAdapter extends BaseAdapter implements ListAdapter, Filterable {
    private ArrayList<HabitEvent> eventList = new ArrayList<>();
    private ArrayList<HabitEvent> allEventList = new ArrayList<>();
    private Context context;
    private String habitType;
    private Date date;

    HabitEventListAdapter(ArrayList<HabitEvent> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        allEventList = this.eventList;
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int i) {
        return eventList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // inflates the view
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.habit_event_row, null);
        }
        // gets the counter object that we want to display
        final HabitEvent event = eventList.get(i);
        habitType = event.getHabitType();
        date = event.getCompletionDate();

        TextView habitTitle = view.findViewById(R.id.habitEventRowTitle);
        TextView habitDate = view.findViewById(R.id.habitEventRowDate);
        TextView habitComment = view.findViewById(R.id.habitEventRowComment);
        Button viewButton = view.findViewById(R.id.habitEventRowSelectButton);

        habitTitle.setText(event.getHabitType());
        habitDate.setText(event.getCompletionDate().toString());
        habitComment.setText(event.getComment());

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = sendHabitInfoToView(event);
                Intent intent = new Intent(view.getContext(), ViewHabitEventActivity.class);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Using the events information, makes a bundle so the view event activity can be properly filled
     * @param event the habit event that you want to view
     * @return a bundle that can be sent off to the activity
     */
    private Bundle sendHabitInfoToView(HabitEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString("Title", habitType);
        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.ENGLISH).format(date);
        bundle.putString("Date", dateString);
        bundle.putString("Comment", event.getComment());
        bundle.putString("File Path", habitType.replace(" ", "_") + "_" + dateString);

        return bundle;
    }

    @Override
    public Filter getFilter() {
        eventList = allEventList;
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                eventList = (ArrayList<HabitEvent>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<HabitEvent> filteredEvents = new ArrayList<>();

                constraint = constraint.toString().toLowerCase();
                for (HabitEvent event : eventList) {
                    String comment = event.getComment();
                    if (comment.toLowerCase().contains(constraint.toString()))  {
                        filteredEvents.add(event);
                    }
                }

                results.count = filteredEvents.size();
                results.values = filteredEvents;

                return results;
            }
        };

        return filter;
    }
}
