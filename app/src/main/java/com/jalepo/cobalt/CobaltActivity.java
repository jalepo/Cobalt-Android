package com.jalepo.cobalt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.Observer;
import rx.Subscription;

public abstract class CobaltActivity extends AppCompatActivity {
    protected RecyclerView mRecyclerView;
    FeedFetchHelper feedFetchHelper = new FeedFetchHelper();

    CompositeDisposable disposable = new CompositeDisposable();
    ProfileTracker mProfileTracker;
    String accessToken;
    String nextPageLink;
    boolean loadingNextPage = false;

    Subscription scrollEventSubscription;
    protected ArrayList<?> dataList = new ArrayList<>();

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



        scrollEventSubscription = RxRecyclerView.scrollEvents(mRecyclerView)
                .subscribe(new Observer<RecyclerViewScrollEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(RecyclerViewScrollEvent recyclerViewScrollEvent) {

                        if(!loadingNextPage && !mRecyclerView.canScrollVertically(1)) {
                            loadNextPage();
                            loadingNextPage = true;
                        }
                    }
                });
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if(!loadingNextPage && !recyclerView.canScrollVertically(1)) {
//                    loadNextPage();
//                    loadingNextPage = true;
//                }
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mRecyclerView.removeOnScrollListener(null);
        scrollEventSubscription.unsubscribe();
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
