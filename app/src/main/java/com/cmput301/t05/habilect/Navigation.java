package com.cmput301.t05.habilect;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

/**
 * Navigation drawer that allows navigation around the application through a globally accessible drawer
 * Created by Ian on 2017-11-12.
 */

public class Navigation {
    public static void setup(final View context) {
        final Button navigateHome = (Button) context.findViewById(R.id.navigateHome);
        navigateHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) (view.getParent().getParent())).closeDrawer(Gravity.START);
                Intent intent = new Intent(context.getContext(), HomeActivity.class);
                view.getContext().startActivity(intent);
            }
        });
        final Button navigateHabitTypes = (Button) context.findViewById(R.id.navigateHabitTypes);
        navigateHabitTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) (view.getParent().getParent())).closeDrawer(Gravity.START);
                Intent intent = new Intent(context.getContext(), ViewHabitTypesActivity.class);
                view.getContext().startActivity(intent);
            }
        });
        final Button navigateHistoryButton = (Button) context.findViewById(R.id.navigateHistory);
        navigateHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) (view.getParent().getParent())).closeDrawer(Gravity.START);
                Intent intent = new Intent(context.getContext(), HistoryActivity.class);
                context.getContext().startActivity(intent);
            }
        });
        final Button navigateSocialButton = (Button) context.findViewById(R.id.navigateSocial);
        navigateSocialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) (view.getParent().getParent())).closeDrawer(Gravity.START);
                Intent intent = new Intent(context.getContext(), SocialActivity.class);
                context.getContext().startActivity(intent);
            }
        });
        final Button navigateProfileButton = (Button) context.findViewById(R.id.navigateProfile);
        navigateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) (view.getParent().getParent())).closeDrawer(Gravity.START);
                Intent intent = new Intent(context.getContext(), ProfileActivity.class);
                context.getContext().startActivity(intent);
            }
        });
    }
}
