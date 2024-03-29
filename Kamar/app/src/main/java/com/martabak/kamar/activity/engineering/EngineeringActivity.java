package com.martabak.kamar.activity.engineering;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.guest.AbstractGuestBarsActivity;
import com.martabak.kamar.activity.guest.SimpleDividerItemDecoration;
import com.martabak.kamar.domain.User;
import com.martabak.kamar.domain.managers.PermintaanManager;
import com.martabak.kamar.domain.options.EngineeringOption;
import com.martabak.kamar.domain.permintaan.Engineering;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.service.PermintaanServer;
import com.martabak.kamar.service.Server;
import com.martabak.kamar.service.StaffServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;

/**
 * This activity generates the list of engineering options and allows the guest to request one.
 */
public class EngineeringActivity extends AbstractGuestBarsActivity implements View.OnClickListener {

    @BindView(R.id.engineering_list) RecyclerView recyclerView;

    private List<EngineeringOption> engOptions;
    private Map<String, String> statuses; // Maps engineering option ID -> request status
    private EngineeringRecyclerViewAdapter recyclerViewAdapter;

    protected Options getOptions() {
        return new Options()
                .withBaseLayout(R.layout.activity_engineering)
                .withToolbarLabel(getString(R.string.engineering_label))
                .showTabLayout(false)
                .showLogoutIcon(false)
                .enableChatIcon(true)
                .enableordersIcon(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        loadOptions();
    }

    private void loadOptions() {
        engOptions = new ArrayList<>();

        StaffServer.getInstance(this).getEngineeringOptions().subscribe(new Observer<List<EngineeringOption>>() {
            @Override public void onCompleted() {
                Log.d(EngineeringActivity.class.getCanonicalName(), "onCompleted");
                Log.v(EngineeringActivity.class.getCanonicalName(), engOptions.toString());
                getStatuses();
            }
            @Override public void onError(Throwable e) {
                Log.d(EngineeringActivity.class.getCanonicalName(), "onError", e);
                e.printStackTrace();
            }
            @Override public void onNext(final List<EngineeringOption> options) {
                Log.d(EngineeringActivity.class.getCanonicalName(), options.size() + " engineering options found");
                engOptions.addAll(options);
            }
        });
    }

    private void getStatuses() {
        PermintaanManager.getInstance().getEngineeringStatuses(getBaseContext()).subscribe(new Observer<Map<String, String>>() {
            @Override public void onCompleted() {
                Log.d(EngineeringActivity.class.getCanonicalName(), "getEngineeringStatuses#onCompleted");
                recyclerViewAdapter = new EngineeringRecyclerViewAdapter(engOptions);
                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(EngineeringActivity.this));
            }
            @Override public void onError(Throwable e) {
                Log.d(EngineeringActivity.class.getCanonicalName(), "getEngineeringStatuses#onError", e);
                e.printStackTrace();
            }
            @Override public void onNext(Map<String, String> statuses) {
                Log.d(EngineeringActivity.class.getCanonicalName(), "Fetching statuses of size " + statuses.size());
                EngineeringActivity.this.statuses = statuses;
            }
        });
    }

    private boolean requestInProgress(String optionId) {
        if (statuses.containsKey(optionId)) {
            switch (statuses.get(optionId)) {
                case Permintaan.STATE_INPROGRESS:
                case Permintaan.STATE_NEW:
                    return true;
            }
        }
        return false;
    }

