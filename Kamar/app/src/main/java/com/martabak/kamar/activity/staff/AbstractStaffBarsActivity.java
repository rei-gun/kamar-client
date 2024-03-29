package com.martabak.kamar.activity.staff;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.AbstractCustomFontActivity;
import com.martabak.kamar.activity.chat.StaffChatFragment;

/**
 * An abstract class that provides functionality for the toolbar and bottom staff bar shared
 * across many staff activities.
 * <p>
 * The base layout provided must have the action bar layout and bottom bar layout's included in it.
 */
public abstract class AbstractStaffBarsActivity extends AbstractCustomFontActivity {

    protected abstract Options getOptions();

    public class Options {
        private int baseLayout;
        private boolean enableRefreshIcon;

        public Options withBaseLayout(int baseLayout) {
            this.baseLayout = baseLayout;
            return this;
        }
        public Options enableChatIcon(boolean enableChatIcon) {
            this.enableRefreshIcon = enableChatIcon;
            return this;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getOptions().baseLayout);
        setupToolbar(getOptions().enableRefreshIcon);
    }

    private void setupToolbar(boolean enableRefreshIcon) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        if (enableRefreshIcon) {
            ImageView refreshIconView = (ImageView) findViewById(R.id.refresh_icon);
            refreshIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.staff_container, StaffChatFragment.newInstance())
                            .commit();
                }
            });
        }
    }
}
