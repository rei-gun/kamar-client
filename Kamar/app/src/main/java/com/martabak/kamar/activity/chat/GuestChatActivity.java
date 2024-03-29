package com.martabak.kamar.activity.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.guest.AbstractGuestBarsActivity;

/**
 * An activity that simply loads the {@link ChatDetailFragment} into the view for a guest.
 */
public class GuestChatActivity extends AbstractGuestBarsActivity {

    protected Options getOptions() {
        return new Options()
                .withBaseLayout(R.layout.activity_guest_chat)
                .withToolbarLabel(getString(R.string.title_chat_detail))
                .showTabLayout(false)
                .showLogoutIcon(false)
                .enableChatIcon(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle arguments = new Bundle();
        arguments.putString(ChatDetailFragment.GUEST_ID, getGuestId());
        arguments.putString(ChatDetailFragment.SENDER, getSender());
        ChatDetailFragment fragment = new ChatDetailFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.chat_detail_container, fragment)
                .commit();
    }

    private String getGuestId() {
        SharedPreferences prefs = getSharedPreferences("userSettings", Context.MODE_PRIVATE);
        return prefs.getString("guestId", "-1");
    }

    private String getSender() {
        return "GUEST";
    }

}