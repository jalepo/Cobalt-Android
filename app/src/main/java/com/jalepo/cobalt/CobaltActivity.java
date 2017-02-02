package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

import io.reactivex.disposables.CompositeDisposable;

public abstract class CobaltActivity extends AppCompatActivity {
    protected RecyclerView mRecyclerView;
    FeedFetchHelper feedFetchHelper = new FeedFetchHelper();

    CompositeDisposable disposable = new CompositeDisposable();
    ProfileTracker mProfileTracker;
    String accessToken;
    String nextPageLink;
    boolean loadingNextPage = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                loadFirstPage();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!loadingNextPage && !recyclerView.canScrollVertically(1)) {
                    loadNextPage();
                    loadingNextPage = true;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecyclerView.removeOnScrollListener(null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the access token is null, then switch to the login activity and kill this one
        if (AccessToken.getCurrentAccessToken() == null) {
            Intent loginIntent = new Intent(getApplicationContext(), FBLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(loginIntent);
            finish();
        } else {
            accessToken = AccessToken.getCurrentAccessToken().getToken();
            loadFirstPage();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProfileTracker != null) {
            mProfileTracker.stopTracking();
        }

        disposable.clear();
    }

    public void menuButtonClicked(View view) {
        Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(menuIntent);
    }

    public abstract void loadFirstPage();
    public abstract void loadNextPage();
}
