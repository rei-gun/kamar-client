package com.martabak.kamar.activity.housekeeping;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.martabak.kamar.R;
import com.martabak.kamar.domain.options.HousekeepingOption;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.service.Server;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HousekeepingOptionAdapter
        extends RecyclerView.Adapter<HousekeepingOptionAdapter.ViewHolder> {

    protected int selectedPos = -1;

    private final List<HousekeepingOption> housekeepingOptions;
    private Context context;
    private Map<String, String> statuses;
    private View.OnClickListener submitButtonListener;
    private final RadioGroup.OnCheckedChangeListener l;

    public HousekeepingOptionAdapter(List<HousekeepingOption> hkOptions,
                                     Context context, View.OnClickListener submitButtonListener,
                                     Map<String, String> statuses,
                                     RadioGroup.OnCheckedChangeListener l) {
        this.housekeepingOptions = hkOptions;
        this.context = context;
        this.submitButtonListener = submitButtonListener;
        this.statuses = statuses;
        this.l = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.housekeeping_option_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.itemView.setSelected(selectedPos == position);
        holder.item = housekeepingOptions.get(position);
        holder.nameView.setText(housekeepingOptions.get(position).getName());
        Server.picasso(context)
                .load(holder.item.getImageUrl())
                .resize(250, 125)
                .placeholder(R.drawable.loading_batik)
                .error(R.drawable.error)
                .into(holder.imgView);
        //set radio group and create its buttons
        holder.radioGroup.setOnCheckedChangeListener(l);
        if (holder.radioGroup.getChildCount() == 0) {//stops duplicate buttons
            for (int i = 0; i < holder.item.max; i++) {
                RadioButton rb = new RadioButton(context);
                Integer j = i + 1;
                rb.setText(j.toString());
                rb.setTag(j);
                float scale = context.getResources().getDisplayMetrics().density;
                int leftPad = (int) (20*scale + 0.5f);
                int rightPad = (int) (45*scale + 0.5f);
                rb.setPadding(leftPad, 0, rightPad, 0);

                holder.radioGroup.addView(rb);
                //made first item to be checked as default
                if (i == 0) { rb.setChecked(true);}
            }
        }


        holder.submitButton.setOnClickListener(submitButtonListener);

        //set permintaan statuses
        String state = statuses.containsKey(holder.item._id) ? statuses.get(holder.item._id) : Permintaan.STATE_CANCELLED;
        Log.d(HousekeepingActivity.class.getCanonicalName(), "Status for housekeeping " + holder.item.getName() + " is " + state);
        switch (state) {
            case Permintaan.STATE_NEW:
                holder.sentImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                holder.processedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                holder.completedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                break;
            case Permintaan.STATE_INPROGRESS:
                holder.sentImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                holder.processedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                holder.completedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                break;
            case Permintaan.STATE_COMPLETED:
                holder.sentImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                holder.processedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                holder.completedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
                break;
            default:
                holder.sentImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                holder.processedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                holder.completedImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
                break;
        }

        if (requestInProgress(holder.item._id)) {
            holder.submitButton.setEnabled(false);
        } else {
            holder.submitButton.setEnabled(true);
        }
    }

    private boolean requestInProgress(String optionId) {
        if (statuses.containsKey(optionId)) {
            switch (statuses.get(optionId)) {
                case Permintaan.STATE_COMPLETED:
                case Permintaan.STATE_INPROGRESS:
                case Permintaan.STATE_NEW:
                    return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return housekeepingOptions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public HousekeepingOption item;
        public final View rootView;
        public final RadioButton checkedRadioButton;

        @BindView(R.id.hk_name) TextView nameView;
        @BindView(R.id.hk_submit) View submitButton;
        @BindView(R.id.sent_image) View sentImageView;
        @BindView(R.id.processed_image) View processedImageView;
        @BindView(R.id.completed_image) View completedImageView;
        @BindView(R.id.hk_img) ImageView imgView;
        @BindView(R.id.radio_group) RadioGroup radioGroup;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            rootView = view;
            checkedRadioButton = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
        }

        @Override
        public String toString() {
            return super.toString() + " " + nameView.getText();
        }
    }
}