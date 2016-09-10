package com.martabak.kamar.activity.staff;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.martabak.kamar.R;
import com.martabak.kamar.domain.permintaan.Engineering;
import com.martabak.kamar.domain.permintaan.Housekeeping;
import com.martabak.kamar.domain.permintaan.OrderItem;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.domain.permintaan.RestaurantOrder;
import com.martabak.kamar.domain.permintaan.Transport;
import com.martabak.kamar.service.PermintaanServer;

import rx.Observer;

class StaffExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> states; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> stateToPermIds;
    private HashMap<String, Permintaan> idToPermintaan;
    ImageView assignPermintaanButton;
    ImageView infoPermintaanButton;
    ImageView progressPermintaanButton;
    ImageView regressPermintaanButton;


    public StaffExpandableListAdapter(Context context, List<String> states,
                                 HashMap<String, List<String>> stateToPermIds, HashMap<String,
                                    Permintaan> idToPermintaan) {
        this.context = context;
        this.states = states;
        this.stateToPermIds = stateToPermIds;
        this.idToPermintaan = idToPermintaan;
    }

    @Override
    public Permintaan getChild(int groupPosition, int childPosition) {
        Log.v("states", states.toString());
        Log.v("stateToPermIds", stateToPermIds.toString());
        return idToPermintaan.get(stateToPermIds.get(states.get(groupPosition)).get(childPosition));
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Permintaan currPermintaan = getChild(groupPosition, childPosition);

        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.staff_permintaan_item, null);


        //Set main text
        TextView txtListChild = (TextView) convertView.findViewById(R.id.permintaan_list_item);
        String simpleCreated = new SimpleDateFormat("hh:mm a").format(currPermintaan.created);
        final String childText = currPermintaan.type+" ~~~ Room No. "+currPermintaan.roomNumber+" ~~~ Ordered @ "+simpleCreated;
        txtListChild.setText(childText);

        //Set up grey filter
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);

        //Initialize the ImageViews (buttons)
        assignPermintaanButton =(ImageView) convertView.findViewById(R.id.assign_permintaan_button);
        infoPermintaanButton = (ImageView) convertView.findViewById(R.id.info_permintaan_button);
        progressPermintaanButton = (ImageView) convertView.findViewById(R.id.progress_permintaan_button);
        regressPermintaanButton = (ImageView) convertView.findViewById(R.id.regress_permintaan_button);

        /**
         * Assign staff member to a permintaan
         */
        assignPermintaanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getApplicationContext().getString(R.string.permintaan_assign_person));

                // Set up the input
                final EditText textInput = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                textInput.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(textInput);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String assignee = textInput.getText().toString();
                        doGetAndUpdatePermintaan(currPermintaan._id, 0, assignee);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        /**
         * Display info on a permintaan
         */
        infoPermintaanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager manager = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);
                manager.getDefaultDisplay().getMetrics(displayMetrics);
                int width, height;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    width = displayMetrics.widthPixels;
                    height = displayMetrics.heightPixels;
                } else {
                    Point point = new Point();
                    manager.getDefaultDisplay().getSize(point);
                    width = point.x;
                    height = point.y;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //build the AlertDialog's content
                String simpleUpdated;
                long lastStateChange;
                if (currPermintaan.updated != null) {
                    simpleUpdated = new SimpleDateFormat("hh:mm a").format(currPermintaan.updated);
                    lastStateChange = (new Date().getTime() - currPermintaan.updated.getTime())/1000;
                } else {
                    simpleUpdated = "never";
                    lastStateChange = 0;
                }
                String contentString = "";
                String simpleCreated = new SimpleDateFormat("hh:mm a").format(currPermintaan.created);
                if (currPermintaan.content.getType().equals(Permintaan.TYPE_RESTAURANT)) {
                    RestaurantOrder restoOrder = (RestaurantOrder) currPermintaan.content;
                    for (OrderItem o:restoOrder.items) {
                        contentString += "\n"+o.quantity+" "+o.name+" Rp. "+o.price;
                    }
                    contentString += " \nTotal: Rp. "+ restoOrder.totalPrice;
                } else if (currPermintaan.content.getType().equals(Permintaan.TYPE_TRANSPORT)) {
                    Transport transportOrder = (Transport) currPermintaan.content;
                    contentString += "\nDeparting in: "+transportOrder.departingIn+
                                    "\nDestination: "+transportOrder.destination+
                                    "\nNumber of passengers: "+transportOrder.passengers;
                } else if (currPermintaan.content.getType().equals((Permintaan.TYPE_HOUSEKEEPING))) {
                    Housekeeping hkOrder = (Housekeeping) currPermintaan.content;
                    contentString += "\nOrder: "+hkOrder.option.nameEn+
                                    "\nQuantity: "+hkOrder.quantity;
                } else if (currPermintaan.content.getType().equals((Permintaan.TYPE_ENGINEERING))) {
                    Log.v("DICK",currPermintaan.toString());
                }

                builder
                        .setTitle(currPermintaan.type + " ORDER DETAILS")
                        .setMessage("Room No. "+currPermintaan.roomNumber+"\n"+
                                "Guest's id: "+currPermintaan.guestId+"\n"+
                                "State: "+currPermintaan.state+"\n"+
                                "Message: "+currPermintaan.content.message+"\n"+
                                "Order lodged at: "+simpleCreated+"\n"+
                                "Last Status change at "+simpleUpdated+"\n"+
                                "Time since latest status change: "+lastStateChange/60+" minutes ago\n"+
                                "Assignee: "+currPermintaan.assignee+
                                "\nContent: "+contentString)
                        .setCancelable(true)
                ;
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(width, (height-100));
            }
        });

        /**
         * Move a permintaan to the next state
         */
        if (currPermintaan.isCancellable()) {
            progressPermintaanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("progressPermintaan", String.valueOf(groupPosition)+" "+String.valueOf(childPosition));
//                    final Permintaan currPermintaan = getChild(groupPosition, childPosition);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getApplicationContext().getString(R.string.permintaan_progress_confirmation));
                    builder.setCancelable(false);
                    builder.setPositiveButton(context.getApplicationContext().getString(R.string.positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Check permintaan can be progressed
                            if (currPermintaan.isCancellable()) {
//                                Permintaan currPermintaan = getChild(groupPosition, childPosition);
                                Log.v("id", currPermintaan._id);

                                doGetAndUpdatePermintaan(currPermintaan._id, 1, null);
/*
                                //Get the list of permintaans in the next state
                                List<String> nextPermintaans = stateToPermIds.get(states.get(groupPosition + 1));

                                //Add child to the next state
                                nextPermintaans.add(currPermintaan._id);
                                //Remove the child from the current state
                                stateToPermIds.get(states.get(groupPosition)).remove(currPermintaan._id);*/

                            } else {
                                //TODO: Tell the user they can't progress the permintaan
                            }
                        }
                    });

                    builder.setNegativeButton(context.getApplicationContext().getString(R.string.negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            });
        } else {
            progressPermintaanButton.setColorFilter(cf);
            progressPermintaanButton.setAlpha(0.5f);   // 128 = 0.5
        }

        /**
         * Move a permintaan to a previous state
         */
        if (currPermintaan.isRegressable()) {
            regressPermintaanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("regressPermintaan", String.valueOf(groupPosition)+" "+String.valueOf(childPosition));
//                    final Permintaan currPermintaan = getChild(groupPosition, childPosition);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getApplicationContext().getString(R.string.permintaan_regress_confirmation));
                    builder.setCancelable(false);
                    builder.setPositiveButton(context.getApplicationContext().getString(R.string.positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Check permintaan can be regressed
                            if (currPermintaan.isRegressable()) {
                                doGetAndUpdatePermintaan(currPermintaan._id, -1, null);
                            } else {
                                //TODO tell user they cannot regress
                            }
                        }
                    });

                    builder.setNegativeButton(context.getApplicationContext().getString(R.string.negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            });
        } else {
            regressPermintaanButton.setColorFilter(cf);
            regressPermintaanButton.setAlpha(0.5f);
        }

        return convertView;
    }


    /**
     * Gets the permintaan from the server to obtain the latest _rev and then
     * updates the permintaan on the server before updating the view.
     * @param _id id of permintaan to be updated
     * @param increment 1 to progress, -1 to regress
     * @param assignee name of staff member to be assigned, null if progressing/regressing
     */
    private void doGetAndUpdatePermintaan(final String _id, final int increment,
                                          final String assignee) {
        Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "Doing get permintaan of state");

        final String currState = idToPermintaan.get(_id).state;
        final String targetState = setTargetState(currState, increment);
