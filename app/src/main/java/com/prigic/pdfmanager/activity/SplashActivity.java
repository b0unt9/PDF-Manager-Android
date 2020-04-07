package com.prigic.pdfmanager.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.prigic.pdfmanager.R;

public class SplashActivity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initVariables();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFunctionality();
    }

    private void initVariables() {
        mActivity = SplashActivity.this;
        mContext = mActivity.getApplicationContext();
    }

    private void initView() {
        setContentView(R.layout.activity_splash);
    }

    private void initFunctionality() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                startActivity(new Intent(mActivity,MainActivity.class));
                SplashActivity.this.finish();
            }
        }, SPLASH_DURATION);
    }
}
