package com.cmput301.t05.habilect;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment show the user a list of their friends and allows them to find new friends
 * @author rarog
 */

public class SocialFollowingFragment extends Fragment {
    FragmentManager fragmentManager;
    private Context mContext;
    private Context context;
    private ArrayList<UserAccount> friendList = new ArrayList<>();
    ListView friendListView;
    Button socialFriendFindFriendButton;
    SocialFollowingAdapter friendAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_social_followee, container, false);
        fragmentManager = getActivity().getSupportFragmentManager();

        mContext = getActivity().getApplicationContext();

        context = getActivity();

        final UserAccount profile = new UserAccount();
        profile.load(mContext);

        friendListView = rootView.findViewById(R.id.socialFriendListView);

        socialFriendFindFriendButton = rootView.findViewById(R.id.socialFriendFindFriendButton);

        socialFriendFindFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                ShowUserDialog dialog = new ShowUserDialog();
                dialog.show(fragmentManager, "showUserDialog");
            }
        });

        ArrayList<UserAccount> userAccounts = (ArrayList<UserAccount>) profile.getFollowees();
        friendAdapter = new SocialFollowingAdapter(userAccounts, context, mContext);
        friendListView.setAdapter(friendAdapter);

        return rootView;

    }

    /**
     * When the user is currently on this fragment, set the title
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(getActivity() != null) {
                getActivity().setTitle("Following");
            }
    }
    }
}
