package com.martabak.kamar.activity.guest;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.chat.GuestChatActivity;
import com.martabak.kamar.activity.chat.GuestChatService;
import com.martabak.kamar.activity.engineering.EngineeringActivity;
import com.martabak.kamar.activity.home.SelectLanguageActivity;
import com.martabak.kamar.activity.housekeeping.HousekeepingActivity;
import com.martabak.kamar.activity.laundry.LaundryActivity;
import com.martabak.kamar.activity.massage.MassageActivity;
import com.martabak.kamar.activity.restaurant.RestaurantActivity;
import com.martabak.kamar.activity.staff.CheckGuestInFragment;
import com.martabak.kamar.activity.staff.StaffHomeActivity;
import com.martabak.kamar.activity.survey.SurveyActivity;
import com.martabak.kamar.domain.Guest;
import com.martabak.kamar.domain.User;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.service.GuestServer;

import java.util.Calendar;
import java.util.Date;

import rx.Observer;

/**
 * An abstract class that provides functionality for the toolbar and bottom guest bar shared
 * across many guest activities.
 * <p>
 * The base layout provided must have the action bar layout and bottom bar layout's included in it.
 */
public abstract class AbstractGuestBarsActivity extends AppCompatActivity implements
        LogoutDialogFragment.LogoutDialogListener,
        ChangeRoomNumberDialogFragment.ChangeRoomDialogListener {

    protected abstract Options getOptions();

    public class Options {
        private int baseLayout;
        private String toolbarLabel;
        private boolean showTabLayout;
        private boolean showLogoutIcon;
        private boolean enableChatIcon;

        public Options withBaseLayout(int baseLayout) {
            this.baseLayout = baseLayout;
            return this;
        }
        public Options withToolbarLabel(String toolbarLabel) {
            this.toolbarLabel = toolbarLabel;
            return this;
        }
        public Options showTabLayout(boolean showTabLayout) {
            this.showTabLayout = showTabLayout;
            return this;
        }
        public Options showLogoutIcon(boolean showLogoutIcon) {
            this.showLogoutIcon = showLogoutIcon;
            return this;
        }
        public Options enableChatIcon(boolean enableChatIcon) {
            this.enableChatIcon = enableChatIcon;
            return this;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getOptions().baseLayout);

        setupToolbar(getOptions().toolbarLabel, getOptions().enableChatIcon);
        setupBottomBar(getOptions().showLogoutIcon);
        setupTabLayout(getOptions().showTabLayout);
    }

    private void setupToolbar(String label, boolean enableChatIcon) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarText = (TextView)findViewById(R.id.toolbar_title);
        toolbarText.setText(label);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        if (enableChatIcon) {
            ImageView chatIconView = (ImageView) findViewById(R.id.chat_icon);
            chatIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(v.getContext(), GuestChatActivity.class));
                    }
            });
        }
    }

    private void setupBottomBar(boolean showLogoutIcon) {
        LinearLayout bottomBarLinearLayout = (LinearLayout) findViewById(R.id.bottombar_linearlayout);

        String roomNumber = getSharedPreferences("userSettings", MODE_PRIVATE)
                .getString("roomNumber", "none");
        TextView roomTextView = (TextView) findViewById(R.id.room_number);
        roomTextView.setText(roomNumber);

        View logoutView = findViewById(R.id.logoutIcon);
        if (showLogoutIcon) {
            if (logoutView != null) {
                logoutView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment logoutDialogFragment = new LogoutDialogFragment();
                        logoutDialogFragment.show(getFragmentManager(), "logout");
                    }
                });
            }
        } else {
            bottomBarLinearLayout.removeView(logoutView);
        }
    }

    private void setupTabLayout(boolean useTabLayout) {
        if (!useTabLayout) {
            LinearLayout actionBarLinearLayout = (LinearLayout) findViewById(R.id.actionbar_linearlayout);
            View tabView = findViewById(R.id.tabs);
            actionBarLinearLayout.removeView(tabView);
        }
    }

    /**
     * Positive click for logout dialog
     * @param dialog The dialog fragment.
     * @param success The outcome of the server request.
     */
    @Override
    public void onLogoutDialogPositiveClick(DialogFragment dialog, Boolean success, String reason) {
        dialog.dismiss();
        if (success) {
            final String roomNumber = getSharedPreferences("userSettings", MODE_PRIVATE)
                    .getString("roomNumber", "none");
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.logout_options))
                    .setCancelable(true)
                    .setNegativeButton(getString(R.string.logout_change_room_number), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            DialogFragment changeRoomNumberFragment = new ChangeRoomNumberDialogFragment();
                            changeRoomNumberFragment.show(getFragmentManager(), "changeRoomNumber");
                        }
                    })
                    .setNeutralButton(getString(R.string.logout_staff), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            SharedPreferences.Editor editor = getSharedPreferences("userSettings", MODE_PRIVATE)
                                    .edit();
                            editor.putString("subUserType", User.TYPE_STAFF_FRONTDESK)
                                    .commit();

                            //Log.v(SelectUserTypeActivity.class.getCanonicalName(), "userType is " + getActivity().getSharedPreferences("userSettings", MODE_PRIVATE).getString("userType", "none"));
                            //Log.v(SelectUserTypeActivity.class.getCanonicalName(), "subUserType is " + getActivity().getSharedPreferences("userSettings", MODE_PRIVATE).getString("subUserType", "none"));

                            startActivity(new Intent(AbstractGuestBarsActivity.this, StaffHomeActivity.class));
                            finish();
                        }
                    })
                    .setPositiveButton(getString(R.string.logout_reset_tablet), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v("App Action", "Resetting tablet");
                            checkGuestOutByRoomNumber(roomNumber);
                        }
                    })
                    .create().show();
        } else {
            Toast.makeText(
                    AbstractGuestBarsActivity.this,
                    reason,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Negative click for logout dialog.
     * @param dialog The dialog fragment.
     */
    @Override
    public void onLogoutDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    /**
     * Gets the guest currently checked in to the room number
     * and then goes to checkGuestOut().
     * @param roomNumber The room number.
     */
    private void checkGuestOutByRoomNumber(final String roomNumber) {
        GuestServer.getInstance(getBaseContext()).getGuestInRoom(
                roomNumber).subscribe(new Observer<Guest>() {
            Guest guest = null;
            @Override public void onCompleted() {
                if (guest != null) {
                    checkGuestOut(guest);
                } else {
                    Toast.makeText(
                            AbstractGuestBarsActivity.this,
                            getString(R.string.guest_empty_room),
                            Toast.LENGTH_LONG
                    ).show();
                }
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "On completed");
            }
            @Override public void onError(Throwable e) {
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "On error");
                e.printStackTrace();
            }
            @Override public void onNext(Guest result) {
                guest = result;
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "On next guest " + result);
            }
        });
    }

    /**
     * Checks the guest out.
     */
    private void checkGuestOut(Guest guest) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1); // little hack here
        Date currentDate = c.getTime();
        Guest updatedGuest;
        updatedGuest = new Guest(guest._id, guest._rev, guest.firstName, guest.lastName,
                guest.phone, guest.email, guest.checkIn, currentDate, guest.roomNumber,
                guest.welcomeMessage, guest.promoImgId);
        GuestServer.getInstance(AbstractGuestBarsActivity.this.getBaseContext()).updateGuest(updatedGuest)
                .subscribe(new Observer<Boolean>() {
                    @Override public void onCompleted() {
                        //rooms.notifyDataSetChanged();
                        Log.d(CheckGuestInFragment.class.getCanonicalName(), "updateGuest() On completed");
                        getSharedPreferences("userSettings", MODE_PRIVATE).edit().
                                putString("guestId", "none").commit();
                        Toast.makeText(
                                AbstractGuestBarsActivity.this,
                                getString(R.string.logout_result),
                                Toast.LENGTH_LONG
                        ).show();
                        Intent intent = new Intent(AbstractGuestBarsActivity.this, SelectLanguageActivity.class);
                        startActivity(intent);
                        }
                    @Override public void onError(Throwable e) {
                        Log.d(CheckGuestInFragment.class.getCanonicalName(), "updateGuest() On error");
                        e.printStackTrace();
                        Toast.makeText(AbstractGuestBarsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                    @Override public void onNext(Boolean result) {
                        Log.v(CheckGuestInFragment.class.getCanonicalName(), "updateGuest() On next " + result);
                        if (result) {
                            Toast.makeText(AbstractGuestBarsActivity.this, getString(R.string.guest_checkout_message), Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(GuestHomeActivity.this, StaffHomeActivity.class));
                        } else {
                            Toast.makeText(AbstractGuestBarsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Positive click for change room number dialog
     * @param dialog The dialog fragment.
     */
    @Override
    public void onChangeRoomDialogPositiveClick(DialogFragment dialog, String roomNumber, boolean success, String reason) {
        if (success) {
            dialog.dismiss();
            SharedPreferences sp = getSharedPreferences("userSettings", MODE_PRIVATE);
            sp.edit().putString("roomNumber", roomNumber).commit();
//            sp.edit().putString("guestId", "none").commit();
            Toast.makeText(
                    AbstractGuestBarsActivity.this,
                    getString(R.string.room_number_changed),
                    Toast.LENGTH_LONG
            ).show();
            //roomNumberTextView.setText(getString(R.string.room_number) + " " + roomNumber);
            startActivity(new Intent(this, SelectLanguageActivity.class));
            finish();
            //String guestId = getSharedPreferences("userSettings", MODE_PRIVATE)
            //      .getString("guestId", "none");

            //setGuestId(roomNumber);

        } else {
            Toast.makeText(
                    AbstractGuestBarsActivity.this,
                    reason,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Negative click for change room number dialog.
     * @param dialog The dialog fragment.
     */
    @Override
    public void onChangeRoomDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

}