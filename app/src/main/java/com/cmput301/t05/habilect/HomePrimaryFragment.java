package com.cmput301.t05.habilect;

import android.content.Intent;
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

import java.util.Date;
import java.util.ArrayList;

/**
 * Shows a list of habit types that can be completed today, allows the user to navigate to
 * another activity using the drawer, allows the user to create a new habit event or habit type
 * using the FAB (to be implemented). ViewHabitDialog and OnViewHabitListener to be deleted or
 * modified.
 *
 * @author ioltuszy
 * @author amwhitta
 * @see HabitTypeListener
 * @see HabitType
 * @see HabitEvent
 */
public class HomePrimaryFragment extends Fragment {
    FragmentManager fragmentManager;

    private ListView habitTypeList;
    private ArrayList<HabitType> habit_types;
    ArrayAdapter<HabitType> adapter;;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_home_primary, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        habitTypeList = rootView.findViewById(R.id.incompleteHabitsListView);
        habitTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), HabitTypeActivity.class);
                intent.putExtra("ClickedHabitType", habit_types.get(i));
                startActivity(intent);
            }
        });

        habitTypeList.setAdapter(adapter);
        habit_types = GSONController.GSON_CONTROLLER.loadHabitTypeFromFile();
        adapter = new ArrayAdapter<>(getActivity(), R.layout.habit_type_list_item, habit_types);
        habitTypeList.setAdapter(adapter);

        final Button addHabitButton = (Button) rootView.findViewById(R.id.addHabitButton);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddHabitDialog addHabitDialog = new AddHabitDialog();
                addHabitDialog.setHabitTypeListener(new HabitTypeListener() {
                    @Override
                    public void OnAddedOrEdited(String title, String reason, Date start_date, boolean[] weekly_plan) {
                        try {
                            HabitType habit_type = new HabitType(title, reason, start_date, weekly_plan);
                            GSONController.GSON_CONTROLLER.saveHabitTypeInFile(habit_type);
                            habit_types = GSONController.GSON_CONTROLLER.loadHabitTypeFromFile();
                            adapter.notifyDataSetChanged();
                            /*userProfile.addPlans(habit_type);
                            userProfile.addPlans(habit_type);
                            WebService.UpdateUserProfileTask updateUserProfileTask = new WebService.UpdateUserProfileTask();
                            updateUserProfileTask.execute(userProfile);*/
                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }
                    @Override
                    public void OnCancelled() {
                        // TODO: implement OnCancelled
                    }
                });
                addHabitDialog.show(fragmentManager, "addHabitDialog");
            }
        });

        final Button viewHabitButton = (Button) rootView.findViewById(R.id.viewHabitButton);
        viewHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ViewHabitDialog viewHabitDialog = new ViewHabitDialog();
                viewHabitDialog.setOnViewHabitListener(new OnViewHabitListener() {
                    @Override
                    public void OnDeleted() {
                        // TODO: implement OnDeleted
                    }

                    @Override
                    public void OnSaved() {
                        // TODO: implement OnSaved
                    }

                    @Override
                    public void OnFollowed() {
                        // TODO: implement OnFollowed
                    }

                    @Override
                    public void OnCancelled() {
                        // TODO: implement OnCancelled
                    }
                });
                viewHabitDialog.show(fragmentManager, "viewHabitDialog");
            }
        });

        // TODO: Add habit event from title once information saving is done
        /*final Button addHabitEventButton = rootView.findViewById(R.id.addHabitEvent);
        addHabitEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddHabitEventDialog addHabitEventDialog = new AddHabitEventDialog();
                addHabitEventDialog.setOnAddHabitEventListener(new OnAddHabitEventListener() {
                    @Override
                    public void OnAdded() {
                        // TODO: implement OnAdded
                    }

                    @Override
                    public void OnCancelled() {
                        // TODO: implement OnCancelled
                    }
                });
                addHabitEventDialog.show(fragmentManager, "addHabitEventDialog");
            }
        }); */

        return rootView;
    }

    /**
     * gets all of the user's habit types locally or from elasticsearch (to be implemented) and
     * puts them in the ListView
     *
     * @see HabitType
     */
    @Override
    public void onStart() {
        super.onStart();
    }
}
