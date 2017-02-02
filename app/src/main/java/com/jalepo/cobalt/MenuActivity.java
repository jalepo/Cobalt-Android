package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
}
