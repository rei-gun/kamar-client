package com.martabak.kamar.activity.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.martabak.kamar.R;
import com.martabak.kamar.activity.AbstractCustomFontActivity;

public class SplashScreenActivity extends AbstractCustomFontActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_splash_screen);

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.splash_screen_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreenActivity.this, SelectLanguageActivity.class);
                startActivity(intent);
            }
        });
    }
}
