package com.tistory.dagylo.weatherforecastapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by user on 2017-04-21.
 */

public class IntroActivity extends AppCompatActivity {
    private Handler handler;

    Runnable runnable = new Runnable(){
        @Override
        public void run(){
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);

            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        init();
        handler.postDelayed(runnable, 1500);

        //액션바 없애기
        hideActionBar();
    }

    //액션바 없애기
    private void hideActionBar(){
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.hide();
        }
    }

    public void init(){
        handler = new Handler();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        handler.removeCallbacks(runnable);
    }

}
