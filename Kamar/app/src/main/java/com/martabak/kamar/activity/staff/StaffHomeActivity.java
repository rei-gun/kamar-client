package com.martabak.kamar.activity.staff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.chat.ChatListActivity;
import com.martabak.kamar.activity.chat.StaffChatFragment;
import com.martabak.kamar.activity.chat.StaffChatService;
import com.martabak.kamar.activity.home.SelectLanguageActivity;

public class StaffHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startStaffServices(getSharedPreferences("userSettings", MODE_PRIVATE).getString("subUserType", "none"));
        setContentView(R.layout.activity_staff_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationViewListener());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getFragmentManager().beginTransaction()
                .add(R.id.staff_container, StaffPermintaanFragment.newInstance())
                .addToBackStack(null)
                .commit();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onStop() {
        stopStaffServices();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    class NavigationViewListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Log.v(StaffHomeActivity.class.toString(), "Loading staff requests fragment");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.staff_container, StaffPermintaanFragment.newInstance()).commit();
                    break;
                case R.id.nav_chat:
                    Log.v(StaffHomeActivity.class.toString(), "Going to chat activity for staff");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.staff_container, StaffChatFragment.newInstance())
                            .commit();
                    break;
                case R.id.nav_check_guest_in:
                    Log.v(StaffHomeActivity.class.toString(), "Loading check-guest-in fragment");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.staff_container, CheckGuestInFragment.newInstance())
                            .addToBackStack(null)
                            .commit();
                    break;
                case R.id.nav_check_guest_out:
                    Log.v(StaffHomeActivity.class.toString(), "Loading check-guest-out fragment");
                    getFragmentManager().beginTransaction()
                            .replace(R.id.staff_container, CheckGuestOutFragment.newInstance())
                            .addToBackStack(null)
                            .commit();
                    break;
                case R.id.nav_logout:
                    Log.v(StaffHomeActivity.class.toString(), "Loading select language activity");
                    startActivity(new Intent(StaffHomeActivity.this, SelectLanguageActivity.class));
                    finish();
                    break;
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    /**
     * Start any relevant staff services.
     */
    private void startStaffServices(String userSubType) {
        if (!userSubType.equals("none")) {
            Log.v(StaffHomeActivity.class.getCanonicalName(), "Starting " + StaffPermintaanService.class.getCanonicalName() + " as " + userSubType);
            startService(new Intent(this, StaffPermintaanService.class)
                    .putExtra("subUserType", userSubType));
        }
        Log.v(StaffHomeActivity.class.getCanonicalName(), "Starting " + StaffChatService.class.getCanonicalName());
        startService(new Intent(this, StaffChatService.class));
    }

    /**
     * Stop any relevant staff services.
     */
    private void stopStaffServices() {
        Log.v(StaffHomeActivity.class.getCanonicalName(), "Stopping " + StaffPermintaanService.class.getCanonicalName());
        stopService(new Intent(this, StaffPermintaanService.class));
        Log.v(StaffHomeActivity.class.getCanonicalName(), "Stopping " + StaffChatService.class.getCanonicalName());
        stopService(new Intent(this, StaffChatService.class));
    }

}
