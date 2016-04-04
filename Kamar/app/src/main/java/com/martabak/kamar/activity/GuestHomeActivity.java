package com.martabak.kamar.activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.martabak.kamar.R;

public class GuestHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_home);
        final ImageAdapter imgAdapter = new ImageAdapter(this);
        final GridView gridView = (GridView) findViewById(R.id.guestgridview);
        gridView.setAdapter(imgAdapter);



        //display feature text on each item click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
                Toast.makeText(GuestHomeActivity.this, "" + imgAdapter.getItem(position),
                        Toast.LENGTH_SHORT).show();

                createAction(imgAdapter.getItem(position).toString());
            }
        });


    }

    public void createAction(String option)
    {

        if (option == "TRANSPORT")
        {
            Intent intent = new Intent(this, TransportActivity.class);
            startActivity(intent);
        }

        if (option == "HOUSEKEEPING")
        {
            DialogFragment fragment = new HousekeepingDialogFragment();
            fragment.show(getFragmentManager(), "housekeeping");
        }

        if (option == "BELLBOY")
        {
            DialogFragment fragment = new BellboyDialogFragment();
            fragment.show(getFragmentManager(), "bellboy");
        }

        if (option == "MAINTENANCE")
        {
            DialogFragment fragment = new MaintenanceDialogFragment();
            fragment.show(getFragmentManager(), "maintenance");
        }


        if (option == "TELL US")
        {
            DialogFragment fragment = new TellusDialogFragment();
            fragment.show(getFragmentManager(), "tellus");
        }

    }



}

