package com.martabak.kamar.activity.housekeeping;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.martabak.kamar.R;
import com.martabak.kamar.domain.managers.PermintaanManager;
import com.martabak.kamar.domain.options.HousekeepingOption;
import com.martabak.kamar.domain.managers.HousekeepingManager;
import com.martabak.kamar.domain.permintaan.Housekeeping;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.service.PermintaanServer;
import com.martabak.kamar.service.StaffServer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observer;

/**
 * This activity generates the list of housekeeping sections.
 */
public class HousekeepingActivity extends AppCompatActivity implements
    View.OnClickListener {

    private RecyclerView sectionRecyclerView;
    private List<String> housekeepingSections;
    private List<HousekeepingOption> hkOptions;
    private HashMap<String, Integer> idToQuantity;
//    public RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_housekeeping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });
        TextView roomNumberTextView = (TextView)findViewById(R.id.toolbar_roomnumber);
        String roomNumber = getSharedPreferences("userSettings", MODE_PRIVATE)
                .getString("roomNumber", "none");
        roomNumberTextView.setText(getString(R.string.room_number) + ": " + roomNumber);
        // END GENERIC LAYOUT STUFF

        sectionRecyclerView = (RecyclerView)findViewById(R.id.housekeeping_list);
//        mLayoutManager = new LinearLayoutManager(this);
//        sectionRecyclerView.setLayoutManager(mLayoutManager);

        housekeepingSections = HousekeepingManager.getInstance().getSections();


        if (housekeepingSections == null) {
            housekeepingSections = new ArrayList<>();
            final HousekeepingSectionAdapter sectionRecyclerAdapter = new HousekeepingSectionAdapter(this, housekeepingSections);
            sectionRecyclerView.setAdapter(sectionRecyclerAdapter);
            StaffServer.getInstance(this).getHousekeepingOptions().subscribe(new Observer<List<HousekeepingOption>>() {
                @Override
                public void onCompleted() {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), "onCompleted");
                    idToQuantity = HousekeepingManager.getInstance().getOrder();
                    //order dict has not been initialized which should always be the case
                    if (idToQuantity == null) {
                        idToQuantity = new HashMap<>();
                        setupSectionsOrder();
                        HousekeepingManager.getInstance().setOrder(idToQuantity);
                        HousekeepingManager.getInstance().setSections(housekeepingSections);
                        HousekeepingManager.getInstance().setHkOptions(hkOptions);
                        sectionRecyclerAdapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), "onError", e);
                    e.printStackTrace();
                }

                @Override
                public void onNext(final List<HousekeepingOption> options) {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), options.size() + " housekeeping options found");
                    hkOptions = options;
                }
            });
        } else {
            hkOptions = HousekeepingManager.getInstance().getOptions();
            idToQuantity = HousekeepingManager.getInstance().getOrder();
            final HousekeepingSectionAdapter sectionRecyclerAdapter = new HousekeepingSectionAdapter(this, housekeepingSections);
            sectionRecyclerView.setAdapter(sectionRecyclerAdapter);
        }

    }

    /**
     * Handle a click on a single HK section.
     * Starts the fragment/next recycler list
    */
    @Override
    public void onClick(final View view) {

        int itemPosition = sectionRecyclerView.getChildLayoutPosition(view);
        final String selectedSection = housekeepingSections.get(itemPosition);

        HousekeepingFragment f = new HousekeepingFragment();
        Bundle args = new Bundle();
        args.putString("section", selectedSection);
        f.setArguments(args);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.hk_fragment_container, f);
        ft.commit();

    }

    private void setupSectionsOrder() {
        for (HousekeepingOption hk : hkOptions) {
            if (!housekeepingSections.contains(hk.getSection())) {
                housekeepingSections.add(String.valueOf(hk.getSection()));
            }
            idToQuantity.put(hk._id, 0);
        }
    }

    /**
     * This fragment generates the list of housekeeping options.
     */
    public static class HousekeepingFragment extends Fragment implements
    View.OnClickListener {

//        protected RecyclerView.LayoutManager mLayoutManager;
        private RecyclerView optionRecyclerView;
        private Map<String, String> idToStatus;
        private HousekeepingOptionAdapter hkOptionRecyclerAdapter;

        public HousekeepingFragment() {}

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_housekeeping, container, false);
            optionRecyclerView = (RecyclerView) view.findViewById(R.id.hk_option_recycler);
            final ArrayList<HousekeepingOption> temp = buildOptions();
            final HashMap<String, Integer> idToQuantity = HousekeepingManager.getInstance().getOrder();
            idToStatus = new HashMap<>();

            // Get the statuses,
            PermintaanManager.getInstance().getHousekeepingStatuses(getContext()).subscribe(new Observer<Map<String, String>>() {
                @Override public void onCompleted() {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), "getHousekeepingStatuses#onCompleted");
                    hkOptionRecyclerAdapter = new HousekeepingOptionAdapter(temp, idToQuantity,
                            HousekeepingFragment.this.getContext(), HousekeepingFragment.this, idToStatus);
                    optionRecyclerView.setAdapter(hkOptionRecyclerAdapter);
                }
                @Override public void onError(Throwable e) {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), "getHousekeepingStatuses#onError", e);
                    e.printStackTrace();
                }
                @Override public void onNext(final Map<String, String> statuses) {
                    Log.d(HousekeepingActivity.class.getCanonicalName(), "Fetching statuses of size " + statuses.size());
                    idToStatus = statuses;
                }
            });


            return view;
        }

        public void onClick(final View view) {
            final HousekeepingOptionAdapter.ViewHolder holder =
                    (HousekeepingOptionAdapter.ViewHolder)optionRecyclerView.getChildViewHolder((View)view.getParent());
            final HousekeepingOption option = holder.item;

            if (holder.spinner.getSelectedItem().toString().equals("0")) {
                 new AlertDialog.Builder(getContext())
                         .setTitle(option.getName())
                         .setMessage(R.string.housekeeping_zero_order)
                 .show();
            //Guest chose quantity > 0
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle(option.getName())
                        .setMessage(R.string.housekeeping_confirm)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String owner = Permintaan.OWNER_FRONTDESK;
                                String type = Permintaan.TYPE_HOUSEKEEPING;
                                String roomNumber = getActivity().getSharedPreferences("userSetting s", getActivity().MODE_PRIVATE)
                                        .getString("roomNumber", "none");
                                String guestId = getActivity().getSharedPreferences("userSettings", getActivity().MODE_PRIVATE)
                                        .getString("guestId", "none");
                                String state = Permintaan.STATE_NEW;
                                Date currentDate = Calendar.getInstance().getTime();
                                final Integer quantity = Integer.parseInt(holder.spinner.getSelectedItem().toString());

                                //Create the Housekeeping permintaan
                                PermintaanServer.getInstance(getActivity()).createPermintaan(
                                        new Permintaan(owner, type, roomNumber, guestId, state, currentDate, null, null,
                                                new Housekeeping("", quantity, option))
                                ).subscribe(new Observer<Permintaan>() {
                                    boolean success;

                                    @Override
                                    public void onCompleted() {
                                        Log.d(HousekeepingActivity.class.getCanonicalName(), "On completed");
                                        if (success) {
                                            Toast.makeText(
                                                    getActivity().getApplicationContext(),
                                                    R.string.massage_result,
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            //get, modify, then set the order dictionary in the Manager
                                            HashMap idToQuantity = HousekeepingManager.getInstance().getOrder();
                                            idToQuantity.put(option._id, quantity);
                                            HousekeepingManager.getInstance().setOrder(idToQuantity);
                                            idToStatus.put(option._id, Permintaan.STATE_NEW);
                                            hkOptionRecyclerAdapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(
                                                    getActivity().getApplicationContext(),
                                                    R.string.something_went_wrong,
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(HousekeepingActivity.class.getCanonicalName(), "On error");
                                        e.printStackTrace();
                                        success = false;
                                    }

                                    @Override
                                    public void onNext(Permintaan permintaan) {
                                        Log.d(HousekeepingActivity.class.getCanonicalName(), "On next");
                                        if (permintaan != null) {
                                            success = true;
                                        } else {
                                            success = false;
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                .show();
            }
        }
        //returns a list of options in the chosen section
        public ArrayList<HousekeepingOption> buildOptions() {
            Bundle args = getArguments();
            String section = null;
            if (args != null) {
                section = args.getString("section");
            }
            List<HousekeepingOption> hkOptions = HousekeepingManager.getInstance().getOptions();
            ArrayList<HousekeepingOption> temp = new ArrayList<>();
            for (HousekeepingOption hk : hkOptions) {
                if (hk.getSection().equals(section)) {
                    temp.add(hk);
                }
            }
            return temp;
        }
    }


}
