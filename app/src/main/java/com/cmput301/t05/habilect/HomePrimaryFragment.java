package com.cmput301.t05.habilect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
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
    FragmentManager fragmentManager;
    private Context context;
    private ListView habitTypeList;
    private UserAccount userAccount;
    private static List<HabitType> all_habit_types;
    private static ArrayList<HabitType> incomplete_habit_types;
    HabitTypeListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_home_primary, container, false);

        context = this.getContext();

        userAccount = new UserAccount().load(context);
        all_habit_types = userAccount.getHabits();

        fragmentManager = getActivity().getSupportFragmentManager();
        habitTypeList = rootView.findViewById(R.id.incompleteHabitsListView);


        habitTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), HabitTypeActivity.class);
                intent.putExtra("ClickedHabitType", incomplete_habit_types.get(i).getTitle());
                startActivity(intent);
            }
        });

        incomplete_habit_types = new ArrayList<>();
        getIncompleteHabitTypes();
        adapter = new HabitTypeListAdapter(incomplete_habit_types, getContext());
        habitTypeList.setAdapter(adapter);

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
                            //TODO: GSON
                            all_habit_types = userAccount.getHabits();

                            for (HabitType h: all_habit_types) {
                                if (title.equals(h.getTitle())) {
                                    throw new IllegalArgumentException("title");
                                }
                            }
                            //WebService.AddHabitTypesTask getHabitTypesTask = new WebService.GetHabitTypesTask();
                            HabitType habit_type = new HabitType(title, reason, start_date, weekly_plan);
                            //TODO: GSON
                            all_habit_types.add(habit_type);
                            userAccount.setHabits(all_habit_types);
                            userAccount.save(context);
                            userAccount.sync(context);
                            //TODO: GSON

                            getIncompleteHabitTypes();
                            adapter.notifyDataSetChanged();


                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }
                    @Override
                    public void OnDeleted() {
                        // TODO: implement OnDeleted
                    }
                });
                addHabitDialog.show(fragmentManager, "addHabitDialog");
            }
        });


        // TODO: Add habit event from title once information saving is done
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

                        HabitType habit_type = findHabitType(all_habit_types, event.getHabitType());
                        habit_type.addHabitEvent(event);
                        userAccount.setHabits(all_habit_types);
                        userAccount.save(context);
                        userAccount.sync(context);

                        getIncompleteHabitTypes();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void OnCancelled() {
                        // TODO: implement OnDeleted
                    }
                });
                ArrayList<String> titleList = getHabitTitles();
                if(all_habit_types.size() < 1) {
                    Toast.makeText(getContext(), "You do not have any habits to make events for!", Toast.LENGTH_SHORT).show();
                } else if(titleList.size() < 1) {
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
        String filePath = getter.getFileName();
        String directory = getter.getDirectory();
        Bitmap eventImage = getBitmapFromFilePath(directory, filePath);

        return new HabitEvent(comment, eventImage, location, date, title);
    }

    /**
     * Gets a bitmap from a file name and directory path
     * @param directory the directory with the image file
     * @param filePath the image file name
     * @return a bitmap of the decoded file
     */
    private Bitmap getBitmapFromFilePath(String directory, String filePath) {
        File image = new File(directory, filePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        return bitmap;
    }

    /**
     * Creates a list of string of the titles of all stored habit types
     * @return An ArrayList String of all habit Type title
     */
    private ArrayList<String> getHabitTitles() {
        ArrayList<String> list = new ArrayList<>();
        for(HabitType type : all_habit_types) {
            if(checkIfHabitDoneToday(type)) {
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
     * @return A list of habit types that need to be done
     */
    private void getIncompleteHabitTypes() {

        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_WEEK);
        //Log.d("Debugging", "today in int:" + Integer.toString(today));
        boolean[] plan;
        incomplete_habit_types.clear();
        for (HabitType h : all_habit_types) {
            boolean doneToday = checkIfHabitDoneToday(h);
            if(doneToday) {
                continue;
            }
            plan = h.getWeeklyPlan();
            if (today == Calendar.MONDAY && plan[0]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.TUESDAY && plan[1]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.WEDNESDAY && plan[2]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.THURSDAY && plan[3]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.FRIDAY && plan[4]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.SATURDAY && plan[5]) {
                incomplete_habit_types.add(h);
            }
            else if (today == Calendar.SUNDAY && plan[6]) {
                incomplete_habit_types.add(h);
            }
        }
    }

    private boolean checkIfHabitDoneToday(HabitType habit) {
        ArrayList<HabitEvent> eventList = habit.getHabitEvents();
        Locale locale = new Locale("English", "Canada");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE',' MMMM d',' yyyy", locale);
        String currentDate = simpleDateFormat.format(new Date());
        for(HabitEvent event : eventList) {
            if(currentDate.equals(event.getCompletionDateString())) {
                return true;
            }
        }
        return false;
    }

    private HabitType findHabitType(List<HabitType> habitList, String title) {
        Iterator<HabitType> iterator = habitList.iterator();
        while(iterator.hasNext()) {
            HabitType habit = iterator.next();
            if(habit.getTitle().equals(title)) {
                return habit;
            }
        }
        return null;
    }
}
