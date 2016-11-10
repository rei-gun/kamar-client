package com.martabak.kamar.activity.restaurant;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.martabak.kamar.R;
import com.martabak.kamar.domain.Consumable;
import com.martabak.kamar.service.Server;

import java.util.HashMap;
import java.util.List;

class RestaurantExpListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> subsections; //header text
    //a list of each item's main text with header text as keys
    private HashMap<String, List<String>> subsectionToIds;
    //consumable dictionary with consumable.name keys
    private HashMap<String, Consumable> idToConsumable;
    //quantity dictionary with consumable.name keys
    private HashMap<String, Integer> idToQuantity;
    private HashMap<String, String> idToNote;
    private TextView subtotalText;

    public RestaurantExpListAdapter(Context context, List<String> subsections,
                                    HashMap<String, List<String>> subsectionToIds,
                                    HashMap<String, Consumable> idToConsumable,
                                    HashMap<String, Integer> idToQuantity,
                                    HashMap<String, String> idToNote,
                                    TextView subtotalText) {
        this.context = context;
        this.subsections = subsections;
        this.subsectionToIds = subsectionToIds;
        this.idToConsumable = idToConsumable;
        this.idToQuantity = idToQuantity;
        this.subtotalText = subtotalText;
        this.idToNote = idToNote;
    }

    @Override
    public Consumable getChild(int groupPosition, int childPosition) {
//        return idToConsumable.get(this.subsectionToIds.get(this.subsections.get(groupPosition))
//                .get(childPosititon));
        return idToConsumable.get(subsectionToIds.get(subsections.get(groupPosition)).get(childPosition));
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.restaurant_menu_item, null);
        }

        final Consumable currConsumable = getChild(groupPosition, childPosition);

        final TextView quantity = (TextView) convertView.findViewById(R.id.item_quantity);
        quantity.setText(idToQuantity.get(currConsumable._id).toString());
//        quantity.setBackgroundColor(0xFFac0d13);
        quantity.invalidate();

        //Set up main text
        String childText = currConsumable.name;
        final TextView txtListChild = (TextView) convertView.findViewById(R.id.item_text);
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "fonts/century-gothic.ttf");
        txtListChild.setTypeface(customFont);
        txtListChild.setText(childText);
        /*
        txtListChild.post(new Runnable() {
            @Override
            public void run() {
                Log.v("Child info", "child: "+currConsumable.name+" no. of lines: "+Integer.toString(txtListChild.getLineCount()));
                if (txtListChild.getLineCount() > 1) {//if 2 lines used for item name
                    //reduce item name padding
                    txtListChild.setPadding(txtListChild.getPaddingLeft(),
                            txtListChild.getPaddingTop(),
                            txtListChild.getPaddingRight(),
                            20);
                } /*else {
                    //set item name padding to default
                    txtListChild.setPadding(txtListChild.getPaddingLeft(),
                            txtListChild.getPaddingTop(),
                            txtListChild.getPaddingRight(),
                            90);

//                    quantity.margin
                }
            }

        });
        */



        //Set up price text
        String priceText = currConsumable.price.toString();
        TextView priceView = (TextView)convertView.findViewById(R.id.item_price);
        priceView.setText("Rp. "+priceText+" 000");

        //Set up item image1
        ImageView itemImg = (ImageView) convertView.findViewById(R.id.item_img);
        Log.d(RestaurantExpListAdapter.class.getCanonicalName(), "Loading image " + currConsumable.getImageUrl() + " into " + itemImg);
        Server.picasso(context)
                .load(currConsumable.getImageUrl())
                .placeholder(R.drawable.loading_batik)
                .error(R.drawable.error)
                .into(itemImg);

        /**
         * Implement the minus button
         */
        ImageView minusQuantityButton = (ImageView) convertView.findViewById(R.id.minus_button);

        minusQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Integer currQuantity = idToQuantity.get(currConsumable._id);
                if (currQuantity - 1 > -1) { //Stop user from setting to -1 quantity
                    //Minus 1 off the quantity
                    idToQuantity.put(currConsumable._id, (currQuantity - 1));
                    //minus subtotal by price of item
                    idToQuantity.put("subtotal", idToQuantity.get("subtotal")-currConsumable.price);
                    subtotalText.setText("Rp. "+ idToQuantity.get("subtotal").toString()+" 000");
                    notifyDataSetChanged();
                }
            }
        });

        /**
         * Implement the plus button
         */
        ImageView plusQuantityButton = (ImageView) convertView.findViewById(R.id.plus_button);

        plusQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Integer currQuantity = idToQuantity.get(currConsumable._id);
                //Add 1 to the quantity
                idToQuantity.put(currConsumable._id, (currQuantity + 1));
                //plus subtotal by price of item
                idToQuantity.put("subtotal", idToQuantity.get("subtotal") + currConsumable.price);
                subtotalText.setText("Rp. "+ idToQuantity.get("subtotal").toString()+" 000");
                notifyDataSetChanged();
            }
        });

        final EditText note = (EditText) convertView.findViewById(R.id.item_note);
        note.setText(idToNote.get(currConsumable._id));
/*
        final Activity parentActivity = (Activity)context;

        note.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.v("SOMETHING", "something");
                    InputMethodManager in = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                    // NOTE: In the author's example, he uses an identifier
                    // called searchBar. If setting this code on your EditText
                    // then use v.getWindowToken() as a reference to your
                    // EditText is passed into this callback as a TextView

                    in.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
//                    userValidateEntry();
                    // Must return true here to consume event
                    return true;

                }
                return false;
            }
        });
*/

        note.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                idToNote.put(currConsumable._id, note.getText().toString());
//                Log.v("HERP", idToNote.toString());
            }
        });

        return convertView;
    }

   @Override
    public int getChildrenCount(int groupPosition) {
        return this.subsectionToIds.get(subsections.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.subsections.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.subsections.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.restaurant_menu_subsection, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.restaurant_subsection);
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "fonts/century-gothic.ttf");
        Typeface boldFont = Typeface.create(customFont, Typeface.BOLD);
        lblListHeader.setTypeface(boldFont);
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