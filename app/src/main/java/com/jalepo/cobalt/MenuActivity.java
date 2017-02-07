package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void logoutButtonClicked(View view) {
        LoginManager.getInstance().logOut();
        finish();
    }


    public void photosButtonClicked(View view) {
        Intent photosIntent = new Intent(getApplicationContext(), PhotoListActivity.class);
        startActivity(photosIntent);
    }

    public void videosButtonClicked(View view) {
        Intent videosIntent = new Intent(getApplicationContext(), VideoListActivity.class);
        startActivity(videosIntent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Any touch that does not get handled by this class will dismiss the menu
        if(!super.onTouchEvent(event)) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
                return true;
            }
        }
        return false;
    }
}