//        Log.v("TARGET", targetState);
//        Log.v("CURRENT", currState);

        PermintaanServer.getInstance(context).getPermintaansOfState(currState)
                                                .subscribe(new Observer<Permintaan>() {
            Permintaan tempPermintaan = new Permintaan();

            @Override
            public void onCompleted() {
                Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "completed getpermintaan, now updating");
                String updatedAssignee = null;
                if (assignee == null) {
                    updatedAssignee = tempPermintaan.assignee;
                } else {
//                    Log.v("Assignee", assignee);
                    updatedAssignee = assignee;
                }
                final Permintaan updatedPermintaan = new Permintaan(tempPermintaan._id, tempPermintaan._rev, tempPermintaan.owner, tempPermintaan.type,
                        tempPermintaan.roomNumber, tempPermintaan.guestId, targetState,
                        tempPermintaan.created, new Date(), updatedAssignee, tempPermintaan.content);
                PermintaanServer.getInstance(context).updatePermintaan(updatedPermintaan)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onCompleted() {
                            Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "updatePermintaan() On completed"
                            +currState+targetState);
                            //Add child to the next state
                            stateToPermIds.get(targetState).add(tempPermintaan._id);
                            //Remove the child from the current state
                            stateToPermIds.get(currState).remove(tempPermintaan._id);
                            //update Permintaan dictionary
                            idToPermintaan.put(updatedPermintaan._id, updatedPermintaan);
                            notifyDataSetChanged();
                        }
                        @Override
                        public void onError(Throwable e) {
                            Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "updatePermintaan() On error");
                            e.printStackTrace();
                        }
                        @Override
                        public void onNext(Boolean result) {
                            Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "updatePermintaan() staff permintaan update " + result);
                        }
                    });
            }
            @Override
            public void onError(Throwable e) {
                Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "getPermintaansOfState() On error");
                }
            @Override
            public void onNext(Permintaan result) {
                Log.d(StaffExpandableListAdapter.class.getCanonicalName(), "getPermintaansOfState() On next" + result._id +" "+ _id);
                if (result._id.equals(_id)) {
                    tempPermintaan = result;
//                    Log.v("Id", tempPermintaan._id);
                }
            }
        });
    }

    /**
     * Decides the next or previous state depending on the increment
     * @param currState current state
     * @param increment 1 to progress, -1 to regress
     * @return next or previous state
     */
    private String setTargetState(String currState, int increment) {

        String state = new String();

        if (increment == 0) {
            return currState;
        }

        if (currState.equals(Permintaan.STATE_NEW)) {
            return Permintaan.STATE_INPROGRESS;
        } else if (currState.equals(Permintaan.STATE_INPROGRESS)) {
            if (increment == 1) {
                //return Permintaan.STATE_INDELIVERY
                return Permintaan.STATE_COMPLETED;
            } else {
                return Permintaan.STATE_NEW;
            }
        /*} else if (currState.equals(Permintaan.STATE_INDELIVERY)) {
            if (increment == 1) {
                return Permintaan.STATE_COMPLETED;
            }else {
                return Permintaan.STATE_INPROGRESS;
            }
            */
        } else if (currState.equals((Permintaan.STATE_COMPLETED))) {
            //return Permintaan.STATE_INDELIVERY;
            return Permintaan.STATE_INPROGRESS;
        }

        return state;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.stateToPermIds.get(this.states.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.states.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.states.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.staff_permintaan_state, null);

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.list_state);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}