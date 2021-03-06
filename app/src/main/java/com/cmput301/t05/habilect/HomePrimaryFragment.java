package com.cmput301.t05.habilect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Shows a list of habit types that can be completed today, allows the user to navigate to
 * another activity using the drawer, allows the user to create a new habit event or habit type
 * using the FAB (to be implemented). ViewHabitDialog and OnViewHabitListener to be deleted or
 * modified.
 *
 * @author ioltuszy
 * @author amwhitta
 * @author rarog
 * @see HabitTypeListener
 * @see HabitType
 * @see HabitEvent
 */
public class HomePrimaryFragment extends Fragment {
    private static List<HabitType> allHabitTypes;
    private static ArrayList<HabitType> incompleteHabitTypes;
    FragmentManager fragmentManager;
    HabitTypeListAdapter adapter;
    private Context context;
    private ListView habitTypeList;
    private UserAccount userAccount;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_home_primary, container, false);

        context = this.getContext();

        userAccount = new UserAccount().load(context);
        allHabitTypes = userAccount.getHabits();

        fragmentManager = getActivity().getSupportFragmentManager();
        habitTypeList = rootView.findViewById(R.id.incompleteHabitsListView);

        /**
         * When the user clicks on a displayed habit type, should launch the habit type activity
         */
        habitTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), HabitTypeActivity.class);
                intent.putExtra("ClickedHabitType", incompleteHabitTypes.get(i).getTitle());
                startActivity(intent);
            }
        });


        // gets a list of incomplete habit types for that day, those should be the only ones displayed
        incompleteHabitTypes = new ArrayList<>();
        getIncompleteHabitTypes();

        TreeGrowth userTreeGrowth = userAccount.getTreeGrowth();

        // only check if did not run today already
        if (userTreeGrowth.getLastCheckDateForPreviousDaysIncompleteHabitTypes() != Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            // checks if there are incomplete habit types for the previous day. Returns the number of incomplete.
            int numberOfIncompletedHabits = getIncompleteHabitTypesFromYesterday();
            if (numberOfIncompletedHabits > 0) {

                int nutrientLevel = userAccount.getTreeGrowth().getNutrientLevel();

                nutrientLevel -= numberOfIncompletedHabits;
                userTreeGrowth.setNutrientLevel(nutrientLevel);
                userTreeGrowth.setLastCheckDateForPreviousDaysIncompleteHabitTypes();
                userAccount.save(context);
                userAccount.sync(context);

                Log.i("NUTRIENTLEVEL: ", "" + nutrientLevel);
            }
        }

        adapter = new HabitTypeListAdapter(incompleteHabitTypes, getContext());
        habitTypeList.setAdapter(adapter);

        // opens the add habit event dialog if the user wants to create a new habit type
        final Button addHabitButton = (Button) rootView.findViewById(R.id.addHabitButton);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddHabitTypeDialog addHabitDialog = new AddHabitTypeDialog();
                addHabitDialog.setHabitTypeListener(new HabitTypeListener() {
                    @Override
                    public void OnAddedOrEdited(String title, String reason, Date start_date, boolean[] weekly_plan) {
                        try {
                            // check to make sure title is unique
                            allHabitTypes = userAccount.getHabits();
                            for (HabitType h : allHabitTypes) {
                                if (title.equals(h.getTitle())) {
                                    throw new IllegalArgumentException("title");
                                }
                            }
                            // creates a new habitType from the users input
                            HabitType habit_type = new HabitType(title, reason, start_date, weekly_plan);
                            allHabitTypes.add(habit_type);
                            userAccount.setHabits(allHabitTypes);
                            userAccount.save(context);
                            userAccount.sync(context);
                            getIncompleteHabitTypes();
                            adapter.notifyDataSetChanged();


                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }

                    @Override
                    public void OnDeleted() {
                        // do nothing...
                    }
                });
                addHabitDialog.show(fragmentManager, "addHabitDialog");
            }
        });

        // opens an add habit event dialog if the user wants to make new events
        final Button addHabitEventButton = rootView.findViewById(R.id.addHabitEvent);
        addHabitEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddHabitEventDialog addHabitEventDialog = new AddHabitEventDialog();
                addHabitEventDialog.setOnAddHabitEventListener(new OnAddHabitEventListener() {
                    @Override
                    public void OnAdded() {
                        // TODO: implement OnAdded
                        HabitEvent event =
                                createHabitEventFromBundle(addHabitEventDialog.getResultBundle());

                        HabitType habit_type = findHabitType(allHabitTypes, event.getHabitType());
                        habit_type.addHabitEvent(event);
                        userAccount.setHabits(allHabitTypes);
                        userAccount.save(context);
                        userAccount.sync(context);

                        getIncompleteHabitTypes();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void OnCancelled() {
                    }
                });
                // we should only show the dialog if the user has types to make events for and
                // has types which still need to be completed today, otherwise show an error message
                ArrayList<String> titleList = getHabitTitles();
                if (allHabitTypes.size() < 1) {
                    Toast.makeText(getContext(), "You do not have any habits to make events for!", Toast.LENGTH_SHORT).show();
                } else if (titleList.size() < 1) {
                    Toast.makeText(getContext(), "You have already completed all of your habits today!", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Habit Type", titleList);
                    addHabitEventDialog.setArguments(bundle);
                    addHabitEventDialog.show(fragmentManager, "addHabitEventDialog");

                }
            }
        });

        return rootView;
    }

    /**
     * From a returning bundle, make a new habit event
     *
     * @param bundle the bundle which contains the information from AddHabitEventDialog
     * @return a new HabitEvent
     * @see AddHabitEventDialog
     */
    private HabitEvent createHabitEventFromBundle(Bundle bundle) {
        AddHabitEventDialogInformationGetter getter =
                new AddHabitEventDialogInformationGetter(bundle);
        String title = getter.getTitle();
        String comment = getter.getComment();
        Location location = getter.getLocation();
        Date date = getter.getDate();
        String eventImage = getter.getImage();
        byte[] decodedByteArray = Base64.decode(eventImage, Base64.URL_SAFE | Base64.NO_WRAP);
        Bitmap image = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedString = Base64.encodeToString(byteArray, Base64.URL_SAFE | Base64.NO_WRAP);
        return new HabitEvent(comment, encodedString, location, date, title, userAccount.getId().toString());
    }

    /**
     * Creates a list of string of the titles of all stored habit types
     *
     * @return An ArrayList String of all habit Type title
     */
    private ArrayList<String> getHabitTitles() {
        ArrayList<String> list = new ArrayList<>();
        for (HabitType type : allHabitTypes) {
            if (checkIfHabitDoneToday(type)) {
                continue;
            }
            list.add(type.getTitle());
        }
        return list;
    }

    /**
     * gets all of the user's habit types locally or from elasticsearch (to be implemented) and
     * searches through the list to find the ones that need to be completed today.
     *
     * @see HabitType
     */
    @Override
    public void onResume() {
        super.onResume();
        getIncompleteHabitTypes();
        adapter.notifyDataSetChanged();
    }

    /**
     * From the list of habit types, checks which ones still need to be done today
     *
     * @return A list of habit types that need to be done
     */
    private void getIncompleteHabitTypes() {

        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_WEEK);
        //Log.d("Debugging", "today in int:" + Integer.toString(today));
        boolean[] plan;
        incompleteHabitTypes.clear();
        for (HabitType h : allHabitTypes) {
            boolean doneToday = checkIfHabitDoneToday(h);
            if (doneToday) {
                continue;
            }
            plan = h.getWeeklyPlan();
            if (today == Calendar.MONDAY && plan[0]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.TUESDAY && plan[1]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.WEDNESDAY && plan[2]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.THURSDAY && plan[3]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.FRIDAY && plan[4]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.SATURDAY && plan[5]) {
                incompleteHabitTypes.add(h);
            } else if (today == Calendar.SUNDAY && plan[6]) {
                incompleteHabitTypes.add(h);
            }
        }
    }

    /**
     * Checks if the passed habit type has an event already done today
     *
     * @param habit the habit type you want to check
     * @return returns true if there is a habit event done today
     */
    private boolean checkIfHabitDoneToday(HabitType habit) {
        ArrayList<HabitEvent> eventList = habit.getHabitEvents();
        Locale locale = new Locale("English", "Canada");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE',' MMMM d',' yyyy", locale);
        String currentDate = simpleDateFormat.format(new Date());
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            dayOfWeek = 8;
        }
        if (!habit.getWeeklyPlan()[dayOfWeek - 2]) {
            return true;
        }
        for (HabitEvent event : eventList) {
            if (currentDate.equals(event.getCompletionDateString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * From the list of habit types, checks which ones have not been completed in the previous day
     *
     * @return A integer of the number of incompleted habit types
     */
    private int getIncompleteHabitTypesFromYesterday() {

        Calendar c = Calendar.getInstance();
        int yesterday = c.get(Calendar.DAY_OF_WEEK) - 1;
        boolean[] plan;
        ArrayList<HabitType> yesterdaysIncompleteHabitTypes = new ArrayList<>();
        for (HabitType h : allHabitTypes) {
            boolean doneYesterday = checkIfHabitDoneYesterday(h);
            if (doneYesterday) {
                continue;
            }
            plan = h.getWeeklyPlan();
            if (yesterday == Calendar.MONDAY && plan[0]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.TUESDAY && plan[1]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.WEDNESDAY && plan[2]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.THURSDAY && plan[3]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.FRIDAY && plan[4]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.SATURDAY && plan[5]) {
                yesterdaysIncompleteHabitTypes.add(h);
            } else if (yesterday == Calendar.SUNDAY && plan[6]) {
                yesterdaysIncompleteHabitTypes.add(h);
            }
        }

        return yesterdaysIncompleteHabitTypes.size();
    }

    /**
     * Formats the date to yesterday
     *
     * @return A formatted date
     */
    private Date yesterdayDate() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    /**
     * Checks if the passed habit type has an event already done yesterday
     *
     * @param habit the habit type you want to check
     * @return returns true if there is a habit event done yesterday
     */
    private boolean checkIfHabitDoneYesterday(HabitType habit) {
        ArrayList<HabitEvent> eventList = habit.getHabitEvents();
        Locale locale = new Locale("English", "Canada");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE',' MMMM d',' yyyy", locale);
        String currentDate = simpleDateFormat.format(yesterdayDate());
        for (HabitEvent event : eventList) {
            if (currentDate.equals(event.getCompletionDateString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * From a list of habit types, returns a habit type object from its title
     *
     * @param habitList the list of habit types you want to search
     * @param title     the title of the habit type you want to find
     * @return a habit type object
     */
    private HabitType findHabitType(List<HabitType> habitList, String title) {
        Iterator<HabitType> iterator = habitList.iterator();
        while (iterator.hasNext()) {
            HabitType habit = iterator.next();
            if (habit.getTitle().equals(title)) {
                return habit;
            }
        }
        return null;
    }
}
