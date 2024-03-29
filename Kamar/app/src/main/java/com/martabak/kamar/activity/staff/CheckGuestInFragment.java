package com.martabak.kamar.activity.staff;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.martabak.kamar.R;
import com.martabak.kamar.domain.Event;
import com.martabak.kamar.domain.Guest;
import com.martabak.kamar.domain.Room;
import com.martabak.kamar.domain.managers.RoomManager;
import com.martabak.kamar.service.EventServer;
import com.martabak.kamar.service.GuestServer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckGuestInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckGuestInFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener {

    private List<Event> promoImages;
    private List<String> roomNumbers;
    private ArrayAdapter roomNumAdapter;
    private ArrayAdapter promoImgAdapter;

    //bind views here
    @BindView(R.id.guest_first_name) EditText editFirstName;
    @BindView(R.id.guest_phone) EditText editPhoneNumber;
    @BindView(R.id.guest_spinner) Spinner spinnerRoomNumber;
    @BindView(R.id.guest_email) EditText editEmail;
    @BindView(R.id.guest_welcome_message) EditText editWelcomeMessage;
    @BindView(R.id.promo_img_spinner) Spinner spinnerPromoImg;
    @BindView(R.id.check_guest_in_submit) Button submitButton;

    //on click check guest in submit
    @OnClick(R.id.check_guest_in_submit)
    void onClick() {
        String firstName = editFirstName.getText().toString();
        String phoneNumber = editPhoneNumber.getText().toString();
        String email = editEmail.getText().toString();
        String roomNumber = roomNumbers.get((int)spinnerRoomNumber.getSelectedItemId()).toString();
        Log.v(CheckGuestInFragment.class.getCanonicalName(), "Selected room number " + roomNumber);
        String welcome = editWelcomeMessage.getText().toString();
        Integer selectedPromoId = (int)spinnerPromoImg.getSelectedItemId();
        Log.v(CheckGuestInFragment.class.getCanonicalName(), "Selected promo image with ID " + selectedPromoId.toString());
        String promoImgId = selectedPromoId != 0 ? promoImages.get(selectedPromoId - 1)._id : null;
        sendCreateGuestRequest(firstName, "", phoneNumber, email, roomNumber, null, welcome, promoImgId);
    }

    public CheckGuestInFragment() {
    }

    /**
     * @return A new instance of fragment CheckGuestInFragment.
     */
    public static CheckGuestInFragment newInstance() {
        return new CheckGuestInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_check_guest_in, container, false);
        ButterKnife.bind(this, view);
        editFirstName.addTextChangedListener(this);
        //editLastName.addTextChangedListener(this);
        //editPhoneNumber.addTextChangedListener(this);
        promoImages = null;

        roomNumbers = new ArrayList<>();
        roomNumbers.add(0, getString(R.string.room_select));
        roomNumAdapter = new ArrayAdapter(getActivity().getBaseContext(),
                R.layout.spinner_item, roomNumbers);
//        roomNumAdapter.setDropDownViewResource(R.layout.spinner_item);
        getRoomNumbersWithoutGuests();
        spinnerRoomNumber.setAdapter(roomNumAdapter);
        spinnerRoomNumber.setSelection(0);
        spinnerRoomNumber.setOnItemSelectedListener(this);

        final List<String> promoImageNames = getPromoImages();
        promoImgAdapter = new ArrayAdapter(getActivity().getBaseContext(),
                R.layout.spinner_item, promoImageNames);
        promoImageNames.add(0, getString(R.string.promo_img_select));
//        promImgAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinnerPromoImg.setAdapter(promoImgAdapter);
        spinnerPromoImg.setSelection(0);
        spinnerPromoImg.setOnItemSelectedListener(this);

        submitButton.setEnabled(false);
        return view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(CheckGuestInFragment.class.getCanonicalName(), "Validating form");
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(CheckGuestInFragment.class.getCanonicalName(), "Validating form");
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(CheckGuestInFragment.class.getCanonicalName(), "Validating form");
        validate();
    }

    /**
     * Validate the fields.
     */
    private void validate() {
        boolean invalid = false;
        if (editFirstName.getText().toString().trim().equalsIgnoreCase("")) {
            Log.d(CheckGuestInFragment.class.getCanonicalName(), "First name field empty");
            invalid = true;
            editFirstName.setError(getString(R.string.required));
        }
        /*
        if (editLastName.getText().toString().trim().equalsIgnoreCase("")) {
            Log.d(CheckGuestInFragment.class.getCanonicalName(), "Last name field empty");
            invalid = true;
            editLastName.setError(getString(R.string.required));
        }
        */
        /*
        if (editPhoneNumber.getText().toString().trim().equalsIgnoreCase("")) {
            Log.d(CheckGuestInFragment.class.getCanonicalName(), "Phone number field empty");
            invalid = true;
            editPhoneNumber.setError(getString(R.string.required));
        }*/
        if ((int)spinnerRoomNumber.getSelectedItemId() <= 0) {
            Log.d(CheckGuestInFragment.class.getCanonicalName(), "Valid room number required");
            invalid = true;
            TextView spinnerErrorText = (TextView)spinnerRoomNumber.getSelectedView();
            spinnerErrorText.setError("");
//            spinnerErrorText.setText(getString(R.string.room_select));
        }

        Log.d(CheckGuestInFragment.class.getCanonicalName(), "Setting submit button to " + !invalid);
        submitButton.setEnabled(!invalid);
    }

    /**
     * Gets a list of room number strings without guests currently checked in.
     */
    private void getRoomNumbersWithoutGuests() {
        roomNumbers.set(0, getString(R.string.loading));
        spinnerRoomNumber.setEnabled(false);
        roomNumAdapter.notifyDataSetChanged();
        RoomManager.getInstance().getRoomsWithoutGuests(getActivity().getBaseContext())
                .subscribe(new Observer<Room>() {
            @Override public void onCompleted() {
                roomNumbers.set(0, getString(R.string.room_select));
                spinnerRoomNumber.setEnabled(true);
                roomNumAdapter.notifyDataSetChanged();
                validate();
            }
            @Override public void onError(Throwable e) {
                Log.v(CheckGuestInFragment.class.getCanonicalName(),  "getRoomNumbersWithoutGuests() onError");
                e.printStackTrace();
            }
            @Override public void onNext(Room room) {
                roomNumbers.add(room.number);
                Log.v(CheckGuestInFragment.class.getCanonicalName(), "getRoomNumbersWithoutGuests() onNext");
            }
        });
    }

    /**
     * Sets List<Event> promoImages and....
     * @return A list of image names stored in the event database.
     */
    private List<String> getPromoImages() {
        final List<String>[] promoImageNames = new List[]{new ArrayList<>()};
        EventServer.getInstance(getActivity().getBaseContext()).getAllEvents()
                .subscribe(new Observer<List<Event>>() {

                    @Override public void onCompleted() {
                        Log.d(CheckGuestInFragment.class.getCanonicalName(),  "getPromoImages() onCompleted");
                        promoImgAdapter.notifyDataSetChanged();
                    }
                    @Override public void onError(Throwable e) {
                        Log.d(CheckGuestInFragment.class.getCanonicalName(),  "getPromoImages() onError");
                        e.printStackTrace();
                    }
                    @Override public void onNext(List<Event> result) {
                        Log.d(CheckGuestInFragment.class.getCanonicalName(), "getPromoImages() onNext");

                        if (result.get(0).name != null) { //if view is not at the front
                            for (int i=0; i<result.size()-1; i++) { //skip last event
                                Log.d(CheckGuestInFragment.class.getCanonicalName(), "Found promo image event with name " + result.get(i).name);
                                promoImageNames[0].add(result.get(i).name);
                            }
                            promoImages = result;

                        } else {
                            for (int i=1; i<result.size(); i++) { //skip first event
                                Log.d(CheckGuestInFragment.class.getCanonicalName(), "Found promo image event with name " + result.get(i).name);
                                promoImageNames[0].add(result.get(i).name);
                            }
                            result.remove(0); //remove design doc at the start of the List
                            promoImages = result;
                        }
                        Log.d(CheckGuestInFragment.class.getCanonicalName(), "Setting promoImages to " + promoImages + " with size " + promoImages.size());
                    }
                });
        return promoImageNames[0];
    }

    /**
     * Send a request to create a new guest, given their details.
     * @param firstName Guest's first name.
     * @param lastName Guest's last name.
     * @param phoneNumber Guest's phone number.
     * @param email Guest's email.
     * @param roomNumber Guest's room number.
     * @param checkOutDate Guest's expected check-out date.
     * @param welcome A welcome message for the guest.
     * @param promoImageId id of the promo image.
     */
    public void sendCreateGuestRequest(String firstName, String lastName, String phoneNumber,
            String email, String roomNumber, Date checkOutDate, String welcome, String promoImageId) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1); // little hack here
        Date currentDate = c.getTime();

        GuestServer.getInstance(getActivity().getBaseContext()).createGuest(new Guest(
                firstName,
                lastName,
                phoneNumber,
                email,
                currentDate,
                checkOutDate,
                roomNumber,
                welcome,
                promoImageId)
        ).subscribe(new Observer<Guest>() {
            @Override public void onCompleted() {
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "createGuest() On completed");
            }
            @Override public void onError(Throwable e) {
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "On error");
                e.printStackTrace();
                Toast.makeText(
                        getActivity(),
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                ).show();
            }
            @Override public void onNext(Guest guest) {
                Log.d(CheckGuestInFragment.class.getCanonicalName(), "createGuest() " + guest.toString());
                if (guest != null) {
                    Toast.makeText(
                            getActivity(),
                            R.string.guest_checkin_message,
                            Toast.LENGTH_LONG
                    ).show();
                    startActivity(new Intent(getActivity(), StaffHomeActivity.class));
                } else {
                    Toast.makeText(
                            getActivity(),
                            R.string.something_went_wrong,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }
}
