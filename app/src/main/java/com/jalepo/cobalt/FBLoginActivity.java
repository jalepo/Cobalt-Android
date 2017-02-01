package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FBLoginActivity extends AppCompatActivity {

    /**
     * Callback Manager for Facebook login
     */
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_fblogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        if(AccessToken.getCurrentAccessToken() == null) {
            loginButton.setVisibility(View.VISIBLE);
            loginButton.setReadPermissions("email", "user_posts", "user_photos");


            mCallbackManager = CallbackManager.Factory.create();
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    launchListActivity();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }

            });
        } else {
            // If the user is already logged in, hide the login button and launch the ListActivity
            loginButton.setVisibility(View.GONE);
            launchListActivity();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void launchListActivity() {
        Intent listActivityIntent = new Intent(getApplicationContext(), ListActivity.class);
        startActivity(listActivityIntent);
    }
}
