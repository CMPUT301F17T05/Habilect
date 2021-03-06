package com.cmput301.t05.habilect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * This allows a habit event to be displayed in a ListView. It also includes edit and
 * delete buttons for the row to edit or delete the habit event.
 *
 * @author rarog
 */
// TODO: get rid of the user profile and replace with user account
public class HabitEventEditListAdapter extends BaseAdapter implements ListAdapter {
    public ImageButton deleteButton;
    // all of the views in the event that we need to populate
    private ArrayList<HabitEvent> eventList = new ArrayList<>();
    private UserAccount userAccount;
    private List<HabitType> habitTypeList;
    private HabitType relatedHabitType;
    private Context context;
    private HabitEvent event;
    private Context mContext;
    private String habitType;
    private Date date;

    HabitEventEditListAdapter(String habitType, Context context, Context mContext) {
        this.context = context;
        this.mContext = mContext;
        userAccount = new UserAccount().load(context);
        habitTypeList = userAccount.getHabits();
        relatedHabitType = findRelatedHabitType(habitType);
        this.eventList = relatedHabitType.getHabitEvents();
    }

    /**
     * Gets the size of the event list
     */
    @Override
    public int getCount() {
        return eventList.size();
    }

    /**
     * Gets a particular event item at a specified index
     */
    @Override
    public Object getItem(int i) {
        return eventList.get(i);
    }

    /**
     * Gets the event id of a specified index
     */
    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // inflates the view
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.habit_event_row_with_edit, null);
        }
        // gets the counter object that we want to display
        event = eventList.get(i);
        relatedHabitType = findRelatedHabitType(event.getHabitType());

        final UserAccount user = new UserAccount();
        TreeGrowth profileTreeGrowth = user.getTreeGrowth();


        Button viewButton = view.findViewById(R.id.habitEventEditRowSelectButton);

        TextView habitTitle = view.findViewById(R.id.habitEventRowEditTitle);
        TextView habitDate = view.findViewById(R.id.habitEventRowEditDate);
        TextView habitComment = view.findViewById(R.id.habitEventRowEditComment);
        ImageButton editButton = view.findViewById(R.id.habitEventRowEditEditButton);
        deleteButton = view.findViewById(R.id.habitEventRowEditDeleteButton);

        habitTitle.setText(event.getHabitType());
        habitDate.setText(event.getCompletionDateString());
        String comment = event.getComment();
        if (comment.equals("")) {
            habitComment.setText("[no comment]");
        } else {
            habitComment.setText(comment);
        }

        habitType = habitTitle.getText().toString();
        date = event.getCompletionDate();

        // when the user wishes to edit, it opens a dialog so that they can edit their event
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditHabitEventDialog editHabitEventDialog = new EditHabitEventDialog();
                editHabitEventDialog.setOnEditHabitEventListener(new OnEditHabitEventListener() {
                    @Override
                    public void OnAdded() {
                        // when user hits create button, make event from it
                        HabitEvent newEvent =
                                editHabitEventFromBundle(editHabitEventDialog.getResultBundle());

                        // removes the old event from the habit type list and adds in the new one
                        relatedHabitType.removeHabitEvent(event);
                        relatedHabitType.addHabitEvent(newEvent);
                        //userAccount.setHabits(habitTypeList);
                        userAccount.save(context);
                        userAccount.sync(context);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void OnCancelled() {
                        // do nothing...
                    }
                });
                // puts the current event information into a bundle and open the edit dialog
                FragmentActivity activity = (FragmentActivity) context;
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Bundle bundle = sendHabitInfoToDialog();
                editHabitEventDialog.setArguments(bundle);
                editHabitEventDialog.show(fragmentManager, "addHabitEventDialog");
            }
        });

        // when the user wishes to delete, it removes the event from their account
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // removes the event from file and elastisearch
                relatedHabitType.removeHabitEvent(event);
                userAccount.save(context);
                userAccount.sync(context);

                eventList.remove(event);
                notifyDataSetChanged();

                TreeGrowth userTreeGrowth = userAccount.getTreeGrowth();

                int nutrientLevel = userAccount.getTreeGrowth().getNutrientLevel();

                nutrientLevel -= 1;
                userTreeGrowth.setNutrientLevel(nutrientLevel);
                userAccount.save(context);
                userAccount.sync(context);

                Log.i("NUTRIENTLEVEL: ", "" + nutrientLevel);
            }
        });

        // if the user wants to see the event details, this will open the ViewEventActivity
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
     * Finds the habit type object from a list given it title
     *
     * @param habitType the title of the habit type you want to find
     * @return The habit type that you wanted
     */
    private HabitType findRelatedHabitType(String habitType) {
        Iterator<HabitType> iterator = habitTypeList.iterator();
        while (iterator.hasNext()) {
            HabitType habit = iterator.next();
            if (habit.getTitle().equals(habitType)) {
                return habit;
            }
        }
        return null;
    }

    /**
     * Using the events information, makes a bundle so the edit dialog can be properly filled
     *
     * @return a bundle that can be sent off to the dialog
     */
    private Bundle sendHabitInfoToDialog() {
        Bundle bundle = new Bundle();
        ArrayList<String> list = new ArrayList<>();
        list.add(habitType);
        bundle.putString("Title", habitType);
        bundle.putString("Comment", event.getComment());
        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.ENGLISH).format(date);
        bundle.putString("Date", dateString);
        bundle.putSerializable("Habit Type", list);
        bundle.putString("Image", event.getEventPicture());
        return bundle;
    }

    /**
     * Using the events information, makes a bundle so the view event activity can be properly filled
     *
     * @param event the habit event that you want to view
     * @return a bundle that can be sent off to the activity
     */
    private Bundle sendHabitInfoToView(HabitEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString("Title", habitType);
        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.ENGLISH).format(date);
        bundle.putString("Date", event.getCompletionDateString());
        bundle.putString("Comment", event.getComment());
        bundle.putString("Image", event.getEventPicture());

        return bundle;
    }

    /**
     * Makes a new habit event based on the provided information in the bundle
     *
     * @param bundle a bundle with the result from the dialog
     * @return a new HabitEvent
     * @see AddHabitEventDialogInformationGetter
     */
    private HabitEvent editHabitEventFromBundle(Bundle bundle) {
        AddHabitEventDialogInformationGetter getter =
                new AddHabitEventDialogInformationGetter(bundle);
        String title = getter.getTitle();
        String comment = getter.getComment();
        Location location = getter.getLocation();
        Date date = getter.getDate();
        String imageString = getter.getImage();
        byte[] decodedByteArray = Base64.decode(imageString, Base64.URL_SAFE | Base64.NO_WRAP);
        Bitmap eventImage = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        eventImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedString = Base64.encodeToString(byteArray, Base64.URL_SAFE | Base64.NO_WRAP);
        return new HabitEvent(comment, encodedString, location, date, title, userAccount.getId().toString());
    }

}