    /**
     * Handle a click on a single engineering option.
     * Bring up a confirmation dialog.
     * @param buttonView The button that was clicked on.
     */
    @Override
    public void onClick(final View buttonView) {
        final View view = (View)buttonView.getParent().getParent();
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        final EngineeringOption item = engOptions.get(itemPosition);
        final EditText inputOther = new EditText(this); // Provide a text input when they select the "OTHER" engineering option
        Log.d(EngineeringActivity.class.getCanonicalName(), "Selected " + item.getName() + " with ID " + item._id);
        Log.d(EngineeringActivity.class.getCanonicalName(), "Statuses map is " + Arrays.toString(statuses.entrySet().toArray()));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setMessage(R.string.engineering_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        createEngineeringPermintaan(inputOther.getText().toString(), item);
                    }})
                .setNegativeButton(android.R.string.no, null);
        if (item.getName().equalsIgnoreCase("OTHER")) {
            Log.d(EngineeringActivity.class.getCanonicalName(), "Adding text input because OTHER engineering option was selected");
            alertDialog
                    .setMessage(R.string.engineering_other_message)
                    .setView(inputOther);
        }

        alertDialog.show();
    }

    private void createEngineeringPermintaan(String message, final EngineeringOption item) {
        String owner = User.TYPE_STAFF_FRONTDESK;
        final String creator = getSharedPreferences("userSettings", MODE_PRIVATE)
                .getString("userType", "none");
        String type = Permintaan.TYPE_ENGINEERING;
        String roomNumber = getSharedPreferences("userSettings", MODE_PRIVATE)
                .getString("roomNumber", "none");
        String guestId = getSharedPreferences("userSettings", MODE_PRIVATE)
                .getString("guestId", "none");
        if (guestId.equals("none")) {
            Toast.makeText(
                    EngineeringActivity.this.getApplicationContext(),
                    R.string.no_guest_in_room,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        String state = Permintaan.STATE_NEW;
        Date currentDate = Calendar.getInstance().getTime();
        PermintaanServer.getInstance(EngineeringActivity.this).createPermintaan(
                new Permintaan(owner, creator, type, roomNumber, guestId, state, currentDate,
                        new Engineering(message, item))
        ).subscribe(new Observer<Permintaan>() {
            boolean success;
            @Override public void onCompleted() {
                Log.d(EngineeringActivity.class.getCanonicalName(), "On completed");
                if (success) {
                    EngineeringActivity.this.statuses.put(item._id, Permintaan.STATE_NEW);
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (creator.equals("GUEST")) {
                        Toast.makeText(
                                EngineeringActivity.this.getApplicationContext(),
                                R.string.engineering_result,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    else if (creator.equals("STAFF")) {
                        setResult(Permintaan.SUCCESS);
                        finish();
                    }
                } else {
                    Toast.makeText(
                            EngineeringActivity.this.getApplicationContext(),
                            R.string.something_went_wrong,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
            @Override public void onError(Throwable e) {
                Log.d(EngineeringActivity.class.getCanonicalName(), "On error");
                e.printStackTrace();
                success = false;
            }
            @Override public void onNext(Permintaan permintaan) {
                Log.d(EngineeringActivity.class.getCanonicalName(), "On next");
                success = permintaan != null;
            }
        });
    }

    public class EngineeringRecyclerViewAdapter
            extends RecyclerView.Adapter<EngineeringRecyclerViewAdapter.ViewHolder> {

        protected int selectedPos = -1;

        private final List<EngineeringOption> mValues;

        public EngineeringRecyclerViewAdapter(List<EngineeringOption> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.engineering_list_row, parent, false);
            view.findViewById(R.id.order_button).setOnClickListener(EngineeringActivity.this);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.itemView.setSelected(selectedPos == position);
            holder.item = mValues.get(position);

            Log.d(EngineeringActivity.class.getCanonicalName(), "Loading image " + holder.item.getImageUrl() + " into " + holder.imageView);
            Server.picasso(EngineeringActivity.this)
                    .load(holder.item.getImageUrl())
                    .resize(250, 125)
                    .placeholder(R.drawable.loading_batik)
                    .error(R.drawable.error)
                    .into(holder.imageView);
            holder.nameView.setText(holder.item.getName());
            String state = statuses.containsKey(holder.item._id) ? statuses.get(holder.item._id) : Permintaan.STATE_CANCELLED;
            Log.d(EngineeringActivity.class.getCanonicalName(), "Status for engineering " + holder.item.getName() + " is " + state);
            switch (state) {
                case Permintaan.STATE_NEW:
                    holder.sentImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    holder.processedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    holder.completedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    break;
                case Permintaan.STATE_INPROGRESS:
                    holder.sentImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    holder.processedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    holder.completedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    break;
                case Permintaan.STATE_COMPLETED:
                    holder.sentImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    holder.processedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    holder.completedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_green));
                    break;
                default:
                    holder.sentImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    holder.processedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    holder.completedImageView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.circle_red));
                    break;
            }
            if (requestInProgress(holder.item._id)) {
                holder.buttonView.setEnabled(false);
            } else {
                holder.buttonView.setEnabled(true);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public EngineeringOption item;
            public final View rootView;
            //binding views here
            @BindView(R.id.engineering_image) ImageView imageView;
            @BindView(R.id.engineering_name) TextView nameView;
            @BindView(R.id.sent_image) View sentImageView;
            @BindView(R.id.processed_image) View processedImageView;
            @BindView(R.id.completed_image) View completedImageView;
            @BindView(R.id.order_button) View buttonView;

            public ViewHolder(View view) {
                super(view);
                rootView = view;
                imageView = (ImageView) view.findViewById(R.id.engineering_image);
                nameView = (TextView) view.findViewById(R.id.engineering_name);
                sentImageView = view.findViewById(R.id.sent_image);
                processedImageView = view.findViewById(R.id.processed_image);
                completedImageView = view.findViewById(R.id.completed_image);
                buttonView = view.findViewById(R.id.order_button);
            }

            @Override
            public String toString() {
                return super.toString() + " " + nameView.getText();
            }
        }
    }

}
